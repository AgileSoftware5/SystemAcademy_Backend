-- Defensa en profundidad para Supabase: su API REST (PostgREST) expone el esquema public.
-- Con RLS activo y sin políticas, los roles anon/authenticated no ven nada.
-- El backend conecta como dueño de las tablas (BYPASSRLS / owner), así que no se ve afectado.
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.courses ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.enrollments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.assessments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.questions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.rubric_criteria ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.submissions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.answers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.criterion_results ENABLE ROW LEVEL SECURITY;
