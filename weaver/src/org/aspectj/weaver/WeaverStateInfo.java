/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.aspectj.bridge.IMessage;
import org.aspectj.weaver.bcel.BcelTypeMunger;


/**
 * WeaverStateInfo represents how a type was processed.  It is used by the weaver to determine how a type 
 * was previously treated and whether reweaving is allowed.
 * The format in the data stream is:
 * 
 * Byte:  Kind.  UNTOUCHED|WOVEN|EXTENDED - If extended it can have two extra bits set 'REWEAVABLE' and 'REWEAVABLE_COMPRESSION_BIT'
 * Short: typeMungerCount - how many type mungers have affected this type
 * <TypeX & ResolvedTypeMunger>: The type mungers themselves
 * If we are reweavable then we also have:
 * Short: Number of aspects that touched this type in some way when it was previously woven
 * <String> The fully qualified name of each type
 * Int: Length of class file data (i.e. the unwovenclassfile)
 * Byte[]: The class file data, compressed if REWEAVABLE_COMPRESSION_BIT set.
 */


public class WeaverStateInfo {
	private List/*Entry*/ typeMungers;
	private boolean oldStyle;
	
	
	
	private boolean reweavable;
	private boolean reweavableCompressedMode;     // If true, unwovenClassFile is compressed on write and uncompressed on read
	private Set /*String*/ aspectsAffectingType;  // These must exist in the world for reweaving to be valid
	private byte[] unwovenClassFile;			  // Original 'untouched' class file
	private static boolean reweavableDefault = false;
	private static boolean reweavableCompressedModeDefault = false;
	
	public WeaverStateInfo() {
		this(new ArrayList(), false,reweavableDefault,reweavableCompressedModeDefault);
	}
	
	private WeaverStateInfo(List typeMungers, boolean oldStyle,boolean reweavableMode,boolean reweavableCompressedMode) {
		this.typeMungers = typeMungers;
		this.oldStyle    = oldStyle;
		this.reweavable  = reweavableMode;
		this.reweavableCompressedMode = reweavableCompressedMode;
		this.aspectsAffectingType= new HashSet();
		this.unwovenClassFile = null;
	}
	
	public static void setReweavableModeDefaults(boolean mode, boolean compress) {
		reweavableDefault = mode;
		reweavableCompressedModeDefault = compress;
	}
	
	private static final int UNTOUCHED=0, WOVEN=2, EXTENDED=3;
	
	// Use 'bits' for these capabilities - only valid in EXTENDED mode
	private static final byte REWEAVABLE_BIT             = 1<<4;
	private static final byte REWEAVABLE_COMPRESSION_BIT = 1<<5;
	
	public static final WeaverStateInfo read(DataInputStream s, ISourceContext context) throws IOException {
		byte b = s.readByte();
		
		boolean isReweavable = ((b&REWEAVABLE_BIT)!=0);
		if (isReweavable) b=(byte) (b-REWEAVABLE_BIT);

		boolean isReweavableCompressed = ((b&REWEAVABLE_COMPRESSION_BIT)!=0);
		if (isReweavableCompressed) b=(byte) (b-REWEAVABLE_COMPRESSION_BIT);

		switch(b) {
			case UNTOUCHED:
				throw new RuntimeException("unexpected UNWOVEN");
			case WOVEN: 
				return new WeaverStateInfo(Collections.EMPTY_LIST, true,isReweavable,isReweavableCompressed);
			case EXTENDED:
				int n = s.readShort();
				List l = new ArrayList();
				for (int i=0; i < n; i++) {
					TypeX aspectType = TypeX.read(s);
					ResolvedTypeMunger typeMunger = 
						ResolvedTypeMunger.read(s, context);
					l.add(new Entry(aspectType, typeMunger));
				}
			    WeaverStateInfo wsi = new WeaverStateInfo(l,false,isReweavable,isReweavableCompressed);
			    readAnyReweavableData(wsi,s);
				return wsi;
		} 
		throw new RuntimeException("bad WeaverState.Kind: " + b);
	}
	
	
	
	private static class Entry {
		public TypeX aspectType;
		public ResolvedTypeMunger typeMunger;
		public Entry(TypeX aspectType, ResolvedTypeMunger typeMunger) {
			this.aspectType = aspectType;
			this.typeMunger = typeMunger;
		}
		
		public String toString() {
			return "<" + aspectType + ", " + typeMunger + ">";
		}
	} 

	public void write(DataOutputStream s) throws IOException {
		if (oldStyle) throw new RuntimeException("shouldn't be writing this");
		
		byte weaverStateInfoKind = EXTENDED;
		if (reweavable) weaverStateInfoKind |= REWEAVABLE_BIT;
		if (reweavableCompressedMode) weaverStateInfoKind |= REWEAVABLE_COMPRESSION_BIT;
		s.writeByte(weaverStateInfoKind);
		int n = typeMungers.size();
		s.writeShort(n);
		for (int i=0; i < n; i++) {
			Entry e = (Entry)typeMungers.get(i);
			e.aspectType.write(s);
			e.typeMunger.write(s);
		}
		writeAnyReweavableData(this,s);
	}

	public void addConcreteMunger(ConcreteTypeMunger munger) {
		typeMungers.add(new Entry(munger.getAspectType(), munger.getMunger()));
	}
	
	public String toString() {
		return "WeaverStateInfo(" + typeMungers + ", " + oldStyle + ")";
	}
	

	public List getTypeMungers(ResolvedTypeX onType) {
		World world = onType.getWorld();
		List ret = new ArrayList();
		for (Iterator i = typeMungers.iterator(); i.hasNext();) {
			Entry entry = (Entry) i.next();
			ResolvedTypeX aspectType = world.resolve(entry.aspectType, true);
			if (aspectType == ResolvedTypeX.MISSING) {
				world.showMessage(IMessage.ERROR,
					WeaverMessages.format(WeaverMessages.ASPECT_NEEDED,entry.aspectType,onType),
					onType.getSourceLocation(), null);
				continue;
			}
			
			ret.add(new BcelTypeMunger(entry.typeMunger, aspectType));
		}
		return ret;
	}

	public boolean isOldStyle() {
		return oldStyle;
	}

	public byte[] getUnwovenClassFileData() {
		return unwovenClassFile;
	}

	public void setUnwovenClassFileData(byte[] data) {
		unwovenClassFile = data;
	}

	public boolean isReweavable() {
		return reweavable;
	}
	
	public void setReweavable(boolean rw,boolean compressData) {
		reweavable = rw;
		reweavableCompressedMode = compressData;
	}
	
	public void addAspectsAffectingType(Collection /*String*/ aspects) {
		aspectsAffectingType.addAll(aspects);
	}
	public void addAspectAffectingType(String aspectType) {
		aspectsAffectingType.add(aspectType);
	}
	public Set /*String*/ getAspectsAffectingType() {
		return this.aspectsAffectingType;
	}


    ////
    
	private static void readAnyReweavableData(WeaverStateInfo wsi,DataInputStream s) throws IOException {

		if (wsi.isReweavable()) {		
			// Load list of aspects that need to exist in the world for reweaving to be 'legal'
			int numberAspectsAffectingType = s.readShort();
			for (int i=0; i < numberAspectsAffectingType; i++) {wsi.addAspectAffectingType(s.readUTF());} 
			
			int unwovenClassFileSize = s.readInt();
			byte[] classData = null;					
			// The data might or might not be compressed:
			if (!wsi.reweavableCompressedMode) {
				// Read it straight in
				classData = new byte[unwovenClassFileSize];
				int bytesread = s.read(classData);
				if (bytesread!=unwovenClassFileSize) 
				  throw new IOException("ERROR whilst reading reweavable data, expected "+
				                        unwovenClassFileSize+" bytes, only found "+bytesread);
			} else {
				// Decompress it
				classData = new byte[unwovenClassFileSize];
						
				ZipInputStream zis = new ZipInputStream(s);
				ZipEntry zen = zis.getNextEntry();
				int current = 0; 
				int bytesToGo=unwovenClassFileSize;
				while (bytesToGo>0) {
					int amount = zis.read(classData,current,bytesToGo);
					current+=amount;
					bytesToGo-=amount;
				}
				zis.closeEntry();
				if (bytesToGo!=0) 
				  throw new IOException("ERROR whilst reading compressed reweavable data, expected "+
				                        unwovenClassFileSize+" bytes, only found "+current);
			}
			wsi.setUnwovenClassFileData(classData);
		}
	}



	private static void writeAnyReweavableData(WeaverStateInfo wsi,DataOutputStream s) throws IOException {
		if (wsi.isReweavable()) {
			// Write out list of aspects that must exist next time we try and weave this class
			s.writeShort(wsi.aspectsAffectingType.size());
			if (wsi.aspectsAffectingType.size()>0) {
				for (Iterator iter = wsi.aspectsAffectingType.iterator(); iter.hasNext();) {
					String type = (String) iter.next();
					s.writeUTF(type);	
				}
			}
			byte[] data = wsi.unwovenClassFile;
			s.writeInt(data.length);
			// Do we need to compress the data?
			if (!wsi.reweavableCompressedMode) {
				s.write(wsi.unwovenClassFile);
			} else {
				ZipOutputStream zos = new ZipOutputStream(s);
				ZipEntry ze = new ZipEntry("data");
				zos.putNextEntry(ze);
				zos.write(wsi.unwovenClassFile,0,wsi.unwovenClassFile.length);
				zos.closeEntry();
			}
		}
	}

}
