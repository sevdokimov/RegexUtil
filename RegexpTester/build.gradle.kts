import org.jetbrains.intellij.platform.gradle.tasks.RunIdeTask

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij.platform") version "2.3.0"
}

group = "com.ess"
version = "2.2.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    intellijPlatform {
        create("IC", "2024.2.5")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)

        testImplementation("org.assertj:assertj-core:3.17.2")
        testImplementation("org.mockito:mockito-core:4.4.0")
        // Add necessary plugin dependencies for compilation here, example:
        // bundledPlugin("com.intellij.java")
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "242"
        }

        changeNotes = """
<ul>
<li>Fixed compatibility with 2025.3.x</li>
</ul> 
    """.trimIndent()
    }
}

tasks.named("runIde") {
    // Works if the task exposes jvmArgs (it typically does)
    (this as JavaExec).jvmArgs(
        "-Didea.ProcessCanceledException=disabled",
        "-Didea.debug=true"
    )
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "21"
    }
}

tasks.withType<Test>().configureEach {
    systemProperty("idea.force.use.core.classloader", "true")
}
