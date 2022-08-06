/*
 *  Jthink Copyright (C)2010
 *
 *  This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser
 *  General Public  License as published by the Free Software Foundation; either version 2.1 of the License,
 *  or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 *  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License along with this library; if not,
 *  you can get a copy from http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package com.kylentt.musicplayer.common.media.audio.meta_tag.tag.id3.framebody

import com.kylentt.musicplayer.common.media.audio.meta_tag.tag.datatype.ByteArraySizeTerminated
import com.kylentt.musicplayer.common.media.audio.meta_tag.tag.datatype.DataTypes
import java.nio.ByteBuffer

/**
 * Encrypted frame.
 *
 *
 * Container for an encrypted frame, we cannot decrypt encrypted frame but it may be possible
 * for the calling application to decrypt the frame if they understand how it has been encrypted,
 * information on this will be held within an ENCR frame
 *
 * @author : Paul Taylor
 */
class FrameBodyEncrypted : AbstractID3v2FrameBody, ID3v24FrameBody, ID3v23FrameBody {
	/**
	 * The ID3v2 frame identifier
	 *
	 * @return the ID3v2 frame identifier  for this frame type
	 */
	override var identifier: String? = null
		private set

	/**
	 * Creates a new FrameBodyEncrypted dataType.
	 */
	constructor(identifier: String?) {
		this.identifier = identifier
	}

	constructor(body: FrameBodyEncrypted) : super(body)

	/**
	 * Read from file
	 *
	 * @param identifier
	 * @param byteBuffer
	 * @param frameSize
	 * @throws InvalidTagException
	 */
	constructor(identifier: String?, byteBuffer: ByteBuffer, frameSize: Int) : super(
		byteBuffer,
		frameSize
	) {
		this.identifier = identifier
	}

	/**
	 * TODO:proper mapping
	 */
	override fun setupObjectList() {
		objectList.add(ByteArraySizeTerminated(DataTypes.OBJ_DATA, this))
	}
}
