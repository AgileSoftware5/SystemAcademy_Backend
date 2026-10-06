package io.github.agilesoftware5.systemacademy;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

/** Reglas de la arquitectura hexagonal/DDD verificadas en cada build. */
@AnalyzeClasses(
        packages = "io.github.agilesoftware5.systemacademy",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class ArchitectureTest {

    @ArchTest
    static final ArchRule domainIsFrameworkFree = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    "..infrastructure..",
                    "..interfaces.."
            )
            .because("el dominio no conoce frameworks ni adaptadores");

    @ArchTest
    static final ArchRule applicationDoesNotDependOnAdapters = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..interfaces..")
            .because("los casos de uso dependen de puertos, no de implementaciones");

    @ArchTest
    static final ArchRule controllersGoThroughApplicationServices = noClasses()
            .that().resideInAPackage("..interfaces.rest..")
            .should().dependOnClassesThat().resideInAnyPackage("..domain.repositories..", "..infrastructure..")
            .because("los controllers llaman a command/query services, nunca a repositorios");

    @ArchTest
    static final ArchRule constructorInjectionOnly = NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

    @ArchTest
    static final ArchRule useLoggerNotStandardStreams = NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;
}
