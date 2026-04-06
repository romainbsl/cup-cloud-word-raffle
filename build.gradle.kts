plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
    alias(libs.plugins.cup)
}

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/kodeinkoders/kodein-themes")
        credentials {
            username = providers.gradleProperty("gpr.user").orNull
            password = providers.gradleProperty("gpr.key").orNull
        }
    }
}

cup {
    targetDesktop()
}

kotlin {
    sourceSets.commonMain {
        dependencies {
            implementation(cup.plugin.speakerWindow)
            implementation(cup.plugin.laser)
            implementation(libs.compose.material)
            implementation(cup.widgets.material)

            implementation(libs.kodein.themes.cup)
        }
    }
}
