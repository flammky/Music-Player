package com.flammky.musicplayer.ui.playbackcontrol

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.flammky.android.medialib.player.Player
import com.flammky.androidx.content.context.findActivity
import com.flammky.common.kotlin.comparable.clamp
import com.flammky.common.kotlin.comparable.clampPositive
import com.flammky.musicplayer.R
import com.flammky.musicplayer.base.compose.rememberLocalContextHelper
import com.flammky.musicplayer.media.playback.PlaybackConstants
import com.flammky.musicplayer.media.playback.PlaybackQueue
import com.flammky.musicplayer.playbackcontrol.ui.PlaybackControlTrackMetadata
import com.flammky.musicplayer.playbackcontrol.ui.PlaybackControlViewModel
import com.flammky.musicplayer.playbackcontrol.ui.PlaybackDetailPropertiesInfo
import com.flammky.musicplayer.playbackcontrol.ui.controller.PlaybackController
import com.flammky.musicplayer.playbackcontrol.ui.presenter.PlaybackObserver
import com.flammky.musicplayer.ui.Theme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.coroutines.coroutineContext
import kotlin.reflect.KProperty
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds

/**
 * @param showSelfState whether to show this composable
 */
@Composable
internal fun TransitioningPlaybackControl(
	modifier: Modifier = Modifier,
	showSelfState: State<Boolean>,
	dismiss: () -> Unit
) {
	val showSelf = showSelfState.value
	val savedTransitionedState = rememberSaveable { mutableStateOf(false) }
	// remember the first value we got from the saver
	val savedWasTransitionedState = remember { mutableStateOf(savedTransitionedState.value) }

	val localDismiss = remember(dismiss) {
		{
			savedTransitionedState.value = false
			dismiss()
		}
	}

	if (showSelf) {
		BackHandler(onBack = localDismiss)
		LockScreenOrientation(landscape = false)
	}

	val viewModel: PlaybackControlViewModel = viewModel()

	BoxWithConstraints(modifier = modifier) {

		Box(
			modifier = Modifier
				.fillMaxSize(fraction = if (showSelf) 1f else 0f)
				// Ensure that background contents are not clickable during transition
				.clickable(
					interactionSource = remember { MutableInteractionSource() },
					indication = null,
					onClick = {}
				),
		)

		// Think if there is somewhat better way to do this
		val transitionHeightState = updateTransition(targetState = showSelf, label = "")
			.animateDp(
				label = "Playback Control Transition",
				targetValueByState = { targetShowSelf ->
					if (targetShowSelf) maxHeight else 0.dp
				},
				transitionSpec = {
					remember(targetState) {
						tween(
							durationMillis = when {
								savedWasTransitionedState.value -> {
									savedWasTransitionedState.value = false
									0
								}
								targetState -> 250
								else -> 150
							},
							easing = if (targetState) FastOutSlowInEasing else LinearOutSlowInEasing
						)
					}
				}
			)

		NoInline {

			if (transitionHeightState.value == maxHeight) {
				savedTransitionedState.value = true
			}

			ContentTransition(
				heightTargetDp = maxHeight,
				heightDpState = transitionHeightState,
				viewModel = viewModel,
				dismiss = localDismiss
			)
		}
	}


}

@Composable
private fun ContentTransition(
	modifier: Modifier = Modifier,
	heightTargetDp: Dp,
	heightDpState: State<Dp>,
	viewModel: PlaybackControlViewModel,
	dismiss: () -> Unit
) {
	val transitionedState = remember { mutableStateOf(false) }

	val yOffsetState = heightDpState.derive { heightDp ->
		heightTargetDp - heightDp
	}

	// allows early return
	NoInlineBox(modifier) {

		// switch the state
		when (yOffsetState.value) {
			0.dp -> {
				// no offset, fully visible, transitioned
				transitionedState.value = true
			}
			heightTargetDp -> {
				// yOffset == heightTargetDp means that the layout is fully hidden,
				transitionedState.value = false
				// in that case remove content from the composition tree
				return@NoInlineBox
			}
			// else keep whatever it was
		}

		Content(
			modifier = modifier
				.fillMaxWidth()
				.height(heightTargetDp)
				.offset(0.dp, yOffsetState.value)
				.background(
					Theme
						.dayNightAbsoluteColor()
						.copy(alpha = 0.94f)
				),
			viewModel = viewModel,
			transitionedState = transitionedState,
			dismiss = dismiss
		)
	}
}

@Composable
private fun Content(
	modifier: Modifier,
	viewModel: PlaybackControlViewModel,
	transitionedState: State<Boolean>,
	dismiss: () -> Unit
) {
	val sessionController = playbackControllerAsState(viewModel = viewModel).value

	Box(modifier = modifier.fillMaxSize()) {

		val darkTheme = Theme.isDarkAsState().value

		val animatedAlphaState = animateFloatAsState(
			targetValue = if (transitionedState.value) 1f else 0f,
			animationSpec = tween(if (transitionedState.value && darkTheme) 150 else 0)
		)

		if (sessionController != null) {
			RadialPlaybackPaletteBackground(
				modifier = Modifier.alpha(animatedAlphaState.value),
				controller = sessionController,
				viewModel = viewModel
			)
		}

		NoInlineColumn(
			modifier = Modifier
				.fillMaxSize()
				.statusBarsPadding()
		) {
			DetailToolbar(
				modifier = Modifier.padding(top = 10.dp),
				controller = sessionController,
				dismiss = dismiss
			)
			if (sessionController != null) {
				DetailsContent(
					modifier = Modifier
						.padding(top = 20.dp)
						.alpha(animatedAlphaState.value),
					viewModel,
					sessionController
				)
			}
		}
	}
}

@Composable
private fun playbackControllerAsState(
	viewModel: PlaybackControlViewModel
): State<PlaybackController?> {
	val coroutineScope = rememberCoroutineScope()
	val idState = remember {
		mutableStateOf<String?>(viewModel.currentSessionID())
	}
	val controllerState = remember {
		mutableStateOf<PlaybackController?>(idState.value?.let { id ->
			viewModel.createController(id, coroutineScope.coroutineContext)
		})
	}
	val id = idState.value
	val controller = controllerState.value
	LaunchedEffect(key1 = null) {
		viewModel.observeCurrentSessionId().collect { idState.value = it }
	}
	DisposableEffect(key1 = id) {
		if (id == null) {
			return@DisposableEffect onDispose {  }
		}
		if (id == controller?.sessionID) {
			return@DisposableEffect onDispose { controller.dispose() }
		}
		val newController = viewModel.createController(id, coroutineScope.coroutineContext)
		controllerState.value = newController
		onDispose {
			newController.dispose()
		}
	}
	return controllerState
}

@Composable
private fun DetailToolbar(
	modifier: Modifier,
	controller: PlaybackController?,
	dismiss: () -> Unit
) {
	Box(modifier = modifier) {
		Column(
			modifier = Modifier
				.height(50.dp)
				.padding(vertical = 5.dp)
		) {
			Row(modifier = Modifier
				.height(40.dp)
				.fillMaxWidth()
				.padding(horizontal = 15.dp, vertical = 5.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				DismissAction(dismiss)
				Spacer(modifier = Modifier.weight(2f))
				if (controller != null) {
					Icon(
						modifier = Modifier.size(26.dp),
						painter = painterResource(id = R.drawable.more_vert_48px),
						contentDescription = "more",
						tint = Theme.dayNightAbsoluteContentColor()
					)
				}
			}
		}
	}
}

@Composable
private fun RowScope.DismissAction(
	dismiss: () -> Unit
) {
	val interactionSource = remember { MutableInteractionSource() }
	val size = if (interactionSource.collectIsPressedAsState().read()) 24.dp else 26.dp
	Box(
		modifier = Modifier
			.size(35.dp)
			.align(Alignment.CenterVertically)
			.clickable(interactionSource, null, onClick = dismiss)
	) {
		Icon(
			modifier = Modifier
				.align(Alignment.Center)
				.size(size),
			painter = painterResource(id = R.drawable.ios_glyph_expand_arrow_down_100),
			contentDescription = "close",
			tint = Theme.dayNightAbsoluteContentColor()
		)
	}
}



@Composable
private fun DetailsContent(
	modifier: Modifier,
	viewModel: PlaybackControlViewModel,
	playbackController: PlaybackController,
) {
	BoxWithConstraints(modifier = modifier) {
		val maxHeight = maxHeight
		val maxWidth = maxWidth
		Column(
			modifier = Modifier.fillMaxSize(),
			verticalArrangement = Arrangement.Top,
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			TracksPagerDisplay(
				viewModel = viewModel,
				controller = playbackController
			)
			CurrentTrackPlaybackDescriptions(
				modifier = Modifier.padding(top = 15.dp),
				viewModel = viewModel
			)
			PlaybackControlProgressSeekbar(
				modifier = Modifier.padding(top = 15.dp),
				maxWidth = maxWidth,
				controller = playbackController,
			)
			PlaybackControls(
				modifier = Modifier.padding(top = 5.dp),
				viewModel
			)
		}
	}
}

@Composable
private fun TracksPagerDisplay(
	viewModel: PlaybackControlViewModel,
	controller: PlaybackController
) {
	val observerState = remember { mutableStateOf<PlaybackObserver?>(null) }
	val queueState = remember { mutableStateOf(PlaybackQueue.UNSET) }

	DisposableEffect(key1 = controller) {
		val observer = controller.createPlaybackObserver()
		observerState.value = observer
		queueState.value = PlaybackQueue.UNSET
		onDispose { observer.dispose() }
	}

	NoInlineBox(
		modifier = Modifier
			.fillMaxWidth()
			.height(300.dp)
	) {
		val observer = observerState.value ?: return@NoInlineBox

		val coroutineScope = rememberCoroutineScope()

		DisposableEffect(key1 = observer) {
			observer.createQueueCollector(coroutineScope.coroutineContext)
				.apply {
					coroutineScope.launch {
						startCollect().join()
						queueStateFlow.collect { queueState.value = it }
					}
				}
			onDispose { observer.dispose() }
		}

		TracksPager(
			queueState = queueState,
			metadataStateForId = { id ->
				viewModel.observeMetadata(id).collectAsState()
			},
			seekIndex = { index ->
				coroutineContext.ensureActive()
				val result = controller.requestSeekAsync(index, Duration.ZERO, coroutineContext).await()
				result.eventDispatch?.join()
				result.success
			}
		)
	}
}

@Composable
private fun RadialPlaybackPaletteBackground(
	modifier: Modifier = Modifier,
	artState: State<Any?>
) {
	val absoluteBackgroundColor = Theme.dayNightAbsoluteColor()
	val backgroundColor = Theme.backgroundColor()
	val radialPaletteColor = remember { mutableStateOf(backgroundColor) }

	val compositeBase =
		if (isSystemInDarkTheme()) {
			backgroundColor
		} else {
			absoluteBackgroundColor
		}

	BoxWithConstraints(
		modifier = modifier,
	) {
		val color = animateColorAsState(
			targetValue = radialPaletteColor.read(),
			animationSpec = tween(500)
		).value
		val brush = remember(color) {
			Brush.radialGradient(
				colors = listOf(
					color.copy(alpha = 0.55f).compositeOver(compositeBase),
					color.copy(alpha = 0.45f).compositeOver(compositeBase),
					color.copy(alpha = 0.35f).compositeOver(compositeBase),
					color.copy(alpha = 0.2f).compositeOver(compositeBase),
					color.copy(alpha = 0.15f).compositeOver(compositeBase),
					color.copy(alpha = 0.1f).compositeOver(compositeBase),
					color.copy(alpha = 0.05f).compositeOver(compositeBase),
					color.copy(alpha = 0.0f).compositeOver(compositeBase)
				),
				center = Offset(constraints.maxWidth.toFloat() / 2, constraints.maxHeight.toFloat() / 3.5f),
				radius = constraints.maxWidth.toFloat() * 0.9f
			)
		}
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(brush)
		)
	}
	val darkTheme = isSystemInDarkTheme()
	val artwork = artState.value
	LaunchedEffect(artwork) {
		val color = if (artwork is Bitmap) {
			withContext(Dispatchers.IO) {
				val palette = Palette.from(artwork).maximumColorCount(16).generate()
				ensureActive()
				val argb = palette.run {
					if (darkTheme) {
						getDarkVibrantColor(getMutedColor(getDominantColor(-1)))
					} else {
						getVibrantColor(getLightMutedColor(getDominantColor(-1)))
					}
				}
				if (argb != -1) {
					Color(argb)
				} else {
					backgroundColor
				}
			}
		} else {
			backgroundColor
		}
		if (isActive) radialPaletteColor.overwrite(color)
	}
}

@Composable
private fun RadialPlaybackPaletteBackground(
	modifier: Modifier = Modifier,
	controller: PlaybackController,
	viewModel: PlaybackControlViewModel,
) {
	val coroutineScope = rememberCoroutineScope()

	val observerState = remember(controller) {
		mutableStateOf<PlaybackObserver?>(null)
	}

	val observer = observerState.value
	val idState = remember(observer) { mutableStateOf("") }
	val id = idState.value
	val metadataStateFlow = remember(id) {
		viewModel.observeMetadata(id)
	}
	RadialPlaybackPaletteBackground(
		modifier,
		metadataStateFlow.collectAsState().rememberDerive(calculation = { it.artwork })
	)
	DisposableEffect(key1 = controller) {
		val newObserver = controller.createPlaybackObserver(coroutineScope.coroutineContext)
		observerState.value = newObserver
		onDispose { newObserver.dispose() }
	}
	DisposableEffect(key1 = observer) {
		if (observer == null) return@DisposableEffect onDispose {  }
		val queueCollector = observer.createQueueCollector()
			.apply {
				coroutineScope.launch {
					startCollect().join()
					queueStateFlow.collect { queue ->
						idState.value = queue.list.getOrNull(queue.currentIndex) ?: ""
					}
				}
			}
		onDispose { queueCollector.dispose() }
	}
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun TracksPager(
	queueState: State<PlaybackQueue>,
	metadataStateForId: @Composable (String) -> State<PlaybackControlTrackMetadata>,
	seekIndex: suspend (Int) -> Boolean,
) {
	val queueOverrideAmountState = remember { mutableStateOf(0) }
	val queueOverrideState = remember { mutableStateOf<PlaybackQueue?>(null) }
	val actualQueueState = queueState.rememberDerive(calculation = { queueOverrideState.value ?: it })
	val queue = actualQueueState.value
	val queueIndex = queue.currentIndex
	val queueList = queue.list

	remember(queueIndex) {
		Timber.d("PagerListenPageChange recomposed with new index: $queueIndex")
		null
	}

	remember(queueList.size, queueIndex) {
		if (queueList.isEmpty()) {
			require( queueIndex == PlaybackConstants.INDEX_UNSET) {
				"empty QueueList must be followed by an invalid Index, queue=$queueList index=$queueIndex"
			}
			null
		} else {
			requireNotNull(queueList.getOrNull(queueIndex)) {
				"non-empty QueueList must be followed by a valid index"
			}
		}
	} ?: return

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(300.dp),
	) {
		val pagerState = rememberPagerState(actualQueueState.value.currentIndex.clampPositive())

		HorizontalPager(
			modifier = Modifier.fillMaxSize(),
			state = pagerState,
			count = queueList.size
		) {
			TracksPagerItem(metadataStateForId(queueList[it]))
		}

		val touched = remember { mutableStateOf(false) }

		PagerListenMediaIndexChange(
			indexState = queueState.rememberDerive { it.currentIndex },
			pagerState = pagerState,
			overrideState = queueOverrideState.rememberDerive(calculation = { it != null }),
			onScroll = { touched.overwrite(false) }
		)

		PagerListenUserDrag(
			pagerState = pagerState
		) {
			touched.overwrite(true)
		}

		PagerListenPageChange(
			pagerState = pagerState,
			touchedState = touched,
			shouldSeekIndex = { index ->
				queueOverrideState.value = queue.copy(currentIndex = index)
				true
			},
			seekIndex = { index ->
				Timber.d("PagerListenPageChange seek to $index")
				queueOverrideAmountState.value++
				runCatching {
					seekIndex(index)
				}.isSuccess.also {
					if (--queueOverrideAmountState.value == 0) queueOverrideState.value = null
					Timber.d("PagerListenPageChange seek to $index done, ${queueOverrideAmountState.value}, ${queueOverrideState.value}")
				}
			}
		)
	}
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun PagerListenMediaIndexChange(
	indexState: State<Int>,
	pagerState: PagerState,
	overrideState: State<Boolean>,
	onScroll: suspend () -> Unit
) {
	val currentIndex = indexState.value
	val currentOverride = overrideState.value
	LaunchedEffect(
		key1 = currentIndex,
		key2 = currentOverride
	) {
		if (currentOverride) return@LaunchedEffect
		// TODO: Velocity check, if the user is dragging the pager but aren't moving
		// or if the user drag velocity will ends in another page
		if (currentIndex >= 0 &&
			pagerState.currentPage != currentIndex &&
			!pagerState.isScrollInProgress
		) {
			onScroll()
			pagerState.animateScrollToPage(currentIndex)
		}
	}
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun PagerListenUserDrag(
	pagerState: PagerState,
	onUserInteract: () -> Unit
) {
	val dragged = pagerState.interactionSource.collectIsDraggedAsState()
	LaunchedEffect(
		dragged.value
	) {
		if (dragged.value) onUserInteract()
	}
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun PagerListenPageChange(
	pagerState: PagerState,
	touchedState: State<Boolean>,
	shouldSeekIndex: (Int) -> Boolean,
	seekIndex: suspend (Int) -> Boolean
) {
	val dragging by pagerState.interactionSource.collectIsDraggedAsState()
	val touched by touchedState
	val coroutineScope = rememberCoroutineScope()
	val page = pagerState.currentPage
	val rememberedPageState = remember {
		mutableStateOf(page)
	}
	remember(page, dragging, touched) {
		if (touched && !dragging && page != rememberedPageState.value) {
			if (shouldSeekIndex(page)) {
				rememberedPageState.value = page
				coroutineScope.launch { seekIndex(page) }
			}
		}
		null
	}
}

private fun Int.inRangeSpread(a: Int, amount: Int): Boolean {
	return (this in a - amount.. a + amount)
}

@Composable
private fun TracksPagerItem(
	metadataState: State<PlaybackControlTrackMetadata>
) {
	val art = metadataState.value.artwork
	Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
		val context = LocalContext.current
		val req = remember(art) {
			ImageRequest.Builder(context)
				.data(art)
				.build()
		}
		AsyncImage(
			modifier = Modifier
				.fillMaxWidth(0.8f)
				.height(280.dp)
				.align(Alignment.Center),
			model = req,
			contentDescription = "art",
			contentScale = ContentScale.Crop
		)
	}
}

@Composable
private fun CurrentTrackPlaybackDescriptions(
	modifier: Modifier = Modifier,
	viewModel: PlaybackControlViewModel
) {
	PlaybackDescription(
		modifier = modifier,
		metadataState = viewModel.currentMetadataStateFlow.collectAsState()
	)
}

@Composable
private fun PlaybackDescription(
	modifier: Modifier,
	metadataState: State<PlaybackControlTrackMetadata>
) {
	val metadata = metadataState.value
	Timber.d("PlaybackDescription metadataState: $metadata")
	Box(modifier = modifier.fillMaxWidth()) {
		Column(
			modifier = Modifier
				.fillMaxWidth(0.8f)
				.align(Alignment.Center),
			horizontalAlignment = Alignment.Start,
			verticalArrangement = Arrangement.spacedBy(5.dp)
		) {
			val titleState = metadataState.rememberDerive { metadata -> metadata.title ?: "" }
			val subtitleState = metadataState.rememberDerive { metadata -> metadata.subtitle ?: "" }
			PlaybackDescriptionTitle(textState = titleState)
			PlaybackDescriptionSubtitle(textState = subtitleState)
		}
	}
}

@Composable
private fun PlaybackDescriptionTitle(textState: State<String>) {
	Text(
		text = textState.read(),
		color = Theme.dayNightAbsoluteContentColor(),
		style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
		overflow = TextOverflow.Ellipsis
	)
}

@Composable
private fun PlaybackDescriptionSubtitle(textState: State<String>) {
	Text(
		text = textState.read(),
		color = Theme.dayNightAbsoluteContentColor(),
		style = MaterialTheme.typography.titleMedium,
		overflow = TextOverflow.Ellipsis
	)
}

@Composable
private fun PlaybackControls(
	modifier: Modifier = Modifier,
	viewModel: PlaybackControlViewModel
) {
	Box(modifier = modifier) {
		PlaybackControlButtons(
			viewModel.playbackPropertiesStateFlow.collectAsState(),
			play = viewModel::playWhenReady,
			pause = viewModel::pause,
			next = viewModel::seekNext,
			previous = viewModel::seekPrevious,
			enableRepeat = viewModel::enableRepeatMode,
			enableRepeatAll = viewModel::enableRepeatAllMode,
			disableRepeat = viewModel::disableRepeatMode,
			enableShuffle = viewModel::enableShuffleMode,
			disableShuffle = viewModel::disableShuffleMode
		)
	}
}

@Composable
private fun PlaybackControlButtons(
	propertiesInfoState: State<PlaybackDetailPropertiesInfo>,
	play: () -> Unit,
	pause: () -> Unit,
	next: () -> Unit,
	previous: () -> Unit,
	enableRepeat: () -> Unit,
	enableRepeatAll: () -> Unit,
	disableRepeat: () -> Unit,
	enableShuffle: () -> Unit,
	disableShuffle: () -> Unit,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth(0.9f)
			.height(40.dp),
		horizontalArrangement = Arrangement.SpaceAround,
		verticalAlignment = Alignment.CenterVertically
	) {
		// Shuffle
		NoInlineBox(
			modifier = Modifier.size(40.dp)
		) {
			val interactionSource = remember { MutableInteractionSource() }
			val size by animateDpAsState(
				targetValue = if (interactionSource.collectIsPressedAsState().read()) 27.dp else 30.dp
			)
			val shuffleOn = propertiesInfoState.rememberDerive(calculation = { it.shuffleOn }).value
			Icon(
				modifier = Modifier
					.size(size)
					.align(Alignment.Center)
					.clickable(
						interactionSource = interactionSource,
						indication = null,
						onClick = { if (shuffleOn) disableShuffle() else enableShuffle() }
					)
				,
				painter = painterResource(id = R.drawable.ios_glyph_shuffle_100),
				contentDescription = "shuffle",
				tint = if (shuffleOn)
					Theme.dayNightAbsoluteContentColor()
				else
					Color(0xFF787878)
			)
		}

		// Previous
		NoInlineBox(modifier = Modifier.size(40.dp)) {
			// later check for command availability
			val interactionSource = remember { MutableInteractionSource() }
			val size by animateDpAsState(
				targetValue = if (interactionSource.collectIsPressedAsState().read()) 32.dp else 35.dp
			)
			Icon(
				modifier = Modifier
					.size(size)
					.align(Alignment.Center)
					.clickable(
						interactionSource = interactionSource,
						indication = null,
						onClick = { previous() }
					)
				,
				painter = painterResource(id = R.drawable.ios_glyph_seek_previos_100),
				contentDescription = "previous",
				tint = Theme.dayNightAbsoluteContentColor()
			)
		}

		// Play / Pause
		NoInlineBox(modifier = Modifier.size(40.dp)) {
			// later check for command availability
			val rememberPlayPainter = painterResource(id = R.drawable.ios_glyph_play_100)
			val rememberPausePainter = painterResource(id = R.drawable.ios_glyph_pause_100)
			val interactionSource = remember { MutableInteractionSource() }
			val size by animateDpAsState(
				targetValue = if (interactionSource.collectIsPressedAsState().read()) 37.dp else 40.dp
			)
			val playWhenReady = propertiesInfoState.rememberDerive(calculation = { it.playWhenReady }).value
			Icon(
				modifier = Modifier
					.size(size)
					.align(Alignment.Center)
					.clickable(
						interactionSource = interactionSource,
						indication = null,
						onClick = { if (playWhenReady) pause() else play() }
					)
				,
				painter = if (playWhenReady)
					rememberPausePainter
				else
					rememberPlayPainter
				,
				contentDescription = "play",
				tint = Theme.dayNightAbsoluteContentColor()
			)
		}

		// Next
		NoInlineBox(modifier = Modifier.size(40.dp)) {
			// later check for command availability

			val interactionSource = remember { MutableInteractionSource() }
			val size by animateDpAsState(
				targetValue = if (interactionSource.collectIsPressedAsState().read()) 32.dp else 35.dp
			)
			val hasNext = propertiesInfoState.rememberDerive(calculation = { it.hasNextMediaItem }).value
			Icon(
				modifier = Modifier
					.size(size)
					.align(Alignment.Center)
					.clickable(
						interactionSource = interactionSource,
						indication = null,
						onClick = { if (hasNext) next() }
					)
				,
				painter = painterResource(id = R.drawable.ios_glyph_seek_next_100),
				contentDescription = "next",
				tint = if (hasNext)
					Theme.dayNightAbsoluteContentColor()
				else
					Color(0xFF787878)
			)
		}

		// Repeat
		NoInlineBox(modifier = Modifier.size(40.dp)) {
			val interactionSource = remember { MutableInteractionSource() }
			val size by animateDpAsState(
				targetValue = if (interactionSource.collectIsPressedAsState().read()) 27.dp else 30.dp
			)
			val repeatMode = propertiesInfoState.rememberDerive(calculation = { it.repeatMode }).value
			Icon(
				modifier = Modifier
					.size(size)
					.align(Alignment.Center)
					.clickable(
						interactionSource = interactionSource,
						indication = null,
						onClick = {
							when (repeatMode) {
								Player.RepeatMode.OFF -> enableRepeat()
								Player.RepeatMode.ONE -> enableRepeatAll()
								Player.RepeatMode.ALL -> disableRepeat()
							}
						}
					),
				painter = when (repeatMode) {
					Player.RepeatMode.OFF -> painterResource(id = R.drawable.ios_glyph_repeat_100)
					Player.RepeatMode.ONE -> painterResource(id = R.drawable.ios_glyph_repeat_one_100)
					Player.RepeatMode.ALL -> painterResource(id = R.drawable.ios_glyph_repeat_100)
				},
				contentDescription = "repeat",
				tint = if (repeatMode == Player.RepeatMode.OFF)
					Color(0xFF787878)
				else
					Theme.dayNightAbsoluteContentColor()
			)
		}
	}
}

@Composable
private fun PlaybackControlProgressSeekbar(
	modifier: Modifier = Modifier,
	maxWidth: Dp,
	controller: PlaybackController
) {
	val playbackObserverState = remember(controller) {
		mutableStateOf<PlaybackObserver?>(null)
	}

	val coroutineScope = rememberCoroutineScope()

	DisposableEffect(key1 = controller) {
		val observer = controller.createPlaybackObserver(coroutineScope.coroutineContext)
		playbackObserverState.value = observer
		onDispose { observer.dispose() }
	}

	val playbackObserver = playbackObserverState.value
		?: return Box(modifier = Modifier
			.height(35.dp)
			.then(modifier.fillMaxWidth()))

	val sliderPositionCollectorReady = remember {
		mutableStateOf(false)
	}
	val sliderTextPositionCollectorReady = remember {
		mutableStateOf(false)
	}
	val sliderDurationCollectorReady = remember {
		mutableStateOf(false)
	}
	val sliderReady = remember {
		mutableStateOf(false)
	}
	val sliderTextReady = remember {
		mutableStateOf(false)
	}
	val sliderWidth = remember(maxWidth) {
		maxWidth * 0.85f
	}

	val sliderPositionCollector = remember {
		playbackObserver.createProgressionCollector(
			collectorContext = coroutineScope.coroutineContext,
			includeEvent = true,
		).apply {
			coroutineScope.launch {
				startCollectPosition().join()
				sliderPositionCollectorReady.value = true
			}
			setIntervalHandler { _, progress, duration, speed ->
				if (progress == ZERO || duration == ZERO || speed == 0f) {
					null
				} else {
					(duration.inWholeMilliseconds / sliderWidth.value / speed).toLong()
						.takeIf { it > 100 }?.milliseconds
						?: PlaybackConstants.DURATION_UNSET
				}.also {
					Timber.d("Playback_Slider_Debug: intervalHandler($it) param: $progress $duration $speed")
				}
			}
		}
	}

	val sliderTextPositionCollector = remember {
		playbackObserver.createProgressionCollector(
			collectorContext = coroutineScope.coroutineContext,
			includeEvent = true,
		).apply {
			coroutineScope.launch {
				startCollectPosition().join()
				sliderTextPositionCollectorReady.value = true
			}
		}
	}

	val durationCollector = remember {
		playbackObserver.createDurationCollector(
			collectorContext = coroutineScope.coroutineContext
		).apply {
			coroutineScope.launch {
				startCollect().join()
				sliderDurationCollectorReady.value = true
			}
		}
	}

	val ready = remember {
		derivedStateOf { sliderReady.value && sliderTextReady.value }
	}

	val alpha = if (ready.value) 1f else 0f

	if (sliderPositionCollectorReady.value && sliderDurationCollectorReady.value) {
		sliderReady.value = true
	}

	if (sliderTextPositionCollectorReady.value && sliderDurationCollectorReady.value) {
		sliderTextReady.value = true
	}

	if (!sliderReady.value || !sliderTextReady.value) {
		return
	}

	val sliderTextPositionState = sliderTextPositionCollector.positionStateFlow.collectAsState()
	val sliderPositionState = sliderPositionCollector.positionStateFlow.collectAsState()
	val durationState = durationCollector.durationStateFlow.collectAsState()
	val interactionSource = remember { MutableInteractionSource() }
	val draggedState = interactionSource.collectIsDraggedAsState()
	val pressedState = interactionSource.collectIsPressedAsState()

	val changedValueState = remember {
		// the Slider should constraint it to 0, consider change it to nullable Float
		mutableStateOf(-1f)
	}
	val seekRequestCountState = remember {
		mutableStateOf(0)
	}

	// consume the `animate` composable, it will recreate a new state so it will not animate
	// should the first visible progress be animated ?
	val consumeAnimationState = remember { mutableStateOf(1) }

	NoInlineBox(modifier = modifier.fillMaxWidth()) {

		Column(
			modifier = Modifier
				.align(Alignment.Center)
				.alpha(alpha),
			horizontalAlignment = Alignment.CenterHorizontally
		) {

			val seekRequestCount = seekRequestCountState.value
			val consumeAnimationCount = consumeAnimationState.value

			require(seekRequestCount == 0 || consumeAnimationCount > 0) {
				"seekRequest should be followed by consumeAnimation"
			}

			val position by sliderPositionState
			val duration by durationState

			val sliderValue = when {
				draggedState.value or pressedState.value or (changedValueState.value >= 0) -> {
					changedValueState.value
				}
				consumeAnimationState.value > 0 -> {
					consumeAnimationState.value = 0
					(position / duration).toFloat().clamp(0f, 1f)
				}
				else -> {
					val clampedProgress = (position / duration).toFloat().clamp(0f, 1f)
					// should snap
					val tweenDuration = if (clampedProgress == 1f || clampedProgress == 0f) 0 else 100
					animateFloatAsState(
						targetValue = clampedProgress,
						animationSpec = tween(tweenDuration)
					).value
				}
			}

			Slider(
				modifier = Modifier
					.width(sliderWidth)
					.height(16.dp),
				value = sliderValue,
				// unfortunately There's no guarantee these 2 will be called in order
				onValueChange = { value ->
					Timber.d("Slider_DEBUG: onValueChange: $value")
					changedValueState.value = value
				},
				onValueChangeFinished = {
					Timber.d("Slider_DEBUG: onValueChangeFinished: ${changedValueState.value}")
					changedValueState.value.takeIf { it >= 0 }?.let {
						seekRequestCountState.value++
						consumeAnimationState.value++
						coroutineScope.launch {
							val seekTo = (duration.inWholeMilliseconds * it).toLong().milliseconds
							controller.requestSeekAsync(seekTo, coroutineContext).await().eventDispatch?.join()
							if (--seekRequestCountState.value == 0) {
								changedValueState.value = -1f
							}
						}
						return@Slider
					}
					if (seekRequestCountState.value == 0) changedValueState.value = -1f
				},
				interactionSource = interactionSource,
				trackHeight = 6.dp,
				thumbSize = 12.dp,
				colors = SliderDefaults.colors(
					activeTrackColor = Theme.dayNightAbsoluteContentColor(),
					thumbColor = Theme.dayNightAbsoluteContentColor()
				)
			)
			Row(
				modifier = Modifier
					.width(maxWidth * 0.85f - 14.dp)
					.padding(top = 3.dp),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.SpaceBetween
			) {
				PlaybackProgressSliderText(
					progressState = sliderTextPositionState.rememberDerive(
						calculation = { raw ->
							if (changedValueState.value >= 0) {
								changedValueState.value
							} else {
								raw.inWholeMilliseconds / durationState.value.inWholeMilliseconds.toFloat()
							}
						}
					),
					durationState = durationState
				)
			}
		}
	}
}

@Composable
private fun NoInline(content: @Composable () -> Unit) = content()

@Composable
private fun <C> C.NoInline(content: @Composable C.() -> Unit) = content()

/**
 * non-Inlined box for easier recomposition control without introducing a function
 * and reducing possibility of Imbalance error
 */
@Composable
private fun NoInlineBox(
	modifier: Modifier = Modifier
) = Box(modifier)

/**
 * non-Inlined box for easier recomposition control without introducing a function
 * and reducing possibility of Imbalance error
 */
@Composable
private fun NoInlineBox(
	modifier: Modifier = Modifier,
	contentAlignment: Alignment = Alignment.TopStart,
	propagateMinConstraints: Boolean = false,
	content: @Composable BoxScope.() -> Unit
) = Box(modifier, contentAlignment, propagateMinConstraints, content)

/**
 * non-Inlined column for easier recomposition control without introducing additional composable function
 * and reducing possibility of Imbalance error
 */
@Composable
private fun NoInlineColumn(
	modifier: Modifier = Modifier,
	verticalArrangement: Arrangement.Vertical = Arrangement.Top,
	horizontalAlignment: Alignment.Horizontal = Alignment.Start,
	content: @Composable ColumnScope.() -> Unit
) = Column(modifier, verticalArrangement, horizontalAlignment, content)

/**
 * non-Inlined row for easier recomposition control without introducing additional composable function
 * and reducing possibility of Imbalance error
 */
@Composable
private fun NoInlineRow(
	modifier: Modifier = Modifier,
	horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
	verticalAlignment: Alignment.Vertical = Alignment.Top,
	content: @Composable RowScope.() -> Unit
) = Row(modifier, horizontalArrangement, verticalAlignment, content)

@Composable
private fun RowScope.PlaybackProgressSliderText(
	progressState: State<Float>,
	durationState: State<Duration>
) {

	NoInline {
		val duration = durationState.value
		val progress = progressState.value
		val formattedProgress: String = remember(progress, duration) {
			val seconds =
				if (duration.isNegative()) 0
				else (duration.inWholeSeconds * progress).toLong()
			if (seconds > 3600) {
				String.format(
					"%02d:%02d:%02d",
					seconds / 3600,
					seconds % 3600 / 60,
					seconds % 60
				)
			} else {
				String.format(
					"%02d:%02d",
					seconds / 60,
					seconds % 60
				)
			}
		}

		Text(
			text = formattedProgress,
			style = MaterialTheme.typography.bodySmall
		)
	}

	NoInline {
		val duration = durationState.value
		val formattedDuration: String = remember(duration) {
			val seconds =
				if (duration.isNegative() || duration.isInfinite()) 0
				else duration.inWholeSeconds
			if (seconds > 3600) {
				String.format(
					"%02d:%02d:%02d",
					seconds / 3600,
					seconds % 3600 / 60,
					seconds % 60
				)
			} else {
				String.format(
					"%02d:%02d",
					seconds / 60,
					seconds % 60
				)
			}
		}
		Text(
			text = formattedDuration,
			style = MaterialTheme.typography.bodySmall
		)
	}
}


enum class Visibility {
	VISIBLE,
	GONE
}

private inline val Visibility.isVisible
	get() = this == Visibility.VISIBLE

@Composable
private fun LockScreenOrientation(landscape: Boolean) {
	val activity = LocalContext.current.findActivity()
		?: error("cannot Lock Screen Orientation, LocalContext is not an Activity")
	DisposableEffect(key1 = Unit) {
		val original = activity.requestedOrientation
		activity.requestedOrientation =
			if (landscape) {
				ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
			} else {
				ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
			}
		onDispose { activity.requestedOrientation = original }
	}
}

@Composable
private fun verticalLayoutGuard(): State<Boolean> {
	val contextHelper = rememberLocalContextHelper()
	val verticalLayoutState = remember { mutableStateOf(false) }
	verticalLayoutState.overwrite(contextHelper.configurations.isOrientationPortrait())
	return verticalLayoutState
}

@Composable
private fun AssertVerticalOrientation(lazyMessage: (Int) -> Any = {}) {
	val contextHelper = rememberLocalContextHelper()
	check(contextHelper.configurations.isOrientationPortrait()) {
		val int = contextHelper.configurations.orientationInt
		"AssertVerticalLayout detected non-portrait orientation: ${int}\n" +
			"msg: ${lazyMessage(int)}"
	}
	BoxWithConstraints {
		check(maxHeight >= maxWidth)
	}
}

@Composable
private fun <T, R> State<T>.rememberDerive(calculation: (T) -> R): State<R> {
	return remember { derive(calculation) }
}

private fun <T, R> State<T>.derive(calculation: (T) -> R): State<R> {
	return derivedStateOf { calculation(read()) }
}

// is explicit read like this better ?
@Suppress("NOTHING_TO_INLINE")
private inline fun <T> State<T>.read(): T {
	return this.value
}

// is explicit write like this better ?
@Suppress("NOTHING_TO_INLINE")
private inline fun <T> MutableState<T>.overwrite(value: T) {
	this.value = value
}

@Suppress("NOTHING_TO_INLINE")
private inline fun <T> MutableState<T>.rewrite(value: (T) -> T) {
	this.value = value(this.value)
}

@SuppressLint("BanParcelableUsage", "ParcelCreator")
private class ValueWrapper <T> (
	@get:Synchronized
	@set:Synchronized
	var value: T
) : Parcelable {
	override fun describeContents(): Int = 0

	override fun writeToParcel(p0: Parcel, p1: Int) {
		p0.writeValue(value)
	}
}

private operator fun <T> ValueWrapper<T>.getValue(receiver: Any?, property: KProperty<*>): T = value
private operator fun <T> ValueWrapper<T>.setValue(receiver: Any?, property: KProperty<*>, value: T) {
	this.value = value
}

private class SeekRequest(
	val position: Long,
)