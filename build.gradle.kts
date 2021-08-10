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
    rootProject.extra["databaseName"] = env("JDBC_DATABASE", "react_spring_messenger")
    rootProject.extra["databaseUrl"] = "jdbc:mariadb://${project.extra["databaseHost"]}:${project.extra["databasePort"]}/${project.extra["databaseName"]}"
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
        classpath("gradle.plugin.org.flywaydb:gradle-plugin-publishing:7.12.1")
    }
}

plugins {
    id("org.flywaydb.flyway") version "7.12.1"
    id("nu.studer.jooq") version "6.0"
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

    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }

    configurations {
        "developmentOnly" {}
        "runtimeClasspath" { extendsFrom(configurations["developmentOnly"]) }
        "compileOnly" { extendsFrom(configurations["annotationProcessor"]) }
    }

    tasks.withType<Test> { useJUnitPlatform() }

    dependencies {
        "implementation"("org.jetbrains.kotlin:kotlin-reflect")
        "implementation"("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core")
        "testImplementation"("org.jetbrains.kotlin:kotlin-test")
        // "testImplementation"("org.jetbrains.kotlin:kotlin-test-junit")
    }
}

project(":persistence-model") {
    val jar: Jar by tasks
    jar.enabled = true

    apply { plugin("org.flywaydb.flyway") }
    flyway {
        url = rootProject.extra["databaseUrl"] as String
        user = rootProject.extra["databaseUsername"] as String
        password = rootProject.extra["databasePassword"] as String
    }

    apply { plugin("nu.studer.jooq") }
    jooq {
        configurations {
            create("main") {  // name of the jOOQ configuration
                jooqConfiguration.apply {
                    logging = org.jooq.meta.jaxb.Logging.WARN
                    jdbc.apply {
                        driver = "org.mariadb.jdbc.Driver"
                        url = rootProject.extra["databaseUrl"] as String
                        user = rootProject.extra["databaseUsername"] as String
                        password = rootProject.extra["databasePassword"] as String
                        properties.add(org.jooq.meta.jaxb.Property().withKey("ssl").withValue("true"))
                    }
                    generator.apply {
                        name = "org.jooq.codegen.DefaultGenerator"
                        database.apply {
                            name = "org.jooq.meta.mariadb.MariaDBDatabase"
                            inputSchema = rootProject.extra["databaseName"] as String
                            forcedTypes.addAll(
                                arrayOf(
                                    org.jooq.meta.jaxb.ForcedType()
                                        .withName("BOOLEAN")
                                        .withIncludeExpression(".*")
                                        .withIncludeTypes("(?i:TINYINT)\\(1\\)"),
                                    org.jooq.meta.jaxb.ForcedType()
                                        .withName("UUID")
                                        .withIncludeExpression(".*")
                                        .withIncludeTypes("(?i:BINARY)\\(16\\)"),
                                ).toList()
                            )
                        }
                        generate.apply {
                            isRelations = true
                            isDeprecated = false
                            isRecords = true
                            isPojos = true
                            isFluentSetters = true
                            isJavaTimeTypes = true
                        }
                        target.apply {
                            packageName = "org.tinywind.messenger.jooq"
                        }
                        strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                    }
                }
            }
        }
    }

    dependencies {
        "api"("org.jooq:jooq")
        "jooqGenerator"("org.mariadb.jdbc:mariadb-java-client:${rootProject.extra["mariadbJavaClientVersion"]}")
        "implementation"("org.mariadb.jdbc:mariadb-java-client:${rootProject.extra["mariadbJavaClientVersion"]}")
    }
}

project(":api-server") {
    val bootJar: Jar by tasks
    bootJar.enabled = true

    val jar: Jar by tasks
    jar.enabled = false

    dependencies {
        "implementation"(project(":persistence-model"))

        "runtimeOnly"("org.springframework.boot:spring-boot-devtools")
        "annotationProcessor"("org.springframework.boot:spring-boot-configuration-processor")
        "testImplementation"("org.springframework.boot:spring-boot-starter-test") {
            exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        }
        "implementation"("org.springframework.boot:spring-boot-starter-web:${rootProject.extra["springBootVersion"]}")
        "implementation"("org.springframework.boot:spring-boot-starter-jooq:${rootProject.extra["springBootVersion"]}")
        "implementation"("com.graphql-java-kickstart:graphql-spring-boot-starter:11.1.0")
        // "testImplementation"("com.graphql-java-kickstart:graphql-spring-boot-starter-test:11.1.0")
    }
}
