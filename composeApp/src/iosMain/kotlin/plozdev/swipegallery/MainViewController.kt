package plozdev.swipegallery

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import plozdev.swipegallery.data.IosMediaPermissionManager
import plozdev.swipegallery.data.IosSwipePreferences
import plozdev.swipegallery.data.IosMediaFetcher
import plozdev.swipegallery.data.repository.PhotoRepoImpl

fun MainViewController() = ComposeUIViewController {
    val permissionManager = remember { IosMediaPermissionManager() }
    val prefs = remember { IosSwipePreferences() }
    val fetcher = remember { IosMediaFetcher() }
    val repo = remember { PhotoRepoImpl(fetcher, prefs) }

    App(
        photoRepo = repo,
        permissionManager = permissionManager
    )
}