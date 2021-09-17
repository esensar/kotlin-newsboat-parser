buildscript {
    repositories {
        maven("https://plugins.gradle.org/m2/")
    }
    dependencies {
        classpath("org.jlleitschuh.gradle:ktlint-gradle:10.2.0")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.5.0")
    }
}

plugins {
    kotlin("multiplatform") version "1.5.21"
}

val snapshot: String? by project

group = "com.ensarsarajcic.kotlinx.newsboatparser"
version = version.toString() + if (snapshot == "true") "-SNAPSHOT" else ""

val sonatypeStaging = "https://oss.sonatype.org/service/local/staging/deploy/maven2"
val sonatypeSnapshots = "https://oss.sonatype.org/content/repositories/snapshots"

val sonatypePassword: String? by project
val sonatypeUsername: String? by project

val sonatypePasswordEnv: String? = System.getenv()["SONATYPE_PASSWORD"]
val sonatypeUsernameEnv: String? = System.getenv()["SONATYPE_USERNAME"]

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }
    js {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                    webpackConfig.cssSupport.enabled = true
                }
            }
        }
    }
    ios()
    tvos()
    watchos()
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }


    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting
        val jsMain by getting
        val jsTest by getting
        val nativeMain by getting
        val nativeTest by getting
    }
}

afterEvaluate {
    val dokkaHtml = tasks["dokkaHtml"]
    tasks {
        create<Jar>("javadocJar") {
            dependsOn(dokkaHtml)
            archiveClassifier.set("javadoc")
            from(dokkaHtml)
        }
    }

    configure<SigningExtension> {
        isRequired = false
        sign(extensions.getByType<PublishingExtension>().publications)
    }

    configure<PublishingExtension> {
        publications.withType(MavenPublication::class) {
            artifact(tasks["javadocJar"])
            pom {
                name.set("Kotlinx Newsboat Parser")
                description.set("Kotlin parser for newsboat configuration and url file format")
                url.set("https://github.com/esensar/kotlinx-newsboat-parser")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/mit-license.php")
                    }
                }
                developers {
                    developer {
                        id.set("esensar")
                        name.set("Ensar Sarajčić")
                        url.set("https://ensarsarajcic.com")
                        email.set("es.ensar@gmail.com")
                    }
                }
                scm {
                    url.set("https://github.com/esensar/kotlinx-newsboat-parser")
                    connection.set("scm:git:https://github.com/esensar/kotlinx-newsboat-parser.git")
                    developerConnection.set("scm:git:git@github.com:esensar/kotlinx-newsboat-parser.git")
                }
            }
        }
        repositories {
            maven {
                url = uri(sonatypeStaging)
                credentials {
                    username = sonatypeUsername ?: sonatypeUsernameEnv ?: ""
                    password = sonatypePassword ?: sonatypePasswordEnv ?: ""
                }
            }

            maven {
                name = "snapshot"
                url = uri(sonatypeSnapshots)
                credentials {
                    username = sonatypeUsername ?: sonatypeUsernameEnv ?: ""
                    password = sonatypePassword ?: sonatypePasswordEnv ?: ""
                }
            }

            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/esensar/kotlinx-newsboat-parser")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }
}

apply(plugin = "org.jlleitschuh.gradle.ktlint")
apply(plugin = "maven-publish")
apply(plugin = "signing")
apply(plugin = "org.jetbrains.dokka")