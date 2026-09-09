package plozdev.swipegallery.data

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHPhotoLibrary
import plozdev.swipegallery.data.media.MediaPermissionManagerI
import kotlin.coroutines.resume

/**
 * In ios, check and request permission not tied to UI Lifecycle
 * so just use org PHPPhotoLibrary
 */
class IosMediaPermissionManager : MediaPermissionManagerI {
    override suspend fun requestPermissions(): Boolean {
        val status = PHPhotoLibrary.authorizationStatus()
        // return true if it has permission
        // if still not have permission yet, shows pop-up for permission
        return if (status == PHAuthorizationStatusAuthorized || status == PHAuthorizationStatusLimited) true
            else suspendCancellableCoroutine { cont ->
            PHPhotoLibrary.requestAuthorization { newStatus ->
                val isGranted = newStatus == PHAuthorizationStatusAuthorized || newStatus == PHAuthorizationStatusLimited
                cont.resume(isGranted)
            }
        }
    }

    override fun openAppSettings() {
        val url = platform.Foundation.NSURL.URLWithString(platform.UIKit.UIApplicationOpenSettingsURLString)
        if (url != null && platform.UIKit.UIApplication.sharedApplication.canOpenURL(url)) {
            platform.UIKit.UIApplication.sharedApplication.openURL(url)
        }
    }
}