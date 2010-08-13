/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.weaver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.aspectj.bridge.IMessage;
import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;

/**
 * WeaverStateInfo represents how a type was processed. It is used by the weaver to determine how a type was previously treated and
 * whether reweaving is allowed. The format in the data stream is:
 * 
 * Byte: Kind. UNTOUCHED|WOVEN|EXTENDED - If extended it can have two extra bits set 'REWEAVABLE' and 'REWEAVABLE_COMPRESSION_BIT'
 * Short: typeMungerCount - how many type mungers have affected this type <UnresolvedType & ResolvedTypeMunger>: The type mungers
 * themselves If we are reweavable then we also have: Short: Number of aspects that touched this type in some way when it was
 * previously woven <String> The fully qualified name of each type Int: Length of class file data (i.e. the unwovenclassfile)
 * Byte[]: The class file data, compressed if REWEAVABLE_COMPRESSION_BIT set.
 */
public class WeaverStateInfo {
	private List<Entry> typeMungers;
	private boolean oldStyle;

	private boolean reweavable;
	private boolean reweavableCompressedMode; // If true, unwovenClassFile is uncompressed on read
	private boolean reweavableDiffMode; // if true, unwovenClassFile is written and read as a diff

	// These must exist in the world for reweaving to be valid.
	// It is a set of signatures 'La/b/c/D;'
	private Set<String> aspectsAffectingType;

	private byte[] unwovenClassFile; // Original 'untouched' class file
	private static boolean reweavableDefault = true; // ajh02: changed from false;
	private static boolean reweavableCompressedModeDefault = false;
	private static boolean reweavableDiffModeDefault = true;

	// when serializing the WeaverStateInfo we come to adding the reweavable data,
	// we'd like to add a diff of the unwovenClassFile and the wovenClassFile,
	// but we don't have the wovenClassFile yet as we're still in the process of making it.
	// so we put this key there instead as a stub.
	// Then when the wovenClassFile has been made, replaceKeyWithDiff is called.
	private static byte[] key = { -51, 34, 105, 56, -34, 65, 45, 78, -26, 125, 114, 97, 98, 1, -1, -42 };
	private boolean unwovenClassFileIsADiff = false;

	int compressionEnabled = 0; // 0=dont know, 1=no, 2=yes

	private void checkCompressionEnabled() {
		if (compressionEnabled == 0) {
			// work it out!
			compressionEnabled = 1;
			try {
				String value = System.getProperty("aspectj.compression.weaverstateinfo", "false");
				if (value.equalsIgnoreCase("true")) {
					System.out.println("ASPECTJ: aspectj.compression.weaverstateinfo=true: compressing weaverstateinfo");
					compressionEnabled = 2;
				}
			} catch (Throwable t) {
				// nop
			}
		}
	}

	private WeaverStateInfo() {
		// this(new ArrayList(), false,reweavableDefault,reweavableCompressedModeDefault,reweavableDiffModeDefault);
	}

	public WeaverStateInfo(boolean reweavable) {
		this(new ArrayList<Entry>(), false, reweavable, reweavableCompressedModeDefault, reweavableDiffModeDefault);
	}

	private WeaverStateInfo(List<Entry> typeMungers, boolean oldStyle, boolean reweavableMode, boolean reweavableCompressedMode,
			boolean reweavableDiffMode) {
		this.typeMungers = typeMungers;
		this.oldStyle = oldStyle;
		this.reweavable = reweavableMode;
		this.reweavableCompressedMode = reweavableCompressedMode;
		this.reweavableDiffMode = reweavableMode ? reweavableDiffMode : false;
		this.aspectsAffectingType = new HashSet<String>();
		this.unwovenClassFile = null;
	}

	public static void setReweavableModeDefaults(boolean mode, boolean compress, boolean diff) {
		reweavableDefault = mode;
		reweavableCompressedModeDefault = compress;
		reweavableDiffModeDefault = diff;
	}

	private static final int UNTOUCHED = 0, WOVEN = 2, EXTENDED = 3;

	// Use 'bits' for these capabilities - only valid in EXTENDED mode
	private static final byte REWEAVABLE_BIT = 1 << 4;
	private static final byte REWEAVABLE_COMPRESSION_BIT = 1 << 5;
	private static final byte REWEAVABLE_DIFF_BIT = 1 << 6;

	/** See comments on write() */
	public static final WeaverStateInfo read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		byte b = s.readByte();

		boolean isReweavable = ((b & REWEAVABLE_BIT) != 0);
		if (isReweavable) {
			b = (byte) (b - REWEAVABLE_BIT);
		}

		boolean isReweavableCompressed = ((b & REWEAVABLE_COMPRESSION_BIT) != 0);
		if (isReweavableCompressed) {
			b = (byte) (b - REWEAVABLE_COMPRESSION_BIT);
		}

		boolean isReweavableDiff = ((b & REWEAVABLE_DIFF_BIT) != 0);
		if (isReweavableDiff) {
			b = (byte) (b - REWEAVABLE_DIFF_BIT);
		}

		switch (b) {
		case UNTOUCHED:
			throw new RuntimeException("unexpected UNWOVEN");
		case WOVEN:
			return new WeaverStateInfo(Collections.EMPTY_LIST, true, isReweavable, isReweavableCompressed, isReweavableDiff);
		case EXTENDED:
			boolean isCompressed = false;
			if (s.isAtLeast169()) {
				isCompressed = s.readBoolean();
			}

			int n = s.readShort();
			List l = new ArrayList();
			for (int i = 0; i < n; i++) {
				// conditional on version
				UnresolvedType aspectType = null;
				if (isCompressed) {
					int cpIndex = s.readShort();
					String signature = s.readUtf8(cpIndex);
					if (signature.charAt(0) == '@') { // '@missing@'
						aspectType = ResolvedType.MISSING;
					} else {
						aspectType = UnresolvedType.forSignature(signature);
					}
				} else {
					aspectType = UnresolvedType.read(s);
				}
				ResolvedTypeMunger typeMunger = ResolvedTypeMunger.read(s, context);
				l.add(new Entry(aspectType, typeMunger));
			}
			WeaverStateInfo wsi = new WeaverStateInfo(l, false, isReweavable, isReweavableCompressed, isReweavableDiff);
			readAnyReweavableData(wsi, s, isCompressed);
			return wsi;
		}
		throw new RuntimeException("bad WeaverState.Kind: " + b + ".  File was :"
				+ (context == null ? "unknown" : context.makeSourceLocation(0, 0).toString()));
	}

	private static class Entry {
		public UnresolvedType aspectType;
		public ResolvedTypeMunger typeMunger;

		public Entry(UnresolvedType aspectType, ResolvedTypeMunger typeMunger) {
			this.aspectType = aspectType;
			this.typeMunger = typeMunger;
		}

		public String toString() {
			return "<" + aspectType + ", " + typeMunger + ">";
		}
	}

	/**
	 * Serialize the WeaverStateInfo. Various bits are set within the 'kind' flag to indicate the structure of the attribute. In
	 * reweavable diff mode a 'marker' is inserted at the start of the attribute to indicate where the final calculated diff should
	 * be inserted. When the key is replaced with the diff, the 'kind' byte moves to the front of the attribute - thats why in the
	 * read logic you'll see it expecting the kind as the first byte.
	 */
	public void write(CompressingDataOutputStream s) throws IOException {
		checkCompressionEnabled();
		if (oldStyle || reweavableCompressedMode) {
			throw new RuntimeException("shouldn't be writing this");
		}

		byte weaverStateInfoKind = EXTENDED;
		if (reweavable) {
			weaverStateInfoKind |= REWEAVABLE_BIT;
		}

		if (reweavableDiffMode) {
			s.write(key); // put key in so we can replace it with the diff later
			weaverStateInfoKind |= REWEAVABLE_DIFF_BIT;
		}

		s.writeByte(weaverStateInfoKind);

		// Tag whether the remainder of the data is subject to cp compression
		try {
			s.compressionEnabled = compressionEnabled == 2;
			s.writeBoolean(s.canCompress());

			int n = typeMungers.size();
			s.writeShort(n);
			for (Entry e : typeMungers) {
				if (s.canCompress()) {
					s.writeCompressedSignature(e.aspectType.getSignature());
				} else {
					e.aspectType.write(s);
				}
				e.typeMunger.write(s);
			}
			writeAnyReweavableData(this, s, s.canCompress());
		} finally {
			s.compressionEnabled = true;
		}
	}

	public void addConcreteMunger(ConcreteTypeMunger munger) {
		typeMungers.add(new Entry(munger.getAspectType(), munger.getMunger()));
	}

	public String toString() {
		return "WeaverStateInfo(aspectsAffectingType=" + aspectsAffectingType + "," + typeMungers + ", " + oldStyle + ")";
	}

	public List<ConcreteTypeMunger> getTypeMungers(ResolvedType onType) {
		World world = onType.getWorld();
		List<ConcreteTypeMunger> ret = new ArrayList<ConcreteTypeMunger>();
		for (Entry entry : typeMungers) {
			ResolvedType aspectType = world.resolve(entry.aspectType, true);
			if (aspectType.isMissing()) {
				world.showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.ASPECT_NEEDED, entry.aspectType, onType),
						onType.getSourceLocation(), null);
				continue;
			}

			ret.add(new TemporaryTypeMunger(entry.typeMunger, aspectType));
		}
		return ret;
	}

	public boolean isOldStyle() {
		return oldStyle;
	}

	public byte[] getUnwovenClassFileData(byte wovenClassFile[]) {
		if (unwovenClassFileIsADiff) {
			unwovenClassFile = applyDiff(wovenClassFile, unwovenClassFile);
			unwovenClassFileIsADiff = false;
		}
		return unwovenClassFile;
	}

	public void setUnwovenClassFileData(byte[] data) {
		unwovenClassFile = data;
	}

	public boolean isReweavable() {
		return reweavable;
	}

	public void setReweavable(boolean rw) {
		reweavable = rw;
	}

	public void addAspectsAffectingType(Collection<String> aspects) {
		aspectsAffectingType.addAll(aspects);
	}

	public void addAspectAffectingType(String aspectSignature) {
		aspectsAffectingType.add(aspectSignature);
	}

	public Set<String> getAspectsAffectingType() {
		return this.aspectsAffectingType;
	}

	private static void readAnyReweavableData(WeaverStateInfo wsi, VersionedDataInputStream s, boolean compressed)
			throws IOException {
		if (wsi.isReweavable()) {
			// Load list of aspects that need to exist in the world for reweaving to be 'legal'
			int numberAspectsAffectingType = s.readShort();
			for (int i = 0; i < numberAspectsAffectingType; i++) {
				String str = null;
				if (compressed) {
					str = s.readSignature();
				} else {
					str = s.readUTF();
					// Prior to 1.6.9 we were writing out names (com.foo.Bar) rather than signatures (Lcom/foo/Bar;)
					// From 1.6.9 onwards we write out signatures (pr319431)
					if (s.getMajorVersion() < WeaverVersionInfo.WEAVER_VERSION_AJ169) {
						// It is a name, make it a signature
						StringBuilder sb = new StringBuilder();
						sb.append("L").append(str.replace('.', '/')).append(";");
						str = sb.toString();
					}
				}
				wsi.addAspectAffectingType(str);
			}

			int unwovenClassFileSize = s.readInt();
			byte[] classData = null;
			// the unwovenClassFile may have been compressed:
			if (wsi.reweavableCompressedMode) {
				classData = new byte[unwovenClassFileSize];
				ZipInputStream zis = new ZipInputStream(s);
				ZipEntry zen = zis.getNextEntry();
				int current = 0;
				int bytesToGo = unwovenClassFileSize;
				while (bytesToGo > 0) {
					int amount = zis.read(classData, current, bytesToGo);
					current += amount;
					bytesToGo -= amount;
				}
				zis.closeEntry();
				if (bytesToGo != 0) {
					throw new IOException("ERROR whilst reading compressed reweavable data, expected " + unwovenClassFileSize
							+ " bytes, only found " + current);
				}
			} else {
				classData = new byte[unwovenClassFileSize];
				int bytesread = s.read(classData);
				if (bytesread != unwovenClassFileSize) {
					throw new IOException("ERROR whilst reading reweavable data, expected " + unwovenClassFileSize
							+ " bytes, only found " + bytesread);
				}
			}

			// if it was diffMode we'll have to remember to apply the diff if someone
			// asks for the unwovenClassFile
			wsi.unwovenClassFileIsADiff = wsi.reweavableDiffMode;
			wsi.setUnwovenClassFileData(classData);
		}
	}

	/**
	 * Here is the cleverness for reweavable diff mode. The class file on disk contains, inside the weaverstateinfo attribute, a
	 * diff that can be applied to 'itself' to recover the original class - which can then be rewoven.
	 */
	public byte[] replaceKeyWithDiff(byte wovenClassFile[]) {
		// we couldn't have made the diff earlier
		// as we didn't have the wovenClassFile
		// so we left a key there as a marker to come back to

		if (reweavableDiffMode) {
			ByteArrayOutputStream arrayStream = new ByteArrayOutputStream();
			DataOutputStream s = new DataOutputStream(arrayStream);

			int endOfKey = findEndOfKey(wovenClassFile);
			int startOfKey = endOfKey - key.length;
			// the length of the wsi attribute is written infront of it in the classFile,
			// swapping the diff for the key will probably change the length of the wsi,
			// so we'll have to fiddle with the four 'int length' bytes
			int oldLengthLocation = startOfKey - 4;
			int oldLength = readInt(wovenClassFile, oldLengthLocation);
			wovenClassFile = deleteInArray(wovenClassFile, startOfKey, endOfKey); // delete the key

			byte[] wovenClassFileUpToWSI = new byte[oldLengthLocation];
			System.arraycopy(wovenClassFile, 0, wovenClassFileUpToWSI, 0, oldLengthLocation);

			byte[] diff = generateDiff(wovenClassFileUpToWSI, unwovenClassFile);
			try { // put the length of the diff infront of the diff
				s.writeInt(diff.length);
				s.write(diff);
			} catch (IOException e) {
			}
			diff = arrayStream.toByteArray();
			// we have to swap the oldLength for the new one,
			// and add the diff, using the oldLength to work out where it should go :)

			int newLength = oldLength - key.length + diff.length;
			byte newLengthBytes[] = serializeInt(newLength);

			// swap in the serialized newLength for the oldOne:
			wovenClassFile[oldLengthLocation] = newLengthBytes[0];
			wovenClassFile[oldLengthLocation + 1] = newLengthBytes[1];
			wovenClassFile[oldLengthLocation + 2] = newLengthBytes[2];
			wovenClassFile[oldLengthLocation + 3] = newLengthBytes[3];

			// add the diff
			wovenClassFile = insertArray(diff, wovenClassFile, oldLengthLocation + 4 + oldLength - key.length);
		}
		return wovenClassFile;
	}

	private static final int findEndOfKey(byte[] wovenClassFile) {
		// looks through the classfile backwards (as the attributes are all near the end)
		for (int i = wovenClassFile.length - 1; i > 0; i--) {
			if (endOfKeyHere(wovenClassFile, i)) {
				return i + 1;
			}
		}
		throw new RuntimeException("key not found in wovenClassFile"); // should never happen
	}

	private static final boolean endOfKeyHere(byte lookIn[], int i) {
		for (int j = 0; j < key.length; j++) {
			if (key[key.length - 1 - j] != lookIn[i - j]) {
				return false;
			}
		}
		return true;
	}

	private static final byte[] insertArray(byte toInsert[], byte original[], int offset) {
		byte result[] = new byte[original.length + toInsert.length];
		System.arraycopy(original, 0, result, 0, offset);
		System.arraycopy(toInsert, 0, result, offset, toInsert.length);
		System.arraycopy(original, offset, result, offset + toInsert.length, original.length - offset);
		return result;
	}

	private static final int readInt(byte[] a, int offset) {
		ByteArrayInputStream b = new ByteArrayInputStream(a, offset, 4);
		DataInputStream d = new DataInputStream(b);
		int length = -1;
		try {
			length = d.readInt();
		} catch (IOException e) {
			throw (new RuntimeException("readInt called with a bad array or offset")); // should never happen
		}
		return length;
	}

	private static final byte[] deleteInArray(byte a[], int start, int end) {
		int lengthToDelete = end - start;
		byte result[] = new byte[a.length - lengthToDelete]; // make a new array
		System.arraycopy(a, 0, result, 0, start); // copy in the bit before the deleted bit
		System.arraycopy(a, end, result, start, a.length - end); // copy in the bit after the deleted bit
		return result;
	}

	// ajh02: a quick note about the diff format...
	//
	// classfiles consist of:
	// 8 bytes: magic number and minor and major versions,
	// 2 bytes: its constant pool count
	// n bytes: the rest of the class file
	//
	// weaving a classfile never changes the classfile's first 8 bytes,
	// and after the constant pool count there's usually a run of bytes that weaving didn't change
	// hereafter referred to as the run
	//
	// so the diff consists of:
	// 2 bytes: its constant pool count
	// 4 bytes: length of the run
	// n bytes: the rest of the unwovenClassFile

	byte[] generateDiff(byte[] wovenClassFile, byte[] unWovenClassFile) {

		// find how long the run is
		int lookingAt = 10;
		int shorterLength = (wovenClassFile.length < unWovenClassFile.length) ? wovenClassFile.length : unWovenClassFile.length;
		while (lookingAt < shorterLength && (wovenClassFile[lookingAt] == unWovenClassFile[lookingAt])) {
			lookingAt++;
		}
		int lengthInCommon = lookingAt - 10;
		byte[] diff = new byte[unWovenClassFile.length - 4 - lengthInCommon];

		// first 2 bytes of the diff are the constant pool count
		diff[0] = unWovenClassFile[8];
		diff[1] = unWovenClassFile[9];

		// then 4 bytes saying how long the run is
		byte[] lengthInCommonBytes = serializeInt(lengthInCommon);
		diff[2] = lengthInCommonBytes[0];
		diff[3] = lengthInCommonBytes[1];
		diff[4] = lengthInCommonBytes[2];
		diff[5] = lengthInCommonBytes[3];

		// then we just dump the rest of the unWovenClassFile verbatim
		System.arraycopy(unWovenClassFile, 10 + lengthInCommon, diff, 6, diff.length - 6);

		return diff;
	}

	byte[] applyDiff(byte[] wovenClassFile, byte[] diff) {

		int lengthInCommon = readInt(diff, 2);
		byte[] unWovenClassFile = new byte[4 + diff.length + lengthInCommon];

		// copy the first 8 bytes from the wovenClassFile
		System.arraycopy(wovenClassFile, 0, unWovenClassFile, 0, 8);

		// copy the constant pool count from the diff
		unWovenClassFile[8] = diff[0];
		unWovenClassFile[9] = diff[1];

		// copy the run from the wovenClassFile
		System.arraycopy(wovenClassFile, 10, unWovenClassFile, 10, lengthInCommon);

		// copy the stuff after the run from the diff
		System.arraycopy(diff, 6, unWovenClassFile, 10 + lengthInCommon, diff.length - 6);

		return unWovenClassFile;
	}

	private byte[] serializeInt(int i) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(4);
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeInt(i);
		} catch (IOException e) {
		}
		return bos.toByteArray();
	}

	private static void writeAnyReweavableData(WeaverStateInfo wsi, CompressingDataOutputStream s, boolean compress)
			throws IOException {
		if (wsi.isReweavable()) {
			// Write out list of aspects that must exist next time we try and weave this class
			s.writeShort(wsi.aspectsAffectingType.size());
			for (String type : wsi.aspectsAffectingType) {
				if (compress) {
					s.writeCompressedSignature(type);
				} else {
					s.writeUTF(type);
				}
			}
			byte[] data = wsi.unwovenClassFile;
			// if we're not in diffMode, write the unwovenClassFile now,
			// otherwise we'll insert it as a diff later
			if (!wsi.reweavableDiffMode) {
				s.writeInt(data.length);
				s.write(wsi.unwovenClassFile);
			}
		}
	}

	/**
	 * @return true if the supplied aspect is already in the list of those affecting this type
	 */
	public boolean isAspectAlreadyApplied(ResolvedType someAspect) {
		String someAspectSignature = someAspect.getSignature();
		for (String aspectSignature : aspectsAffectingType) {
			if (aspectSignature.equals(someAspectSignature)) {
				return true;
			}
		}
		return false;
	}

}
