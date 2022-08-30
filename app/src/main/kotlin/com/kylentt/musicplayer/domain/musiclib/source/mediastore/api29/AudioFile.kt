package com.kylentt.musicplayer.domain.musiclib.source.mediastore.api29

import android.provider.MediaStore
import com.kylentt.musicplayer.domain.musiclib.source.mediastore.audio.MediaStoreAudioFile

/**
 * class representing Audio File Information on MediaStore API 29 / Android 10.0 / Android Q.
 * @see MediaStore29.Files
 */
class MediaStoreAudioFile29 private constructor(

	override val absolutePath: String,

	/**
	 * the bucket (folder) name of the file,
	 *
	 * @see [MediaStore.MediaColumns.BUCKET_DISPLAY_NAME]
	 */
	val bucketDisplayName: String,

	/**
	 * the bucket (folder) id of the file,
	 * + computed from [absolutePath] if this column is not present
	 *
	 * ```
	 * val parentFile = File(absolutePath).parentFile ?: File("/")
	 * parentFile.toString().lowerCase().hashCode()
	 * ```
	 *
	 * @see [MediaStore.MediaColumns.BUCKET_ID]
	 */
	val bucketId: Long,

	override val dateAdded: Long,
	override val dateModified: Long,
	override val fileName: String,
	override val fileNameWithExtension: String,
	override val mimeType: String,
	override val size: Long,
) : MediaStoreAudioFile() {

	class Builder internal constructor() {
		var absolutePath: String = ""
		var bucketDisplayName: String = ""
		var bucketId: Long = Int.MIN_VALUE - 1L
		var dateAdded: Long = -1L
		var dateModified: Long = -1L
		var fileName: String = ""
		var fileNameWithExtension: String = ""
		var mimeType: String = ""
		var size: Long = -1L

		fun build(): MediaStoreAudioFile29 {
			return MediaStoreAudioFile29(
				absolutePath = absolutePath,
				bucketDisplayName = bucketDisplayName,
				bucketId = bucketId,
				dateAdded = dateAdded,
				dateModified = dateModified,
				fileName = fileName,
				fileNameWithExtension = fileNameWithExtension,
				mimeType = mimeType,
				size = size
			)
		}

	}
}