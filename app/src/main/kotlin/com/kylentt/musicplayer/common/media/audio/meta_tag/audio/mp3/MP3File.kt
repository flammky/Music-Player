package com.kylentt.musicplayer.common.media.audio.meta_tag.audio.mp3

import com.kylentt.musicplayer.common.media.audio.meta_tag.audio.AudioFile
import com.kylentt.musicplayer.common.media.audio.meta_tag.audio.exceptions.CannotWriteException
import com.kylentt.musicplayer.common.media.audio.meta_tag.audio.exceptions.InvalidAudioFrameException
import com.kylentt.musicplayer.common.media.audio.meta_tag.audio.exceptions.NoWritePermissionsException
import com.kylentt.musicplayer.common.media.audio.meta_tag.audio.exceptions.UnableToModifyFileException
import com.kylentt.musicplayer.common.media.audio.meta_tag.audio.generic.Permissions.displayPermissions
import com.kylentt.musicplayer.common.media.audio.meta_tag.logging.*
import com.kylentt.musicplayer.common.media.audio.meta_tag.tag.Tag
import com.kylentt.musicplayer.common.media.audio.meta_tag.tag.TagException
import com.kylentt.musicplayer.common.media.audio.meta_tag.tag.TagNotFoundException
import com.kylentt.musicplayer.common.media.audio.meta_tag.tag.TagOptionSingleton
import com.kylentt.musicplayer.common.media.audio.meta_tag.tag.id3.*
import com.kylentt.musicplayer.common.media.audio.meta_tag.tag.id3.AbstractID3v2Tag.Companion.getV2TagSizeIfExists
import com.kylentt.musicplayer.common.media.audio.meta_tag.tag.lyrics3.AbstractLyrics3
import com.kylentt.musicplayer.common.media.audio.meta_tag.tag.reference.ID3V2Version
import com.kylentt.musicplayer.core.sdk.VersionHelper
import timber.log.Timber
import java.io.*
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.logging.Level

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
 */
/**
 * This class represents a physical MP3 File
 */
class MP3File : AudioFile {
	/**
	 * the ID3v2 tag that this file contains.
	 */
	private var id3v2tag: AbstractID3v2Tag? = null

	/**
	 * @return a representation of tag as v24
	 */
	/**
	 * Representation of the idv2 tag as a idv24 tag
	 */
	var iD3v2TagAsv24: ID3v24Tag? = null
		private set

	/**
	 * The Lyrics3 tag that this file contains.
	 */
	private val lyrics3tag: AbstractLyrics3? = null

	/**
	 * The ID3v1 tag that this file contains.
	 */
	private var id3v1tag: ID3v1Tag? = null

	/**
	 * Creates a new empty MP3File datatype that is not associated with a
	 * specific file.
	 */
	constructor()

	/**
	 * Creates a new MP3File datatype and parse the tag from the given filename.
	 *
	 * @param filename MP3 file
	 * @throws IOException  on any I/O error
	 * @throws TagException on any exception generated by this library.
	 * @throws ReadOnlyFileException
	 * @throws InvalidAudioFrameException
	 */
	constructor(filename: String?) : this(File(filename))

	/**
	 * Read v1 tag
	 *
	 * @param file
	 * @param newFile
	 * @param loadOptions
	 * @throws IOException
	 */
	@Throws(IOException::class)
	private fun readV1Tag(file: File, newFile: RandomAccessFile?, loadOptions: Int) {
		if (loadOptions and LOAD_IDV1TAG != 0) {
			logger.finer("Attempting to read id3v1tags")
			try {
				id3v1tag = ID3v11Tag(
					newFile!!, file.name
				)
			} catch (ex: TagNotFoundException) {
				logger.config("No ids3v11 tag found")
			}
			try {
				if (id3v1tag == null) {
					id3v1tag = ID3v1Tag(
						newFile!!, file.name
					)
				}
			} catch (ex: TagNotFoundException) {
				logger.config("No id3v1 tag found")
			}
		}
	}

	/**
	 * Read V2tag, if exists.
	 *
	 * TODO:shouldn't we be handing TagExceptions:when will they be thrown
	 *
	 * @param file the file to read tags from
	 * @param loadOptions load options
	 * @throws IOException IO issues
	 * @throws TagException tag issues
	 */
	@Throws(IOException::class, TagException::class)
	private fun readV2Tag(file: File, loadOptions: Int, startByte: Int) {
		//We know where the actual Audio starts so load all the file from start to that point into
		//a buffer then we can read the IDv2 information without needing any more File I/O
		if (startByte >= AbstractID3v2Tag.TAG_HEADER_LENGTH) {
			logger.finer("Attempting to read id3v2tags")
			FileInputStream(file).use { fis ->
				val bb = ByteBuffer.allocateDirect(startByte)
				fis.channel.read(bb, 0)
				bb.rewind()
				if (loadOptions and LOAD_IDV2TAG != 0) {
					logger.config("Attempting to read id3v2tags")
					try {
						setID3v2Tag(ID3v24Tag(bb, file.name))
					} catch (ex: TagNotFoundException) {
						Timber.d("No id3v24 Tag Found")
						logger.config("No id3v24 tag found")
					}
					try {
						if (id3v2tag == null) {
							setID3v2Tag(ID3v23Tag(bb, file.name))
						}
					} catch (ex: TagNotFoundException) {
						Timber.d("No id3v23 Tag Found")
						logger.config("No id3v23 tag found")
					}
					try {
						if (id3v2tag == null) {
							setID3v2Tag(ID3v22Tag(bb, file.name))
						}
					} catch (ex: TagNotFoundException) {
						Timber.d("No id3v22 Tag Found")
						logger.config("No id3v22 tag found")
					}
				}
			}
		} else {
			Timber.d("Not enough room for valid id3v2 tag:$startByte")
			logger.config("Not enough room for valid id3v2 tag:$startByte")
		}
	}

	/**
	 *
	 * @param startByte
	 * @param endByte
	 * @return
	 * @throws Exception
	 *
	 * @return true if all the bytes between in the file between startByte and endByte are null, false
	 * otherwise
	 */
	@Throws(IOException::class)
	private fun isFilePortionNull(startByte: Int, endByte: Int): Boolean {
		logger.config("Checking file portion:" + Hex.asHex(startByte) + ":" + Hex.asHex(endByte))
		var fis: FileInputStream? = null
		var fc: FileChannel? = null
		try {
			fis = FileInputStream(file)
			fc = fis.channel
			fc.position(startByte.toLong())
			val bb = ByteBuffer.allocateDirect(endByte - startByte)
			fc.read(bb)
			while (bb.hasRemaining()) {
				if (bb.get().toInt() != 0) {
					return false
				}
			}
		} finally {
			fc?.close()
			fis?.close()
		}
		return true
	}

	/**
	 * Regets the audio header starting from start of file, and write appropriate logging to indicate
	 * potential problem to user.
	 *
	 * @param startByte
	 * @param firstHeaderAfterTag
	 * @return
	 * @throws IOException
	 * @throws InvalidAudioFrameException
	 */
	@Throws(IOException::class, InvalidAudioFrameException::class)
	private fun checkAudioStart(
		startByte: Long,
		firstHeaderAfterTag: MP3AudioHeader
	): MP3AudioHeader {
		val headerOne: MP3AudioHeader
		val headerTwo: MP3AudioHeader
		logger.warning(
			ErrorMessage.MP3_ID3TAG_LENGTH_INCORRECT.getMsg(
				file?.path,
				Hex.asHex(startByte),
				Hex.asHex(firstHeaderAfterTag.mp3StartByte)
			)
		)

		//because we cant agree on start location we reread the audioheader from the start of the file, at least
		//this way we cant overwrite the audio although we might overwrite part of the tag if we write this file
		//back later
		headerOne = MP3AudioHeader(file!!, 0)
		logger.config("Checking from start:$headerOne")

		//Although the id3 tag size appears to be incorrect at least we have found the same location for the start
		//of audio whether we start searching from start of file or at the end of the alleged of file so no real
		//problem
		return if (firstHeaderAfterTag.mp3StartByte == headerOne.mp3StartByte) {
			logger.config(
				ErrorMessage.MP3_START_OF_AUDIO_CONFIRMED.getMsg(
					file?.path,
					Hex.asHex(headerOne.mp3StartByte)
				)
			)
			firstHeaderAfterTag
		} else {

			//We get a different value if read from start, can't guarantee 100% correct lets do some more checks
			logger.config(
				ErrorMessage.MP3_RECALCULATED_POSSIBLE_START_OF_MP3_AUDIO.getMsg(
					file?.path,
					Hex.asHex(headerOne.mp3StartByte)
				)
			)

			//Same frame count so probably both audio headers with newAudioHeader being the first one
			if (firstHeaderAfterTag.numberOfFrames == headerOne.numberOfFrames) {
				logger.warning(
					ErrorMessage.MP3_RECALCULATED_START_OF_MP3_AUDIO.getMsg(
						file?.path,
						Hex.asHex(
							headerOne.mp3StartByte
						)
					)
				)
				return headerOne
			}

			//If the size reported by the tag header is a little short and there is only nulls between the recorded value
			//and the start of the first audio found then we stick with the original header as more likely that currentHeader
			//DataInputStream not really a header
			if (isFilePortionNull(startByte.toInt(), firstHeaderAfterTag.mp3StartByte.toInt())) {
				return firstHeaderAfterTag
			}

			//Skip to the next header (header 2, counting from start of file)
			headerTwo = MP3AudioHeader(
				file!!, headerOne.mp3StartByte
					+ headerOne.mp3FrameHeader!!.frameLength
			)

			//It matches the header we found when doing the original search from after the ID3Tag therefore it
			//seems that newAudioHeader was a false match and the original header was correct
			if (headerTwo.mp3StartByte == firstHeaderAfterTag.mp3StartByte) {
				logger.warning(
					ErrorMessage.MP3_START_OF_AUDIO_CONFIRMED.getMsg(
						file?.path,
						Hex.asHex(
							firstHeaderAfterTag.mp3StartByte
						)
					)
				)
				return firstHeaderAfterTag
			}

			//It matches the frameCount the header we just found so lends weight to the fact that the audio does indeed start at new header
			//however it maybe that neither are really headers and just contain the same data being misrepresented as headers.
			if (headerTwo.numberOfFrames == headerOne.numberOfFrames) {
				logger.warning(
					ErrorMessage.MP3_RECALCULATED_START_OF_MP3_AUDIO.getMsg(
						file?.path,
						Hex.asHex(
							headerOne.mp3StartByte
						)
					)
				)
				headerOne
			} else {
				logger.warning(
					ErrorMessage.MP3_RECALCULATED_START_OF_MP3_AUDIO.getMsg(
						file?.path,
						Hex.asHex(
							firstHeaderAfterTag.mp3StartByte
						)
					)
				)
				firstHeaderAfterTag
			}
		}
	}
	/**
	 * Creates a new MP3File dataType and parse the tag from the given file
	 * Object, files can be opened read only if required.
	 *
	 * @param file        MP3 file
	 * @param loadOptions decide what tags to load
	 * @param readOnly    causes the files to be opened readonly
	 * @throws IOException  on any I/O error
	 * @throws TagException on any exception generated by this library.
	 * @throws ReadOnlyFileException
	 * @throws InvalidAudioFrameException
	 */
	/**
	 * Creates a new MP3File dataType and parse the tag from the given file
	 * Object, files must be writable to use this constructor.
	 *
	 * @param file        MP3 file
	 * @param loadOptions decide what tags to load
	 * @throws IOException  on any I/O error
	 * @throws TagException on any exception generated by this library.
	 * @throws ReadOnlyFileException
	 * @throws InvalidAudioFrameException
	 */
	/**
	 * Returns true if this datatype contains a `Lyrics3` tag
	 * TODO disabled until Lyrics3 fixed
	 * @return true if this datatype contains a `Lyrics3` tag
	 */
	/*
	public boolean hasLyrics3Tag()
	{
			return (lyrics3tag != null);
	}
	*/
	/**
	 * Creates a new MP3File datatype and parse the tag from the given file
	 * Object.
	 *
	 * @param file MP3 file
	 * @throws IOException  on any I/O error
	 * @throws TagException on any exception generated by this library.
	 * @throws ReadOnlyFileException
	 * @throws InvalidAudioFrameException
	 */
	@JvmOverloads
	constructor(file: File, loadOptions: Int = LOAD_ALL, readOnly: Boolean = false) {
		var newFile: RandomAccessFile? = null
		try {
			this.file = file

			Timber.d("try creating MP3File for: $file")

			//Check File accessibility
			newFile = checkFilePermissions(file, readOnly)

			Timber.d("try creating MP3File for: $file, checkedFilePermissions")

			//Read ID3v2 tag size (if tag exists) to allow audioHeader parsing to skip over tag
			val tagSizeReportedByHeader = getV2TagSizeIfExists(file)
			logger.config("TagHeaderSize:" + Hex.asHex(tagSizeReportedByHeader))
			audioHeader = MP3AudioHeader(file, tagSizeReportedByHeader)

			Timber.d("try creating MP3File for: $file, createdAudioHeader")

			//If the audio header is not straight after the end of the tag then search from start of file
			if (tagSizeReportedByHeader != (audioHeader as MP3AudioHeader).mp3StartByte) {
				logger.config("First header found after tag:$audioHeader")
				audioHeader = checkAudioStart(tagSizeReportedByHeader, audioHeader as MP3AudioHeader)
				Timber.d("try creating MP3File for: $file, reCreatedAudioHeader")
			}

			Timber.d("try creating MP3File for: $file, reading V1Tag")

			//Read v1 tags (if any)
			readV1Tag(file, newFile, loadOptions)

			Timber.d("try creating MP3File for: $file, V1Tag read")

			Timber.d("try creating MP3File for: $file, reading V2Tag")

			//Read v2 tags (if any)
			readV2Tag(file, loadOptions, (audioHeader as MP3AudioHeader).mp3StartByte.toInt())

			Timber.d("try creating MP3File for: $file, V2Tag read")

			//If we have a v2 tag use that, if we do not but have v1 tag use that
			//otherwise use nothing
			//TODO:if have both should we merge
			//rather than just returning specific ID3v22 tag, would it be better to return v24 version ?
			if (iD3v2Tag != null) {
				tag = iD3v2Tag
			} else if (id3v1tag != null) {
				tag = id3v1tag
			}
		} finally {
			newFile?.close()
		}
	}

	/**
	 * Used by tags when writing to calculate the location of the music file
	 *
	 * @param file
	 * @return the location within the file that the audio starts
	 * @throws IOException
	 * @throws InvalidAudioFrameException
	 */
	@Throws(InvalidAudioFrameException::class, IOException::class)
	fun getMP3StartByte(file: File): Long {
		return try {
			//Read ID3v2 tag size (if tag exists) to allow audio header parsing to skip over tag
			val startByte = getV2TagSizeIfExists(file)
			var audioHeader = MP3AudioHeader(file, startByte)
			if (startByte != audioHeader.mp3StartByte) {
				logger.config(
					"First header found after tag:$audioHeader"
				)
				audioHeader = checkAudioStart(startByte, audioHeader)
			}
			audioHeader.mp3StartByte
		} catch (iafe: InvalidAudioFrameException) {
			throw iafe
		} catch (ioe: IOException) {
			throw ioe
		}
	}

	/**
	 * Extracts the raw ID3v2 tag data into a file.
	 *
	 * This provides access to the raw data before manipulation, the data is written from the start of the file
	 * to the start of the Audio Data. This is primarily useful for manipulating corrupted tags that are not
	 * (fully) loaded using the standard methods.
	 *
	 * @param outputFile to write the data to
	 * @return
	 * @throws TagNotFoundException
	 * @throws IOException
	 */
	@Throws(TagNotFoundException::class, IOException::class)
	fun extractID3v2TagDataIntoFile(outputFile: File): File {
		val startByte = (audioHeader as MP3AudioHeader).mp3StartByte.toInt()
		if (startByte >= 0) {

			//Read byte into buffer
			val fis = FileInputStream(file)
			val fc = fis.channel
			val bb = ByteBuffer.allocate(startByte)
			fc.read(bb)

			//Write bytes to outputFile
			val out = FileOutputStream(outputFile)
			out.write(bb.array())
			out.close()
			fc.close()
			fis.close()
			return outputFile
		}
		throw TagNotFoundException("There is no ID3v2Tag data in this file")
	}

	/**
	 * Return audio header
	 * @return
	 */
	val mP3AudioHeader: MP3AudioHeader
		get() = audioHeader as MP3AudioHeader

	/**
	 * Returns true if this datatype contains an `Id3v1` tag
	 *
	 * @return true if this datatype contains an `Id3v1` tag
	 */
	fun hasID3v1Tag(): Boolean {
		return id3v1tag != null
	}

	/**
	 * Returns true if this datatype contains an `Id3v2` tag
	 *
	 * @return true if this datatype contains an `Id3v2` tag
	 */
	fun hasID3v2Tag(): Boolean {
		return id3v2tag != null
	}

	fun setID3v1Tag(id3v1tag: Tag?) {
		logger.config("setting tagv1:v1 tag")
		this.id3v1tag = id3v1tag as ID3v1Tag?
	}

	/**
	 * Sets the `ID3v1` tag for this dataType. A new
	 * `ID3v1_1` dataType is created from the argument and then used
	 * here.
	 *
	 * @param mp3tag Any MP3Tag dataType can be used and will be converted into a
	 * new ID3v1_1 dataType.
	 */
	fun setID3v1Tag(mp3tag: AbstractTag?) {
		logger.config("setting tagv1:abstract")
		id3v1tag = ID3v11Tag(mp3tag)
	}
	/**
	 * Returns the `ID3v1` tag for this dataType.
	 *
	 * @return the `ID3v1` tag for this dataType
	 */
	/**
	 * Sets the ID3v1(_1)tag to the tag provided as an argument.
	 *
	 * @param id3v1tag
	 */
	var iD3v1Tag: ID3v1Tag?
		get() = id3v1tag
		set(id3v1tag) {
			logger.config("setting tagv1:v1 tag")
			this.id3v1tag = id3v1tag
		}

	/**
	 * Calculates hash with given algorithm. Buffer size is 32768 byte.
	 * Hash is calculated EXCLUDING meta-data, like id3v1 or id3v2
	 *
	 * @return hash value in byte
	 * @param algorithm options MD5,SHA-1,SHA-256
	 * @throws IOException
	 * @throws InvalidAudioFrameException
	 * @throws NoSuchAlgorithmException
	 */
	@Throws(NoSuchAlgorithmException::class, InvalidAudioFrameException::class, IOException::class)
	fun getHash(algorithm: String?): ByteArray {
		return getHash(algorithm, 32768)
	}

	/**
	 * Calculates hash with given buffer size.
	 * Hash is calculated EXCLUDING meta-data, like id3v1 or id3v2
	 * @param  buffer
	 * @return byte[] hash value in byte
	 * @throws IOException
	 * @throws InvalidAudioFrameException
	 * @throws NoSuchAlgorithmException
	 */
	@Throws(NoSuchAlgorithmException::class, InvalidAudioFrameException::class, IOException::class)
	fun getHash(buffer: Int): ByteArray {
		return getHash("MD5", buffer)
	}

	/**
	 * Calculates hash with algorithm "MD5". Buffer size is 32768 byte.
	 * Hash is calculated EXCLUDING meta-data, like id3v1 or id3v2
	 *
	 * @return byte[] hash value.
	 * @throws IOException
	 * @throws InvalidAudioFrameException
	 * @throws NoSuchAlgorithmException
	 */
	@get:Throws(
		NoSuchAlgorithmException::class,
		InvalidAudioFrameException::class,
		IOException::class
	)
	val hash: ByteArray
		get() = getHash("MD5", 32768)

	/**
	 * Calculates hash with algorithm "MD5", "SHA-1" or SHA-256".
	 * Hash is calculated EXCLUDING meta-data, like id3v1 or id3v2
	 *
	 * @return byte[] hash value in byte
	 * @throws IOException
	 * @throws InvalidAudioFrameException
	 * @throws NoSuchAlgorithmException
	 */
	@Throws(
		InvalidAudioFrameException::class,
		IOException::class,
		NoSuchAlgorithmException::class,
		NotImplementedError::class
	)
	fun getHash(algorithm: String?, bufferSize: Int): ByteArray {
		if (!VersionHelper.hasOreo()) TODO("Require API >= 26")

		val mp3File = file
		val startByte = getMP3StartByte(mp3File!!)
		var id3v1TagSize = 0
		if (hasID3v1Tag()) {
			val id1tag =
				iD3v1Tag
			id3v1TagSize = id1tag!!.size
		}
		val inStream = Files
			.newInputStream(Paths.get(mp3File.absolutePath))
		val buffer = ByteArray(bufferSize)
		val digest = MessageDigest.getInstance(algorithm)
		inStream.skip(startByte)
		var read: Int
		val totalSize = mp3File.length() - startByte - id3v1TagSize
		var pointer = buffer.size
		while (pointer <= totalSize) {
			read = inStream.read(buffer)
			digest.update(buffer, 0, read)
			pointer += buffer.size
		}
		read = inStream.read(buffer, 0, totalSize.toInt() - pointer + buffer.size)
		digest.update(buffer, 0, read)
		return digest.digest()
	}

	val iD3v2Tag: AbstractID3v2Tag?
		get() = id3v2tag

	/**
	 * Sets the v2 tag to the v2 tag provided as an argument.
	 * Also store a v24 version of tag as v24 is the interface to be used
	 * when talking with client applications.
	 *
	 * @param id3v2tag
	 */
	fun setID3v2Tag(id3v2tag: AbstractID3v2Tag?) {
		this.id3v2tag = id3v2tag
		iD3v2TagAsv24 = if (id3v2tag is ID3v24Tag) {
			this.id3v2tag as ID3v24Tag?
		} else {
			ID3v24Tag(id3v2tag)
		}
	}

	fun setID3v2TagAs24(id3v2tag: AbstractID3v2Tag?) {
		this.id3v2tag = ID3v24Tag(id3v2tag)
	}

	/**
	 * Set v2 tag ,don't need to set v24 tag because saving
	 *
	 *
	 * @param id3v2tag
	 */
	//TODO temp its rather messy
	fun setID3v2TagOnly(id3v2tag: AbstractID3v2Tag?) {
		this.id3v2tag = id3v2tag
		iD3v2TagAsv24 = null
	}


	/**
	 * Sets the `Lyrics3` tag for this dataType. A new
	 * `Lyrics3v2` dataType is created from the argument and then
	 *
	 * used here.
	 *
	 * @param mp3tag Any MP3Tag dataType can be used and will be converted into a
	 * new Lyrics3v2 dataType.
	 */
	/*
	public void setLyrics3Tag(AbstractTag mp3tag)
	{
			lyrics3tag = new Lyrics3v2(mp3tag);
	}
	*/
	/**
	 *
	 *
	 * @param lyrics3tag
	 */
	/*
	public void setLyrics3Tag(AbstractLyrics3 lyrics3tag)
	{
			this.lyrics3tag = lyrics3tag;
	}
	*/
	/**
	 * Returns the `ID3v1` tag for this datatype.
	 *
	 * @return the `ID3v1` tag for this datatype
	 */
	/*
	public AbstractLyrics3 getLyrics3Tag()
	{
			return lyrics3tag;
	}
	*/
	/**
	 * Remove tag from file
	 *
	 * @param mp3tag
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	@Throws(FileNotFoundException::class, IOException::class)
	fun delete(mp3tag: AbstractTag) {
		val raf = RandomAccessFile(file, "rw")
		mp3tag.delete(raf)
		raf.close()
		if (mp3tag is ID3v1Tag) {
			id3v1tag = null
		}
		if (mp3tag is AbstractID3v2Tag) {
			id3v2tag = null
		}
	}

	/**
	 * Overridden for compatibility with merged code
	 *
	 * @throws NoWritePermissionsException if the file could not be written to due to file permissions
	 * @throws CannotWriteException
	 */
	@Throws(CannotWriteException::class)
	override fun commit() {
		try {
			save()
		} catch (umfe: UnableToModifyFileException) {
			throw NoWritePermissionsException(umfe)
		} catch (ioe: IOException) {
			throw CannotWriteException(ioe)
		} catch (te: TagException) {
			throw CannotWriteException(te)
		}
	}

	/**
	 * Check can write to file
	 *
	 * @param file
	 * @throws IOException
	 */
	@Throws(IOException::class)
	fun precheck(file: File) {
		if (!VersionHelper.hasOreo()) TODO("Implement API < 26")

		val path = file.toPath()
		if (!Files.exists(path)) {
			logger.severe(ErrorMessage.GENERAL_WRITE_FAILED_BECAUSE_FILE_NOT_FOUND.getMsg(file.name))
			throw IOException(ErrorMessage.GENERAL_WRITE_FAILED_BECAUSE_FILE_NOT_FOUND.getMsg(file.name))
		}
		if (TagOptionSingleton.instance.isCheckIsWritable && !Files.isWritable(path)) {
			logger.severe(displayPermissions(path))
			logger.severe(ErrorMessage.GENERAL_WRITE_FAILED.getMsg(file.name))
			throw IOException(ErrorMessage.GENERAL_WRITE_FAILED.getMsg(file.name))
		}
		if (file.length() <= MINIMUM_FILESIZE) {
			logger.severe(ErrorMessage.GENERAL_WRITE_FAILED_BECAUSE_FILE_IS_TOO_SMALL.getMsg(file.name))
			throw IOException(
				ErrorMessage.GENERAL_WRITE_FAILED_BECAUSE_FILE_IS_TOO_SMALL.getMsg(
					file.name
				)
			)
		}
	}
	/**
	 * Saves the tags in this dataType to the file argument. It will be saved as
	 * TagConstants.MP3_FILE_SAVE_WRITE
	 *
	 * @param fileToSave file to save the this dataTypes tags to
	 * @throws FileNotFoundException if unable to find file
	 * @throws IOException           on any I/O error
	 */
	/**
	 * Saves the tags in this dataType to the file referred to by this dataType.
	 *
	 * @throws IOException  on any I/O error
	 * @throws TagException on any exception generated by this library.
	 */
	@JvmOverloads
	@Throws(IOException::class)
	fun save(fileToSave: File = file!!) {
		//Ensure we are dealing with absolute filepaths not relative ones
		val file = fileToSave.absoluteFile
		logger.config("Saving  : " + file.path)

		//Checks before starting write
		precheck(file)
		var rfile: RandomAccessFile? = null
		try {
			//ID3v2 Tag
			if (TagOptionSingleton.instance.isId3v2Save) {
				if (id3v2tag == null) {
					rfile = RandomAccessFile(file, "rw")
					ID3v24Tag().delete(rfile)
					ID3v23Tag().delete(rfile)
					ID3v22Tag().delete(rfile)
					logger.config("Deleting ID3v2 tag:" + file.name)
					rfile.close()
				} else {
					logger.config("Writing ID3v2 tag:" + file.name)
					val mp3AudioHeader = audioHeader as MP3AudioHeader
					val mp3StartByte = mp3AudioHeader.mp3StartByte
					val newMp3StartByte = id3v2tag!!.write(file, mp3StartByte)
					if (mp3StartByte != newMp3StartByte) {
						logger.config(
							"New mp3 start byte: $newMp3StartByte"
						)
						mp3AudioHeader.mp3StartByte = newMp3StartByte
					}
				}
			}
			rfile = RandomAccessFile(file, "rw")

			//Lyrics 3 Tag
			if (TagOptionSingleton.instance.isLyrics3Save) {
				lyrics3tag?.write(rfile)
			}
			//ID3v1 tag
			if (TagOptionSingleton.instance.isId3v1Save) {
				logger.config("Processing ID3v1")
				if (id3v1tag == null) {
					logger.config("Deleting ID3v1")
					ID3v1Tag().delete(rfile)
				} else {
					logger.config("Saving ID3v1")
					id3v1tag!!.write(rfile)
				}
			}
		} catch (ex: FileNotFoundException) {
			logger.log(
				Level.SEVERE,
				ErrorMessage.GENERAL_WRITE_FAILED_BECAUSE_FILE_NOT_FOUND.getMsg(file.name),
				ex
			)
			throw ex
		} catch (iex: IOException) {
			logger.log(
				Level.SEVERE,
				ErrorMessage.GENERAL_WRITE_FAILED_BECAUSE.getMsg(file.name, iex.message),
				iex
			)
			throw iex
		} catch (re: RuntimeException) {
			logger.log(
				Level.SEVERE,
				ErrorMessage.GENERAL_WRITE_FAILED_BECAUSE.getMsg(file.name, re.message),
				re
			)
			throw re
		} finally {
			rfile?.close()
		}
	}

	/**
	 * Displays MP3File Structure
	 */
	override fun displayStructureAsXML(): String {
		createXMLStructureFormatter()
		structureFormatter!!.openHeadingElement("file", file!!.absolutePath)
		if (iD3v1Tag != null) {
			iD3v1Tag!!.createStructure()
		}
		if (iD3v2Tag != null) {
			iD3v2Tag!!.createStructure()
		}
		structureFormatter!!.closeHeadingElement("file")
		return structureFormatter.toString()
	}

	/**
	 * Displays MP3File Structure
	 */
	override fun displayStructureAsPlainText(): String {
		createPlainTextStructureFormatter()
		structureFormatter!!.openHeadingElement("file", file!!.absolutePath)
		if (iD3v1Tag != null) {
			iD3v1Tag!!.createStructure()
		}
		if (iD3v2Tag != null) {
			iD3v2Tag!!.createStructure()
		}
		structureFormatter!!.closeHeadingElement("file")
		return structureFormatter.toString()
	}

	override var tag: Tag? = super.tag
		/**
		 * Set the Tag
		 *
		 * If the parameter tag is a v1tag then the v1 tag is set if v2tag then the v2tag.
		 */
		set(value) {
			field = value
			if (value is ID3v1Tag) {
				iD3v1Tag = value
			} else {
				setID3v2Tag(value as AbstractID3v2Tag)
			}
		}

	/** Create Default Tag
	 *
	 * @return
	 */
	override fun createDefaultTag(): Tag {
		if (TagOptionSingleton.instance.iD3V2Version === ID3V2Version.ID3_V24) {
			return ID3v24Tag()
		} else if (TagOptionSingleton.instance.iD3V2Version === ID3V2Version.ID3_V23) {
			return ID3v23Tag()
		} else if (TagOptionSingleton.instance.iD3V2Version === ID3V2Version.ID3_V22) {
			return ID3v22Tag()
		}
		//Default in case not set somehow
		return ID3v24Tag()
	}

	override val tagOrCreateDefault: Tag?
		/**
		 * Overridden to only consider ID3v2 Tag
		 *
		 * @return
		 */
		get() = iD3v2Tag ?: createDefaultTag()


	override val tagAndConvertOrCreateDefault: Tag
		/**
		 * Get the ID3v2 tag and convert to preferred version or if the file doesn't have one at all
		 * create a default tag of preferred version and set it. The file may already contain a ID3v1 tag but because
		 * this is not terribly useful the v1tag is not considered for this problem.
		 *
		 * @return
		 */
		get() {
			val tag = tagOrCreateDefault
			val convertedTag: Tag? =
				convertID3Tag(tag as AbstractID3v2Tag, TagOptionSingleton.instance.iD3V2Version)
			return convertedTag ?: tag
		}

	override val tagAndConvertOrCreateAndSetDefault: Tag
		/**
		 * Get the ID3v2 tag and convert to preferred version and set as the current tag
		 *
		 * @return
		 */
		get() = tagAndConvertOrCreateDefault.apply { tag = this }

	companion object {
		private const val MINIMUM_FILESIZE = 150

		var structureFormatter: AbstractTagDisplayFormatter? = null
			protected set

		/* Load ID3V1tag if exists */
		const val LOAD_IDV1TAG = 2

		/* Load ID3V2tag if exists */
		const val LOAD_IDV2TAG = 4

		/**
		 * This option is currently ignored
		 */
		const val LOAD_LYRICS3 = 8
		const val LOAD_ALL = LOAD_IDV1TAG or LOAD_IDV2TAG or LOAD_LYRICS3

		private fun createXMLStructureFormatter() {
			structureFormatter = XMLTagDisplayFormatter()
		}

		private fun createPlainTextStructureFormatter() {
			structureFormatter = PlainTextTagDisplayFormatter()
		}
	}
}
