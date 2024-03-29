package dev.dexsr.klio.player.android.presentation.root.bw

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flammky.musicplayer.base.auth.AuthService
import com.flammky.musicplayer.base.user.User
import com.flammky.musicplayer.player.presentation.main.PlaybackControlViewModel
import com.flammky.musicplayer.player.presentation.root.main.*
import dev.dexsr.klio.player.android.presentation.root.main.PlaybackControlScreenState

@Composable
fun OldRootPlaybackControlScreen(
    state: PlaybackControlScreenState
) {
    val userState = remember {
        mutableStateOf<User?>(null)
    }
    LaunchedEffect(key1 = Unit, block = {
        AuthService.get().observeCurrentUser().collect { userState.value = it }
    })
    userState.value?.let { user ->
        val vm = viewModel<PlaybackControlViewModel>()
        val oldState = remember(user, vm) {
            RootPlaybackControlState(user, vm)
        }.apply {

        }
        val cord = remember(oldState) {
            PlaybackControlCoordinator(oldState)
        }
        cord.ComposeLayout(
            screen = @Composable {
                com.flammky.musicplayer.player.presentation.root.main.PlaybackControlScreen(
                    state = rememberPlaybackControlScreenState(
                        intents = remember(intentsKey) {
                            PlaybackControlScreenIntents(
                                requestSeekNextAsync = requestSeekNextAsync,
                                requestSeekPreviousAsync = requestSeekPreviousAsync,
                                requestSeekNextWithExpectAsync = requestSeekNextWithExpectAsync,
                                requestSeekPreviousWithExpectAsync = requestSeekPreviousWithExpectAsync,
                                requestSeekAsync = requestSeekAsync,
                                requestSeekPositionAsync = requestSeekPositionAsync,
                                requestMoveQueueItemAsync = requestMoveQueueItemAsync,
                                requestPlayAsync = requestPlayAsync,
                                requestPauseAsync = requestPauseAsync,
                                requestToggleRepeatAsync = requestToggleRepeatAsync,
                                requestToggleShuffleAsync = requestToggleShuffleAsync,
                                dismiss = dismiss
                            )
                        },
                        source = remember(sourceKey) {
                            PlaybackControlScreenDataSource(
                                user = user,
                                observeQueue = observeQueue,
                                observePlaybackProperties = observePlaybackProperties,
                                observeDuration = observeDuration,
                                observePositionWithIntervalHandle = observePositionWithIntervalHandle,
                                observeTrackMetadata = observeTrackMetadata,
                                observeArtwork = observeArtwork
                            )
                        },
                        composeBackPressRegistry = state.backPressRegistry
                    ).apply {
                        remember(showSelf) {
                            if (showSelf) {
                                requestShow()
                            } else {
                                if (requestHide()) state.hide()
                            }
                        }
                    }
                )
            },
            compact = {
                LaunchedEffect(key1 = state.showSelf, block = {
                    if (state.showSelf) onBaseClicked()
                })
            }
        )
    }
}