package plozdev.swipegallery.data

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import plozdev.swipegallery.PermissionDelegate
import plozdev.swipegallery.data.media.MediaPermissionManagerI

class AndroidMediaPermissionManger(private val context : Context) : MediaPermissionManagerI {
    override suspend fun requestPermissions(): Boolean {
        val permissions = getRequiredPermissions()

        return if (hasPermissions(permissions)) true else PermissionDelegate.requestPermissions(permissions)
    }

    private fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VISUAL_USER_SELECTED)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(READ_MEDIA_IMAGES)
        } else {
            arrayOf(READ_EXTERNAL_STORAGE)
        }
    }

    private fun hasPermissions(permissions: Array<String>): Boolean {
        return permissions.any { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}