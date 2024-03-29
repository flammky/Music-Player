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
 */
package com.flammky.musicplayer.common.media.audio.meta_tag.tag.id3.framebody

import com.flammky.musicplayer.common.media.audio.meta_tag.tag.datatype.DataTypes
import com.flammky.musicplayer.common.media.audio.meta_tag.tag.datatype.NumberFixedLength
import com.flammky.musicplayer.common.media.audio.meta_tag.tag.id3.ID3v24Frames
import java.nio.ByteBuffer

class FrameBodySEEK : AbstractID3v2FrameBody, ID3v24FrameBody {


	/**
	 * Creates a new FrameBodySEEK datatype.
	 */
	constructor() {
		//        this.setObject("Minimum Offset to Next Tag", new Integer(0));
	}

	/**
	 * Creates a new FrameBodySEEK datatype.
	 *
	 * @param minOffsetToNextTag
	 */
	constructor(minOffsetToNextTag: Int) {
		setObjectValue(DataTypes.OBJ_OFFSET, minOffsetToNextTag)
	}

	constructor(body: FrameBodySEEK) : super(body)

	/**
	 * Creates a new FrameBodySEEK datatype.
	 *
	 * @param byteBuffer
	 * @param frameSize
	 * @throws InvalidTagException if unable to create framebody from buffer
	 */
	constructor(byteBuffer: ByteBuffer, frameSize: Int) : super(byteBuffer, frameSize)

	/**
	 * The ID3v2 frame identifier
	 *
	 * @return the ID3v2 frame identifier  for this frame type
	 */
	override val identifier: String
		get() = ID3v24Frames.FRAME_ID_AUDIO_SEEK_POINT_INDEX

	/**
	 *
	 */
	override fun setupObjectList() {
		objectList.add(NumberFixedLength(DataTypes.OBJ_OFFSET, this, 4))
	}
}
