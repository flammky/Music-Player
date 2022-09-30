package com.flammky.musicplayer.core.app

import android.app.Application
import android.content.Context
import com.flammky.android.app.ApplicationDelegate
import com.flammky.android.app.cache.CacheManager
import com.flammky.android.app.device.DeviceManager
import com.flammky.android.app.permission.PermissionManager
import com.flammky.android.medialib.temp.common.context.ContextInfo
import com.flammky.kotlin.common.lazy.LazyConstructor


interface ApplicationDelegate {
	val cacheManager: CacheManager
	val contextInfo: ContextInfo
	val deviceManager: DeviceManager
	val permissionManager: PermissionManager
}

class AppDelegate private constructor (context: Context) : ApplicationDelegate {
	private val mBase: Application = context.applicationContext as Application

	override val cacheManager = CacheManager.get(mBase)
	override val contextInfo = ContextInfo(mBase)
	override val deviceManager = DeviceManager.get(mBase)
	override val permissionManager = PermissionManager.get(mBase)

	companion object {
		private val delegate = LazyConstructor<AppDelegate>()

		val cacheManager
			get() = delegate.value.cacheManager

		val deviceManager
			get() = delegate.value.deviceManager

		val permissionManager
			get() = delegate.value.permissionManager

		infix fun provides(context: Context) = delegate.construct {
			AppDelegate(context)
		}
	}
}
