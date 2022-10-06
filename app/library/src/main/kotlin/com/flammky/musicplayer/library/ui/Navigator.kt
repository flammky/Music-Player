package com.flammky.musicplayer.library.ui

import android.util.Log
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.contains
import androidx.navigation.get
import timber.log.Timber

interface IDK

class IDKImpl : IDK

fun idk() {
	Log.d("", "")
	Timber.d("")
}

object LibraryNavigator {

	fun NavGraphBuilder.addRoot() {
		composable("library") {
			Library()
		}
	}

	fun NavHostController.addRoot() {
		if (!graph.contains("library")) {
			ComposeNavigator.Destination(navigatorProvider[ComposeNavigator::class]) {
				Library()
			}.apply {
				route = "library"
				graph.addDestination(this)
			}
		}
	}
}
