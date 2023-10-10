import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

val openApiCodeGenDir = "generated/openapi"

val generatedCodeDirectory = generatedCodeDirectory(layout, openApiCodeGenDir)

val clientGeneratorProperties = mapOf(
    "apiPackage" to "io.whitefox.sharing.api.client",
    "invokerPackage" to "io.whitefox.sharing.api.utils",
    "modelPackage" to "io.whitefox.sharing.api.client.model",
    "dateLibrary" to "java8",
    "sourceFolder" to "src/gen/java",
    "openApiNullable" to "true",
    "annotationLibrary" to "none",
    "serializationLibrary" to "jackson",
    "useJakartaEe" to "true",
    "useRuntimeException" to "true"
)

plugins {
    java
    id("io.quarkus")
    id("whitefox.java-conventions")
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-rest-client-reactive-jackson") // TODO review
    implementation("io.quarkus:quarkus-arc") // TODO review
    implementation("io.quarkus:quarkus-resteasy-reactive") // TODO review
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured") // TODO review
}

tasks.register<GenerateTask>("openapiGenerateWhitefox") {
    generatorName.set("java")
    inputSpec.set("$rootDir/docs/protocol/whitefox-protocol-api.yml")
    library.set("native")
    outputDir.set(generatedCodeDirectory)
    additionalProperties.set(clientGeneratorProperties)
}

tasks.register<GenerateTask>("openapiGenerateDeltaSharing") {
    generatorName.set("java")
    inputSpec.set("$rootDir/docs/protocol/delta-sharing-protocol-api.yml")
    library.set("native")
    outputDir.set(generatedCodeDirectory)
    additionalProperties.set(clientGeneratorProperties)
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}

tasks.jacocoTestReport {
    val packagesToExclude = fileTree("$generatedCodeDirectory/src/gen/java")
            .map { f -> f.relativeTo( file("$generatedCodeDirectory/src/gen/java")).toString() }
            .map { path -> path.substringBeforeLast(".") + "**"}

    doFirst {
        logger.debug("Excluding generated classes: $packagesToExclude")
    }

    doLast {
        println("The report can be found at: file://" + reports.html.entryPoint)
    }

    classDirectories.setFrom(
            files(classDirectories.files.map {
                fileTree(it) {
                    exclude(packagesToExclude)
                }
            })
    )
    finalizedBy(tasks.jacocoTestCoverageVerification)
}

tasks.jacocoTestCoverageVerification {

    val packagesToExclude = fileTree("$generatedCodeDirectory/src/gen/java")
            .map { f -> f.relativeTo( file("$generatedCodeDirectory/src/gen/java")).toString() }
            .map { path -> path.substringBeforeLast(".") + "**"}

    classDirectories.setFrom(
            files(classDirectories.files.map {
                fileTree(it) {
                    exclude(packagesToExclude)
                }
            })
    )

    violationRules {
        rule {
            limit {
                minimum = "0.7".toBigDecimal()
            }
            limit {

            }
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
    dependsOn(tasks.named("openapiGenerateWhitefox"), tasks.named("openapiGenerateDeltaSharing"))
}

spotless {
    java {
        targetExclude("${relativeGeneratedCodeDirectory(layout, openApiCodeGenDir)}/**/*.java")
    }
}

sourceSets {
    getByName("main") {
        java {
            srcDir("${generatedCodeDirectory}/src/gen/java")
        }
    }
}