package org.aspectj.weaver;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.aspectj.util.FileUtil;

public abstract class UnwovenClassFile implements IUnwovenClassFile {

	protected String filename;
	protected char[] charfilename;
	protected byte[] bytes;
	// protected JavaClass javaClass = null;
	// protected byte[] writtenBytes = null;
	protected List<ChildClass> writtenChildClasses = Collections.emptyList();
	protected String className = null;
	protected boolean isModule = false;

	public abstract Clazz getJavaClass();

	public boolean shouldBeWoven() {
		// Skip module-info files for now, they aren't really types
		return !isModule;
	}

	public String getFilename() {
		return filename;
	}

	public String makeInnerFileName(String innerName) {
		String prefix = filename.substring(0, filename.length() - 6); // strip the .class
		return prefix + "$" + innerName + ".class";
	}

	public byte[] getBytes() {
		// if (bytes == null) bytes = javaClass.getBytes();
		return bytes;
	}
	
	public void writeUnchangedBytes() throws IOException {
		writeWovenBytes(getBytes(), Collections.<ChildClass>emptyList());
	}

	public void writeWovenBytes(byte[] bytes, List<ChildClass> childClasses) throws IOException {
		writeChildClasses(childClasses);

		// System.err.println("should write: " + getClassName());

		// System.err.println("about to write: " + this + ", " + writtenBytes + ", ");
		// + writtenBytes != null + " && " + unchanged(bytes, writtenBytes) );

		// if (writtenBytes != null && unchanged(bytes, writtenBytes)) return;

		// System.err.println("    actually wrote it");

		BufferedOutputStream os = FileUtil.makeOutputStream(new File(filename));
		os.write(bytes);
		os.close();

		// writtenBytes = bytes;
	}

	private void writeChildClasses(List<ChildClass> childClasses) throws IOException {
		// ??? we only really need to delete writtenChildClasses whose
		// ??? names aren't in childClasses; however, it's unclear
		// ??? how much that will affect performance
		deleteAllChildClasses();

		childClasses.removeAll(writtenChildClasses); // XXX is this right

		for (ChildClass childClass : childClasses) {
			writeChildClassFile(childClass.name, childClass.bytes);
		}

		writtenChildClasses = childClasses;

	}

	private void writeChildClassFile(String innerName, byte[] bytes) throws IOException {
		BufferedOutputStream os = FileUtil.makeOutputStream(new File(makeInnerFileName(innerName)));
		os.write(bytes);
		os.close();
	}

	protected void deleteAllChildClasses() {
		for (ChildClass childClass : writtenChildClasses) {
			deleteChildClassFile(childClass.name);
		}
	}

	protected void deleteChildClassFile(String innerName) {
		File childClassFile = new File(makeInnerFileName(innerName));
		childClassFile.delete();
	}

	/* private */static boolean unchanged(byte[] b1, byte[] b2) {
		int len = b1.length;
		if (b2.length != len)
			return false;
		for (int i = 0; i < len; i++) {
			if (b1[i] != b2[i])
				return false;
		}
		return true;
	}

	public char[] getClassNameAsChars() {
		if (charfilename == null) {
			charfilename = getClassName().replace('.', '/').toCharArray();
		}
		return charfilename;
	}

	public String getClassName() {
		if (className == null)
			className = getJavaClass().getClassName(); // OPTIMIZE quicker way to determine name??? surely?
		return className;
	}

	@Override
	public String toString() {
		return "UnwovenClassFile(" + filename + ", " + getClassName() + ")";
	}

	// record
	// OPTIMIZE why is the 'short name' used here (the bit after the dollar) - seems we mess about a lot trimming it off only to put
	// it back on!
	public static class ChildClass {
		public final String name;
		public final byte[] bytes;

		public ChildClass(String name, byte[] bytes) {
			this.name = name;
			this.bytes = bytes;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof ChildClass))
				return false;
			ChildClass o = (ChildClass) other;
			return o.name.equals(name) && unchanged(o.bytes, bytes);
		}

		@Override
		public int hashCode() {
			return name.hashCode();
		}

		@Override
		public String toString() {
			return "(ChildClass " + name + ")";
		}
	}

	public void setClassNameAsChars(char[] classNameAsChars) {
		this.charfilename = classNameAsChars;
	}
}
