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
    compileOnly("jakarta.enterprise:jakarta.enterprise.cdi-api")
    compileOnly("jakarta.ws.rs:jakarta.ws.rs-api")
    compileOnly("org.eclipse.microprofile.config:microprofile-config-api")


    testFixturesImplementation("jakarta.inject:jakarta.inject-api:2.0.1")
    testFixturesImplementation("org.eclipse.microprofile.config:microprofile-config-api:3.0.3")

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
    testImplementation("org.hamcrest:hamcrest:2.1")
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

tasks.withType<Test> {
    environment = env.allVariables
}

// endregion

// region code coverage

tasks.jacocoTestCoverageVerification {
    if (!isWindowsBuild()) {
        violationRules {
            rule {
                limit {
                    minimum = BigDecimal.valueOf(0.66)
                }
            }
        }
    }
}

// endregion