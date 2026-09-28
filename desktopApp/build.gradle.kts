import dev.nucleusframework.desktop.application.dsl.NativeImageMarch
import dev.nucleusframework.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.nucleus)
}

kotlin {
    dependencies {
        implementation(projects.shared)

        implementation(compose.desktop.currentOs)
        implementation(libs.kotlinx.coroutines.swing)
        implementation(libs.nucleus.application)
        implementation(libs.nucleus.decorated.window.tao)
        implementation(libs.nucleus.core.runtime)
        implementation("dev.nucleusframework:compose-macos-ui:1.1.0")
    }
}

// Nucleus application plugin: JVM run, packaging, GraalVM native-image.
nucleus.application {
    mainClass = "org.lazy.wanandroid.MainKt"

    graalvm {
        isEnabled = true
        javaLanguageVersion = 21
        jvmVendor = JvmVendorSpec.BELLSOFT
        imageName = "WanAndroid"
        // Leave unset for the per-platform default; -PnativeMarch=native overrides it locally.
        providers.gradleProperty("nativeMarch").orNull?.let {
            march = NativeImageMarch.valueOf(it.uppercase())
        }
    }

    nativeDistributions {
        targetFormats(TargetFormat.Dmg, TargetFormat.Nsis, TargetFormat.Deb)
        appName = "WanAndroid"
        packageName = "org.lazy.wanandroid"
        packageVersion = "1.0.0"

        cleanupNativeLibs = true

        linux {
            // WebKit2GTK is a system dependency of the embedded Linux backend.
            debMaintainer = "LazyIonEs <lazyiones@gmail.com>"
        }

        macOS {
            bundleID = "org.lazy.wanandroid"
        }
    }
}
