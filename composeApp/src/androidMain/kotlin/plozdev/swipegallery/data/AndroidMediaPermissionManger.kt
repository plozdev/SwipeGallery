package plozdev.swipegallery.data

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import plozdev.swipegallery.data.PermissionDelegate
import plozdev.swipegallery.data.media.MediaPermissionManagerI

class AndroidMediaPermissionManger(private val context : Context) : MediaPermissionManagerI {
    override suspend fun requestPermissions(): Boolean {
        val permissions = getRequiredPermissions()

        return if (hasPermissions(permissions)) true else PermissionDelegate.requestPermissions(permissions)
    }

    override fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
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