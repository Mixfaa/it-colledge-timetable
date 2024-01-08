
plugins {
    kotlin("jvm") version "1.9.21"
    application
}

group = "ua.helpme"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-html:0.10.1")
}

application {
    mainClass = "ua.helpme.MainKt"
}


tasks.jar {
    manifest {
        attributes["Main-Class"] = "ua.helpme.MainKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
}
kotlin {
    jvmToolchain(17)
}
tasks.test {
    useJUnitPlatform()
}
