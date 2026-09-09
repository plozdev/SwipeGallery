package plozdev.swipegallery.data

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.IntentSenderRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import plozdev.swipegallery.data.media.MediaFetcherI
import plozdev.swipegallery.domain.models.Album
import plozdev.swipegallery.domain.models.PhotoItem

class AndroidMediaFetcher(private val context: Context) : MediaFetcherI {

    override suspend fun getAlbums(): List<Album> = withContext(Dispatchers.IO) {
        val albumMap = mutableMapOf<String, AlbumData>()
        
        // 1. Query Images
        val imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val imgProjection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED
        )
        context.contentResolver.query(imageUri, imgProjection, null, null, "${MediaStore.Images.Media.DATE_ADDED} DESC")?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val bucketIdCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
            val bucketNameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val bucketId = cursor.getString(bucketIdCol) ?: "default"
                val bucketName = cursor.getString(bucketNameCol) ?: "Chưa phân loại"
                val coverUri = ContentUris.withAppendedId(imageUri, id).toString()
                
                val existing = albumMap[bucketId]
                if (existing == null) {
                    albumMap[bucketId] = AlbumData(bucketId, bucketName, coverUri, 1)
                } else {
                    existing.count++
                }
            }
        }
        
        // 2. Query Videos
        val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val vidProjection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DATE_ADDED
        )
        context.contentResolver.query(videoUri, vidProjection, null, null, "${MediaStore.Video.Media.DATE_ADDED} DESC")?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val bucketIdCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID)
            val bucketNameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
            
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val bucketId = cursor.getString(bucketIdCol) ?: "default"
                val bucketName = cursor.getString(bucketNameCol) ?: "Chưa phân loại"
                val coverUri = ContentUris.withAppendedId(videoUri, id).toString()
                
                val existing = albumMap[bucketId]
                if (existing == null) {
                    albumMap[bucketId] = AlbumData(bucketId, bucketName, coverUri, 1)
                } else {
                    existing.count++
                }
            }
        }
        
        albumMap.values.map {
            Album(
                id = it.id,
                name = it.name,
                coverPhotoUri = it.coverUri,
                photoCount = it.count
            )
        }
    }

    override suspend fun getPhotos(albumId: String?): List<PhotoItem> = withContext(Dispatchers.IO) {
        val photos = mutableListOf<PhotoItem>()
        
        // Query Images
        val imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val imgProjection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.MIME_TYPE
        )
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        if (albumId != null) {
            selection = "${MediaStore.Images.Media.BUCKET_ID} = ?"
            selectionArgs = arrayOf(albumId)
        }
        
        context.contentResolver.query(imageUri, imgProjection, selection, selectionArgs, "${MediaStore.Images.Media.DATE_ADDED} DESC")?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val bucketIdCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val dateAdded = cursor.getLong(dateAddedCol)
                val bucketId = cursor.getString(bucketIdCol)
                val size = cursor.getLong(sizeCol)
                val width = cursor.getInt(widthCol)
                val height = cursor.getInt(heightCol)
                val mime = cursor.getString(mimeCol)
                val uriStr = ContentUris.withAppendedId(imageUri, id).toString()
                
                photos.add(
                    PhotoItem(
                        id = id.toString(),
                        uri = uriStr,
                        dateAdded = dateAdded,
                        albumId = bucketId,
                        fileSize = size,
                        width = width,
                        height = height,
                        isVideo = false,
                        duration = null,
                        mimeType = mime
                    )
                )
            }
        }
        
        // Query Videos
        val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val vidProjection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DURATION
        )
        var vidSelection: String? = null
        var vidSelectionArgs: Array<String>? = null
        if (albumId != null) {
            vidSelection = "${MediaStore.Video.Media.BUCKET_ID} = ?"
            vidSelectionArgs = arrayOf(albumId)
        }
        
        context.contentResolver.query(videoUri, vidProjection, vidSelection, vidSelectionArgs, "${MediaStore.Video.Media.DATE_ADDED} DESC")?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val bucketIdCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
            val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
            val durCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val dateAdded = cursor.getLong(dateAddedCol)
                val bucketId = cursor.getString(bucketIdCol)
                val size = cursor.getLong(sizeCol)
                val width = cursor.getInt(widthCol)
                val height = cursor.getInt(heightCol)
                val mime = cursor.getString(mimeCol)
                val duration = cursor.getLong(durCol)
                val uriStr = ContentUris.withAppendedId(videoUri, id).toString()
                
                photos.add(
                    PhotoItem(
                        id = id.toString(),
                        uri = uriStr,
                        dateAdded = dateAdded,
                        albumId = bucketId,
                        fileSize = size,
                        width = width,
                        height = height,
                        isVideo = true,
                        duration = duration,
                        mimeType = mime
                    )
                )
            }
        }
        
        photos.sortByDescending { it.dateAdded }
        photos
    }

    override suspend fun deletePhotos(photoIds: List<String>): Boolean = withContext(Dispatchers.IO) {
        if (photoIds.isEmpty()) return@withContext true
        
        // Distinguish image IDs from video IDs
        val idsAsLong = photoIds.mapNotNull { it.toLongOrNull() }
        val videoIds = mutableSetOf<Long>()
        if (idsAsLong.isNotEmpty()) {
            val placeholders = idsAsLong.map { "?" }.joinToString(",")
            val selection = "${MediaStore.Video.Media._ID} IN ($placeholders)"
            val selectionArgs = idsAsLong.map { it.toString() }.toTypedArray()
            try {
                context.contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.Video.Media._ID),
                    selection,
                    selectionArgs,
                    null
                )?.use { cursor ->
                    val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                    while (cursor.moveToNext()) {
                        videoIds.add(cursor.getLong(idCol))
                    }
                }
            } catch (e: Exception) {}
        }

        val uris = photoIds.map { id ->
            val idLong = id.toLong()
            if (idLong in videoIds) {
                ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, idLong)
            } else {
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, idLong)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val pendingIntent = MediaStore.createDeleteRequest(context.contentResolver, uris)
                val intentSenderRequest = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                DeleteDelegate.requestDelete(intentSenderRequest)
            } catch (e: Exception) {
                false
            }
        } else {
            var allSuccess = true
            for (uri in uris) {
                try {
                    val deletedRows = context.contentResolver.delete(uri, null, null)
                    if (deletedRows <= 0) {
                        allSuccess = false
                    }
                } catch (e: Exception) {
                    allSuccess = false
                }
            }
            allSuccess
        }
    }

    private data class AlbumData(
        val id: String,
        val name: String,
        val coverUri: String,
        var count: Int
    )
}