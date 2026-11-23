-- V1: esquema base generado desde las entidades JPA (9 tablas). Las siguientes migraciones son incrementales.

CREATE TABLE public.answers (
    id uuid NOT NULL,
    ai_general_feedback text,
    ai_model character varying(100),
    answer_text text,
    error_message text,
    grading_status character varying(30) NOT NULL,
    is_correct boolean,
    question_id uuid NOT NULL,
    reviewed_at timestamp(6) with time zone,
    reviewed_by uuid,
    selected_option_id uuid,
    updated_at timestamp(6) with time zone NOT NULL,
    submission_id uuid NOT NULL,
    CONSTRAINT answers_grading_status_check CHECK (((grading_status)::text = ANY ((ARRAY['PENDING'::character varying, 'PROCESSING'::character varying, 'COMPLETED'::character varying, 'FAILED'::character varying])::text[])))
);

CREATE TABLE public.assessments (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    closes_at timestamp(6) with time zone,
    course_id uuid NOT NULL,
    instructions text,
    opens_at timestamp(6) with time zone,
    results_released_at timestamp(6) with time zone,
    status character varying(20) NOT NULL,
    title character varying(255) NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    CONSTRAINT assessments_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'PUBLISHED'::character varying, 'CLOSED'::character varying])::text[])))
);

CREATE TABLE public.courses (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    code character varying(255) NOT NULL,
    created_by uuid NOT NULL,
    description character varying(255),
    enrollment_code character varying(255) NOT NULL,
    instructor_id uuid NOT NULL,
    name character varying(255) NOT NULL,
    status character varying(20) NOT NULL,
    CONSTRAINT courses_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'ARCHIVED'::character varying])::text[])))
);

CREATE TABLE public.criterion_results (
    id uuid NOT NULL,
    ai_evidence text,
    ai_feedback text,
    ai_verdict character varying(30) NOT NULL,
    final_feedback text,
    final_verdict character varying(30) NOT NULL,
    "position" integer,
    rubric_criterion_id uuid NOT NULL,
    answer_id uuid NOT NULL,
    CONSTRAINT criterion_results_ai_verdict_check CHECK (((ai_verdict)::text = ANY ((ARRAY['MEETS'::character varying, 'PARTIALLY_MEETS'::character varying, 'DOES_NOT_MEET'::character varying])::text[]))),
    CONSTRAINT criterion_results_final_verdict_check CHECK (((final_verdict)::text = ANY ((ARRAY['MEETS'::character varying, 'PARTIALLY_MEETS'::character varying, 'DOES_NOT_MEET'::character varying])::text[])))
);

CREATE TABLE public.enrollments (
    id uuid NOT NULL,
    course_id uuid NOT NULL,
    enrolled_at timestamp(6) with time zone NOT NULL,
    status character varying(20) NOT NULL,
    student_id uuid NOT NULL,
    CONSTRAINT enrollments_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'DROPPED'::character varying])::text[])))
);

CREATE TABLE public.questions (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    explanation text,
    model_answer text,
    "position" integer NOT NULL,
    question_type character varying(20) NOT NULL,
    stem text NOT NULL,
    assessment_id uuid NOT NULL
);

CREATE TABLE public.rubric_criteria (
    id uuid NOT NULL,
    description text NOT NULL,
    "position" integer NOT NULL,
    question_id uuid NOT NULL
);

CREATE TABLE public.submissions (
    id uuid NOT NULL,
    assessment_id uuid NOT NULL,
    started_at timestamp(6) with time zone NOT NULL,
    status character varying(30) NOT NULL,
    student_id uuid NOT NULL,
    submitted_at timestamp(6) with time zone,
    CONSTRAINT submissions_status_check CHECK (((status)::text = ANY ((ARRAY['IN_PROGRESS'::character varying, 'SUBMITTED'::character varying])::text[])))
);

CREATE TABLE public.users (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    email character varying(255) NOT NULL,
    full_name character varying(255) NOT NULL,
    password_hash character varying(255) NOT NULL,
    role character varying(20) NOT NULL,
    status character varying(20) NOT NULL,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['ADMIN'::character varying, 'TEACHER'::character varying, 'STUDENT'::character varying])::text[]))),
    CONSTRAINT users_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying, 'SUSPENDED'::character varying])::text[])))
);

ALTER TABLE ONLY public.answers
    ADD CONSTRAINT answers_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.assessments
    ADD CONSTRAINT assessments_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT courses_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.criterion_results
    ADD CONSTRAINT criterion_results_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.enrollments
    ADD CONSTRAINT enrollments_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.questions
    ADD CONSTRAINT questions_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.rubric_criteria
    ADD CONSTRAINT rubric_criteria_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.submissions
    ADD CONSTRAINT submissions_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT uk5qdao06h1my8arr58vvvthwb6 UNIQUE (enrollment_code);

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT uk61og8rbqdd2y28rx2et5fdnxd UNIQUE (code);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);

ALTER TABLE ONLY public.criterion_results
    ADD CONSTRAINT uk_criterion_result_answer_criterion UNIQUE (answer_id, rubric_criterion_id);

ALTER TABLE ONLY public.submissions
    ADD CONSTRAINT uk_submission_assessment_student UNIQUE (assessment_id, student_id);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.questions
    ADD CONSTRAINT fk2g7a2cfymnunug0860elnr447 FOREIGN KEY (assessment_id) REFERENCES public.assessments(id);

ALTER TABLE ONLY public.rubric_criteria
    ADD CONSTRAINT fkia0ht7fh6jyggqm4ufu6lt17x FOREIGN KEY (question_id) REFERENCES public.questions(id);

ALTER TABLE ONLY public.criterion_results
    ADD CONSTRAINT fkod73s6byvj4xtufx63fhxh6yt FOREIGN KEY (answer_id) REFERENCES public.answers(id);

ALTER TABLE ONLY public.answers
    ADD CONSTRAINT fkrg2tg6244faytkhlsemsmrwe0 FOREIGN KEY (submission_id) REFERENCES public.submissions(id);

