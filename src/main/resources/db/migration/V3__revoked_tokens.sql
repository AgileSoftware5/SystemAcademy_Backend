-- Tokens JWT revocados por logout (RevokedTokenEntity). Se consultan en cada petición autenticada.
CREATE TABLE public.revoked_tokens (
    token_id character varying(64) NOT NULL,
    expires_at timestamp(6) with time zone NOT NULL,
    CONSTRAINT revoked_tokens_pkey PRIMARY KEY (token_id)
);

ALTER TABLE public.revoked_tokens ENABLE ROW LEVEL SECURITY;
