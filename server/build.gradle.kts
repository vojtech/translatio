plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ktor)
    application
}

group = "com.fediim.translatio"
version = "1.0.0"

application {
    mainClass.set("com.fediim.translatio.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

ktor {
    fatJar {
        archiveFileName.set("translatio-server-all.jar")
    }
}

dependencies {
    implementation(platform(libs.ktor.bom))
    implementation(platform(libs.koin.bom))
    implementation(projects.shared)
    implementation(projects.sharedModel)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.sessions)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.server.htmlBuilder)
    implementation(libs.ktor.server.callLogging)
    implementation(libs.ktor.server.statusPages)
    implementation(libs.ktor.server.autoHeadResponse)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.serialization.kotlinxJson)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.hikaricp)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.postgresql)
    implementation(libs.bcrypt)
    implementation(libs.kotlinx.datetime)

    // DI
    implementation(libs.koin.core)
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger)

    // OAuth userinfo HTTP client
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.contentNegotiation)
    implementation(libs.ktor.serialization.kotlinxJson)

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}

tasks.register<JavaExec>("exportAvailableLocale") {
    group = "generation"
    description = "Generates locales.json"
    mainClass.set("com.fediim.translatio.tools.GenerateIanaKt")
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = rootDir
}