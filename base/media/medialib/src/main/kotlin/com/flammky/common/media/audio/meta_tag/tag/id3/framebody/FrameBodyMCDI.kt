/*
 *  MusicTag Copyright (C)2003,2004
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
package com.flammky.musicplayer.common.media.audio.meta_tag.tag.id3.framebody

import com.flammky.musicplayer.common.media.audio.meta_tag.tag.datatype.ByteArraySizeTerminated
import com.flammky.musicplayer.common.media.audio.meta_tag.tag.datatype.DataTypes
import com.flammky.musicplayer.common.media.audio.meta_tag.tag.id3.ID3v24Frames
import java.nio.ByteBuffer

/**
 * Music CD identifier frame.
 *
 *
 * This frame is intended for music that comes from a CD, so that the CD
 * can be identified in databases such as the CDDB. The frame
 * consists of a binary dump of the Table Of Contents, TOC, from the CD,
 * which is a header of 4 bytes and then 8 bytes/track on the CD plus 8
 * bytes for the 'lead out' making a maximum of 804 bytes. The offset to
 * the beginning of every track on the CD should be described with a
 * four bytes absolute CD-frame address per track, and not with absolute
 * time. This frame requires a present and valid "TRCK" frame, even if
 * the CD's only got one track. There may only be one "MCDI" frame in
 * each tag.
 *
 * <table border=0 width="70%">
 * <tr><td colspan=2> &lt;Header for 'Music CD identifier', ID: "MCDI"&gt;</td></tr>
 * <tr><td>CD TOC</td><td>&lt;binary data&gt;</td></tr>
</table> *
 *
 *
 * For more details, please refer to the ID3 specifications:
 *
 *  * [ID3 v2.3.0 Spec](http://www.id3.org/id3v2.3.0.txt)
 *
 *
 * @author : Paul Taylor
 * @author : Eric Farng
 * @version $Id$
 */
class FrameBodyMCDI : AbstractID3v2FrameBody, ID3v24FrameBody, ID3v23FrameBody {

	override val identifier: String?
		get() = ID3v24Frames.FRAME_ID_MUSIC_CD_ID

	/**
	 * Creates a new FrameBodyMCDI datatype.
	 */
	constructor() {
		setObjectValue(DataTypes.OBJ_DATA, ByteArray(0))
	}

	constructor(body: FrameBodyMCDI) : super(body)

	/**
	 * Creates a new FrameBodyMCDI datatype.
	 *
	 * @param cdTOC
	 */
	constructor(cdTOC: ByteArray?) {
		setObjectValue(DataTypes.OBJ_DATA, cdTOC)
	}

	/**
	 * Creates a new FrameBodyMCDI datatype.
	 *
	 * @param byteBuffer
	 * @param frameSize
	 * @throws InvalidTagException if unable to create framebody from buffer
	 */
	constructor(byteBuffer: ByteBuffer, frameSize: Int) : super(byteBuffer, frameSize)

	/**
	 *
	 */
	override fun setupObjectList() {
		objectList.add(ByteArraySizeTerminated(DataTypes.OBJ_DATA, this))
	}
}
