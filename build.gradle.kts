buildscript {
    fun env(name: String, defaultValue: String): String {
        return System.getProperty(name, System.getenv(name)) ?: defaultValue
    }

    rootProject.extra["kotlinVersion"] = "1.5.21"
    rootProject.extra["springBootVersion"] = "2.5.3"
    rootProject.extra["mariadbJavaClientVersion"] = "2.7.3"
    rootProject.extra["jooqVersion"] = "3.15.1"
    rootProject.extra["databaseHost"] = env("JDBC_HOST", "localhost")
    rootProject.extra["databasePort"] = env("JDBC_POST", "3306")
    rootProject.extra["databaseUsername"] = env("JDBC_USERNAME", "root")
    rootProject.extra["databasePassword"] = env("JDBC_PASSWORD", "1234")
    rootProject.extra["databaseName"] = env("JDBC_DATABASE", "react-spring-messenger")
    rootProject.extra["databaseUrl"] = "jdbc:postgresql://${project.extra["databaseHost"]}:${project.extra["databasePort"]}/${project.extra["databaseName"]}"
    rootProject.extra["profile"] = if (project.hasProperty("profile"))
        project.property("profile") else "local"

    repositories {
        mavenCentral()
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${rootProject.extra["kotlinVersion"]}")
        classpath("org.jetbrains.kotlin:kotlin-allopen:${rootProject.extra["kotlinVersion"]}") // kotlin-spring 사용을 위해 필요하다.
        classpath("org.springframework.boot:spring-boot-gradle-plugin:${rootProject.extra["springBootVersion"]}")
        classpath("io.spring.gradle:dependency-management-plugin:1.0.11.RELEASE")
        classpath("nu.studer:gradle-jooq-plugin:6.0")
        classpath("gradle.plugin.org.flywaydb:gradle-plugin-publishing:7.11.4")
    }
}

allprojects {
    group = "org.tinywind.messenger"
    version = "1.0-SNAPSHOT"
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "idea")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

//    sourceCompatibility = JavaVersion.VERSION_1_8
//    targetCompatibility = JavaVersion.VERSION_1_8
//    compileJava.options.encoding = "UTF-8"
//    compileTestJava.options.encoding = "UTF-8"

    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }

    configurations {
        "developmentOnly" {}
        "runtimeClasspath" { extendsFrom(configurations["developmentOnly"]) }
        "compileOnly" { extendsFrom(configurations["annotationProcessor"]) }
    }

//    sourceSets {
//        main {
//            resources {
//                srcDirs(listOf("src/main/resources", "src/main/resources-${profile}"))
//            }
//        }
//    }

//    test {
//        useJUnitPlatform()
//    }

    dependencies {
//        "runtimeOnly"("org.springframework.boot:spring-boot-devtools")
//        developmentOnly 'org.springframework.boot:spring-boot-devtools'
//        annotationProcessor 'org.springframework.boot:spring-boot-configuration-processor'
        "testImplementation"("org.springframework.boot:spring-boot-starter-test") {
            exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        }
        "implementation"("org.jetbrains.kotlin:kotlin-reflect")
        "implementation"("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core")
        "testImplementation"("org.jetbrains.kotlin:kotlin-test")
        "testImplementation"("org.jetbrains.kotlin:kotlin-test-junit")
    }
}

project(":api-server") {
//    tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootWar>("bootJar") {
//        enabled = true
//    }
//    jar.enabled = false

    dependencies {
        "implementation"("org.springframework.boot:spring-boot-starter-web:${rootProject.extra["springBootVersion"]}")
    }
}

