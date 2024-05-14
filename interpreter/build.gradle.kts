plugins {
    id("java")
    id("groovy")
    id("io.freefair.lombok") version "8.6"
}

group = "pl.interpreter"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.spockframework:spock-core:2.4-M4-groovy-4.0")
}

tasks.named<Test>("test"){
    useJUnitPlatform()
}

tasks.register("prepareKotlinBuildScriptModel") {}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
        vendor = JvmVendorSpec.AMAZON
    }
}
