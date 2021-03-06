package com.kylentt.musicplayer.domain.musiclib.service.manager

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.ControllerInfo
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.kylentt.mediaplayer.helper.Preconditions.checkArgument
import com.kylentt.mediaplayer.helper.Preconditions.checkState
import com.kylentt.musicplayer.common.generic.ChangedNotNull
import com.kylentt.musicplayer.core.app.dependency.CoroutineModule
import com.kylentt.musicplayer.domain.musiclib.core.media3.mediaitem.MediaItemPropertyHelper
import com.kylentt.musicplayer.domain.musiclib.service.MusicLibraryService
import com.kylentt.musicplayer.domain.musiclib.service.provider.SessionProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import timber.log.Timber

class SessionManager(
	private val sessionProvider: SessionProvider
) : MusicLibraryService.ServiceComponent() {

	private lateinit var sessionManagerJob: Job

	private val sessionRegistry = SessionRegistry()

	private val appDispatchers = CoroutineModule.provideAppDispatchers()
	private val mainScope by lazy { CoroutineScope(appDispatchers.main + sessionManagerJob) }

	override fun create(serviceDelegate: MusicLibraryService.ServiceDelegate) {
		super.create(serviceDelegate)
		val serviceJob = serviceDelegate.propertyInteractor.serviceMainJob
		sessionManagerJob = SupervisorJob(serviceJob)
	}

	override fun serviceDependencyInjected() {
		super.serviceDependencyInjected()
		val delegate = serviceDelegate
		val context = delegate.propertyInteractor.context!!
		val callback = librarySessionCallback
		val get = sessionProvider
			.getNewLibrarySession(context, delegate.propertyInteractor.injectedPlayer, callback)
		sessionRegistry.changeLocalLibrarySession(get)
	}

	override fun release() {
		super.release()

		sessionManagerJob.cancel()
		sessionRegistry.release()
	}

	val interactor: Interactor = Interactor()

	private fun getCurrentMediaSession(): MediaSession? {
		if (isReleased || !sessionRegistry.isLibrarySessionInitialized) return null

		return sessionRegistry.localLibrarySession
	}

	private fun getSessionPlayer(): Player? {
		if (isReleased || !sessionRegistry.isLibrarySessionInitialized) return null

		return sessionRegistry.localLibrarySession.player
	}

	private fun changeSessionPlayer(player: Player, release: Boolean) {
		if (isReleased) return

		sessionRegistry.changeSessionPlayer(player, release)
	}

	private fun registerPlayerChangedListener(onChanged: ChangedNotNull<Player>) {
		if (isReleased) return

		sessionRegistry.registerOnPlayerChangedListener(onChanged)
	}

	private fun unregisterPlayerChangedListener(onChanged: ChangedNotNull<Player>): Boolean {
		if (isReleased) return false

		return sessionRegistry.unRegisterOnPlayerChangedListener(onChanged)
	}

	private fun registerPlayerEventListener(listener: Player.Listener) {
		if (isReleased) return

		sessionRegistry.registerOnPlayerEventListener(listener)
	}

	private fun unRegisterPlayerEventListener(listener: Player.Listener): Boolean {
		if (isReleased) return false

		return sessionRegistry.unregisterOnPlayerEventListener(listener)
	}

	fun onGetSession(controllerInfo: ControllerInfo): MediaLibrarySession? {
		return if (!isReleased) sessionRegistry.localLibrarySession else null
	}

	private inner class SessionRegistry {

		lateinit var localLibrarySession: MediaLibrarySession
			private set

		private val onLibrarySessionChangedListener: MutableList<ChangedNotNull<MediaLibrarySession>> =
			mutableListOf()

		private val onPlayerChangedListener: MutableList<ChangedNotNull<Player>> =
			mutableListOf()

		private val onPlayerEventListener: MutableList<Player.Listener> =
			mutableListOf()

		/**
		 * MediaLibrarySession status will be tracked manually as the library didn't provide one
		 */

		var isLibrarySessionReleased = false
			private set

		val isLibrarySessionInitialized
			get() = ::localLibrarySession.isInitialized

		private fun onSessionPlayerChanged(old: Player?, new: Player) {
			checkArgument(old !== new)
			checkState(localLibrarySession.player === new)

			onPlayerEventListener.forEach { listener ->
				old?.removeListener(listener)
				new.addListener(listener)
			}

			onPlayerChangedListener.forEach { it.onChanged(old, new) }
		}

		fun changeLocalLibrarySession(session: MediaLibrarySession) {
			var oldSession: MediaLibrarySession? = null
			var oldPlayer: Player? = null

			if (isLibrarySessionInitialized) {
				if (localLibrarySession === session) {
					return Timber.w(
						"Tried to change LocalLibrarySession to same Instance." +
							"\n $localLibrarySession === $session"
					)
				}

				oldSession = localLibrarySession
				oldPlayer = localLibrarySession.player
			}

			localLibrarySession = session

			oldSession?.release()

			onLibrarySessionChangedListener.forEach { it.onChanged(oldSession, session) }

			if (session.player !== oldPlayer) {
				oldPlayer?.release()
				onSessionPlayerChanged(oldPlayer, session.player)
			}
		}

		fun changeSessionPlayer(player: Player, release: Boolean) {
			val old = localLibrarySession.player

			if (player === old) return
			if (release) old.release()

			localLibrarySession.player = player
			onSessionPlayerChanged(old, player)
		}

		fun registerOnLibrarySessionChangedListener(listener: ChangedNotNull<MediaLibrarySession>) {
			if (onLibrarySessionChangedListener.find { it === listener } != null) return

			onLibrarySessionChangedListener.add(listener)
		}

		fun registerOnPlayerChangedListener(listener: ChangedNotNull<Player>) {
			if (onPlayerChangedListener.find { it === listener } != null) return

			onPlayerChangedListener.add(listener)
		}

		fun registerOnPlayerEventListener(listener: Player.Listener) {
			if (onPlayerEventListener.find { it === listener } != null) return

			if (::localLibrarySession.isInitialized) {
				localLibrarySession.player.addListener(listener)
			}

			onPlayerEventListener.add(listener)
		}

		fun unRegisterOnLibrarySessionChangedListener(listener: ChangedNotNull<MediaLibrarySession>): Boolean {
			return onLibrarySessionChangedListener.removeAll { it === listener }
		}

		fun unRegisterOnPlayerChangedListener(listener: ChangedNotNull<Player>): Boolean {
			return onPlayerChangedListener.removeAll { it === listener }
		}

		fun unregisterOnPlayerEventListener(listener: Player.Listener): Boolean {
			if (::localLibrarySession.isInitialized) {
				localLibrarySession.player.removeListener(listener)
			}

			return onPlayerEventListener.removeAll { it === listener }
		}

		fun releaseSession() {
			localLibrarySession.release()
			isLibrarySessionReleased = true
		}

		fun release() {
			onLibrarySessionChangedListener.clear()
			onPlayerChangedListener.clear()
			releaseSession()
		}
	}

	inner class Interactor : MusicLibraryService.ServiceComponent.Interactor() {

		val sessionPlayer: Player?
			get() = getSessionPlayer()

		val mediaSession: MediaSession?
			get() = getCurrentMediaSession()

		fun registerPlayerChangedListener(listener: ChangedNotNull<Player>): Unit =
			this@SessionManager.registerPlayerChangedListener(listener)

		fun removePlayerChangedListener(listener: ChangedNotNull<Player>): Boolean =
			this@SessionManager.unregisterPlayerChangedListener(listener)

		fun registerPlayerEventListener(listener: Player.Listener) =
			this@SessionManager.registerPlayerEventListener(listener)

		fun removePlayerEventListener(listener: Player.Listener) =
			this@SessionManager.unRegisterPlayerEventListener(listener)
	}

	private val librarySessionCallback = object : MediaLibrarySession.Callback {
		override fun onAddMediaItems(
			mediaSession: MediaSession,
			controller: ControllerInfo,
			mediaItems: MutableList<MediaItem>
		): ListenableFuture<MutableList<MediaItem>> {
			val toReturn = mutableListOf<MediaItem>()

			mediaItems.forEach {

				with(MediaItemPropertyHelper) {
					val uri = it.mediaUri ?: return@forEach
					val filled =
						com.kylentt.musicplayer.domain.musiclib.core.media3.mediaitem.MediaItemFactory.fillInLocalConfig(
							it,
							uri
						)
					toReturn.add(filled)
				}
			}

			return Futures.immediateFuture(toReturn)
		}
	}
}
