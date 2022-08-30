package com.kylentt.musicplayer.domain.musiclib.source.mediastore.entity

import android.net.Uri
import android.widget.EditText
import com.kylentt.musicplayer.domain.musiclib.source.mediastore.media.MediaStoreFile
import com.kylentt.musicplayer.domain.musiclib.source.mediastore.media.MediaStoreMetadata
import com.kylentt.musicplayer.domain.musiclib.source.mediastore.query.MediaStoreQuery

/**
 * [GoogleSource](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-7.0.0_r36/core/java/android/provider/MediaStore.java)
 */

abstract class MediaStoreEntity internal constructor() {

	/**
	 * Unique Identifier
	 */
	abstract val uid: String

	/**
	 * The Content [Uri]
	 */
	abstract val uri: Uri

	/**
	 * The File Information.
	 */
	abstract val fileInfo: MediaStoreFile

	/**
	 * The Metadata Information.
	 */
	abstract val metadataInfo: MediaStoreMetadata

	/**
	 * The Query Information, anything that is specific to MediaStore query tables
	 */
	internal abstract val queryInfo: MediaStoreQuery
}