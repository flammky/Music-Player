package com.kylentt.musicplayer.common.late

import androidx.annotation.GuardedBy
import com.kylentt.mediaplayer.helper.Preconditions.checkState
import com.kylentt.musicplayer.BuildConfig
import com.kylentt.musicplayer.common.coroutines.CoroutineDispatchers
import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Lateinit val basically, any attempt to update the value is ignored
 * @param lock the Lock
 * @throws IllegalStateException on any attempt to get value when its not Initialized
 * @sample LateLazySample.testCase1
 */

class LateLazy<T>(lock: Any? = null) : ReadOnlyProperty<Any?, T>{
	private object EMPTY

	private val localLock: Any = lock ?: this

	@GuardedBy("lock")
	private var localValue: Any? = EMPTY

	private val value: T
		@Suppress("UNCHECKED_CAST") get() = localValue as T

	val isInitialized
		get() = localValue !== EMPTY

	fun initializeValue(lazyValue: () -> T): T {
		if (!isInitialized) sync {
			if (!isInitialized) localValue = lazyValue()
		}
		return value
	}

	override fun getValue(thisRef: Any?, property: KProperty<*>): T {
		if (!isInitialized) sync()
		return value
	}

	private fun sync(): Unit = sync { }
	private fun <T> sync(block: () -> T): T = synchronized(localLock) { block() }
}

object LateLazySample {

	fun runTestCase(times: Int = 5) {
		if (!BuildConfig.DEBUG) return
		repeat(times) {
			testCase1()
			testCase2()
		}
	}

	private fun testCase1() {
		val initializer = LateLazy<Any>()
		val myObject by initializer

		val any1 = Any()
		val any2 = Any()
		val any3 = Any()

		initializer.initializeValue { any1 }
		initializer.initializeValue { any2 }
		initializer.initializeValue { any3 }

		checkState(myObject === any1)
	}

	private fun testCase2() {
		val initializer = LateLazy<String>()
		val myObject by initializer

		val jobs = mutableListOf<String>()

		val scope = CoroutineScope(CoroutineDispatchers.DEFAULT.io)
		scope.launch {
			val c = async {
				val key = "c"
				delay(100)
				jobs.add(key)
				initializer.initializeValue { key }
			}
			val b = async {
				val key = "b"
				delay(100)
				jobs.add(key)
				initializer.initializeValue { key }
			}
			val a = async {
				val key = "a"
				delay(100)
				jobs.add(key)
				initializer.initializeValue { key }
			}

			a.await()
			b.await()
			c.await()

			checkState(myObject === jobs.first()) {
				"LateLazy validCase3 failed." +
					"\nresult: $myObject" +
					"\nexpected ${jobs.first()}" +
					"\nlist: $jobs"
			}
			Timber.d(
				"LateLazy validCase3 Success" +
					"\nresult: $myObject" +
					"\nlist: $jobs"
			)
			scope.cancel()
		}
	}
}
