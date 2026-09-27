# Evaluador Virtual de Academia

## Descripción General 

El Evaluador Virtual de Academia es una aplicación web orientada a instituciones educativas y centros de preparación que busca automatizar la calificación de evaluaciones con preguntas abiertas — aquellas cuya respuesta no es exacta ni cerrada y que tradicionalmente dependen del criterio subjetivo del docente.

A diferencia de un corrector de respuestas cerradas (opción múltiple, verdadero/falso), el sistema debe interpretar la intención del alumno al responder, identificar los conceptos clave presentes en su texto y contrastarlos semánticamente contra los puntos esperados de la respuesta modelo, devolviendo un porcentaje de acierto y, cuando corresponda, retroalimentación orientada a la mejora.

## Problema que resuelve

La corrección manual de preguntas abiertas es lenta, inconsistente entre distintos correctores y difícil de escalar cuando el volumen de alumnos crece (exámenes de admisión, simulacros, prácticas calificadas). Esto genera demoras en la entrega de resultados y limita la frecuencia con la que un alumno recibe feedback formativo sobre su razonamiento, no solo sobre si "acertó o no".

## Propuesta de valor
- Reduce el tiempo de corrección de preguntas abiertas de horas/días a minutos.
- Estandariza el criterio de evaluación entre secciones y docentes, al basarse en los mismos puntos clave definidos por pregunta.

- Da feedback accionable al alumno (no solo una nota), fomentando la mejora continua en simulacros previos a un examen real.

- Escala sin necesidad de contratar más correctores conforme crece el número de evaluados.

## MVP (ajustado)

El MVP consiste en una aplicación web donde:

1. El docente registra una evaluación: crea preguntas abiertas y define, para cada una, los puntos clave o criterios esperados de respuesta (una rúbrica base en texto que alimentará al modelo).
2. El alumno responde las preguntas desde la misma plataforma.
3. El motor de ML/LLM procesa cada respuesta: identifica el texto, comprende el contexto de la pregunta y extrae las palabras clave y datos relevantes presentes en ella.
4. El sistema compara lo extraído contra los criterios definidos por el docente y devuelve un resultado cualitativo (por ejemplo: "cumple", "cumple parcialmente", "no cumple" por cada criterio esperado), junto con una observación breve de qué le faltó a la respuesta. Aún no se calcula un porcentaje de acierto — esa cuantificación queda para una siguiente iteración, una vez validado que el modelo identifica bien los conceptos clave.

### Quedan fuera del MVP (mejoras futuras, en orden de prioridad hacia la visión final del producto):

Cálculo de porcentaje de acierto por respuesta, una vez calibrado el criterio cualitativo.
Detección y calificación de imágenes o diagramas dentro de la respuesta del alumno.
Comparación entre respuestas de distintos alumnos para detectar plagio.
Dashboard analítico de desempeño por curso/sección.
Ajuste fino del modelo con feedback histórico de los propios docentes.

## Capas de la Aplicación

### Frontend

Interfaz web para dos roles: docente (creación y gestión de evaluaciones, definición de criterios por pregunta, visualización de resultados agregados) y alumno (rendir la evaluación, ver su porcentaje y feedback una vez corregida).

### Backend

Gestiona la persistencia de evaluaciones, preguntas, criterios de corrección y respuestas de alumnos; orquesta el envío de cada respuesta al motor de ML/LLM y almacena el resultado (porcentaje + feedback) asociado a cada intento.

### LLM o ML

Núcleo de procesamiento de lenguaje natural: recibe la pregunta, los criterios esperados y la respuesta del alumno; realiza comprensión semántica y extracción de conceptos clave; calcula el grado de coincidencia con lo esperado y redacta el feedback cuando el acierto es menor a 80%.

## Modelo de Datos

```mermaid
erDiagram

    users {
        uuid id PK
        varchar email UK
        varchar password_hash
        varchar full_name
        varchar role
        varchar status
        timestamptz created_at
        timestamptz updated_at
    }

    courses {
        uuid id PK
        uuid instructor_id FK
        uuid created_by FK
        varchar code UK
        varchar name
        text description
        varchar enrollment_code UK
        varchar status
        timestamptz created_at
        timestamptz updated_at
    }

    enrollments {
        uuid id PK
        uuid course_id FK
        uuid student_id FK
        varchar status
        timestamptz enrolled_at
    }

    assessments {
        uuid id PK
        uuid course_id FK
        varchar title
        text instructions
        timestamptz opens_at
        timestamptz closes_at
        varchar status
        timestamptz results_released_at
        timestamptz created_at
        timestamptz updated_at
    }

    questions {
        uuid id PK
        uuid assessment_id FK
        int position
        varchar question_type
        text stem
        text model_answer
        text explanation
        timestamptz created_at
        timestamptz updated_at
    }

    question_options {
        uuid id PK
        uuid question_id FK
        int position
        text label
        boolean is_correct
    }

    rubric_criteria {
        uuid id PK
        uuid question_id FK
        int position
        text description
    }

    submissions {
        uuid id PK
        uuid assessment_id FK
        uuid student_id FK
        varchar status
        timestamptz started_at
        timestamptz submitted_at
    }

    answers {
        uuid id PK
        uuid submission_id FK
        uuid question_id FK
        uuid selected_option_id FK
        text answer_text
        boolean is_correct
        varchar grading_status
        text ai_general_feedback
        varchar ai_model
        text error_message
        uuid reviewed_by FK
        timestamptz reviewed_at
        timestamptz updated_at
    }

    criterion_results {
        uuid id PK
        uuid answer_id FK
        uuid rubric_criterion_id FK
        varchar ai_verdict
        text ai_evidence
        text ai_feedback
        varchar final_verdict
        text final_feedback
    }

    users ||--o{ courses : teaches
    users ||--o{ courses : creates
    courses ||--o{ enrollments : has
    users ||--o{ enrollments : enrolls

    courses ||--o{ assessments : contains
    assessments ||--|{ questions : contains
    questions ||--o{ question_options : offers
    questions ||--o{ rubric_criteria : defines

    assessments ||--o{ submissions : receives
    users ||--o{ submissions : submits
    submissions ||--|{ answers : contains
    questions ||--o{ answers : answered_by
    question_options |o--o{ answers : selected_in

    answers ||--o{ criterion_results : evaluated_in
    rubric_criteria ||--o{ criterion_results : applied_in
    users |o--o{ answers : reviews
```

### Cómo funciona hoy

`users` → `courses`/`enrollments` → `assessments`/`questions`/`rubric_criteria` ya están implementados y probados de punta a punta. El circuito actual es:

1. Un usuario se registra y se autentica (`users`), queda identificado por rol.
2. Un docente crea un curso (`courses`) y recibe un código de matrícula; un alumno se une con ese código (`enrollments`).
3. El docente arma una evaluación sobre su curso (`assessments`): agrega preguntas abiertas (`questions`) y, para cada una, los criterios que la IA va a usar para calificar (`rubric_criteria`). Recién cuando la evaluación tiene al menos una pregunta se puede publicar.
4. La respuesta modelo de cada pregunta solo es visible para el docente — un alumno que consulta la evaluación nunca la recibe, para no invalidar la corrección.

Estas cuatro tablas ya sostienen el ciclo completo de "crear curso → matricular → armar evaluación", que es la base sin la cual no tiene sentido construir nada del resto.

### Qué falta

**`submissions`** — registra el intento de un alumno rindiendo una evaluación puntual: qué evaluación, qué alumno, cuándo empezó y cuándo entregó, y en qué estado está (en curso, entregado). Es el "sobre cerrado" del examen: existe uno por cada vez que un alumno rinde una evaluación.

**`answers`** — cada respuesta individual que el alumno escribió, una por pregunta, colgando de su `submission`. Guarda el texto de la respuesta y el estado de corrección (pendiente, procesando, completado), más los campos que llena la IA una vez que la califica: feedback general, qué modelo la evaluó, y si hubo un error en el proceso. También tiene lugar para que un docente revise y quede registrado (`reviewed_by`/`reviewed_at`), por si la IA se equivoca.

**`criterion_results`** — el detalle fino: por cada criterio de la rúbrica de la pregunta, un veredicto separado ("cumple" / "cumple parcialmente" / "no cumple") con la evidencia y el feedback de por qué. Una sola respuesta puede tener varios `criterion_results` (uno por cada criterio que definió el docente). Acá vive también la posibilidad de que el docente sobrescriba el veredicto de la IA (`final_verdict`/`final_feedback`) sin perder el veredicto original de la IA (`ai_verdict`/`ai_feedback`).

En conjunto: un `submission` agrupa varias `answers`, y cada `answer` se abre en varios `criterion_results` — así se arma el detalle completo de qué le faltó al alumno en cada punto de cada pregunta, no solo una nota global.

`question_options` tampoco existe aún: el MVP solo contempla preguntas abiertas, así que se posterga hasta que haya una necesidad real de preguntas cerradas.

### Integración con IA (LLM)

Se define un puerto (`GradingEngine`) que el dominio usa para pedir "calificá esta respuesta contra estos criterios" sin saber qué hay detrás. Mientras se define el proveedor de IA a usar, se conecta una implementación de prueba fija (mock) para no bloquear el resto del desarrollo. La implementación real se resuelve con **Spring AI** (framework oficial del ecosistema Spring, ya en versión GA estable, compatible con la versión de Spring Boot de este proyecto): permite llamar a distintos proveedores de LLM (OpenAI, Anthropic, Google, entre otros) detrás de una misma interfaz, y devolver el veredicto de la IA como un dato estructurado (no texto libre a interpretar), que es exactamente lo que necesita `criterion_results`. Al ser una dependencia más del mismo proyecto Spring Boot, no se suma otro servicio ni otro lenguaje — se mantiene todo en un solo backend.

Falta decidir: qué proveedor de LLM se usa y con qué modelo (uno económico alcanza para esta tarea, no hace falta el modelo más potente disponible), y quién administra esa clave de API.
