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

/**
 * Uses prefix coding on words, and gamma coding of document numbers differences.
 */
public class GammaCompressedIndexBlock extends IndexBlock {
	CodeByteStream writeCodeStream= new CodeByteStream();
	CodeByteStream readCodeStream;
	char[] prevWord= null;
	int offset= 0;

	public GammaCompressedIndexBlock(int blockSize) {
		super(blockSize);
		readCodeStream= new CodeByteStream(field.buffer());
	}
	/**
	 * @see IndexBlock#addEntry
	 */
	public boolean addEntry(WordEntry entry) {
		writeCodeStream.reset();
		encodeEntry(entry, prevWord, writeCodeStream);
		if (offset + writeCodeStream.byteLength() > this.blockSize - 2) {
			return false;
		}
		byte[] bytes= writeCodeStream.toByteArray();
		field.put(offset, bytes);
		offset += bytes.length;
		prevWord= entry.getWord();
		return true;
	}
	protected void encodeEntry(WordEntry entry, char[] prevWord, CodeByteStream codeStream) {
		char[] word= entry.getWord();
		int prefixLen= prevWord == null ? 0 : Util.prefixLength(prevWord, word);
		codeStream.writeByte(prefixLen);
		codeStream.writeSmallUTF(word, prefixLen, word.length);
		int n= entry.getNumRefs();
		codeStream.writeGamma(n);
		int prevRef= 0;
		for (int i= 0; i < n; ++i) {
			int ref= entry.getRef(i);
			if (ref <= prevRef)
				throw new IllegalArgumentException();
			codeStream.writeGamma(ref - prevRef);
			prevRef= ref;
		}
	}
	/**
	 * @see IndexBlock#flush
	 */
	public void flush() {
		if (offset > 0) {
			field.putInt2(offset, 0);
			offset= 0;
			prevWord= null;
		}
	}
	/**
	 * @see IndexBlock#isEmpty
	 */
	public boolean isEmpty() {
		return offset == 0;
	}
	/**
	 * @see IndexBlock#nextEntry
	 */
	public boolean nextEntry(WordEntry entry) {
		try {
			readCodeStream.reset(field.buffer(), offset);
			int prefixLength= readCodeStream.readByte();
			char[] word= readCodeStream.readSmallUTF();
			if (prevWord != null && prefixLength > 0) {
				char[] temp= new char[prefixLength + word.length];
				System.arraycopy(prevWord, 0, temp, 0, prefixLength);
				System.arraycopy(word, 0, temp, prefixLength, word.length);
				word= temp;
			}
			if (word.length == 0) {
				return false;
			}
			entry.reset(word);
			int n= readCodeStream.readGamma();
			int prevRef= 0;
			for (int i= 0; i < n; ++i) {
				int ref= prevRef + readCodeStream.readGamma();
				if (ref < prevRef)
					throw new InternalError();
				entry.addRef(ref);
				prevRef= ref;
			}
			offset= readCodeStream.byteLength();
			prevWord= word;
			return true;
		} catch (UTFDataFormatException e) {
			return false;
		}
	}
	/**
	 * @see IndexBlock#reset
	 */
	public void reset() {
		super.reset();
		offset= 0;
		prevWord= null;
	}
}
