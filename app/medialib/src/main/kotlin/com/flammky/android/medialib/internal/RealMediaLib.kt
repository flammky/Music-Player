package com.flammky.android.medialib.internal

import android.content.Context
import com.flammky.android.medialib.context.AndroidContext
import com.flammky.android.medialib.context.internal.RealLibraryContext
import com.flammky.android.medialib.core.MediaLibrary
import com.flammky.android.medialib.core.internal.RealMediaLibrary
import com.flammky.kotlin.common.lazy.LazyConstructor

internal object RealMediaLib {
	private val Singleton = LazyConstructor<MediaLibrary>()

	val singleton: MediaLibrary?
		get() = if (Singleton.isConstructedAtomic()) Singleton.value else null

	fun singleton(context: Context): MediaLibrary = Singleton.construct {
		RealMediaLibrary(RealLibraryContext(AndroidContext(context)))
	}
}
