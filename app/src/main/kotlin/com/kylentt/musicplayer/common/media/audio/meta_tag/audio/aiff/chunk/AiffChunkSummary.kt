package com.kylentt.musicplayer.common.media.audio.meta_tag.audio.aiff.chunk

import com.kylentt.musicplayer.common.media.audio.meta_tag.audio.iff.ChunkSummary
import com.kylentt.musicplayer.common.media.audio.meta_tag.tag.aiff.AiffTag

/**
 * AIFF Specific methods for ChunkSummarys
 */
object AiffChunkSummary {
	/**
	 * Checks that there are only id3 tags after the currently selected id3tag because this means its safe to truncate
	 * the remainder of the file.
	 *
	 * @param tag
	 * @return
	 */
	@JvmStatic
	fun isOnlyMetadataTagsAfterStartingMetadataTag(tag: AiffTag): Boolean {
		var firstId3Tag = false
		for (cs in tag.getChunkSummaryList()) {
			if (firstId3Tag) {
				if (cs.chunkId != AiffChunkType.TAG.code) {
					return false
				}
			} else {
				if (cs.fileStartLocation == tag.startLocationInFileOfId3Chunk) {
					//Found starting point
					firstId3Tag = true
				}
			}
		}

		//Should always be true but this is to protect against something gone wrong
		return firstId3Tag
	}

	/**
	 * Get chunk before starting metadata tag
	 *
	 * @param tag
	 * @return
	 */
	@JvmStatic
	fun getChunkBeforeStartingMetadataTag(tag: AiffTag): ChunkSummary? {
		for (i in tag.getChunkSummaryList().indices) {
			val cs = tag.getChunkSummaryList()[i]
			if (cs.fileStartLocation == tag.startLocationInFileOfId3Chunk) {
				return tag.getChunkSummaryList()[i - 1]
			}
		}
		return null
	}
}
