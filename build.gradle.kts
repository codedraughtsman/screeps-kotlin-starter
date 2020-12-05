import org._10ne.gradle.rest.RestTask
import org.jetbrains.kotlin.gradle.dsl.KotlinJsDce
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import java.util.*

plugins {
    id("kotlin2js") version "1.3.31"
    id("kotlin-dce-js") version "1.3.31"
    id("org.tenne.rest") version "0.4.2"
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation("ch.delconte.screeps-kotlin:screeps-kotlin-types:1.3.0")
    implementation(kotlin("stdlib-js"))
    testImplementation(kotlin("test-js"))
    implementation( "com.beust:klaxon:5.2")

//    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
//    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.4.2")
//    testImplementation(platform("org.junit:junit-bom:5.7.0"))
//    testImplementation("org.junit.jupiter:junit-jupiter")
//    testImplementation( "org.junit.jupiter:junit-jupiter-api:5.1.1")
//    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.1.1")
}


val screepsUser: String? by project
val screepsPassword: String? by project
val screepsToken: String? by project
val screepsHost: String? by project
val screepsBranch: String? by project
val branch = screepsBranch ?: "kotlin-start"
val branchStable = "kotlinStable"
val host = screepsHost ?: "https://screeps.com"

fun String.encodeBase64() = Base64.getEncoder().encodeToString(this.toByteArray())

tasks {
    test {
        useJUnitPlatform()
//        testLogging {
////            events = setOf(
////                    TestLogEvent.STARTED,
////                    TestLogEvent.PASSED,
////                    TestLogEvent.FAILED
////            )
//            // show standard out and standard error of the test
//            // JVM(s) on the console
//            showStandardStreams = true
//        }
    }
    "compileKotlin2Js"(Kotlin2JsCompile::class) {
        kotlinOptions {
            moduleKind = "commonjs"
            outputFile = "${buildDir}/screeps/main.js"
            sourceMap = true
            metaInfo = true
        }
    }

    "runDceKotlinJs"(KotlinJsDce::class) {
        keep("main.loop")
        dceOptions.devMode = false
    }

    register<RestTask>("deploy") {
        group = "screeps"
        dependsOn("build")
        val modules = mutableMapOf<String, String>()
        val minifiedCodeLocation = File("$buildDir/kotlin-js-min/main")

        httpMethod = "post"
        uri = "$host/api/user/code"
        requestHeaders = if (screepsToken != null)
            mapOf("X-Token" to screepsToken)
        else
            mapOf("Authorization" to "Basic " + "$screepsUser:$screepsPassword".encodeBase64())
        contentType = groovyx.net.http.ContentType.JSON
        requestBody = mapOf("branch" to branch, "modules" to modules)

        doFirst {
            if (screepsUser == null && screepsPassword == null && screepsToken == null) {
                throw InvalidUserDataException("you need to supply either screepsUser and screepsPassword or screepsToken before you can upload code")
            }
            if (!minifiedCodeLocation.isDirectory) {
                throw InvalidUserDataException("found no code to upload at ${minifiedCodeLocation.path}")
            }

            val jsFiles = minifiedCodeLocation.listFiles { _, name -> name.endsWith(".js") }
            modules.putAll(jsFiles.associate { it.nameWithoutExtension to it.readText() })

            println("uploading ${jsFiles.count()} files to branch $branch on server $host")
        }

    }
    register<RestTask>("deployStable") {
        group = "screeps"
        dependsOn("build")
        val modules = mutableMapOf<String, String>()
        val minifiedCodeLocation = File("$buildDir/kotlin-js-min/main")

        httpMethod = "post"
        uri = "$host/api/user/code"
        requestHeaders = if (screepsToken != null)
            mapOf("X-Token" to screepsToken)
        else
            mapOf("Authorization" to "Basic " + "$screepsUser:$screepsPassword".encodeBase64())
        contentType = groovyx.net.http.ContentType.JSON
        requestBody = mapOf("branch" to branchStable, "modules" to modules)

        doFirst {
            if (screepsUser == null && screepsPassword == null && screepsToken == null) {
                throw InvalidUserDataException("you need to supply either screepsUser and screepsPassword or screepsToken before you can upload code")
            }
            if (!minifiedCodeLocation.isDirectory) {
                throw InvalidUserDataException("found no code to upload at ${minifiedCodeLocation.path}")
            }

            val jsFiles = minifiedCodeLocation.listFiles { _, name -> name.endsWith(".js") }
            modules.putAll(jsFiles.associate { it.nameWithoutExtension to it.readText() })

            println("uploading ${jsFiles.count()} files to branch $branchStable on server $host")
        }

    }
}

