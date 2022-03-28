package com.ephemeris.publish

plugins {
    id("signing")
    id("maven-publish")
}

group = "io.github.boswelja.ephemeris"
description = "The flexible, multiplatform calendar library!"
val version: String? by project

signing {
//    val signingKey: String? by project
//    val signingPassword: String? by project
//    useInMemoryPgpKeys(signingKey, signingPassword)
//    sign(publishing.publications)
}

// Empty javadoc JAR for publishing. Kotlin Multiplatform does not build Javadoc
tasks {
    create<Jar>("javadocJar") {
        archiveClassifier.set("javadoc")
    }
}

publishing {
    publications {
        repositories {
            maven("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/") {
                val ossrhUsername: String? by project
                val ossrhPassword: String? by project
                name = "sonatype"
                credentials {
                    username = ossrhUsername
                    password = ossrhPassword
                }
            }
        }
        withType<MavenPublication> {
            artifact(tasks["javadocJar"])

            pom {
                // If the project has a parent, use that as part of the name
                val projectName = if (project.parent != project.rootProject) {
                    "${project.parent!!.name}-${project.name}"
                } else project.name
                name.set(projectName)
                description.set(project.description)
                url.set("https://github.com/boswelja/Ephemeris")
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
                    developer {
                        id.set("Iannnr")
                        name.set("Ian Roberts")
                        url.set("https://github.com/Iannnr")
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
}