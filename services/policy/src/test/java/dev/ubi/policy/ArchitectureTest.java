package dev.ubi.policy;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Правила гексагональной архитектуры policy, проверяемые на каждом {@code ./gradlew test}.
 * allowEmptyShould и withOptionalLayers нужны, пока пакеты пустые: иначе ArchUnit
 * по умолчанию падает на правилах, под которые не попал ни один класс.
 */
@AnalyzeClasses(packages = "dev.ubi.policy", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    // В policy домен event-sourced и полностью чистый: даже JPA-аннотаций нет.
    @ArchTest
    static final ArchRule domainIsFrameworkFree = classes()
        .that().resideInAPackage("..domain..")
        .should().onlyDependOnClassesThat().resideInAnyPackage(
            "..domain..",
            "java..",
            "org.jspecify.."
        )
        .allowEmptyShould(true);

    @ArchTest
    static final ArchRule layers = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .withOptionalLayers(true)
            .layer("Domain").definedBy("..domain..")
            .layer("Application").definedBy("..application..")
            .layer("Adapters").definedBy("..adapter..")
            .whereLayer("Adapters").mayNotBeAccessedByAnyLayer()
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Adapters")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Adapters");
}
