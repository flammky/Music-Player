package com.flammky.android.medialib.context

import com.flammky.android.medialib.context.internal.InternalLibraryContext

/**
 * The Context for Library Instances.
 *
 * Implementations are internal, but Configurable
 */

abstract class LibraryContext internal constructor(
	internal val internal: InternalLibraryContext
) {

}
