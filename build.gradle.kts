// AGP 9 intègre Kotlin : le plugin org.jetbrains.kotlin.android n'est plus
// appliqué. Le classpath ci-dessous fixe la version de Kotlin utilisée par AGP ;
// elle doit rester égale à `kotlin` dans gradle/libs.versions.toml, qui règle
// aussi le plugin Compose.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.20")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.services) apply false
}
