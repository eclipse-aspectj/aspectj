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

public class Field {
	protected byte[] buffer; // contents
	protected int offset; // offset of the field within the byte array
	protected int length; // length of the field

	/**
	 * ByteSegment constructor comment.
	 */
	public Field(byte[] bytes) {
		this.buffer= bytes;
		this.offset= 0;
		this.length= bytes.length;
	}
	/**
	 * ByteSegment constructor comment.
	 */
	public Field(byte[] bytes, int length) {
		this.buffer= bytes;
		this.offset= 0;
		this.length= length;
	}
	/**
	 * ByteSegment constructor comment.
	 */
	public Field(byte[] bytes, int offset, int length) {
		this.buffer= bytes;
		this.offset= offset;
		this.length= length;
	}
	/**
	 * Creates a new field containing an empty buffer of the given length.
	 */
	public Field(int length) {
		this.buffer= new byte[length];
		this.offset= 0;
		this.length= length;
	}
	public byte[] buffer() {
		return buffer;
	}
	public Field buffer(byte[] buffer) {
		this.buffer= buffer;
		return this;
	}
	public Field clear() {
		clear(buffer, offset, length);
		return this;
	}
	protected static void clear(byte[] buffer, int offset, int length) {
		int n= offset;
		for (int i= 0; i < length; i++) {
			buffer[n]= 0;
			n++;
		}
	}
	public Field clear(int length) {
		clear(buffer, offset, length);
		return this;
	}
	public Field clear(int offset, int length) {
		clear(buffer, this.offset + offset, length);
		return this;
	}
	protected static int compare(byte[] buffer1, int offset1, int length1, byte[] buffer2, int offset2, int length2) {
		int n= Math.min(length1, length2);
		for (int i= 0; i < n; i++) {
			int j1= buffer1[offset1 + i] & 255;
			int j2= buffer2[offset2 + i] & 255;
			if (j1 > j2)
				return 1;
			if (j1 < j2)
				return -1;
		}
		if (length1 > n) {
			for (int i= n; i < length1; i++)
				if (buffer1[offset1 + i] != 0)
					return 1;
			return 0;
		}
		for (int i= n; i < length2; i++)
			if (buffer2[offset2 + i] != 0)
				return -1;
		return 0;
	}
	public static int compare(Field f1, Field f2) {
		return compare(f1.buffer, f1.offset, f1.length, f2.buffer, f2.offset, f2.length);
	}
	// copy bytes from one offset to another within the field
	public Field copy(int fromOffset, int toOffset, int length) {
		System.arraycopy(buffer, offset + fromOffset, buffer, offset + toOffset, length);
		return this;
	}
	public Field dec(int n) {
		offset -= n;
		return this;
	}
	public byte[] get() {
		byte[] result= new byte[length];
		System.arraycopy(buffer, offset, result, 0, length);
		return result;
	}
	public byte[] get(int offset, int length) {
		byte[] result= new byte[length];
		System.arraycopy(buffer, this.offset + offset, result, 0, length);
		return result;
	}
	public Field getField(int offset, int length) {
		return new Field(buffer, this.offset + offset, length);
	}
	public int getInt1() {
		return buffer[this.offset];
	}
	public int getInt1(int offset) {
		return buffer[this.offset + offset];
	}
	public int getInt2() {
		int i= this.offset;
		int v= buffer[i++];
		v= (v << 8) | (buffer[i++] & 255);
		return v;
	}
	public int getInt2(int offset) {
		int i= this.offset + offset;
		int v= buffer[i++];
		v= (v << 8) | (buffer[i++] & 255);
		return v;
	}
	public int getInt3() {
		int i= this.offset;
		int v= buffer[i++];
		v= (v << 8) | (buffer[i++] & 255);
		v= (v << 8) | (buffer[i++] & 255);
		return v;
	}
	public int getInt3(int offset) {
		int i= this.offset + offset;
		int v= buffer[i++];
		v= (v << 8) | (buffer[i++] & 255);
		v= (v << 8) | (buffer[i++] & 255);
		return v;
	}
	public int getInt4() {
		int i= this.offset;
		int v= buffer[i++];
		v= (v << 8) | (buffer[i++] & 255);
		v= (v << 8) | (buffer[i++] & 255);
		v= (v << 8) | (buffer[i++] & 255);
		return v;
	}
	public int getInt4(int offset) {
		int i= this.offset + offset;
		int v= buffer[i++];
		v= (v << 8) | (buffer[i++] & 255);
		v= (v << 8) | (buffer[i++] & 255);
		v= (v << 8) | (buffer[i++] & 255);
		return v;
	}
	public int getUInt1() {
		return buffer[this.offset] & 255;
	}
	public int getUInt1(int offset) {
		return buffer[this.offset + offset] & 255;
	}
	public int getUInt2() {
		int i= this.offset;
		int v= (buffer[i++] & 255);
		v= (v << 8) | (buffer[i++] & 255);
		return v;
	}
	public int getUInt2(int offset) {
		int i= this.offset + offset;
		int v= (buffer[i++] & 255);
		v= (v << 8) | (buffer[i++] & 255);
		return v;
	}
	public int getUInt3() {
		int i= this.offset;
		int v= (buffer[i++] & 255);
		v= (v << 8) | (buffer[i++] & 255);
		v= (v << 8) | (buffer[i++] & 255);
		return v;
	}
	public int getUInt3(int offset) {
		int i= this.offset + offset;
		int v= (buffer[i++] & 255);
		v= (v << 8) | (buffer[i++] & 255);
		v= (v << 8) | (buffer[i++] & 255);
		return v;
	}
	public char[] getUTF(int offset) throws UTFDataFormatException {
		int pos= this.offset + offset;
		int utflen= getUInt2(pos);
		pos += 2;
		char str[]= new char[utflen];
		int count= 0;
		int strlen= 0;
		while (count < utflen) {
			int c= buffer[pos++] & 0xFF;
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
					char2= buffer[pos++] & 0xFF;
					if ((char2 & 0xC0) != 0x80)
						throw new UTFDataFormatException();
					str[strlen++]= (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
					break;
				case 14 :
					// 1110 xxxx  10xx xxxx  10xx xxxx
					count += 3;
					if (count > utflen)
						throw new UTFDataFormatException();
					char2= buffer[pos++] & 0xFF;
					char3= buffer[pos++] & 0xFF;
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
	public Field inc(int n) {
		offset += n;
		return this;
	}
	public int length() {
		return length;
	}
	public Field length(int length) {
		this.length= length;
		return this;
	}
	/**
	Returns the offset into the underlying byte array that this field is defined over.
	*/
	public int offset() {
		return offset;
	}
	public Field offset(int offset) {
		this.offset= offset;
		return this;
	}
	public Field pointTo(int offset) {
		return new Field(buffer, this.offset + offset, 0);
	}
	public Field put(byte[] b) {
		return put(0, b);
	}
	public Field put(int offset, byte[] b) {
		System.arraycopy(b, 0, buffer, this.offset + offset, b.length);
		return this;
	}
	public Field put(int offset, Field f) {
		System.arraycopy(f.buffer, f.offset, buffer, this.offset + offset, f.length);
		return this;
	}
	public Field put(Field f) {
		System.arraycopy(f.buffer, f.offset, buffer, offset, f.length);
		return this;
	}
	public Field putInt1(int n) {
		buffer[offset]= (byte) (n);
		return this;
	}
	public Field putInt1(int offset, int n) {
		buffer[this.offset + offset]= (byte) (n);
		return this;
	}
	public Field putInt2(int n) {
		int i= offset;
		buffer[i++]= (byte) (n >> 8);
		buffer[i++]= (byte) (n >> 0);
		return this;
	}
	public Field putInt2(int offset, int n) {
		int i= this.offset + offset;
		buffer[i++]= (byte) (n >> 8);
		buffer[i++]= (byte) (n >> 0);
		return this;
	}
	public Field putInt3(int n) {
		int i= offset;
		buffer[i++]= (byte) (n >> 16);
		buffer[i++]= (byte) (n >> 8);
		buffer[i++]= (byte) (n >> 0);
		return this;
	}
	public Field putInt3(int offset, int n) {
		int i= this.offset + offset;
		buffer[i++]= (byte) (n >> 16);
		buffer[i++]= (byte) (n >> 8);
		buffer[i++]= (byte) (n >> 0);
		return this;
	}
	public Field putInt4(int n) {
		int i= offset;
		buffer[i++]= (byte) (n >> 24);
		buffer[i++]= (byte) (n >> 16);
		buffer[i++]= (byte) (n >> 8);
		buffer[i++]= (byte) (n >> 0);
		return this;
	}
	public Field putInt4(int offset, int n) {
		int i= this.offset + offset;
		buffer[i++]= (byte) (n >> 24);
		buffer[i++]= (byte) (n >> 16);
		buffer[i++]= (byte) (n >> 8);
		buffer[i++]= (byte) (n >> 0);
		return this;
	}
	public int putUTF(int offset, char[] str) {
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
		int pos= this.offset + offset;
		buffer[pos++]= (byte) ((utflen >>> 8) & 0xFF);
		buffer[pos++]= (byte) ((utflen >>> 0) & 0xFF);
		for (int i= 0; i < strlen; i++) {
			int c= str[i];
			if ((c >= 0x0001) && (c <= 0x007F)) {
				buffer[pos++]= ((byte) c);
			} else if (c > 0x07FF) {
				buffer[pos++]= ((byte) (0xE0 | ((c >> 12) & 0x0F)));
				buffer[pos++]= ((byte) (0x80 | ((c >> 6) & 0x3F)));
				buffer[pos++]= ((byte) (0x80 | ((c >> 0) & 0x3F)));
			} else {
				buffer[pos++]= ((byte) (0xC0 | ((c >> 6) & 0x1F)));
				buffer[pos++]= ((byte) (0x80 | ((c >> 0) & 0x3F)));
			}
		}
		return 2 + utflen;
	}
}
