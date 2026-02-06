package com.example.apiorchestrator.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

public class OrchestratorArchitectureTest {

    private JavaClasses classes;

    @BeforeEach
    public void setup() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.example.apiorchestrator");
    }

    @Test
    public void servicesShouldNotDependOnWebLayer() {
        noClasses()
                .that().resideInAnyPackage("com.example.apiorchestrator.domain.service..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("com.example.apiorchestrator.adapter.in.controller..")
                .because("Services should not depend on web layer")
                .check(classes);
    }

    @Test
    public void domainDoesNotDependOnAdapters() {
        noClasses()
                .that().resideInAnyPackage("com.example.apiorchestrator.domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("com.example.apiorchestrator.adapter..")
                .because("Services should not depend on web layer")
                .check(classes);
    }

    @Test
    public void onlyAdaptersUseFeignClient() {
        noClasses()
                .that().resideOutsideOfPackages("com.example.apiorchestrator.adapter.out..")
                .and().areNotAnnotatedWith(org.springframework.boot.autoconfigure.SpringBootApplication.class)
                .should().dependOnClassesThat().areAnnotatedWith(org.springframework.cloud.openfeign.FeignClient.class)
                .check(classes);
    }

    @Test
    public void controllersHaveCorrectStructure() {
        classes()
                .that().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
                .should().haveSimpleNameEndingWith("Controller")
                .andShould().resideInAPackage("com.example.apiorchestrator.adapter.in.controller..")
                .check(classes);
    }

    @Test
    void serviceClassesShouldHaveSpringServiceAnnotation() {
        classes()
                .that().resideInAPackage("com.example.apiorchestrator.domain.service..")
                .and().areTopLevelClasses()
                .should().beAnnotatedWith(Service.class)
                .check(classes);
    }
}
