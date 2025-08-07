import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "2.1.0"
    kotlin("kapt") version "2.1.0"
    id("com.gradleup.shadow") version "9.0.0-beta6"
    id("org.ajoberstar.grgit") version "4.1.1"
}

allprojects {
    group = "mc.arch.skin.bridge"
    version = "1.0.0-oss"

    repositories {
        mavenCentral()
        maven("https://repo.inventivetalent.org/repository/public/")
        configureArchRepository()
    }
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly("gg.scala.commons:bukkit:7.1.0")
    compileOnly("gg.scala.spigot:server:1.1.3")

    api("org.mineskin:java-client:3.0.6")
    api("org.mineskin:java-client-java11:3.0.6")
}

kotlin {
    jvmToolchain(jdkVersion = 21)
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
    archiveFileName.set(
        "SkinBridge.jar"
    )
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
    options.fork()
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        javaParameters = true
        jvmTarget = JvmTarget.JVM_21
    }
}

tasks.getByName("build").dependsOn("shadowJar")

fun RepositoryHandler.configureArchRepository(dev: Boolean = false)
{
    maven("${property("artifactory_contextUrl")}/gradle-${if (dev) "dev" else "release"}") {
        name = "arch"
        credentials {
            username = property("artifactory_user").toString()
            password = property("artifactory_password").toString()
        }
    }
}
