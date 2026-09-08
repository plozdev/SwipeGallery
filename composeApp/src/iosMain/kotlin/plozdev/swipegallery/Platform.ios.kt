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