package com.kylentt.musicplayer.common.kotlin.mutablecollection

fun <T> MutableCollection<T>.forEachClear(lock: Any? = null, action: (T) -> Unit) {
	val doAction = { forEach(action) }
	if (lock != null) synchronized(lock) { doAction() } else doAction()
	clear()
}
