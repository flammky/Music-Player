package com.flammky.musicplayer.ui.main.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flammky.mediaplayer.domain.viewmodels.MainViewModel
import com.flammky.mediaplayer.domain.viewmodels.MediaViewModel
import com.flammky.mediaplayer.ui.activity.mainactivity.compose.MainActivityRoot
import com.flammky.musicplayer.ui.main.MainActivity
import com.flammky.musicplayer.ui.main.compose.entry.MainEntry
import com.flammky.musicplayer.ui.main.compose.theme.MainMaterial3Theme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun MainActivity.ComposeContent() {
	MainTheme() {
		MainEntry {
			val mainViewModel: MainViewModel = viewModel()
			val mediaViewModel: MediaViewModel = viewModel()
			mainViewModel.readPermissionGranted()
			mediaViewModel.readPermissionGranted()
			MainActivityRoot(mainViewModel.appSettings.collectAsState().value)
		}
	}
}

@Composable
private fun MainTheme(
	content: @Composable () -> Unit
) = MainMaterial3Theme { MainSurface(content = content) }

@Composable
private fun MainSurface(content: @Composable () -> Unit) {
	Surface(
		modifier = Modifier.fillMaxSize(),
		content = content
	)
}

