package io.github.agilesoftware5.systemacademy.shared.infrastructure.audit;

import io.github.agilesoftware5.systemacademy.shared.application.audit.AuditEntry;
import io.github.agilesoftware5.systemacademy.shared.application.audit.AuditLog;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JdbcAuditLog implements AuditLog {

    private final JdbcClient jdbc;

    public JdbcAuditLog(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void record(AuditEntry entry) {
        jdbc.sql("""
                INSERT INTO audit_log (id, actor_id, action, target_type, target_id, detail)
                VALUES (:id, :actor, :action, :targetType, :targetId, :detail)
                """)
                .param("id", UUID.randomUUID())
                .param("actor", entry.actorId())
                .param("action", entry.action())
                .param("targetType", entry.targetType())
                .param("targetId", entry.targetId())
                .param("detail", entry.detail())
                .update();
    }
}
