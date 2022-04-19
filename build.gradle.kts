import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

// TODO: To be removed once we will migrate to kotlin version 1.6.20
// https://youtrack.jetbrains.com/issue/KT-49109#focus=Comments-27-5667134.0-0
rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().nodeVersion = "16.13.2"
}

plugins {
    kotlin("multiplatform") apply false
    kotlin("plugin.serialization") apply false
    id("base")
}

buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.3")
        classpath("com.adarshr:gradle-test-logger-plugin:2.1.1")
    }
}

val targetSdkVersion by extra(31)
val minSdkVersion by extra(16)
val firebaseBoMVersion by extra("29.0.0")

val cinteropDir: String by extra("src/nativeInterop/cinterop")

tasks {
    val updateVersions by registering {
        dependsOn(
            "firebase-app:updateVersion", "firebase-app:updateDependencyVersion",
            "firebase-auth:updateVersion", "firebase-auth:updateDependencyVersion",
            "firebase-common:updateVersion", "firebase-common:updateDependencyVersion",
            "firebase-database:updateVersion", "firebase-database:updateDependencyVersion",
            "firebase-firestore:updateVersion", "firebase-firestore:updateDependencyVersion",
            "firebase-functions:updateVersion", "firebase-functions:updateDependencyVersion",
            "firebase-config:updateVersion", "firebase-config:updateDependencyVersion"
        )
    }
}

subprojects {

    group = "dev.gitlive"

    apply(plugin="com.adarshr.test-logger")

    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }

    tasks.withType<Sign>().configureEach {
        // disable signing when publishing any artifact to maven local
        onlyIf {
            project.gradle.startParameter.taskNames.none { name ->
                name.matches(Regex("publish.*ToMavenLocal"))
            }
        }
    }

    tasks.whenTaskAdded {
        enabled = when(name) {
            "compileDebugUnitTestKotlinAndroid" -> false
            "compileReleaseUnitTestKotlinAndroid" -> false
            "testDebugUnitTest" -> false
            "testReleaseUnitTest" -> false
            else -> enabled
        }
    }

    tasks {

        val updateVersion by registering(Exec::class) {
            commandLine("npm", "--allow-same-version", "--prefix", projectDir, "version", "${project.property("${project.name}.version")}")
        }

        val updateDependencyVersion by registering(Copy::class) {
            mustRunAfter("updateVersion")
            val from = file("package.json")
            from.writeText(
                from.readText()
                    .replace("firebase-common\": \"([^\"]+)".toRegex(), "firebase-common\": \"${project.property("firebase-common.version")}")
                    .replace("firebase-app\": \"([^\"]+)".toRegex(), "firebase-app\": \"${project.property("firebase-app.version")}")
            )
        }

        val copyReadMe by registering(Copy::class) {
            from(rootProject.file("README.md"))
            into(file("$buildDir/node_module"))
        }

        val copyPackageJson by registering(Copy::class) {
            from(file("package.json"))
            into(file("$buildDir/node_module"))
        }

        val unzipJar by registering(Copy::class) {
            val zipFile = File("$buildDir/libs", "${project.name}-js-${project.version}.jar")
            from(this.project.zipTree(zipFile))
            into("$buildDir/classes/kotlin/js/main/")
        }

        val copyJS by registering {
            mustRunAfter("unzipJar", "copyPackageJson")
            doLast {
                val from = File("$buildDir/classes/kotlin/js/main/${rootProject.name}-${project.name}.js")
                val into = File("$buildDir/node_module/${project.name}.js")
                into.createNewFile()
                into.writeText(
                    from.readText()
                        .replace("require('firebase-kotlin-sdk-", "require('@gitlive/")
                    //                .replace("require('kotlinx-serialization-kotlinx-serialization-runtime')", "require('@gitlive/kotlinx-serialization-runtime')")
                )
            }
        }

        val copySourceMap by registering(Copy::class) {
            from(file("$buildDir/classes/kotlin/js/main/${project.name}.js.map"))
            into(file("$buildDir/node_module"))
        }

        val prepareForNpmPublish by registering {
            dependsOn(
                unzipJar,
                copyPackageJson,
                copySourceMap,
                copyReadMe,
                copyJS
            )
        }

        val publishToNpm by creating(Exec::class) {
            workingDir("$buildDir/node_module")
            isIgnoreExitValue = true
            if(Os.isFamily(Os.FAMILY_WINDOWS)) {
                commandLine("cmd", "/c", "npm publish")
            } else {
                commandLine("npm", "publish")
            }
        }

        withType<Test> {
            testLogging {
                showExceptions = true
                exceptionFormat = TestExceptionFormat.FULL
                showStandardStreams = true
                showCauses = true
                showStackTraces = true
                events = setOf(
                    TestLogEvent.STARTED,
                    TestLogEvent.FAILED,
                    TestLogEvent.PASSED,
                    TestLogEvent.SKIPPED,
                    TestLogEvent.STANDARD_OUT,
                    TestLogEvent.STANDARD_ERROR
                )
            }
        }

        if (projectDir.resolve("$cinteropDir/Cartfile").exists()) { // skipping firebase-common module
            listOf("bootstrap", "update").forEach {
                task<Exec>("carthage${it.capitalize()}") {
                    group = "carthage"
                    executable = "carthage"
                    args(
                        it,
                        "--project-directory", projectDir.resolve(cinteropDir),
                        "--platform", "iOS",
                        "--cache-builds"
                    )
                }
            }
        }

        if (Os.isFamily(Os.FAMILY_MAC)) {
            withType(org.jetbrains.kotlin.gradle.tasks.CInteropProcess::class) {
                dependsOn("carthageBootstrap")
            }
        }

        create("carthageClean", Delete::class.java) {
            group = "carthage"
            delete(
                projectDir.resolve("$cinteropDir/Carthage"),
                projectDir.resolve("$cinteropDir/Cartfile.resolved")
            )
        }
    }

    afterEvaluate  {
        // create the projects node_modules if they don't exist
        if(!File("$buildDir/node_module").exists()) {
            mkdir("$buildDir/node_module")
        }

        tasks.named<Delete>("clean") {
            dependsOn("carthageClean")
        }

        val coroutinesVersion: String by project

        dependencies {
            "commonMainImplementation"(kotlin("stdlib-common"))
            "commonMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            "jsMainImplementation"(kotlin("stdlib-js"))
            "androidMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$coroutinesVersion")
            "commonTestImplementation"(kotlin("test-common"))
            "commonTestImplementation"(kotlin("test-annotations-common"))
            "commonTestImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            "jsTestImplementation"(kotlin("test-js"))
            "androidAndroidTestImplementation"(kotlin("test-junit"))
            "androidAndroidTestImplementation"("junit:junit:4.13.1")
            "androidAndroidTestImplementation"("androidx.test:core:1.4.0")
            "androidAndroidTestImplementation"("androidx.test.ext:junit:1.1.3")
            "androidAndroidTestImplementation"("androidx.test:runner:1.4.0")
        }
    }

    apply(plugin="maven-publish")
    apply(plugin="signing")


    configure<PublishingExtension> {

        repositories {
            maven {
                url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
                credentials {
                    username = project.findProperty("sonatypeUsername") as String? ?: System.getenv("sonatypeUsername")
                    password = project.findProperty("sonatypePassword") as String? ?: System.getenv("sonatypePassword")
                }
            }
        }

        publications.all {
            this as MavenPublication

            pom {
                name.set("firebase-kotlin-sdk")
                description.set("The Firebase Kotlin SDK is a Kotlin-first SDK for Firebase. It's API is similar to the Firebase Android SDK Kotlin Extensions but also supports multiplatform projects, enabling you to use Firebase directly from your common source targeting iOS, Android or JS.")
                url.set("https://github.com/GitLiveApp/firebase-kotlin-sdk")
                inceptionYear.set("2019")

                scm {
                    url.set("https://github.com/GitLiveApp/firebase-kotlin-sdk")
                    connection.set("scm:git:https://github.com/GitLiveApp/firebase-kotlin-sdk.git")
                    developerConnection.set("scm:git:https://github.com/GitLiveApp/firebase-kotlin-sdk.git")
                    tag.set("HEAD")
                }

                issueManagement {
                    system.set("GitHub Issues")
                    url.set("https://github.com/GitLiveApp/firebase-kotlin-sdk/issues")
                }

                developers {
                    developer {
                        name.set("Nicholas Bransby-Williams")
                        email.set("nbransby@gmail.com")
                    }
                }

                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                        comments.set("A business-friendly OSS license")
                    }
                }
            }
        }
    }
}
