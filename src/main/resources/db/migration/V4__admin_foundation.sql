CREATE INDEX idx_courses_instructor ON public.courses (instructor_id);
CREATE INDEX idx_enrollments_course ON public.enrollments (course_id);
CREATE INDEX idx_enrollments_student ON public.enrollments (student_id);
CREATE INDEX idx_assessments_course ON public.assessments (course_id);
CREATE INDEX idx_questions_assessment ON public.questions (assessment_id);
CREATE INDEX idx_submissions_submitted_at ON public.submissions (submitted_at);
CREATE INDEX idx_answers_submission ON public.answers (submission_id);
CREATE INDEX idx_answers_grading_status ON public.answers (grading_status);
CREATE INDEX idx_users_role_status ON public.users (role, status);
CREATE INDEX idx_users_created_at ON public.users (created_at);

CREATE TABLE public.audit_log (
    id uuid NOT NULL,
    actor_id uuid NOT NULL,
    action character varying(40) NOT NULL,
    target_type character varying(30) NOT NULL,
    target_id uuid,
    detail character varying(255),
    created_at timestamp(6) with time zone DEFAULT now() NOT NULL,
    CONSTRAINT audit_log_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_audit_log_created_at ON public.audit_log (created_at DESC);
CREATE INDEX idx_audit_log_actor ON public.audit_log (actor_id);

ALTER TABLE public.audit_log ENABLE ROW LEVEL SECURITY;
