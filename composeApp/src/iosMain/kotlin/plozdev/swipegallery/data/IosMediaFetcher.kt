package plozdev.swipegallery.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.Foundation.*
import platform.Photos.*
import plozdev.swipegallery.data.media.MediaFetcherI
import plozdev.swipegallery.domain.models.Album
import plozdev.swipegallery.domain.models.PhotoItem
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
class IosMediaFetcher : MediaFetcherI {

    override suspend fun getAlbums(): List<Album> = withContext(Dispatchers.Default) {
        val albumsList = mutableListOf<Album>()

        // 1. Fetch smart album "Recents"
        val smartAlbums = PHAssetCollection.fetchAssetCollectionsWithType(
            type = PHAssetCollectionTypeSmartAlbum,
            subtype = PHAssetCollectionSubtypeSmartAlbumUserLibrary,
            options = null
        )
        for (i in 0 until smartAlbums.count.toInt()) {
            val collection = smartAlbums.objectAtIndex(i.toULong()) as? PHAssetCollection ?: continue
            val album = createAlbumFromCollection(collection)
            if (album != null) {
                albumsList.add(album)
            }
        }

        // 2. Fetch user-created albums
        val userAlbums = PHAssetCollection.fetchAssetCollectionsWithType(
            type = PHAssetCollectionTypeAlbum,
            subtype = PHAssetCollectionSubtypeAny,
            options = null
        )
        for (i in 0 until userAlbums.count.toInt()) {
            val collection = userAlbums.objectAtIndex(i.toULong()) as? PHAssetCollection ?: continue
            val album = createAlbumFromCollection(collection)
            if (album != null) {
                albumsList.add(album)
            }
        }

        albumsList
    }

    private fun createAlbumFromCollection(collection: PHAssetCollection): Album? {
        val options = PHFetchOptions().apply {
            predicate = platform.Foundation.NSPredicate.predicateWithFormat("mediaType == %d || mediaType == %d", PHAssetMediaTypeImage, PHAssetMediaTypeVideo)
        }
        val fetchResult = PHAsset.fetchAssetsInAssetCollection(collection, options)
        val count = fetchResult.count.toInt()
        if (count <= 0) return null

        val lastAsset = fetchResult.lastObject as? PHAsset
        val coverUri = lastAsset?.localIdentifier?.let { "phasset://$it" }

        return Album(
            id = collection.localIdentifier,
            name = collection.localizedTitle ?: "Chưa đặt tên",
            coverPhotoUri = coverUri,
            photoCount = count
        )
    }

    override suspend fun getPhotos(albumId: String?): List<PhotoItem> = withContext(Dispatchers.Default) {
        val photosList = mutableListOf<PhotoItem>()
        val options = PHFetchOptions().apply {
            predicate = platform.Foundation.NSPredicate.predicateWithFormat("mediaType == %d || mediaType == %d", PHAssetMediaTypeImage, PHAssetMediaTypeVideo)
            sortDescriptors = listOf(platform.Foundation.NSSortDescriptor.sortDescriptorWithKey("creationDate", ascending = false))
        }

        val fetchResult: PHFetchResult = if (albumId != null) {
            val collectionResult = PHAssetCollection.fetchAssetCollectionsWithLocalIdentifiers(listOf(albumId), options = null)
            val collection = collectionResult.firstObject as? PHAssetCollection
            if (collection != null) {
                PHAsset.fetchAssetsInAssetCollection(collection, options)
            } else {
                PHAsset.fetchAssetsWithOptions(options)
            }
        } else {
            PHAsset.fetchAssetsWithOptions(options)
        }

        for (i in 0 until fetchResult.count.toInt()) {
            val asset = fetchResult.objectAtIndex(i.toULong()) as? PHAsset ?: continue
            val id = asset.localIdentifier
            val creationDate = asset.creationDate?.timeIntervalSince1970?.toLong() ?: 0L
            val isVideo = asset.mediaType == PHAssetMediaTypeVideo
            val duration = if (isVideo) (asset.duration * 1000).toLong() else null
            val width = asset.pixelWidth.toInt()
            val height = asset.pixelHeight.toInt()

            var fileSize = 0L
            try {
                val resources = PHAssetResource.assetResourcesForAsset(asset)
                val resource = resources.firstOrNull() as? PHAssetResource
                if (resource != null) {
                    val sizeVal = resource.valueForKey("fileSize")
                    if (sizeVal != null) {
                        fileSize = (sizeVal as? platform.Foundation.NSNumber)?.longLongValue ?: 0L
                    }
                }
            } catch (e: Exception) {
                fileSize = if (isVideo) 15_000_000L else 3_000_000L
            }

            photosList.add(
                PhotoItem(
                    id = id,
                    uri = "phasset://$id",
                    dateAdded = creationDate,
                    albumId = albumId,
                    fileSize = fileSize,
                    width = width,
                    height = height,
                    isVideo = isVideo,
                    duration = duration,
                    mimeType = null
                )
            )
        }

        photosList
    }

    override suspend fun deletePhotos(photoIds: List<String>): Boolean = suspendCancellableCoroutine { cont ->
        val assetsResult = PHAsset.fetchAssetsWithLocalIdentifiers(photoIds, options = null)
        val assetsToDelete = mutableListOf<PHAsset>()
        for (i in 0 until assetsResult.count.toInt()) {
            val asset = assetsResult.objectAtIndex(i.toULong()) as? PHAsset
            if (asset != null) {
                assetsToDelete.add(asset)
            }
        }

        if (assetsToDelete.isEmpty()) {
            cont.resume(true)
            return@suspendCancellableCoroutine
        }

        val nsArray = NSMutableArray()
        assetsToDelete.forEach { nsArray.addObject(it) }

        PHPhotoLibrary.sharedPhotoLibrary().performChanges(
            changeBlock = {
                PHAssetChangeRequest.deleteAssets(nsArray)
            },
            completionHandler = { success, error ->
                cont.resume(success && error == null)
            }
        )
    }
}