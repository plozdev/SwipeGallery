package plozdev.swipegallery.data.media

import plozdev.swipegallery.domain.models.Album
import plozdev.swipegallery.domain.models.PhotoItem

interface MediaFetcherI {

    suspend fun getAlbums(): List<Album>

    suspend fun getPhotos(albumId: String? = null): List<PhotoItem>

    suspend fun deletePhotos(photoIds: List<String>): Boolean
}
