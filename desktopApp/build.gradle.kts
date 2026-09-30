import dev.nucleusframework.desktop.application.dsl.GraalvmDistribution
import dev.nucleusframework.desktop.application.dsl.NativeImageMarch
import dev.nucleusframework.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.nucleus)
}

kotlin {
    jvmToolchain(21)

    dependencies {
        implementation(projects.shared)

        implementation(compose.desktop.currentOs)
        implementation(libs.compose.material3)
        implementation(libs.compose.runtime)
        implementation(libs.compose.ui)
        implementation(libs.androidx.lifecycle.viewmodelCompose)
        implementation(project.dependencies.platform(libs.koin.bom))
        implementation(libs.koin.core)
        implementation(libs.nucleus.application)
        implementation(libs.nucleus.decorated.window.tao)
        implementation(libs.nucleus.core.runtime)
        implementation(libs.nucleus.darkmode)
    }
}

// Nucleus application plugin: JVM run, packaging, GraalVM native-image.
nucleus.application {
    mainClass = "org.lazy.wanandroid.MainKt"

    graalvm {
        isEnabled = true
        imageName = "WanAndroid"
        // Nucleus provisions this only for native-image tasks; regular JVM builds use JDK 21.
        // javaLanguageVersion/jvmVendor do not select the auto-downloaded toolchain.
        toolchain {
            distribution = GraalvmDistribution.COMMUNITY
            version = "25"
        }
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
        description = "WanAndroid articles, community and open-source projects"
        vendor = "LazyIonEs"
        homepage = "https://github.com/LazyIonEs/WanAndroid-ComposeMultiplatform"

        cleanupNativeLibs = true
        // PreferencesSettings persists the shared theme settings through java.util.prefs.
        modules("java.prefs")

        linux {
            debMaintainer = "LazyIonEs <lazyiones@gmail.com>"
            appCategory = "Development"
            shortcut = true
        }

        macOS {
            bundleID = "org.lazy.wanandroid"
        }

        windows {
            menuGroup = "WanAndroid"
            nsis {
                oneClick = false
                perMachine = false
                allowToChangeInstallationDirectory = true
                createDesktopShortcut = true
                createStartMenuShortcut = true
            }
        }
    }
}
