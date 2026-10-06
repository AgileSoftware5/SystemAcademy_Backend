package io.github.agilesoftware5.systemacademy.shared.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * El aggregate solo registra lo que pasó; el adapter de persistencia publica los eventos
 * después de guardar, así nunca se reacciona a un cambio que no llegó a la base.
 */
public abstract class AggregateRoot {

    private final List<Object> domainEvents = new ArrayList<>();

    protected void registerDomainEvent(Object event) {
        domainEvents.add(event);
    }

    public List<Object> domainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
