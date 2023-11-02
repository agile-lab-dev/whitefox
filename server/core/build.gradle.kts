import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    `java-library`
    `java-test-fixtures`
    id("io.quarkus")
    id("whitefox.java-conventions")
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

// region dependencies

dependencies {
    val hadoopVersion = "3.3.6"
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    // QUARKUS
    compileOnly("jakarta.inject:jakarta.inject-api")
    compileOnly("jakarta.enterprise:jakarta.enterprise.cdi-api")
    compileOnly("jakarta.ws.rs:jakarta.ws.rs-api")
    compileOnly("org.eclipse.microprofile.config:microprofile-config-api")

    // DELTA
    implementation("io.delta:delta-standalone_2.13:0.6.0")
    implementation("org.apache.hadoop:hadoop-common:3.3.6")

    //AWS
    compileOnly("com.amazonaws:aws-java-sdk-bom:1.12.367")
    compileOnly("com.amazonaws:aws-java-sdk-s3:1.12.367")
    implementation(String.format("org.apache.hadoop:hadoop-aws:%s", hadoopVersion))

    // TEST
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.quarkus:quarkus-arc")
    testImplementation(project(":server:persistence:memory"))
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