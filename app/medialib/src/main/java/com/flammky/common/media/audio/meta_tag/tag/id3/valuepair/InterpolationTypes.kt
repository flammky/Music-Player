/**
 * @author : Paul Taylor
 *
 * Version @version:$Id$
 *
 * Jaudiotagger Copyright (C)2004,2005
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
package com.flammky.musicplayer.common.media.audio.meta_tag.tag.id3.valuepair

import com.flammky.common.kotlin.generic.sync
import com.flammky.musicplayer.common.media.audio.meta_tag.tag.datatype.AbstractIntStringValuePair

class InterpolationTypes private constructor() : AbstractIntStringValuePair() {
	init {
		idToValue[0] = "Band"
		idToValue[1] = "Linear"
		createMaps()
	}

	companion object {
		private var interpolationTypes: InterpolationTypes? = null

		@JvmStatic
		val instanceOf: InterpolationTypes
			get() {
				if (interpolationTypes == null) {
					sync {
						if (interpolationTypes == null) interpolationTypes = InterpolationTypes()
					}
				}
				return interpolationTypes!!
			}
	}
}