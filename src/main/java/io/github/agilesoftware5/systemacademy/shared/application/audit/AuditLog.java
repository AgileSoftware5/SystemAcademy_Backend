package io.github.agilesoftware5.systemacademy.shared.application.audit;

/** Registro de acciones administrativas. Solo se agrega, nunca se modifica ni se borra. */
public interface AuditLog {

    void record(AuditEntry entry);
}
