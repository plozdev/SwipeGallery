package plozdev.swipegallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import plozdev.swipegallery.data.PermissionDelegate

class MainActivity : ComponentActivity() {

    private val mediaPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) {
        res ->
        PermissionDelegate.onPermissionResult(res)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        PermissionDelegate.registerLauncher(mediaPermissionLauncher)
        setContent {
            App()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        PermissionDelegate.unregisterLauncher()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}