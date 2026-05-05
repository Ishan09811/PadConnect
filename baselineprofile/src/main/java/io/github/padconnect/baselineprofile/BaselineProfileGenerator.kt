package io.github.padconnect.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This test class generates a basic startup baseline profile for the target package.
 *
 * We recommend you start with this but add important user flows to the profile to improve their performance.
 * Refer to the [baseline profile documentation](https://d.android.com/topic/performance/baselineprofiles)
 * for more information.
 *
 * You can run the generator with the "Generate Baseline Profile" run configuration in Android Studio or
 * the equivalent `generateBaselineProfile` gradle task:
 * ```
 * ./gradlew :app:generateReleaseBaselineProfile
 * ```
 * The run configuration runs the Gradle task and applies filtering to run only the generators.
 *
 * Check [documentation](https://d.android.com/topic/performance/benchmarking/macrobenchmark-instrumentation-args)
 * for more information about available instrumentation arguments.
 *
 * After you run the generator, you can verify the improvements running the [StartupBenchmarks] benchmark.
 *
 * When using this class to generate a baseline profile, only API 33+ or rooted API 28+ are supported.
 *
 * The minimum required version of androidx.benchmark to generate a baseline profile is 1.2.0.
 **/
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        // The application id for the running build variant is read from the instrumentation arguments.
        rule.collect(
            packageName = "io.github.padconnect",
            // See: https://d.android.com/topic/performance/baselineprofiles/dex-layout-optimizations
            includeInStartupProfile = true
        ) {
            // This block defines the app's critical user journey. Here we are interested in
            // optimizing for app startup. But you can also navigate and scroll through your most important UI.

            // Start default activity for your app
            pressHome()
            startActivityAndWait()

            device.waitForIdle()

            // Let Compose settle
            Thread.sleep(1500)

            // Navigate to Settings
            device.findObject(By.desc("Settings"))?.click()
            device.waitForIdle()
            Thread.sleep(800)

            // Navigate to Advanced Settings
            device.findObject(By.text("Advanced Settings"))?.click()
            device.waitForIdle()
            Thread.sleep(800)

            // Navigate to core settings
            device.findObject(By.text("Core"))?.click()
            device.waitForIdle()
            Thread.sleep(800)

            // Click a switch twice
            device.findObject(By.text("Enable Haptic Feedback"))?.click()
            device.waitForIdle()
            device.findObject(By.text("Enable Haptic Feedback"))?.click()
            device.waitForIdle()
            Thread.sleep(800)

            // click an int setting
            device.findObject(By.text("Input update rate"))?.click()
            device.waitForIdle()
            Thread.sleep(800)

            // Navigate back
            device.pressBack()
            device.pressBack()
            device.pressBack()
            device.waitForIdle()
            Thread.sleep(800)

            // Back to Home
            device.findObject(By.desc("Layouts"))?.click()
            device.waitForIdle()
            Thread.sleep(800)

            // Try opening a layout (may need refinement)
            val firstLayout = device.findObject(By.clazz("android.view.View"))
            firstLayout?.click()
            device.waitForIdle()
            Thread.sleep(1500)

            // Back navigation
            device.pressBack()
            device.waitForIdle()
            Thread.sleep(800)
        }
    }
}