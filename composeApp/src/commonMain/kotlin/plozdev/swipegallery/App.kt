package plozdev.swipegallery

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import plozdev.swipegallery.data.media.MediaPermissionManagerI
import plozdev.swipegallery.data.repository.PhotoRepo
import plozdev.swipegallery.screens.DiscoverScreen
import plozdev.swipegallery.screens.viewModels.DiscoverViewModel
import plozdev.swipegallery.theme.SwipeGalleryTheme

@Composable
fun App(
    photoRepo: PhotoRepo,
    permissionManager: MediaPermissionManagerI
) {
    SwipeGalleryTheme {
        val viewModel: DiscoverViewModel = viewModel {
            DiscoverViewModel(photoRepo, permissionManager)
        }
        plozdev.swipegallery.screens.MainScreen(viewModel = viewModel)
    }
}
