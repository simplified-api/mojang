plugins {
    id("java-library")
    idea
}

group = "dev.sbs"
version = "0.1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven(url = "https://central.sonatype.com/repository/maven-snapshots")
    maven(url = "https://jitpack.io")
}

dependencies {
    // Simplified Annotations
    annotationProcessor(libs.simplified.annotations)

    // Lombok
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)

    // Tests
    testImplementation(libs.hamcrest)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.junit.platform.launcher)

    // Simplified Libraries (github.com/simplified-dev)
    api("com.github.simplified-dev:collections") { version { strictly("2f2aa58") } }
    api("com.github.simplified-dev:utils") { version { strictly("37dc4a8") } }
    api("com.github.simplified-dev:reflection") { version { strictly("b2cf834") } }
    api("com.github.simplified-dev:gson-extras") { version { strictly("37a2c2f") } }
    api("com.github.simplified-dev:client") { version { strictly("5a5d32e") } }

    // Minecraft-Library (github.com/minecraft-library)
    // MinecraftServerPing parses legacy TextSegment MOTDs.
    api("com.github.minecraft-library:text") { version { strictly("2a75527") } }

    // Gson - @SerializedName, custom JsonDeserializer, and GsonSettings in MinecraftServerPing
    api(libs.gson)
}

idea {
    module {
        excludeDirs.addAll(listOf(
            layout.projectDirectory.dir(".schema").asFile
        ))
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
}
