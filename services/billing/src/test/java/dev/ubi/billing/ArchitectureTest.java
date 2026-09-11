package dev.ubi.billing;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Правила гексагональной архитектуры billing, проверяемые на каждом {@code ./gradlew test}.
 * allowEmptyShould и withOptionalLayers нужны, пока пакеты пустые: иначе ArchUnit
 * по умолчанию падает на правилах, под которые не попал ни один класс.
 */
@AnalyzeClasses(packages = "dev.ubi.billing", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    // Прагматичный компромисс: JPA-аннотации в домене разрешены (см. docs/adr/0001), Spring и Kafka — нет.
    @ArchTest
    static final ArchRule domainIsFrameworkFree = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "org.apache.kafka..")
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
