import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.30"
    `maven-publish`
    signing
    id("org.jetbrains.dokka") version "1.4.20"
    id("io.gitlab.arturbosch.detekt").version("1.16.0")
}


allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    version = System.getenv("RELEASE_VERSION") ?: "local"
    group = "br.com.guiabolso"

    repositories {
        mavenCentral()
    }

    dependencies {
        // Kotlin
        implementation(kotlin("reflect"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")

        // SLF4J
        implementation("org.slf4j:slf4j-api:1.7.25")

        // Logback
        testImplementation("ch.qos.logback:logback-classic:1.2.3")

        // JUnit
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.2")

        // Mockk
        testImplementation("io.mockk:mockk:1.10.0")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<KotlinCompile> {
        targetCompatibility = JavaVersion.VERSION_1_8.toString()
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        kotlinOptions {
            useIR = true
            jvmTarget = "1.8"
        }
    }

    val sourcesJar by tasks.registering(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.getByName("main").allSource)
    }

    val javadoc = tasks.named("javadoc")
    val javadocsJar by tasks.creating(Jar::class) {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Assembles java doc to jar"
        archiveClassifier.set("javadoc")
        from(javadoc)
    }

    publishing {

        repositories {
            maven {
                url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = System.getenv("OSSRH_USERNAME")
                    password = System.getenv("OSSRH_PASSWORD")
                }
            }
        }

        publications.register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(javadocsJar)
            artifact(sourcesJar.get())
            artifactId = "events-${this@allprojects.name}"

            pom {
                name.set("Events Protocol")
                description.set("Events Protocol")
                url.set("https://github.com/GuiaBolso/events-protocol")

                scm {
                    url.set("https://github.com/GuiaBolso/events-protocol")
                    connection.set("scm:git:https://github.com/GuiaBolso/events-protocol")
                }

                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://opensource.org/licenses/Apache-2.0")
                    }
                }

                developers {
                    developer {
                        id.set("Guiabolso")
                        name.set("Guiabolso")
                    }
                }
            }
        }

    }

    signing {
        val signingKey: String? by project
        val signingPassword: String? by project

        useGpgCmd()
        if (signingKey != null && signingPassword != null) {
            useInMemoryPgpKeys(signingKey, signingPassword)
        }

        sign((extensions.getByName("publishing") as PublishingExtension).publications)
    }
}
