package dev.dexsr.klio.base.theme.md3

object Margin {
	const val MARGIN_INCREMENTS_VALUE_COMPACT = 16
	const val MARGIN_INCREMENTS_VALUE_MEDIUM = 24
	const val MARGIN_INCREMENTS_VALUE_EXPANDED = 24
}

val MD3Theme.MARGIN_INCREMENTS_VALUE_COMPACT
	get() = Margin.MARGIN_INCREMENTS_VALUE_COMPACT

val MD3Theme.MARGIN_INCREMENTS_VALUE_MEDIUM
	get() = Margin.MARGIN_INCREMENTS_VALUE_MEDIUM

val MD3Theme.MARGIN_INCREMENTS_VALUE_EXPANDED
	get() = Margin.MARGIN_INCREMENTS_VALUE_EXPANDED

val MD3Spec.margin: Margin
	get() = Margin
