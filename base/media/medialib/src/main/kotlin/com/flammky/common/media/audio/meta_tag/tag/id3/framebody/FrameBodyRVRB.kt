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

import com.flammky.musicplayer.common.media.audio.meta_tag.tag.datatype.DataTypes
import com.flammky.musicplayer.common.media.audio.meta_tag.tag.datatype.NumberFixedLength
import com.flammky.musicplayer.common.media.audio.meta_tag.tag.id3.ID3v24Frames
import java.nio.ByteBuffer

/**
 * Reverb frame.
 *
 *
 * Yet another subjective one. You may here adjust echoes of different
 * kinds. Reverb left/right is the delay between every bounce in ms.
 * Reverb bounces left/right is the number of bounces that should be
 * made. $FF equals an infinite number of bounces. Feedback is the
 * amount of volume that should be returned to the next echo bounce. $00
 * is 0%, $FF is 100%. If this value were $7F, there would be 50% volume
 * reduction on the first bounce, 50% of that on the second and so on.
 * Left to left means the sound from the left bounce to be played in the
 * left speaker, while left to right means sound from the left bounce to
 * be played in the right speaker.
 *
 *
 * 'Premix left to right' is the amount of left sound to be mixed in the
 * right before any reverb is applied, where $00 id 0% and $FF is 100%.
 * 'Premix right to left' does the same thing, but right to left.
 * Setting both premix to $FF would result in a mono output (if the
 * reverb is applied symmetric). There may only be one "RVRB" frame in
 * each tag.
 *
 * <table border=0 width="70%">
 * <tr><td colspan=2> &lt;Header for 'Reverb', ID: "RVRB"&gt;</td></tr>
 * <tr><td>Reverb left (ms)                </td><td>$xx xx   </td></tr>
 * <tr><td>Reverb right (ms)               </td><td>$xx xx   </td></tr>
 * <tr><td>Reverb bounces, left            </td><td>$xx      </td></tr>
 * <tr><td>Reverb bounces, right           </td><td>$xx      </td></tr>
 * <tr><td>Reverb feedback, left to left   </td><td>$xx      </td></tr>
 * <tr><td>Reverb feedback, left to right  </td><td>$xx      </td></tr>
 * <tr><td>Reverb feedback, right to right </td><td>$xx      </td></tr>
 * <tr><td>Reverb feedback, right to left  </td><td>$xx      </td></tr>
 * <tr><td>Premix left to right            </td><td>$xx      </td></tr>
 * <tr><td>Premix right to left            </td><td>$xx      </td></tr>
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
class FrameBodyRVRB : AbstractID3v2FrameBody, ID3v24FrameBody, ID3v23FrameBody {

	override val identifier: String?
		get() = ID3v24Frames.FRAME_ID_REVERB

	/**
	 * Creates a new FrameBodyRVRB datatype.
	 */
	constructor() {
		//        this.setObject("Reverb Left", new Short((short) 0));
		//        this.setObject("Reverb Right", new Short((short) 0));
		//        this.setObject("Reverb Bounces Left", new Byte((byte) 0));
		//        this.setObject("Reverb Bounces Right", new Byte((byte) 0));
		//        this.setObject("Reverb Feedback Left To Left", new Byte((byte) 0));
		//        this.setObject("Reverb Feedback Left To Right", new Byte((byte) 0));
		//        this.setObject("Reverb Feedback Right To Right", new Byte((byte) 0));
		//        this.setObject("Reverb Feedback Right to Left", new Byte((byte) 0));
		//        this.setObject("Premix Left To Right", new Byte((byte) 0));
		//        this.setObject("Premix Right To Left", new Byte((byte) 0));
	}

	constructor(body: FrameBodyRVRB) : super(body)

	/**
	 * Creates a new FrameBodyRVRB datatype.
	 *
	 * @param reverbLeft
	 * @param reverbRight
	 * @param reverbBouncesLeft
	 * @param reverbBouncesRight
	 * @param reverbFeedbackLeftToLeft
	 * @param reverbFeedbackLeftToRight
	 * @param reverbFeedbackRightToRight
	 * @param reverbFeedbackRightToLeft
	 * @param premixLeftToRight
	 * @param premixRightToLeft
	 */
	constructor(
		reverbLeft: Short,
		reverbRight: Short,
		reverbBouncesLeft: Byte,
		reverbBouncesRight: Byte,
		reverbFeedbackLeftToLeft: Byte,
		reverbFeedbackLeftToRight: Byte,
		reverbFeedbackRightToRight: Byte,
		reverbFeedbackRightToLeft: Byte,
		premixLeftToRight: Byte,
		premixRightToLeft: Byte
	) {
		setObjectValue(DataTypes.OBJ_REVERB_LEFT, reverbLeft)
		setObjectValue(DataTypes.OBJ_REVERB_RIGHT, reverbRight)
		setObjectValue(DataTypes.OBJ_REVERB_BOUNCE_LEFT, reverbBouncesLeft)
		setObjectValue(DataTypes.OBJ_REVERB_BOUNCE_RIGHT, reverbBouncesRight)
		setObjectValue(DataTypes.OBJ_REVERB_FEEDBACK_LEFT_TO_LEFT, reverbFeedbackLeftToLeft)
		setObjectValue(DataTypes.OBJ_REVERB_FEEDBACK_LEFT_TO_RIGHT, reverbFeedbackLeftToRight)
		setObjectValue(DataTypes.OBJ_REVERB_FEEDBACK_RIGHT_TO_RIGHT, reverbFeedbackRightToRight)
		setObjectValue(DataTypes.OBJ_REVERB_FEEDBACK_RIGHT_TO_LEFT, reverbFeedbackRightToLeft)
		setObjectValue(DataTypes.OBJ_PREMIX_LEFT_TO_RIGHT, premixLeftToRight)
		setObjectValue(DataTypes.OBJ_PREMIX_RIGHT_TO_LEFT, premixRightToLeft)
	}

	/**
	 * Creates a new FrameBodyRVRB datatype.
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
		objectList.add(NumberFixedLength(DataTypes.OBJ_REVERB_LEFT, this, 2))
		objectList.add(NumberFixedLength(DataTypes.OBJ_REVERB_RIGHT, this, 2))
		objectList.add(NumberFixedLength(DataTypes.OBJ_REVERB_BOUNCE_LEFT, this, 1))
		objectList.add(NumberFixedLength(DataTypes.OBJ_REVERB_BOUNCE_RIGHT, this, 1))
		objectList.add(NumberFixedLength(DataTypes.OBJ_REVERB_FEEDBACK_LEFT_TO_LEFT, this, 1))
		objectList.add(NumberFixedLength(DataTypes.OBJ_REVERB_FEEDBACK_LEFT_TO_RIGHT, this, 1))
		objectList.add(NumberFixedLength(DataTypes.OBJ_REVERB_FEEDBACK_RIGHT_TO_RIGHT, this, 1))
		objectList.add(NumberFixedLength(DataTypes.OBJ_REVERB_FEEDBACK_RIGHT_TO_LEFT, this, 1))
		objectList.add(NumberFixedLength(DataTypes.OBJ_PREMIX_LEFT_TO_RIGHT, this, 1))
		objectList.add(NumberFixedLength(DataTypes.OBJ_PREMIX_RIGHT_TO_LEFT, this, 1))
	}
}
