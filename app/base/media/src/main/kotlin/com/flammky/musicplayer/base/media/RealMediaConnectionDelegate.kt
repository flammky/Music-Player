package com.flammky.musicplayer.base.media

import com.flammky.android.medialib.common.mediaitem.MediaItem
import com.flammky.android.medialib.temp.MediaLibrary

class RealMediaConnectionDelegate() : MediaConnectionDelegate {
	private val s = MediaLibrary.API.sessions.manager.findSessionById("DEBUG")!!

	override fun play(item: MediaItem) {
		s.mediaController.play(item)
	}
}