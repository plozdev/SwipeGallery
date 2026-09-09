package plozdev.swipegallery

import platform.UIKit.UIDevice
import platform.Foundation.*

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun formatEpochSeconds(seconds: Long): String {
    val date = NSDate.dateWithTimeIntervalSince1970(seconds.toDouble())
    val formatter = NSDateFormatter().apply {
        dateFormat = "dd MMM yyyy, HH:mm"
    }
    return formatter.stringFromDate(date)
}

actual fun getCurrentEpochSeconds(): Long {
    return NSDate().timeIntervalSince1970.toLong()
}

actual fun getEpochDay(): Long {
    val now = NSDate()
    val seconds = now.timeIntervalSince1970
    val tz = NSTimeZone.localTimeZone
    val offset = tz.secondsFromGMTForDate(now)
    val localSeconds = seconds + offset
    return (localSeconds / 86400.0).toLong()
}

@androidx.compose.runtime.Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op on iOS
}

actual fun triggerHapticFeedback(isThreshold: Boolean) {
    val style = if (isThreshold) platform.UIKit.UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy else platform.UIKit.UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium
    val generator = platform.UIKit.UIImpactFeedbackGenerator(style)
    generator.prepare()
    generator.impactOccurred()
}

actual fun shareText(text: String, title: String) {
    val window = platform.UIKit.UIApplication.sharedApplication.keyWindow
    val rootVc = window?.rootViewController ?: return
    val activityVc = platform.UIKit.UIActivityViewController(
        activityItems = listOf(text),
        applicationActivities = null
    )
    rootVc.presentViewController(activityVc, animated = true, completion = null)
}