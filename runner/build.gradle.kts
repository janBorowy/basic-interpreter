plugins {
    id("application")
}

group = "org.example"
version = "1.0-SNAPSHOT"

application {
    mainClass = "org.example.InterpretCommand"
}


repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":interpreter"))
    implementation("info.picocli:picocli:4.7.6")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register("prepareKotlinBuildScriptModel") {}
