/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core.index.impl;

import java.io.UTFDataFormatException;

public class CodeByteStream {
	protected byte[] bytes;
	protected int byteOffset= 0;
	protected int bitOffset= 0;
	protected int markByteOffset= -1;
	protected int markBitOffset= -1;

	public CodeByteStream() {
		this(16);
	}
	public CodeByteStream(byte[] bytes) {
		this.bytes= bytes;
	}
	public CodeByteStream(int initialByteLength) {
		bytes= new byte[initialByteLength];
	}
	public int byteLength() {
		return (bitOffset + 7) / 8 + byteOffset;
	}
	public byte[] getBytes(int startOffset, int endOffset) {
		int byteLength= byteLength();
		if (startOffset > byteLength || endOffset > byteLength || startOffset > endOffset)
			throw new IndexOutOfBoundsException();
		int length= endOffset - startOffset;
		byte[] result= new byte[length];
		System.arraycopy(bytes, startOffset, result, 0, length);
		if (endOffset == byteLength && bitOffset != 0) {
			int mask= (1 << bitOffset) - 1;
			result[length - 1] &= (mask << 8 - bitOffset);
		}
		return result;
	}
	protected void grow() {
		byte[] newBytes= new byte[bytes.length * 2 + 1];
		System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
		bytes= newBytes;
	}
	public void mark() {
		markByteOffset= byteOffset;
		markBitOffset= bitOffset;
	}
	/**
	 * Reads a single bit (value == 0 or == 1).
	 */
	public int readBit() {
		int value= (bytes[byteOffset] >> (7 - bitOffset)) & 1;
		if (++bitOffset >= 8) {
			bitOffset= 0;
			++byteOffset;
		}
		return value;
	}
	/**
	 * Read up to 32 bits from the stream.
	 */
	public int readBits(int numBits) {
		int value= 0;
		while (numBits > 0) {
			int bitsToRead= 8 - bitOffset;
			if (bitsToRead > numBits)
				bitsToRead= numBits;
			int mask= (1 << bitsToRead) - 1;
			value |= ((bytes[byteOffset] >> (8 - bitOffset - bitsToRead)) & mask) << (numBits - bitsToRead);
			numBits -= bitsToRead;
			bitOffset += bitsToRead;
			if (bitOffset >= 8) {
				bitOffset -= 8;
				byteOffset += 1;
			}
		}
		return value;
	}
	public final int readByte() {

		// no need to rebuild byte value from bit sequences
		if (bitOffset == 0) return bytes[byteOffset++] & 255;
	
		int value= 0;
		int numBits = 8;
		while (numBits > 0) {
			int bitsToRead= 8 - bitOffset;
			if (bitsToRead > numBits)
				bitsToRead= numBits;
			int mask= (1 << bitsToRead) - 1;
			value |= ((bytes[byteOffset] >> (8 - bitOffset - bitsToRead)) & mask) << (numBits - bitsToRead);
			numBits -= bitsToRead;
			bitOffset += bitsToRead;
			if (bitOffset >= 8) {
				bitOffset -= 8;
				byteOffset += 1;
			}
		}
		return value;
	}
	/**
	 * Reads a value using Gamma coding.
	 */
	public int readGamma() {
		int numBits= readUnary();
		return readBits(numBits - 1) | (1 << (numBits - 1));
	}
	public char[] readSmallUTF() throws UTFDataFormatException {
		int utflen= readByte();
		char str[]= new char[utflen];
		int count= 0;
		int strlen= 0;
		while (count < utflen) {
			int c= readByte();
			int char2, char3;
			switch (c >> 4) {
				case 0 :
				case 1 :
				case 2 :
				case 3 :
				case 4 :
				case 5 :
				case 6 :
				case 7 :
					// 0xxxxxxx
					count++;
					str[strlen++]= (char) c;
					break;
				case 12 :
				case 13 :
					// 110x xxxx   10xx xxxx
					count += 2;
					if (count > utflen)
						throw new UTFDataFormatException();
					char2= readByte();
					if ((char2 & 0xC0) != 0x80)
						throw new UTFDataFormatException();
					str[strlen++]= (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
					break;
				case 14 :
					// 1110 xxxx  10xx xxxx  10xx xxxx
					count += 3;
					if (count > utflen)
						throw new UTFDataFormatException();
					char2= readByte();
					char3= readByte();
					if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
						throw new UTFDataFormatException();
					str[strlen++]= (char) (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
					break;
				default :
					// 10xx xxxx,  1111 xxxx
					throw new UTFDataFormatException();
			}
		}
		if (strlen < utflen)
			System.arraycopy(str, 0, str= new char[strlen], 0, strlen);
		return str;
	}
	/**
	 *  Reads a value in unary.
	 */
	public int readUnary() {
		int value= 1;
		int mask= 1 << (7 - bitOffset);
		while ((bytes[byteOffset] & mask) != 0) {
			++value;
			if (++bitOffset >= 8) {
				bitOffset= 0;
				++byteOffset;
				mask= 0x80;
			} else {
				mask >>>= 1;
			}
		}
		// skip the 0 bit
		if (++bitOffset >= 8) {
			bitOffset= 0;
			++byteOffset;
		}
		return value;
	}
	public void reset() {
		byteOffset= bitOffset= 0;
		markByteOffset= markBitOffset= -1;
	}
	public void reset(byte[] bytes) {
		this.bytes= bytes;
		reset();
	}
	public void reset(byte[] bytes, int byteOffset) {
		reset(bytes);
		this.byteOffset= byteOffset;
	}
	public boolean resetToMark() {
		if (markByteOffset == -1)
			return false;
		byteOffset= markByteOffset;
		bitOffset= markBitOffset;
		markByteOffset= markBitOffset= -1;
		return true;
	}
	public void skipBits(int numBits) {
		int newOffset= byteOffset * 8 + bitOffset + numBits;
		if (newOffset < 0 || (newOffset + 7) / 8 >= bytes.length)
			throw new IllegalArgumentException();
		byteOffset= newOffset / 8;
		bitOffset= newOffset % 8;
	}
	public byte[] toByteArray() {
		return getBytes(0, byteLength());
	}
	/**
	 * Writes a single bit (value == 0 or == 1).
	 */
	public void writeBit(int value) {
		bytes[byteOffset] |= (value & 1) << (7 - bitOffset);
		if (++bitOffset >= 8) {
			bitOffset= 0;
			if (++byteOffset >= bytes.length)
				grow();
		}
	}
	/**
	 * Write up to 32 bits to the stream.
	 * The least significant numBits bits of value are written.
	 */
	public void writeBits(int value, int numBits) {
		while (numBits > 0) {
			int bitsToWrite= 8 - bitOffset;
			if (bitsToWrite > numBits)
				bitsToWrite= numBits;
			int shift= 8 - bitOffset - bitsToWrite;
			int mask= ((1 << bitsToWrite) - 1) << shift;
			bytes[byteOffset]= (byte) ((bytes[byteOffset] & ~mask) | (((value >>> (numBits - bitsToWrite)) << shift) & mask));
			numBits -= bitsToWrite;
			bitOffset += bitsToWrite;
			if (bitOffset >= 8) {
				bitOffset -= 8;
				if (++byteOffset >= bytes.length)
					grow();
			}
		}
	}
	public void writeByte(int value) {
		writeBits(value, 8);
	}
	/**
	 * Writes the given value using Gamma coding, in which positive integer x
	 * is represented by coding floor(log2(x) in unary followed by the value 
	 * of x - 2**floor(log2(x)) in binary.
	 * The value must be >= 1.
	 */
	public void writeGamma(int value) {
		if (value < 1)
			throw new IllegalArgumentException();
		int temp= value;
		int numBits= 0;
		while (temp != 0) {
			temp >>>= 1;
			++numBits;
		}
		writeUnary(numBits);
		writeBits(value, numBits - 1);
	}
	public void writeSmallUTF(char[] str) {
		writeSmallUTF(str, 0, str.length);
	}
	public void writeSmallUTF(char[] str, int start, int end) {
		int utflen= 0;
		for (int i= start; i < end; i++) {
			int c= str[i];
			if ((c >= 0x0001) && (c <= 0x007F)) {
				utflen++;
			} else if (c > 0x07FF) {
				utflen += 3;
			} else {
				utflen += 2;
			}
		}
		if (utflen > 255)
			throw new IllegalArgumentException();
		writeByte(utflen & 0xFF);
		for (int i= start; i < end; i++) {
			int c= str[i];
			if ((c >= 0x0001) && (c <= 0x007F)) {
				writeByte(c);
			} else if (c > 0x07FF) {
				writeByte(0xE0 | ((c >> 12) & 0x0F));
				writeByte(0x80 | ((c >> 6) & 0x3F));
				writeByte(0x80 | ((c >> 0) & 0x3F));
			} else {
				writeByte(0xC0 | ((c >> 6) & 0x1F));
				writeByte(0x80 | ((c >> 0) & 0x3F));
			}
		}
	}
	/**
	 * Write the given value in unary.  The value must be >= 1.
	 */
	public void writeUnary(int value) {
		if (value < 1)
			throw new IllegalArgumentException();
		int mask= 1 << (7 - bitOffset);
		// write N-1 1-bits
		while (--value > 0) {
			bytes[byteOffset] |= mask;
			if (++bitOffset >= 8) {
				bitOffset= 0;
				if (++byteOffset >= bytes.length)
					grow();
				mask= 0x80;
			} else {
				mask >>>= 1;
			}
		}
		// write a 0-bit
		bytes[byteOffset] &= ~mask;
		if (++bitOffset >= 8) {
			bitOffset= 0;
			if (++byteOffset >= bytes.length)
				grow();
		}
	}
	public void writeUTF(char[] str) {
		int strlen= str.length;
		int utflen= 0;
		for (int i= 0; i < strlen; i++) {
			int c= str[i];
			if ((c >= 0x0001) && (c <= 0x007F)) {
				utflen++;
			} else if (c > 0x07FF) {
				utflen += 3;
			} else {
				utflen += 2;
			}
		}
		if (utflen > 65535)
			throw new IllegalArgumentException();
		writeByte((utflen >>> 8) & 0xFF);
		writeByte((utflen >>> 0) & 0xFF);
		for (int i= 0; i < strlen; i++) {
			int c= str[i];
			if ((c >= 0x0001) && (c <= 0x007F)) {
				writeByte(c);
			} else if (c > 0x07FF) {
				writeByte(0xE0 | ((c >> 12) & 0x0F));
				writeByte(0x80 | ((c >> 6) & 0x3F));
				writeByte(0x80 | ((c >> 0) & 0x3F));
			} else {
				writeByte(0xC0 | ((c >> 6) & 0x1F));
				writeByte(0x80 | ((c >> 0) & 0x3F));
			}
		}
	}
}
