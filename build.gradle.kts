import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    java
    id("io.quarkus")
    id("org.openapi.generator") version "6.6.0"
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project
val generatedSourcesDir = "${layout.buildDirectory.get()}/generated/openapi"

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-resteasy-reactive")
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
    implementation("io.quarkus:quarkus-arc")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

group = "it.agilelab"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}

tasks.register<GenerateTask>("openapiGenerateLakeSharing") {
    generatorName.set("java")
    inputSpec.set("$rootDir/docs/protocol/lake-sharing-protocol-api.yml")
    library.set("native")
    outputDir.set(generatedSourcesDir)
    additionalProperties.set(
        mapOf(
            "apiPackage" to "io.lake.sharing.api.client",
            "invokerPackage" to "io.lake.sharing.api.utils",
            "modelPackage" to "io.lake.sharing.api.model",
            "dateLibrary" to "java8",
            "openApiNullable" to "true",
            "serializationLibrary" to "jackson",
            "useJakartaEe" to "true"
        )
    )
}

tasks.register<GenerateTask>("openapiGenerateDeltaSharing") {
    generatorName.set("java")
    inputSpec.set("$rootDir/docs/protocol/delta-sharing-protocol-api.yml")
    library.set("native")
    outputDir.set(generatedSourcesDir)
    additionalProperties.set(
        mapOf(
            "apiPackage" to "io.delta.sharing.api.client",
            "invokerPackage" to "io.delta.sharing.api.utils",
            "modelPackage" to "io.delta.sharing.api.model",
            "dateLibrary" to "java8",
            "openApiNullable" to "true",
            "serializationLibrary" to "jackson",
            "useJakartaEe" to "true"
        )
    )
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
    dependsOn(tasks.named("openapiGenerateLakeSharing"), tasks.named("openapiGenerateDeltaSharing"))
}
tasks.quarkusBuild {
    nativeArgs {
        "additional-build-args" to "-H:-CheckToolchain"
    }
}

buildscript {
    configurations.all {
        resolutionStrategy {
            force("org.yaml:snakeyaml:1.33")
        }
    }
}
sourceSets {
    getByName("main") {
        java {
            srcDir("$generatedSourcesDir/src/main/java")
        }
    }
}