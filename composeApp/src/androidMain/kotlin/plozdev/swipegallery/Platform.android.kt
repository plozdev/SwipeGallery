package plozdev.swipegallery

import android.os.Build

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun formatEpochSeconds(seconds: Long): String {
    val date = Date(seconds * 1000)
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(date)
}

actual fun getCurrentEpochSeconds(): Long {
    return System.currentTimeMillis() / 1000
}

actual fun getEpochDay(): Long {
    val millis = System.currentTimeMillis()
    val tz = java.util.TimeZone.getDefault()
    val localMillis = millis + tz.getOffset(millis)
    return localMillis / (24L * 60L * 60L * 1000L)
}

@androidx.compose.runtime.Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(enabled = enabled, onBack = onBack)
}

object AndroidHaptics {
    @Volatile
    private var vibrator: android.os.Vibrator? = null

    fun init(context: android.content.Context) {
        if (vibrator != null) return
        try {
            val appCtx = context.applicationContext ?: context
            vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vm = appCtx.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                appCtx.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
            }
        } catch (_: Exception) {}
    }

    fun vibrateClick() {
        val v = vibrator ?: return
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                v.vibrate(android.os.VibrationEffect.createPredefined(android.os.VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(28L)
            }
        } catch (_: Exception) {
            try {
                @Suppress("DEPRECATION")
                v.vibrate(28L)
            } catch (_: Exception) {}
        }
    }

    fun vibrateThreshold() {
        val v = vibrator ?: return
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                v.vibrate(android.os.VibrationEffect.createPredefined(android.os.VibrationEffect.EFFECT_HEAVY_CLICK))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(45L)
            }
        } catch (_: Exception) {
            try {
                @Suppress("DEPRECATION")
                v.vibrate(45L)
            } catch (_: Exception) {}
        }
    }
}

actual fun triggerHapticFeedback(isThreshold: Boolean) {
    if (isThreshold) {
        AndroidHaptics.vibrateThreshold()
    } else {
        AndroidHaptics.vibrateClick()
    }
}