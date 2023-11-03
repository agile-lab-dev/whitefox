plugins {
    java
    id("com.diffplug.spotless")
    id("whitefox.java-conventions")
}

group = "io.whitefox"
version = "spark-connector"

repositories {
    mavenCentral()
}

val hadoopVersion = "3.3.6"
dependencies {
    // DELTA
    testImplementation(String.format("org.apache.hadoop:hadoop-common:%s", hadoopVersion))
    testImplementation("io.delta:delta-sharing-spark_2.12:1.0.2")

    //SPARK
    testImplementation("org.apache.spark:spark-core_2.12:3.3.2")
    testImplementation("org.apache.spark:spark-sql_2.12:3.3.2")
    testImplementation("com.github.mrpowers:spark-fast-tests_2.12:1.3.0")

    //JUNIT
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")

}

// region code formatting
spotless {
    java {}
}
// endregion

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}