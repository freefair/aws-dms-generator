plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.power-assert") version "2.0.0"
    antlr
}

group = "io.freefair"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
    implementation("org.jetbrains.kotlinx:kotlinx-cli-jvm:0.3.6")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.3")
    antlr("org.antlr:antlr4:4.5")
}

sourceSets {
    main {
        java {
            // telling that output generateGrammarSource should be part of main source set
            // actuall passed value will be equal to `outputDirectory` that we configured above
            srcDir(tasks.generateGrammarSource)
        }
    }
}

val fatJar = task("fatJar", type = Jar::class) {
    manifest {
        attributes["Implementation-Title"] = "Gradle Jar File Example"
        attributes["Implementation-Version"] = version
        attributes["Main-Class"] = "io.freefair.dmsdsl.MainKt"
    }
    duplicatesStrategy = DuplicatesStrategy.WARN
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}

tasks.generateGrammarSource {
    maxHeapSize = "64m"
    arguments = arguments + "-visitor" + "-long-messages"
}

tasks.compileKotlin {
    dependsOn(tasks.generateGrammarSource)
}

tasks.test {
    useJUnitPlatform()
}