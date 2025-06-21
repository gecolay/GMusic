plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version "9.0.0-beta16"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17" apply false
}

allprojects {
    apply(plugin = "java-library")

    repositories {
        mavenLocal()
        mavenCentral()

        maven("https://repo.papermc.io/repository/maven-public/")
    }

    tasks.compileJava {
        options.encoding = "UTF-8"
    }

    tasks.javadoc {
        options.encoding = "UTF-8"
    }
}

dependencies {
    api(project(":core"))
    api(project(":v1_18", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_18_2", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_19", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_19_1", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_19_3", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_19_4", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_20", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_20_2", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_20_3", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_20_5", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_21", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_21_2", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_21_4", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_21_5", io.papermc.paperweight.util.constants.REOBF_CONFIG))
    api(project(":v1_21_6", "default"))
}

tasks {
    shadowJar {
        archiveClassifier = ""
        minimize()
        manifest {
            attributes["paperweight-mappings-namespace"] = io.papermc.paperweight.util.constants.SPIGOT_NAMESPACE
        }
    }

    build {
        dependsOn(shadowJar)
    }

    compileJava {
        options.release = 17
    }

    processResources {
        from("resources") {
            exclude("resource_pack", "plugin.yml")
        }
        from("resources") {
            include("plugin.yml")
            expand(
                "name" to project.name,
                "version" to project.version,
                "description" to project.description,
                "main" to "${project.group}.${project.name}Main"
            )
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(project.components["java"])
        }
    }
}