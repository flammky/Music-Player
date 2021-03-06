package com.kylentt.mediaplayer.ui.compose

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import com.kylentt.musicplayer.core.app.delegates.device.DeviceWallpaper
import com.kylentt.mediaplayer.ui.activity.mainactivity.compose.LifeCycleExtension.RecomposeOnEvent

@Composable
fun rememberWallpaperBitmapAsState(): State<Bitmap?> {
    val wallpaper = remember { mutableStateOf<Bitmap?>(null) }

    LocalLifecycleOwner.current.lifecycle.RecomposeOnEvent(
        onEvent = Lifecycle.Event.ON_START
    ) { _ ->

        val bitmap = DeviceWallpaper.getBitmap()

        LaunchedEffect(key1 = bitmap.hashCode()) {
            wallpaper.value = bitmap
        }
    }

    return wallpaper
}

@Composable
fun rememberWallpaperDrawableAsState(
    context: Context = LocalContext.current
): State<Drawable?> {
    val value = rememberWallpaperBitmapAsState().value
    val block: () -> State<Drawable?> = { mutableStateOf(BitmapDrawable(context.resources, value)) }
    return block()
}


