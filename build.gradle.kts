plugins {
    id("com.android.application") version BuildPluginsVersion.AGP apply false
    id("com.android.library") version BuildPluginsVersion.AGP apply false
    kotlin("android") version BuildPluginsVersion.KOTLIN apply false
    id("org.jmailen.kotlinter") version BuildPluginsVersion.KOTLINTER
    id("com.github.ben-manes.versions") version BuildPluginsVersion.VERSIONS_PLUGIN
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        google()
        maven { setUrl("https://www.jitpack.io") }
        maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots/") }
        maven { setUrl("https://dl.bintray.com/ibm-cloud-sdks/ibm-cloud-sdk-repo") }
    }
}

subprojects {
    apply(plugin = "org.jmailen.kotlinter")

    kotlinter {
        experimentalRules = true
    }
}

buildscript {
    dependencies {
        classpath("com.github.zellius:android-shortcut-gradle-plugin:0.1.2")
        classpath("com.google.gms:google-services:4.3.4")
        classpath("com.mikepenz.aboutlibraries.plugin:aboutlibraries-plugin:" + BuildPluginsVersion.ABOUTLIB_PLUGIN)
        classpath(kotlin("serialization", version = BuildPluginsVersion.KOTLIN))
        // Realm (EH)
        classpath("io.realm:realm-gradle-plugin:7.0.1")

        // Firebase Crashlytics
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.4.1")
    }
    repositories {
        google()
        jcenter()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
