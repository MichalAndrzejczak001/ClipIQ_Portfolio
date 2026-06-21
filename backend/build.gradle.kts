plugins {
    java
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.clipiq"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

val agent by configurations.creating

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    exclusiveContent {
        forRepositories(maven {
            name = "MavenCentralPomOnly"
            url = uri("https://repo.maven.apache.org/maven2")
            metadataSources {
                mavenPom()
            }
        })
        filter {
            includeModule("org.yaml", "snakeyaml")
        }
    }
    mavenCentral()
}

configurations.all {
    exclude(group = "org.yaml", module = "snakeyaml-android")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("com.squareup.okhttp3:mockwebserver")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:mongodb")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("io.cucumber:cucumber-java:7.14.0")
    testImplementation("io.cucumber:cucumber-spring:7.14.0")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:7.14.0")
    testImplementation("org.junit.platform:junit-platform-suite")
    testImplementation("io.qameta.allure:allure-junit5:2.23.0")
    testImplementation("io.qameta.allure:allure-testng:2.23.0")
    testImplementation("io.qameta.allure:allure-cucumber7-jvm:2.23.0")
    testImplementation("com.github.javafaker:javafaker:1.0.2") {
        exclude(group = "org.yaml", module = "snakeyaml")
    }
    testImplementation("io.rest-assured:rest-assured:5.3.2")
    testImplementation("io.rest-assured:spring-mock-mvc:5.3.2")
    testImplementation("io.rest-assured:json-schema-validator:5.3.2")
    testImplementation("org.testng:testng:7.8.0")
    testImplementation("org.awaitility:awaitility:4.2.0")
    "agent"("org.aspectj:aspectjweaver:1.9.20.1")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-javaagent:${agent.singleFile.absolutePath}")
    systemProperty(
        "allure.results.directory",
        layout.buildDirectory.dir("allure-results").get().asFile.absolutePath
    )
}

tasks.register<Test>("testNG") {
    description = "Runs TestNG tests via suite XML"
    group = "verification"
    useTestNG {
        suites("src/test/resources/suite/api-suite.xml")
    }
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    jvmArgs("-javaagent:${agent.singleFile.absolutePath}")
    systemProperty(
        "allure.results.directory",
        layout.buildDirectory.dir("allure-results").get().asFile.absolutePath
    )
    shouldRunAfter(tasks.test)
}
