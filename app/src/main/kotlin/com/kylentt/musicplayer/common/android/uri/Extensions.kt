package com.kylentt.musicplayer.common.android.uri

import android.net.Uri

fun Uri?.orEmpty(): Uri = this ?: Uri.EMPTY
fun Uri?.orEmptyString(): String = this?.toString() ?: ""
