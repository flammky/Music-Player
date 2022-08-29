package com.kylentt.musicplayer.domain.musiclib.source.mediastore.base.media

import android.provider.MediaStore
import com.kylentt.musicplayer.domain.musiclib.annotation.StorageDataUnit
import com.kylentt.musicplayer.domain.musiclib.annotation.StorageDataValue
import com.kylentt.musicplayer.domain.musiclib.annotation.TimeUnitValue
import java.util.concurrent.TimeUnit

/**
 * [GoogleSource](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-7.0.0_r36/core/java/android/provider/MediaStore.java#471)
 */

abstract class MediaStoreFile {

	/**
	 * the absolute path of the file
	 *
	 * @see [MediaStore.Files.FileColumns.DATA]
	 */
	abstract val absolutePath: String

	/**
	 * the bucket (folder) name of the file,
	 * + computed from [absolutePath] if this column is not present
	 *
	 * @see [MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME]
	 */
	abstract val bucketDisplayName: String

	/**
	 * the bucket (folder) id of the file,
	 * + computed from [absolutePath] if this column is not present
	 *
	 * ```
	 * val parentFile = File(absolutePath).parentFile ?: File("/")
	 * parentFile.toString().lowerCase().hashCode()
	 * ```
	 *
	 * @see [MediaStore.Images.ImageColumns.BUCKET_ID]
	 */
	abstract val bucketId: Long

	/**
	 * the `date-added` of this file,
	 * the value represent elapsed [TimeUnit.SECONDS] since Unix Epoch Time.
	 *
	 * @see [MediaStore.Files.FileColumns.DATE_ADDED]
	 */
	@TimeUnitValue(TimeUnit.SECONDS)
	abstract val dateAdded: Long

	/**
	 * the `date-modified` of this file,
	 * the value represent elapsed [TimeUnit.SECONDS] since Unix Epoch Time.
	 *
	 * @see [MediaStore.Files.FileColumns.DATE_MODIFIED]
	 */
	@TimeUnitValue(TimeUnit.SECONDS)
	abstract val dateModified: Long

	/**
	 * the actual name of the file, without extension
	 *
	 * @see [MediaStore.Files.FileColumns.DISPLAY_NAME]
	 */
	abstract val fileName: String

	/**
	 * the name of the file including its extension
	 *
	 * @see [MediaStore.Files.FileColumns.DISPLAY_NAME]
	 */
	abstract val fileNameWithExtension: String

	/**
	 * The media type (audio, video, image or playlist) of the file,
	 * or 0 for not a media file
	 *
	 * @see [MediaStore.Files.FileColumns.MEDIA_TYPE]
	 */
	abstract val mediaType: String

	/**
	 * the Mime-Type of the file
	 *
	 * @see [MediaStore.Files.FileColumns.MIME_TYPE]
	 */
	abstract val mimeType: String

	/**
	 * the size of the file, in bytes
	 *
	 * @see [MediaStore.Files.FileColumns.SIZE]
	 */
	@StorageDataValue(StorageDataUnit.BYTE)
	abstract val size: Long
}
