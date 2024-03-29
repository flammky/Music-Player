package com.flammky.musicplayer.library.dump.nav.compose

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.flammky.musicplayer.base.nav.compose.ComposeRootDestination
import com.flammky.musicplayer.base.nav.compose.ComposeRootNavigator
import dev.dexsr.klio.library.R
import com.flammky.musicplayer.library.presentation.Library

object LibraryRootNavigator : ComposeRootNavigator("library") {

	private val rootDestination = ComposeRootDestination(
		routeID = "root_library",
		label = "Library",
		iconResource = ComposeRootDestination.IconResource
			.ResID(R.drawable.library_outlined_base_128_24),
		selectedIconResource = ComposeRootDestination.IconResource
			.ResID(R.drawable.library_filled_base_128_24)
	)

	override fun getRootDestination(): ComposeRootDestination {
		return rootDestination
	}

	override fun navigateToRoot(
		controller: NavController,
		navOptionsBuilder: NavOptionsBuilder.() -> Unit
	) {
		controller.navigate(rootDestination.routeID, navOptionsBuilder)
	}

	override fun addRootDestination(
		navGraphBuilder: NavGraphBuilder,
		controller: NavController,
		onAppliedScope: @Composable () -> Unit
	) {
		navGraphBuilder.composable(rootDestination.routeID) {
			Library()
			onAppliedScope()
		}
	}
}
