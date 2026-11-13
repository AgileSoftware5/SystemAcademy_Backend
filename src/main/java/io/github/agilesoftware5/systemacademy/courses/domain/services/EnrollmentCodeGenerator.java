package io.github.agilesoftware5.systemacademy.courses.domain.services;

/** Puerto: el dominio pide un código de matrícula sin conocer la estrategia de generación (random, legible, etc). */
public interface EnrollmentCodeGenerator {

    String generate();
}
