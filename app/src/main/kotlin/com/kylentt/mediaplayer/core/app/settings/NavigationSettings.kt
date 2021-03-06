package com.kylentt.mediaplayer.core.app.settings

import androidx.annotation.FloatRange
import kotlinx.serialization.Serializable

/**
 * Class to store the Preference Settings for App Navigation Component,
 * e.g: Bottom Navigation @Composable Appearance
 * @constructor [BottomNavigationSettings] the settings for Bottom Navigation
 * @property [Companion.DEFAULT] static Default Instance
 * @author Kylentt
 * @since 2022/04/30
 */

@Serializable
data class NavigationSettings(
  val bnvSettings: BottomNavigationSettings
) {

	val defaultValue
		get() = DEFAULT

  companion object {

    @JvmStatic
    val DEFAULT: NavigationSettings by lazy {
      val bnvSettings = BottomNavigationSettings.DEFAULT
      NavigationSettings(bnvSettings = bnvSettings)
    }
  }
}

@Serializable
data class BottomNavigationSettings(
    val itemAnimation: ItemAnimation,
    val itemAlignment: ItemAlignment,
    @FloatRange(from = 0.0, to = 100.0) val visibility: Float
) {

	val defaultValue
		get() = DEFAULT

  enum class ItemAlignment {
    VERTICAL,
    HORIZONTAL
  }

  enum class ItemAnimation {
    NOTHING,
    VISIBILITY
  }

  companion object {
    @JvmStatic
    val DEFAULT: BottomNavigationSettings by lazy {
      val defAnimation = ItemAnimation.VISIBILITY
      val defAlignment = ItemAlignment.VERTICAL
      val defVisibility = 90f
      BottomNavigationSettings(defAnimation, defAlignment, defVisibility)
    }
  }

}
