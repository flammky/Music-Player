package com.kylentt.musicplayer.domain.musiclib.service.manager

import android.app.Notification
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import com.kylentt.musicplayer.domain.musiclib.core.exoplayer.PlayerExtension.isOngoing
import com.kylentt.musicplayer.domain.musiclib.service.MusicLibraryService
import com.kylentt.musicplayer.ui.main.MainActivity
import timber.log.Timber

class StateManager(
	private val stateInteractor: MusicLibraryService.StateInteractor
) : MusicLibraryService.ServiceComponent() {
	private val playerListenerImpl = PlayerListenerImpl()

	private val foregroundServiceCondition: (MediaSession) -> Boolean = {
		val extra = when {
			!MainActivity.StateDelegate.getValue().isVisible() -> it.player.playWhenReady
			else -> true
		}
			it.player.playbackState.isOngoing() && extra
	}


	override fun start(componentDelegate: MusicLibraryService.ComponentDelegate) {
		super.start(componentDelegate)
		componentDelegate.sessionInteractor.registerPlayerEventListener(playerListenerImpl)
	}

	override fun release() {
		if (isReleased) return
		if (isStarted) {
			componentDelegate.sessionInteractor.removePlayerEventListener(playerListenerImpl)
		}
		return super.release()
	}

	val interactor = Interactor()


	private inner class PlayerListenerImpl : Player.Listener {
		override fun onPlaybackStateChanged(playbackState: Int) {
			Timber.d("stateManager received onPlaybackStateChanged")

			if (!isStarted || isReleased) return
			val mediaSession = componentDelegate.sessionInteractor.mediaSession ?: return
			val notificationInteractor = componentDelegate.notificationInteractor

			if (foregroundServiceCondition(mediaSession)) {
				if (!stateInteractor.isForeground) {
					notificationInteractor.startForegroundService(mediaSession, onGoingNotification = true)
				}
			} else {
				if (stateInteractor.isForeground) {
					notificationInteractor.stopForegroundService(false)
				}
			}

			when (playbackState) {
				Player.STATE_READY -> {}
				Player.STATE_BUFFERING -> {}
				Player.STATE_IDLE -> {}
				Player.STATE_ENDED -> {}
			}
		}
	}

	inner class Interactor {
		fun startForegroundService(notification: Notification) {
			stateInteractor.startForeground(notification)
		}

		fun stopForegroundService(removeNotification: Boolean) {
			stateInteractor.stopForeground(removeNotification)
		}

		fun startService() {
			stateInteractor.start()
		}

		fun stopService() {
			stateInteractor.stop(false)
		}

		fun releaseService() {
			stateInteractor.release()
		}

		fun getServiceForegroundCondition(mediaSession: MediaSession): Boolean {
			return foregroundServiceCondition(mediaSession)
		}
	}
}
