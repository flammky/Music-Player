package com.flammky.musicplayer.core.app.dependency

import android.app.NotificationManager
import android.content.Context
import coil.Coil
import com.flammky.android.kotlin.coroutine.AndroidCoroutineDispatchers
import com.flammky.android.app.coroutines.AppScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoilModule {

	@Provides
	@Singleton
	fun provideImageLoader(@ApplicationContext context: Context) = Coil.imageLoader(context)
}

@Module
@InstallIn(SingletonComponent::class)
object CoroutineModule {

	@Provides
	@Singleton
	fun provideAppDispatchers(): com.flammky.android.kotlin.coroutine.AndroidCoroutineDispatchers = com.flammky.android.kotlin.coroutine.AndroidCoroutineDispatchers.DEFAULT

	@Provides
	@Singleton
	fun provideAppScope(dispatchers: com.flammky.android.kotlin.coroutine.AndroidCoroutineDispatchers): AppScope =
		with(dispatchers) {
			AppScope(
				ioScope = CoroutineScope(context = io + SupervisorJob()),
				mainScope = CoroutineScope(context = main + SupervisorJob()),
				computationScope = CoroutineScope(context = computation + SupervisorJob()),
				immediateScope = CoroutineScope(context = mainImmediate + SupervisorJob()),
				unconfinedScope = CoroutineScope(context = unconfined + SupervisorJob())
			)
		}
}


@Module
@InstallIn(SingletonComponent::class)
object SystemServiceModule {

	@Provides
	@Singleton
	fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager {
		return context.getSystemService(NotificationManager::class.java)
	}
}
