package dev.dexsr.klio.player.android.presentation.root.main

import dev.dexsr.klio.player.shared.LocalMediaArtwork
import dev.dexsr.klio.player.shared.PlaybackMediaDescription
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface RootCompactMediaMetadataProvider {

    fun artworkAsFlow(mediaID: String): Flow<LocalMediaArtwork?>

    fun descriptionAsFlow(mediaID: String): Flow<PlaybackMediaDescription?>
}

object NoOpRootCompactMediaMetadataProvider : RootCompactMediaMetadataProvider {

    override fun artworkAsFlow(mediaID: String): Flow<LocalMediaArtwork?> = flowOf()

    override fun descriptionAsFlow(mediaID: String): Flow<PlaybackMediaDescription?> = flowOf()
}
