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
    compileOnly(libs.simplified.annotations)
    annotationProcessor(libs.simplified.annotations)
    testCompileOnly(libs.simplified.annotations)
    testAnnotationProcessor(libs.simplified.annotations)

    // Tests
    testImplementation(libs.hamcrest)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.junit.platform.launcher)

    // Simplified Libraries (github.com/simplified-dev)
    api("com.github.simplified-dev:collections") { version { strictly("4029e80") } }
    api("com.github.simplified-dev:utils") { version { strictly("92ae878") } }
    api("com.github.simplified-dev:reflection") { version { strictly("5186e88") } }
    api("com.github.simplified-dev:gson-extras") { version { strictly("3ac0d4f") } }
    api("com.github.simplified-dev:client") { version { strictly("345de19") } }

    // Minecraft-Library (github.com/minecraft-library)
    // MinecraftServerPing parses legacy TextSegment MOTDs.
    api("com.github.minecraft-library:text") { version { strictly("84f8f1a") } }

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
