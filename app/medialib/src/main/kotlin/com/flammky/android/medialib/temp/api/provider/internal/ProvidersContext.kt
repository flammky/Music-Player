package com.flammky.android.medialib.temp.api.provider.internal

import com.flammky.android.common.kotlin.coroutine.ANDROID
import com.flammky.android.medialib.context.LibraryContext
import com.flammky.common.kotlin.coroutine.CoroutineDispatchers
import kotlinx.coroutines.CoroutineDispatcher

internal class ProvidersContext(
	val library: LibraryContext
) {
	val eventDispatcher: CoroutineDispatcher = CoroutineDispatchers.ANDROID.io
}
