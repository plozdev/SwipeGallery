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