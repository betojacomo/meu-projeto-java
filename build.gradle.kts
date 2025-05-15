plugins {
    java
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.xerial:sqlite-jdbc:3.42.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")  // JUnit 5
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("com.example.Main")
    
    applicationDefaultJvmArgs = listOf(
        "--enable-native-access=ALL-UNNAMED",
        "--add-opens=java.base/java.lang=ALL-UNNAMED"
    )
}

// Configuração correta para input no Kotlin DSL
tasks.run<JavaExec> {
    standardInput = System.`in`
}

tasks.test {
    useJUnitPlatform()
    jvmArgs = listOf(
        "--enable-native-access=ALL-UNNAMED",
        "--add-opens=java.base/java.lang=ALL-UNNAMED"
    )
}