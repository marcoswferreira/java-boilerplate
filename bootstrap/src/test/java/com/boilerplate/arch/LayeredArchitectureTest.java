package com.boilerplate.arch;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * ArchUnit rules that enforce the hexagonal architecture dependency constraints.
 *
 * <p>Rules:
 * <ul>
 *   <li>Core must not depend on anything outside itself (no Spring, no JPA, no Kafka)
 *   <li>Application may only depend on core
 *   <li>Infrastructure may depend on core and application
 *   <li>Web may depend on core and application (NOT infrastructure directly)
 *   <li>Bootstrap may depend on all layers
 * </ul>
 */
@AnalyzeClasses(
    packages = "com.boilerplate",
    importOptions = ImportOption.DoNotIncludeTests.class)
public class LayeredArchitectureTest {

  @ArchTest
  public static final ArchRule coreHasNoFrameworkDependencies =
      noClasses()
          .that()
          .resideInAPackage("com.boilerplate.core..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.springframework..",
              "jakarta.persistence..",
              "org.hibernate..",
              "org.apache.kafka..")
          .because("The core domain must be framework-independent");

  @ArchTest
  public static final ArchRule applicationDoesNotDependOnInfrastructure =
      noClasses()
          .that()
          .resideInAPackage("com.boilerplate.application..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "com.boilerplate.infrastructure..",
              "com.boilerplate.web..")
          .because("Application use-cases must not depend on adapters");

  @ArchTest
  public static final ArchRule webDoesNotDependOnInfrastructureInternals =
      noClasses()
          .that()
          .resideInAPackage("com.boilerplate.web..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("com.boilerplate.infrastructure.persistence..")
          .because("Web adapters must not directly access persistence internals");

  @ArchTest
  public static final ArchRule layeredArchitecture =
      layeredArchitecture()
          .consideringAllDependencies()
          .layer("Core").definedBy("com.boilerplate.core..")
          .layer("Application").definedBy("com.boilerplate.application..")
          .layer("Infrastructure").definedBy("com.boilerplate.infrastructure..")
          .layer("Web").definedBy("com.boilerplate.web..")
          .layer("Bootstrap").definedBy("com.boilerplate.bootstrap..", "com.boilerplate.BoilerplateApplication")
          .whereLayer("Core").mayNotAccessAnyLayer()
          .whereLayer("Application").mayOnlyAccessLayers("Core")
          .whereLayer("Infrastructure").mayOnlyAccessLayers("Core", "Application")
          .whereLayer("Web").mayOnlyAccessLayers("Core", "Application")
          .whereLayer("Bootstrap").mayOnlyAccessLayers("Core", "Application", "Infrastructure", "Web");
}
