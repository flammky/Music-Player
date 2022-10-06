package com.flammky.android.medialib.providers.mediastore.api28

import com.flammky.android.io.exception.ReadExternalStoragePermissionException
import com.flammky.android.medialib.providers.mediastore.MediaStoreContext
import com.flammky.android.medialib.providers.mediastore.MediaStoreProvider
import com.flammky.android.medialib.providers.mediastore.api28.audio.MediaStoreAudioEntity28
import com.flammky.android.medialib.providers.mediastore.api28.audio.MediaStoreAudioProvider28
import com.flammky.android.medialib.providers.mediastore.base.MediaStoreProviderBase28
import com.flammky.android.medialib.providers.mediastore.base.media.MediaStoreEntity

internal class MediaStoreProvider28 (private val context: MediaStoreContext)
	: MediaStoreProviderBase28() {

	override val audio: Audio = MediaStoreAudioProvider28(context)

	override val image: Image = object : Image {
		override suspend fun query(): List<MediaStoreEntity> {
			TODO("Not yet implemented")
		}
	}

	override val video: MediaStoreProvider.Video = object : Video {
		override suspend fun query(): List<MediaStoreEntity> {
			TODO("Not yet implemented")
		}
	}

	interface Audio : MediaStoreProvider.Audio {

		@kotlin.jvm.Throws(ReadExternalStoragePermissionException::class)
		override suspend fun query(): List<MediaStoreAudioEntity28>
	}

	interface Image : MediaStoreProvider.Image
	interface Video : MediaStoreProvider.Video
}
