plugins {
    id("java")
    id("com.github.johnrengelman.shadow").version("8.1.0")
}



repositories {
    mavenCentral()
    maven("https://repo.starfarm.fun/releases") {
        content {
            excludeGroup("org.bukkit")
            excludeGroup("org.spigotmc")
        }

    }
    maven("https://repo.starfarm.fun/private") {
        credentials {
            username = System.getenv("SF_REPO_USER")
            password = System.getenv("SF_REPO_PASSWORD")
        }
    }
    maven("https://maven.elmakers.com/repository/")
    mavenLocal()
    flatDir {
        dirs("libs")
    }
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")

    compileOnly("org.spigotmc:spigot:1.12.2-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
    implementation(files("libs/core-1.5.7.jar"))

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.18.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.18.1")


    testImplementation(platform("org.junit:junit-bom:5.11.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.google.code.gson:gson:2.10.1")
    testImplementation("com.google.guava:guava:33.3.1-jre")
    testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.18.1")
    testImplementation("org.projectlombok:lombok:1.18.24")

    testImplementation("org.slf4j:slf4j-api:2.0.9")
    testImplementation("org.slf4j:slf4j-simple:2.0.9")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    compileJava {
        options.encoding = "UTF-8"
    }

    jar {
        manifest {
            attributes.put("Main-Class", "ru.starfarm.mylittlefarm.MyLittleFarm")
        }
        archiveFileName.set("starfarm-mylittlefarm.jar")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    configurations.all {
        resolutionStrategy.cacheChangingModulesFor(0, "seconds") // Для changing-модулей
        resolutionStrategy.cacheDynamicVersionsFor(0, "seconds") // Для динамических версий
    }

}


