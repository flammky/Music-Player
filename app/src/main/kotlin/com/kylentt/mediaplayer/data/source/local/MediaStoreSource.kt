package com.kylentt.mediaplayer.data.source.local

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.RequestMetadata
import androidx.media3.common.MediaMetadata
import com.kylentt.musicplayer.common.coroutines.CoroutineDispatchers
import com.kylentt.musicplayer.domain.musiclib.core.media3.mediaitem.MediaItemFactory
import com.kylentt.musicplayer.domain.musiclib.core.media3.mediaitem.MediaItemPropertyHelper.mediaUri
import com.kylentt.musicplayer.domain.musiclib.core.media3.mediaitem.MediaMetadataHelper.putDisplayTitle
import com.kylentt.musicplayer.domain.musiclib.core.media3.mediaitem.MediaMetadataHelper.putFileName
import com.kylentt.musicplayer.domain.musiclib.core.media3.mediaitem.MediaMetadataHelper.putStoragePath
import com.kylentt.mediaplayer.data.SongEntity
import com.kylentt.musicplayer.core.sdk.VersionHelper
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Singleton

/**
 * [SongEntity] Implementation for [android.provider.MediaStore] sourced Song
 * @see [MediaStoreSource]
 * @author Kylentt
 * @since 2022/04/30
 */

data class MediaStoreSong @JvmOverloads constructor(
  val album: String = "",
  val albumArtUri: Uri = Uri.EMPTY,
  val albumId: Long = -1,
  val artist: String = "",
  val artistId: Long = -1,
  val byteSize: Long = -1,
  val duration: Long = -1,
  val data: String? = null,
  val fileName: String = "",
  val fileBucket: String = "",
  val fileBuckedId: String = "",
  val lastModified: Long = -1,
  val mediaId: String = "",
  val mediaUri: Uri = Uri.EMPTY,
  val title: String = ""
) : SongEntity {

  override val mediaIdPrefix: String
    get() = MEDIA_ID_PREFIX

  override val albumName: String
    get() = this.album
  override val artistName: String
    get() = this.artist
  override val displayTitle: String
    get() = this.fileName
	override val songFileName: String
		get() = this.fileName
  override val songMediaArtworkUri: Uri
    get() = this.albumArtUri
  override val songMediaId: String
    get() = this.mediaIdPrefix + mediaId
  override val songMediaUri: Uri
    get() = this.mediaUri

  val mediaItem by lazy {
    val artworkUri = MediaItemFactory.hideArtUri(songMediaArtworkUri)
		val bundle = Bundle()

		val metadataBuilder = MediaMetadata.Builder()
			.putDisplayTitle(displayTitle)
			.putFileName(fileName)
			.setArtist(artistName)
			.setAlbumArtist(artistName)
			.setAlbumTitle(albumName)
			.setArtworkUri(artworkUri) // M
			.setSubtitle(artist)
			.setTitle(title) // MediaSession automatically use this one for Notification
			.setIsPlayable(duration > 0)

		val metadataRequestBuilder = RequestMetadata.Builder()
			.setMediaUri(songMediaUri)
			.setExtras(Bundle())

		if (data != null && File(data).exists()) {
			metadataBuilder.putStoragePath(data, bundle)
		}

    MediaItem.Builder()
      .setMediaId(songMediaId)
      .setUri(songMediaUri)
      .setMediaMetadata(metadataBuilder.build())
			.setRequestMetadata(metadataRequestBuilder.build())
		.build()
  }

  override val asMediaItem: MediaItem
    get() = mediaItem

  override fun equalMediaItem(item: MediaItem): Boolean {
    return item.mediaId == this.songMediaId
			&& item.mediaUri == this.songMediaUri
  }

  override fun toString(): String {
    return "MediaStoreSong" +
      "\ntitle: $title" +
      "\nalbum: $album" +
      "\nartist: $artist" +
      "\nduration: $duration" +
      "\ndata: $data" +
      "\nmediaId: $songMediaId" +
      "\nmediaUri: $songMediaUri" +
      "\nextra: $albumArtUri, $albumId, $artistId, $byteSize, $fileName, $fileBucket, " +
      "$fileBuckedId, $lastModified"
  }

  companion object {
    const val MEDIA_ID_PREFIX = "MEDIASTORE_"

    @JvmStatic
		val EMPTY = MediaStoreSong()

    @JvmStatic fun MediaItem.isMediaStoreSong() = this.mediaId.startsWith(MEDIA_ID_PREFIX)
  }
}

/**
 * Data Source Interface for [android.provider.MediaStore]
 * @see [MediaStoreSong]
 * @see [MediaStoreSourceImpl]
 * @author Kylentt
 * @since 2022/04/30
 */

interface MediaStoreSource {
  suspend fun getMediaStoreSong(): List<MediaStoreSong>
}

/**
 * Base Implementation for [MediaStoreSource] Interface
 * @see [MediaStoreSong]
 * @author Kylentt
 * @since 2022/04/30
 */

@Singleton
class MediaStoreSourceImpl(
  private val context: Context,
  private val dispatchers: CoroutineDispatchers
) : MediaStoreSource {

  override suspend fun getMediaStoreSong(): List<MediaStoreSong> = queryAudioColumn()

  private suspend fun queryAudioColumn(): List<MediaStoreSong> =
    withContext(dispatchers.io) {
      val songList = mutableListOf<MediaStoreSong>()

			try {
        val songFolderName =
					if (VersionHelper.hasQ()) {
						MediaStore.Audio.AudioColumns.BUCKET_DISPLAY_NAME
        	} else {
         	 MediaStore.Audio.AudioColumns.DATA
        	}

        val songFolderId =
					if (VersionHelper.hasQ()) {
          MediaStore.Audio.AudioColumns.BUCKET_ID
        	} else {
						MediaStore.Audio.AudioColumns.DATA
					}

        val projector = arrayOf(
          MediaStore.Audio.AudioColumns._ID,
          MediaStore.Audio.AudioColumns.ALBUM,
          MediaStore.Audio.AudioColumns.ALBUM_ID,
          MediaStore.Audio.AudioColumns.ARTIST,
          MediaStore.Audio.AudioColumns.ARTIST_ID,
          MediaStore.Audio.AudioColumns.DISPLAY_NAME,
          MediaStore.Audio.AudioColumns.DURATION,
          MediaStore.Audio.AudioColumns.DATA,
          MediaStore.Audio.AudioColumns.DATE_MODIFIED,
          MediaStore.Audio.AudioColumns.SIZE,
          MediaStore.Audio.AudioColumns.TITLE,
          songFolderName,
          songFolderId,
        )

        val selector = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val selectOrder = MediaStore.Audio.Media.TITLE
        val nCursor = context.contentResolver.query(
          MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
          projector, selector, null, selectOrder
        )

        nCursor?.use { cursor ->

          while (cursor.moveToNext()) {
            ensureActive()

            val songId =
              cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID))
            val album =
              cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM))
            val albumId =
              cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM_ID))
            val artist =
              cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST))
            val artistId =
              cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST_ID))
            val byteSize =
              cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.SIZE))

            val d = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA)
            val data = if (d > -1) cursor.getString(d) else "DEPRECATED"

            val dateModified =
              cursor
                .getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATE_MODIFIED))
            val duration =
              cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION))
            val fileName =
              cursor
                .getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DISPLAY_NAME))
            val title =
              cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE))
            val folder =
              cursor.getString(cursor.getColumnIndexOrThrow(songFolderName))
            val folderId =
              cursor.getString(cursor.getColumnIndexOrThrow(songFolderId))

            val albumUri = ContentUris
              .withAppendedId(Uri.parse(MEDIASTORE_ALBUM_ART_PREFIX), albumId)
            val songUri = ContentUris
              .withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId.toLong()).toString()

            songList.add(
              MediaStoreSong(
                album = album,
                albumArtUri = albumUri,
                albumId = albumId,
                artist = artist,
                artistId = artistId,
                byteSize = byteSize,
                duration = duration,
                data = data,
                fileName = fileName,
                fileBucket = folder ?: "",
                fileBuckedId = folderId,
                lastModified = dateModified,
                mediaId = songId,
                mediaUri = songUri.toUri(),
                title = title
              )
            )
          }
        }
      } catch (e: Exception) {
        Timber.e(e)
      }
      songList.forEachIndexed { index, mediaStoreSong ->
        Timber.d("MediaStoreSource Queried: \n$mediaStoreSong\ntotal:$index")
      }
      ensureActive()
      songList
  }

  companion object {
    const val MEDIASTORE_ALBUM_ART_PREFIX = "content://media/external/audio/albumart"
  }
}
