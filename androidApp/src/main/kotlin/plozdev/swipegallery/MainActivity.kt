package plozdev.swipegallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.activity.result.IntentSenderRequest
import plozdev.swipegallery.data.DeleteDelegate
import plozdev.swipegallery.data.PermissionDelegate

class MainActivity : ComponentActivity() {

    private val mediaPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) {
        res ->
        PermissionDelegate.onPermissionResult(res)
    }

    private val mediaDeleteLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()) {
        res ->
        DeleteDelegate.onDeleteResult(res.resultCode == RESULT_OK)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        PermissionDelegate.registerLauncher(mediaPermissionLauncher)
        DeleteDelegate.registerLauncher(mediaDeleteLauncher)
        setContent {
            val permissionManager = androidx.compose.runtime.remember { 
                plozdev.swipegallery.data.AndroidMediaPermissionManger(this) 
            }
            val prefs = androidx.compose.runtime.remember { 
                plozdev.swipegallery.data.AndroidSwipePreferences(this) 
            }
            val fetcher = androidx.compose.runtime.remember { 
                plozdev.swipegallery.data.AndroidMediaFetcher(this) 
            }
            val repo = androidx.compose.runtime.remember { 
                plozdev.swipegallery.data.repository.PhotoRepoImpl(fetcher, prefs) 
            }

            App(
                photoRepo = repo,
                permissionManager = permissionManager
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        PermissionDelegate.unregisterLauncher()
        DeleteDelegate.unregisterLauncher()
    }
}
