import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.test

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "com.ess"
version = "2.2.2"

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
        intellijIdea("2025.3.1")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)

        // Add necessary plugin dependencies for compilation here, example:
        // bundledPlugin("com.intellij.java")
    }

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testImplementation("org.mockito:mockito-core:4.4.0")
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "253"
        }

        changeNotes = """
<ul>
<li>Fixed compatibility with 2026.2</li>
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

    test {
        useJUnit()
    }
}

//tasks.withType<Test>().configureEach {
//    systemProperty("idea.force.use.core.classloader", "true")
//}
