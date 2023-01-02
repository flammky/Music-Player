@file:OptIn(ExperimentalStdlibApi::class, ExperimentalStdlibApi::class)

package com.flammky.musicplayer.playbackcontrol.ui.real

import androidx.annotation.GuardedBy
import com.flammky.kotlin.common.sync.sync
import com.flammky.musicplayer.media.mediaconnection.playback.PlaybackConnection
import com.flammky.musicplayer.media.playback.RepeatMode
import com.flammky.musicplayer.media.playback.ShuffleMode
import com.flammky.musicplayer.playbackcontrol.ui.controller.PlaybackController
import com.flammky.musicplayer.playbackcontrol.ui.presenter.PlaybackObserver
import com.flammky.musicplayer.ui.playbackcontrol.RealPlaybackControlPresenter
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

internal class RealPlaybackController(
	sessionID: String,
	private val scope: CoroutineScope,
	private val presenter: RealPlaybackControlPresenter,
	private val playbackConnection: PlaybackConnection,
) : PlaybackController(sessionID) {

	private val _stateLock = Any()

	@GuardedBy("_stateLock")
	private var _disposed = false

	private val _observers = mutableListOf<RealPlaybackObserver>()


	override val disposed: Boolean
		get() = sync(_stateLock) { _disposed }

	override fun dispose() {
		sync(_stateLock) {
			if (_disposed) {
				return checkDisposedState()
			}
			scope.cancel()
			_disposed = true
			disposeObservers()
			checkDisposedState()
		}
		presenter.notifyControllerDisposed(this)
	}

	override fun createPlaybackObserver(
		coroutineContext: CoroutineContext
	): PlaybackObserver {
		return RealPlaybackObserver(
			controller = this,
			parentScope = scope,
			connection = playbackConnection
		).also {
			sync(_stateLock) { if (_disposed) it.dispose() else _observers.sync { add(it) } }
		}
	}

	override fun requestSeekAsync(
		position: Duration,
		coroutineContext: CoroutineContext
	): Deferred<RequestResult> {
		return scope.async(coroutineContext.minusKey(CoroutineDispatcher)) {
			val success = playbackConnection.getSession(sessionID)?.controller?.withContext {
				seekProgress(position)
			} ?: false
			RequestResult(
				success = success,
				eventDispatch = if (success) {
					launch {
						val jobs = mutableListOf<Job>()
						_observers.forEach { jobs.add(it.updateProgress()) }
						jobs.joinAll()
					}
				} else {
					null
				}
			)
		}
	}

	override fun requestSeekAsync(
		index: Int,
		startPosition: Duration,
		coroutineContext: CoroutineContext
	): Deferred<RequestResult> {
		return scope.async(coroutineContext.minusKey(CoroutineDispatcher)) {
			val success = playbackConnection.getSession(sessionID)?.controller?.withContext {
				seekIndex(index, startPosition)
			} ?: false
			RequestResult(
				success = success,
				eventDispatch = if (success) {
					scope.launch {
						val jobs = mutableListOf<Job>()
						_observers.forEach {
							jobs.add(it.updateQueue())
						}
						jobs.joinAll()
					}
				} else {
					null
				}
			)
		}
	}

	override fun requestSeekNextAsync(
		startPosition: Duration,
		coroutineContext: CoroutineContext
	): Deferred<RequestResult> {
		return scope.async(coroutineContext.minusKey(CoroutineDispatcher)) {
			val success = playbackConnection.getSession(sessionID)?.controller?.withContext {
				seekNext()
			} ?: false
			RequestResult(
				success = success,
				eventDispatch = if (success) {
					scope.launch {
						val jobs = mutableListOf<Job>()
						_observers.forEach {
							jobs.add(it.updateQueue())
						}
						jobs.joinAll()
					}
				} else {
					null
				}
			)
		}
	}

	override fun requestSeekPreviousAsync(
		startPosition: Duration,
		coroutineContext: CoroutineContext
	): Deferred<RequestResult> {
		return scope.async(coroutineContext.minusKey(CoroutineDispatcher)) {
			val success = playbackConnection.getSession(sessionID)?.controller?.withContext {
				seekPrevious()
			} ?: false
			RequestResult(
				success = success,
				eventDispatch = if (success) {
					scope.launch {
						val jobs = mutableListOf<Job>()
						_observers.forEach {
							jobs.add(it.updateQueue())
						}
						jobs.joinAll()
					}
				} else {
					null
				}
			)
		}
	}

	override fun requestCompareAndSetAsync(
		compareAndSet: CompareAndSetScope.() -> Unit)
	: Deferred<RequestResult> {
		TODO("Not yet implemented")
	}

	override fun requestPlayAsync(
		coroutineContext: CoroutineContext
	): Deferred<RequestResult> {
		return scope.async(coroutineContext.minusKey(CoroutineDispatcher)) {
			val success = playbackConnection.getSession(sessionID)?.controller?.withContext {
				play()
			} ?: false
			RequestResult(
				success = success,
				eventDispatch = if (success) {
					launch {
						val jobs = mutableListOf<Job>()
						_observers.forEach {
							jobs.add(it.updatePlayWhenReady())
						}
						jobs.joinAll()
					}
				} else {
					null
				}
			)
		}
	}

	override fun requestSetPlayWhenReadyAsync(
		playWhenReady: Boolean,
		coroutineContext: CoroutineContext
	): Deferred<RequestResult> {
		return scope.async(coroutineContext.minusKey(CoroutineDispatcher)) {
			val success = playbackConnection.getSession(sessionID)?.controller?.withContext {
				setPlayWhenReady(playWhenReady)
			} ?: false
			RequestResult(
				success = success,
				eventDispatch = if (success) {
					launch {
						val jobs = mutableListOf<Job>()
						_observers.forEach {
							jobs.add(it.updatePlayWhenReady())
						}
						jobs.joinAll()
					}
				} else {
					null
				}
			)
		}
	}

	override fun requestSetRepeatModeAsync(
		repeatMode: RepeatMode,
		coroutineContext: CoroutineContext
	): Deferred<RequestResult> {
		return scope.async(coroutineContext.minusKey(CoroutineDispatcher)) {
			val success = playbackConnection.getSession(sessionID)?.controller?.withContext {
				setRepeatMode(repeatMode)
			} ?: false
			RequestResult(
				success = success,
				eventDispatch = if (success) {
					launch {
						val jobs = mutableListOf<Job>()
						_observers.forEach {
							jobs.add(it.updateRepeatMode())
						}
						jobs.joinAll()
					}
				} else {
					null
				}
			)
		}


	}

	override fun requestSetShuffleModeAsync(
		shuffleMode: ShuffleMode,
		coroutineContext: CoroutineContext
	): Deferred<RequestResult> {
		return scope.async(coroutineContext.minusKey(CoroutineDispatcher)) {
			val success = playbackConnection.getSession(sessionID)?.controller?.withContext {
				setShuffleMode(shuffleMode)
			} ?: false
			RequestResult(
				success = success,
				eventDispatch = if (success) {
					launch {
						val jobs = mutableListOf<Job>()
						_observers.forEach {
							jobs.add(it.updateShuffleMode())
						}
						jobs.joinAll()
					}
				} else {
					null
				}
			)
		}
	}

	fun notifyObserverDisposed(observer: RealPlaybackObserver) {
		_observers.sync {
			remove(observer)
		}
	}

	private fun checkDisposedState() {
		check(Thread.holdsLock(_stateLock))
		check(!scope.isActive && _observers.sync { isEmpty() }) {
			"Controller was not disposed properly"
		}
	}

	private fun disposeObservers() {
		debugDisposeObservers()
	}

	private fun debugDisposeObservers() {
		_observers.sync {
			val actual = this
			val copy = ArrayList(this)
			var count = copy.size
			for (observer in copy) {
				observer.dispose()
				check(actual.size == --count && actual.firstOrNull() != observer) {
					"Observer $observer did not notify Controller ${this@RealPlaybackController} on disposal"
				}
			}
			check(actual.isEmpty() && count == 0) {
				"disposeObservers failed"
			}
		}
	}
}
