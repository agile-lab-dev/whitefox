import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    `java-library`
    id("io.quarkus")
    id("whitefox.java-conventions")
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

// region dependencies

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    // QUARKUS
    compileOnly("jakarta.inject:jakarta.inject-api")
    compileOnly("jakarta.enterprise:jakarta.enterprise.cdi-api")
    compileOnly("jakarta.ws.rs:jakarta.ws.rs-api")
    compileOnly("org.eclipse.microprofile.config:microprofile-config-api")

    compileOnly(project(":server:core"))

    // TEST
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.quarkus:quarkus-arc")
}

// endregion

// region java compile

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

// endregion

// region test running

tasks.check {
    dependsOn(deltaTest)
}
tasks.register("devCheck") {
    dependsOn(tasks.spotlessApply)
    finalizedBy(tasks.check)
    description = "Useful command when iterating locally to apply spotless formatting then running all the checks"
}
tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
    }
}

val deltaTestClasses =
    listOf("io.whitefox.api.deltasharing.DeltaSharedTableTest.*", "io.whitefox.services.DeltaLogServiceTest.*")

tasks.test {
    description = "Runs all other test classes by not forking the jvm."
    filter {
        deltaTestClasses.forEach { s ->
            excludeTestsMatching(s)
        }
    }
    forkEvery = 0
}
val deltaTest = tasks.register<Test>("deltaTest") {
    description = "Runs delta test classes by forking the jvm."
    group = "verification"
    filter {
        deltaTestClasses.forEach { s ->
            includeTestsMatching(s)
        }
    }
    forkEvery = 0
}

// endregion

// region code coverage

tasks.check {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.check) // tests are required to run before generating the report
}

tasks.jacocoTestReport {
    doLast {
        logger.lifecycle("The report can be found at: file://" + reports.html.entryPoint)
    }

    finalizedBy(tasks.jacocoTestCoverageVerification)
}

tasks.jacocoTestCoverageVerification {
    if (!isWindowsBuild()) {
        violationRules {
            rule {
                limit {
                    minimum = BigDecimal.valueOf(0.62)
                }
            }
        }
    }
}

// endregion