import org.jetbrains.dokka.gradle.DokkaTaskPartial
import java.net.URL

plugins {
    kotlin("multiplatform")
//    kotlin("native.cocoapods")
    id("org.jetbrains.kotlinx.kover")
    id("com.ephemeris.library.android")
    id("com.ephemeris.publish.maven")
    id("dev.petuska.npm.publish") version "2.1.2"
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.dokka")
}

//version = findProperty("version")?.let {
//    if (it == Project.DEFAULT_VERSION) null
//    else it
//} ?: "0.1.0"

kotlin {
    explicitApi()

    // Android targets
    android {
        publishLibraryVariants("release")
    }

    // JVM targets
    jvm()

    // JS targets
    js(IR) {
        binaries.library()
        nodejs()
    }

    // Windows targets
    mingwX64()

    // Linux targets
    linuxX64()

    // Mac targets
    ios()
    macosX64()
    macosArm64()

    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
        commonMain {
            dependencies {
                api(libs.kotlinx.datetime)
                api(libs.kotlinx.coroutines.core)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }

//    cocoapods {
//        summary = "The flexible, multiplatform calendar library!"
//        homepage = "https://boswelja.github.io/Ephemeris"
//        license = "MIT"
//
//        framework {
//            baseName = "EphemerisCore"
//        }
//    }
}

android {
    namespace = "com.boswelja.ephemeris.core"
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

detekt {
    config = files("${rootDir.absolutePath}/config/detekt/detekt-base.yml")
    basePath = rootDir.absolutePath
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt> {
    reports.sarif.required.set(true)
}

tasks.koverVerify {
    rule {
        name = "Code line coverage"
        bound {
            minValue = 90
        }
    }
}

publishing {
    publications.withType<MavenPublication> {
        artifact(tasks["javadocJar"])

        pom {
            name.set("core")
            description.set("The flexible, multiplatform calendar library!")
            url.set("https://github.com/boswelja/Ephemeris/tree/main/core")
            licenses {
                license {
                    name.set("GPLv3")
                    url.set("https://github.com/boswelja/Ephemeris/blob/main/LICENSE")
                }
            }
            developers {
                developer {
                    id.set("boswelja")
                    name.set("Jack Boswell")
                    email.set("boswelja@outlook.com")
                    url.set("https://github.com/boswelja")
                }
            }
            scm {
                connection.set("scm:git:github.com/boswelja/Ephemeris.git")
                developerConnection.set("scm:git:ssh://github.com/boswelja/Ephemeris.git")
                url.set("https://github.com/boswelja/Ephemeris")
            }
        }
    }
}

npmPublishing {
    repositories {
        repository("npmjs") {
            val npmToken: String? by project
            registry = uri("https://registry.npmjs.org")
            authToken = npmToken
        }
    }

    readme = file("README.md")
    organization = "ephemeris"
}

tasks.withType<DokkaTaskPartial>().configureEach {
    dokkaSourceSets.configureEach {
        sourceLink {
            localDirectory.set(file("src/commonMain/kotlin"))

            remoteUrl.set(
                URL(
                    "https://github.com/boswelja/Ephemeris/blob/main/core/src/commonMain/kotlin"
                )
            )
        }

        externalDocumentationLink("https://kotlin.github.io/kotlinx.coroutines/")
    }
}
