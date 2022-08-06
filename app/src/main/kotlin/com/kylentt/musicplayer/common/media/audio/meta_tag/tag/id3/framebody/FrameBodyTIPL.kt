/**
 * @author : Paul Taylor
 * @author : Eric Farng
 *
 * Version @version:$Id$
 *
 * MusicTag Copyright (C)2003,2004
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public  License as published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not,
 * you can get a copy from http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * Description:
 * People List
 */
package com.kylentt.musicplayer.common.media.audio.meta_tag.tag.id3.framebody

import com.kylentt.musicplayer.common.media.audio.meta_tag.tag.datatype.DataTypes
import com.kylentt.musicplayer.common.media.audio.meta_tag.tag.datatype.Pair
import com.kylentt.musicplayer.common.media.audio.meta_tag.tag.datatype.PairedTextEncodedStringNullTerminated
import com.kylentt.musicplayer.common.media.audio.meta_tag.tag.id3.ID3v24Frames
import com.kylentt.musicplayer.common.media.audio.meta_tag.tag.id3.valuepair.StandardIPLSKey
import java.nio.ByteBuffer

/**
 * The 'Involved people list' is intended as a mapping between functions like producer and names. Every odd field is a
 * function and every even is an name or a comma delimited list of names.
 *
 */
class FrameBodyTIPL : AbstractFrameBodyPairs, ID3v24FrameBody {

	override val identifier: String?
		get() = ID3v24Frames.FRAME_ID_INVOLVED_PEOPLE

	/**
	 * Creates a new FrameBodyTIPL datatype.
	 */
	constructor() : super()

	/**
	 * Creates a new FrameBodyTIPL data type.
	 *
	 * @param textEncoding
	 * @param text
	 */
	constructor(textEncoding: Byte, text: String?) : super(textEncoding, text)

	/**
	 * Creates a new FrameBodyTIPL data type.
	 *
	 * @param byteBuffer
	 * @param frameSize
	 * @throws InvalidTagException
	 */
	constructor(byteBuffer: ByteBuffer, frameSize: Int) : super(byteBuffer, frameSize)

	/**
	 * Convert from V3 to V4 Frame
	 *
	 * @param body
	 */
	constructor(body: FrameBodyIPLS) {
		setObjectValue(DataTypes.OBJ_TEXT_ENCODING, body.textEncoding)
		setObjectValue(DataTypes.OBJ_TEXT, body.pairing)
	}

	/**
	 * Construct from a set of pairs
	 *
	 * @param textEncoding
	 * @param pairs
	 */
	constructor(textEncoding: Byte, pairs: List<Pair>) {
		setObjectValue(DataTypes.OBJ_TEXT_ENCODING, textEncoding)
		val values = PairedTextEncodedStringNullTerminated.ValuePairs()
		for (next in pairs) {
			values.add(next)
		}
		setObjectValue(DataTypes.OBJ_TEXT, values)
	}

	companion object {
		//Standard function names, code now uses StandardIPLSKey but kept for backwards compatability
		val ENGINEER = StandardIPLSKey.ENGINEER.key
		val MIXER = StandardIPLSKey.MIXER.key
		val DJMIXER = StandardIPLSKey.DJMIXER.key
		val PRODUCER = StandardIPLSKey.PRODUCER.key
		val ARRANGER = StandardIPLSKey.ARRANGER.key
	}
}
