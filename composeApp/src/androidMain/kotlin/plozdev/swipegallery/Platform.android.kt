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