package org.aspectj.apache.bcel.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.generic.ArrayType;
import org.aspectj.apache.bcel.generic.ObjectType;

/**
 * This class represents the constant pool, i.e., a table of constants, of a parsed classfile. It may contain null references, due
 * to the JVM specification that skips an entry after an 8-byte constant (double, long) entry.
 */
public class ConstantPool implements Node {
	private Constant[] pool;
	private int poolSize; // number of entries in the pool (could be < pool.length as the array is resized in 'chunks')

	private Map<String, Integer> utf8Cache = new HashMap<String, Integer>();
	private Map<String, Integer> methodCache = new HashMap<String, Integer>();
	private Map<String, Integer> fieldCache = new HashMap<String, Integer>();

	public int getSize() {
		return poolSize;
	}

	public ConstantPool() {
		pool = new Constant[10];
		poolSize = 0;
	}

	public ConstantPool(Constant[] constants) {
		pool = constants;
		poolSize = (constants == null ? 0 : constants.length);
	}

	ConstantPool(DataInputStream file) throws IOException {
		byte tag;
		poolSize = file.readUnsignedShort();
		pool = new Constant[poolSize];
		// pool[0] is unused by the compiler and may be used freely by the implementation
		for (int i = 1; i < poolSize; i++) {
			pool[i] = Constant.readConstant(file);
			tag = pool[i].getTag();
			if ((tag == Constants.CONSTANT_Double) || (tag == Constants.CONSTANT_Long)) {
				i++;
			}
		}
	}

	public Constant getConstant(int index, byte tag) {
		Constant c = getConstant(index);
		// if (c == null) throw new ClassFormatException("Constant pool at index " + index + " is null.");
		if (c.tag == tag)
			return c;
		throw new ClassFormatException("Expected class '" + Constants.CONSTANT_NAMES[tag] + "' at index " + index + " and found "
				+ c);
	}

	public Constant getConstant(int index) {
		try {
			return pool[index];
		} catch (ArrayIndexOutOfBoundsException aioobe) {
			throw new ClassFormatException("Index " + index + " into constant pool (size:" + poolSize + ") is invalid");
		}
	}

	/**
	 * @return deep copy of this constant pool
	 */
	public ConstantPool copy() {
		Constant[] newConstants = new Constant[poolSize]; // use the correct size
		for (int i = 1; i < poolSize; i++) {
			if (pool[i] != null) {
				newConstants[i] = pool[i].copy();
			}
		}
		return new ConstantPool(newConstants);
	}

	/**
	 * Get string from constant pool and bypass the indirection of `ConstantClass' and `ConstantString' objects. I.e. these classes
	 * have an index field that points to another entry of the constant pool of type `ConstantUtf8' which contains the real data.
	 * 
	 * @param index Index in constant pool
	 * @param tag Tag of expected constant, either ConstantClass or ConstantString
	 * @return Contents of string reference
	 * @see ConstantClass
	 * @see ConstantString
	 * @throws ClassFormatException
	 */
	public String getConstantString(int index, byte tag) throws ClassFormatException {
		Constant c = getConstant(index, tag);
		int i;
		/*
		 * This switch() is not that elegant, since the two classes have the same contents, they just differ in the name of the
		 * index field variable. But we want to stick to the JVM naming conventions closely though we could have solved these more
		 * elegantly by using the same variable name or by subclassing.
		 */
		// OPTIMIZE remove the difference - use the an interface and same index methods for string ref id
		switch (tag) {
		case Constants.CONSTANT_Class:
			i = ((ConstantClass) c).getNameIndex();
			break;
		case Constants.CONSTANT_String:
			i = ((ConstantString) c).getStringIndex();
			break;
		default:
			throw new RuntimeException("getConstantString called with illegal tag " + tag);
		}
		// Finally get the string from the constant pool
		c = getConstant(i, Constants.CONSTANT_Utf8);
		return ((ConstantUtf8) c).getValue();
	}

	/**
	 * Resolve constant to a string representation.
	 */
	public String constantToString(Constant c) {
		String str;
		int i;

		switch (c.tag) {
		case Constants.CONSTANT_Class:
			i = ((ConstantClass) c).getNameIndex();
			c = getConstant(i, Constants.CONSTANT_Utf8);
			str = Utility.compactClassName(((ConstantUtf8) c).getValue(), false);
			break;

		case Constants.CONSTANT_String:
			i = ((ConstantString) c).getStringIndex();
			c = getConstant(i, Constants.CONSTANT_Utf8);
			str = "\"" + escape(((ConstantUtf8) c).getValue()) + "\"";
			break;

		case Constants.CONSTANT_Utf8:
		case Constants.CONSTANT_Double:
		case Constants.CONSTANT_Float:
		case Constants.CONSTANT_Long:
		case Constants.CONSTANT_Integer:
			str = ((SimpleConstant) c).getStringValue();
			break;

		case Constants.CONSTANT_NameAndType:
			str = (constantToString(((ConstantNameAndType) c).getNameIndex(), Constants.CONSTANT_Utf8) + " " + constantToString(
					((ConstantNameAndType) c).getSignatureIndex(), Constants.CONSTANT_Utf8));
			break;

		case Constants.CONSTANT_InterfaceMethodref:
		case Constants.CONSTANT_Methodref:
		case Constants.CONSTANT_Fieldref:
			str = (constantToString(((ConstantCP) c).getClassIndex(), Constants.CONSTANT_Class) + "." + constantToString(
					((ConstantCP) c).getNameAndTypeIndex(), Constants.CONSTANT_NameAndType));
			break;

		default: // Never reached
			throw new RuntimeException("Unknown constant type " + c.tag);
		}

		return str;
	}

	private static final String escape(String str) {
		int len = str.length();
		StringBuffer buf = new StringBuffer(len + 5);
		char[] ch = str.toCharArray();

		for (int i = 0; i < len; i++) {
			switch (ch[i]) {
			case '\n':
				buf.append("\\n");
				break;
			case '\r':
				buf.append("\\r");
				break;
			case '\t':
				buf.append("\\t");
				break;
			case '\b':
				buf.append("\\b");
				break;
			case '"':
				buf.append("\\\"");
				break;
			default:
				buf.append(ch[i]);
			}
		}

		return buf.toString();
	}

	public String constantToString(int index, byte tag) {
		Constant c = getConstant(index, tag);
		return constantToString(c);
	}

	public String constantToString(int index) {
		return constantToString(getConstant(index));
	}

	public void accept(ClassVisitor v) {
		v.visitConstantPool(this);
	}

	public Constant[] getConstantPool() {
		return pool;
	} // TEMPORARY, DONT LIKE PASSING THIS DATA OUT!

	public void dump(DataOutputStream file) throws IOException {
		file.writeShort(poolSize);
		for (int i = 1; i < poolSize; i++)
			if (pool[i] != null)
				pool[i].dump(file);
	}

	public ConstantUtf8 getConstantUtf8(int index) {
		Constant c = getConstant(index);
		assert c != null;
		assert c.tag == Constants.CONSTANT_Utf8;
		return (ConstantUtf8) c;
	}

	public String getConstantString_CONSTANTClass(int index) {
		ConstantClass c = (ConstantClass) getConstant(index, Constants.CONSTANT_Class);
		index = c.getNameIndex();
		return ((ConstantUtf8) getConstant(index, Constants.CONSTANT_Utf8)).getValue();
	}

	public int getLength() {
		return poolSize;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();

		for (int i = 1; i < poolSize; i++)
			buf.append(i + ")" + pool[i] + "\n");

		return buf.toString();
	}

	public int lookupInteger(int n) {
		for (int i = 1; i < poolSize; i++) {
			if (pool[i] instanceof ConstantInteger) {
				ConstantInteger c = (ConstantInteger) pool[i];
				if (c.getValue() == n)
					return i;
			}
		}
		return -1;
	}

	public int lookupUtf8(String string) {
		Integer pos = utf8Cache.get(string);
		if (pos != null) {
			return pos;
		}
		for (int i = 1; i < poolSize; i++) {
			Constant c = pool[i];
			if (c != null && c.tag == Constants.CONSTANT_Utf8) {
				if (((ConstantUtf8) c).getValue().equals(string)) {
					utf8Cache.put(string, i);
					return i;
				}
			}
		}
		return -1;
	}

	public int lookupClass(String classname) {
		for (int i = 1; i < poolSize; i++) {
			Constant c = pool[i];
			if (c != null && c.tag == Constants.CONSTANT_Class) {
				int cIndex = ((ConstantClass) c).getNameIndex();
				String cName = ((ConstantUtf8) pool[cIndex]).getValue();
				if (cName.equals(classname))
					return i;
			}
		}
		return -1;
	}

	public int addUtf8(String n) {
		int ret = lookupUtf8(n);
		if (ret != -1)
			return ret;
		adjustSize();
		ret = poolSize;
		pool[poolSize++] = new ConstantUtf8(n);
		return ret;
	}

	public int addInteger(int n) {
		int ret = lookupInteger(n);
		if (ret != -1)
			return ret;
		adjustSize();
		ret = poolSize;
		pool[poolSize++] = new ConstantInteger(n);
		return ret;
	}

	public int addArrayClass(ArrayType type) {
		return addClass(type.getSignature());
	}

	public int addClass(ObjectType type) {
		return addClass(type.getClassName());
	}

	public int addClass(String classname) {
		String toAdd = classname.replace('.', '/');
		int ret = lookupClass(toAdd);
		if (ret != -1)
			return ret;
		adjustSize();
		ConstantClass c = new ConstantClass(addUtf8(toAdd));
		ret = poolSize;
		pool[poolSize++] = c;
		return ret;
	}

	private void adjustSize() {
		if (poolSize + 3 >= pool.length) {
			Constant[] cs = pool;
			pool = new Constant[cs.length + 8];
			System.arraycopy(cs, 0, pool, 0, cs.length);
		}
		if (poolSize == 0)
			poolSize = 1; // someone about to do something in here!
	}

	public int addFieldref(String class_name, String field_name, String signature) {
		int ret = lookupFieldref(class_name, field_name, signature);
		int class_index, name_and_type_index;

		if (ret != -1)
			return ret;

		adjustSize();

		class_index = addClass(class_name);
		name_and_type_index = addNameAndType(field_name, signature);
		ret = poolSize;
		pool[poolSize++] = new ConstantFieldref(class_index, name_and_type_index);

		return ret;
	}

	public int lookupFieldref(String searchClassname, String searchFieldname, String searchSignature) {
		searchClassname = searchClassname.replace('.', '/');
		String k = new StringBuffer().append(searchClassname).append(searchFieldname).append(searchSignature).toString();
		Integer pos = fieldCache.get(k);
		if (pos != null)
			return pos.intValue();
		for (int i = 1; i < poolSize; i++) {
			Constant c = pool[i];
			if (c != null && c.tag == Constants.CONSTANT_Fieldref) {
				ConstantFieldref cfr = (ConstantFieldref) c;
				ConstantNameAndType cnat = (ConstantNameAndType) pool[cfr.getNameAndTypeIndex()];

				// check the class
				int cIndex = cfr.getClassIndex();
				ConstantClass cc = (ConstantClass) pool[cIndex];
				String cName = ((ConstantUtf8) pool[cc.getNameIndex()]).getValue();
				if (!cName.equals(searchClassname))
					continue;

				// check the name and type
				String name = ((ConstantUtf8) pool[cnat.getNameIndex()]).getValue();
				if (!name.equals(searchFieldname))
					continue; // not this one
				String typeSignature = ((ConstantUtf8) pool[cnat.getSignatureIndex()]).getValue();
				if (!typeSignature.equals(searchSignature))
					continue;
				fieldCache.put(k, new Integer(i));
				return i;
			}
		}
		return -1;
	}

	public int addNameAndType(String name, String signature) {
		int ret = lookupNameAndType(name, signature);
		if (ret != -1)
			return ret;
		adjustSize();
		int name_index = addUtf8(name);
		int signature_index = addUtf8(signature);
		ret = poolSize;
		pool[poolSize++] = new ConstantNameAndType(name_index, signature_index);
		return ret;
	}

	public int lookupNameAndType(String searchName, String searchTypeSignature) {
		for (int i = 1; i < poolSize; i++) {
			Constant c = pool[i];
			if (c != null && c.tag == Constants.CONSTANT_NameAndType) {
				ConstantNameAndType cnat = (ConstantNameAndType) c;
				String name = ((ConstantUtf8) pool[cnat.getNameIndex()]).getValue();
				if (!name.equals(searchName))
					continue; // not this one
				String typeSignature = ((ConstantUtf8) pool[cnat.getSignatureIndex()]).getValue();
				if (!typeSignature.equals(searchTypeSignature))
					continue;
				return i;
			}
		}
		return -1;
	}

	public int addFloat(float f) {
		int ret = lookupFloat(f);
		if (ret != -1)
			return ret;
		adjustSize();
		ret = poolSize;
		pool[poolSize++] = new ConstantFloat(f);
		return ret;
	}

	public int lookupFloat(float f) {
		int bits = Float.floatToIntBits(f);
		for (int i = 1; i < poolSize; i++) {
			Constant c = pool[i];
			if (c != null && c.tag == Constants.CONSTANT_Float) {
				ConstantFloat cf = (ConstantFloat) c;
				if (Float.floatToIntBits(cf.getValue()) == bits)
					return i;
			}
		}
		return -1;
	}

	public int addDouble(double d) {
		int ret = lookupDouble(d);
		if (ret != -1)
			return ret;
		adjustSize();
		ret = poolSize;
		pool[poolSize] = new ConstantDouble(d);
		poolSize += 2;
		return ret;
	}

	public int lookupDouble(double d) {
		long bits = Double.doubleToLongBits(d);
		for (int i = 1; i < poolSize; i++) {
			Constant c = pool[i];
			if (c != null && c.tag == Constants.CONSTANT_Double) {
				ConstantDouble cf = (ConstantDouble) c;
				if (Double.doubleToLongBits(cf.getValue()) == bits)
					return i;
			}
		}
		return -1;
	}

	public int addLong(long l) {
		int ret = lookupLong(l);
		if (ret != -1)
			return ret;
		adjustSize();
		ret = poolSize;
		pool[poolSize] = new ConstantLong(l);
		poolSize += 2;
		return ret;
	}

	public int lookupString(String s) {
		for (int i = 1; i < poolSize; i++) {
			Constant c = pool[i];
			if (c != null && c.tag == Constants.CONSTANT_String) {
				ConstantString cs = (ConstantString) c;
				ConstantUtf8 cu8 = (ConstantUtf8) pool[cs.getStringIndex()];
				if (cu8.getValue().equals(s))
					return i;
			}
		}
		return -1;
	}

	public int addString(String str) {
		int ret = lookupString(str);
		if (ret != -1)
			return ret;
		int utf8 = addUtf8(str);
		adjustSize();
		ConstantString s = new ConstantString(utf8);
		ret = poolSize;
		pool[poolSize++] = s;
		return ret;
	}

	public int lookupLong(long l) {
		for (int i = 1; i < poolSize; i++) {
			Constant c = pool[i];
			if (c != null && c.tag == Constants.CONSTANT_Long) {
				ConstantLong cf = (ConstantLong) c;
				if (cf.getValue() == l)
					return i;
			}
		}
		return -1;
	}

	public int addConstant(Constant c, ConstantPool cp) {
		Constant[] constants = cp.getConstantPool();
		switch (c.getTag()) {

		case Constants.CONSTANT_String: {
			ConstantString s = (ConstantString) c;
			ConstantUtf8 u8 = (ConstantUtf8) constants[s.getStringIndex()];

			return addString(u8.getValue());
		}

		case Constants.CONSTANT_Class: {
			ConstantClass s = (ConstantClass) c;
			ConstantUtf8 u8 = (ConstantUtf8) constants[s.getNameIndex()];

			return addClass(u8.getValue());
		}

		case Constants.CONSTANT_NameAndType: {
			ConstantNameAndType n = (ConstantNameAndType) c;
			ConstantUtf8 u8 = (ConstantUtf8) constants[n.getNameIndex()];
			ConstantUtf8 u8_2 = (ConstantUtf8) constants[n.getSignatureIndex()];

			return addNameAndType(u8.getValue(), u8_2.getValue());
		}

		case Constants.CONSTANT_Utf8:
			return addUtf8(((ConstantUtf8) c).getValue());

		case Constants.CONSTANT_Double:
			return addDouble(((ConstantDouble) c).getValue());

		case Constants.CONSTANT_Float:
			return addFloat(((ConstantFloat) c).getValue());

		case Constants.CONSTANT_Long:
			return addLong(((ConstantLong) c).getValue());

		case Constants.CONSTANT_Integer:
			return addInteger(((ConstantInteger) c).getValue());

		case Constants.CONSTANT_InterfaceMethodref:
		case Constants.CONSTANT_Methodref:
		case Constants.CONSTANT_Fieldref: {
			ConstantCP m = (ConstantCP) c;
			ConstantClass clazz = (ConstantClass) constants[m.getClassIndex()];
			ConstantNameAndType n = (ConstantNameAndType) constants[m.getNameAndTypeIndex()];
			ConstantUtf8 u8 = (ConstantUtf8) constants[clazz.getNameIndex()];
			String class_name = u8.getValue().replace('/', '.');

			u8 = (ConstantUtf8) constants[n.getNameIndex()];
			String name = u8.getValue();

			u8 = (ConstantUtf8) constants[n.getSignatureIndex()];
			String signature = u8.getValue();

			switch (c.getTag()) {
			case Constants.CONSTANT_InterfaceMethodref:
				return addInterfaceMethodref(class_name, name, signature);

			case Constants.CONSTANT_Methodref:
				return addMethodref(class_name, name, signature); // OPTIMIZE indicate it should be cached!

			case Constants.CONSTANT_Fieldref:
				return addFieldref(class_name, name, signature);

			default: // Never reached
				throw new RuntimeException("Unknown constant type " + c);
			}
		}

		default: // Never reached
			throw new RuntimeException("Unknown constant type " + c);
		}
	}

	// OPTIMIZE should put it in the cache now
	public int addMethodref(String class_name, String method_name, String signature) {
		int ret, class_index, name_and_type_index;
		if ((ret = lookupMethodref(class_name, method_name, signature)) != -1)
			return ret; // Already in CP

		adjustSize();

		name_and_type_index = addNameAndType(method_name, signature);
		class_index = addClass(class_name);
		ret = poolSize;
		pool[poolSize++] = new ConstantMethodref(class_index, name_and_type_index);
		return ret;
	}

	public int addInterfaceMethodref(String class_name, String method_name, String signature) {
		int ret = lookupInterfaceMethodref(class_name, method_name, signature);
		int class_index, name_and_type_index;

		if (ret != -1)
			return ret;
		adjustSize();

		class_index = addClass(class_name);
		name_and_type_index = addNameAndType(method_name, signature);
		ret = poolSize;
		pool[poolSize++] = new ConstantInterfaceMethodref(class_index, name_and_type_index);
		return ret;
	}

	public int lookupInterfaceMethodref(String searchClassname, String searchMethodName, String searchSignature) {
		searchClassname = searchClassname.replace('.', '/');
		for (int i = 1; i < poolSize; i++) {
			Constant c = pool[i];
			if (c != null && c.tag == Constants.CONSTANT_InterfaceMethodref) {
				ConstantInterfaceMethodref cfr = (ConstantInterfaceMethodref) c;

				ConstantClass cc = (ConstantClass) pool[cfr.getClassIndex()];
				String cName = ((ConstantUtf8) pool[cc.getNameIndex()]).getValue();
				if (!cName.equals(searchClassname))
					continue;

				// check the name and type
				ConstantNameAndType cnat = (ConstantNameAndType) pool[cfr.getNameAndTypeIndex()];
				String name = ((ConstantUtf8) pool[cnat.getNameIndex()]).getValue();
				if (!name.equals(searchMethodName))
					continue; // not this one
				String typeSignature = ((ConstantUtf8) pool[cnat.getSignatureIndex()]).getValue();
				if (!typeSignature.equals(searchSignature))
					continue;
				return i;
			}
		}
		return -1;
	}

	public int lookupMethodref(String searchClassname, String searchMethodName, String searchSignature) {
		String key = new StringBuffer().append(searchClassname).append(searchMethodName).append(searchSignature).toString();
		Integer cached = methodCache.get(key);
		if (cached != null)
			return cached.intValue();
		searchClassname = searchClassname.replace('.', '/');
		for (int i = 1; i < poolSize; i++) {
			Constant c = pool[i];
			if (c != null && c.tag == Constants.CONSTANT_Methodref) {
				ConstantMethodref cfr = (ConstantMethodref) c;
				ConstantNameAndType cnat = (ConstantNameAndType) pool[cfr.getNameAndTypeIndex()];

				// check the class
				int cIndex = cfr.getClassIndex();
				ConstantClass cc = (ConstantClass) pool[cIndex];
				String cName = ((ConstantUtf8) pool[cc.getNameIndex()]).getValue();
				if (!cName.equals(searchClassname))
					continue;

				// check the name and type
				String name = ((ConstantUtf8) pool[cnat.getNameIndex()]).getValue();
				if (!name.equals(searchMethodName))
					continue; // not this one
				String typeSignature = ((ConstantUtf8) pool[cnat.getSignatureIndex()]).getValue();
				if (!typeSignature.equals(searchSignature))
					continue;
				methodCache.put(key, new Integer(i));
				return i;
			}
		}
		return -1;
	}

	public ConstantPool getFinalConstantPool() {
		Constant[] cs = new Constant[poolSize]; // create it the exact size we need
		System.arraycopy(pool, 0, cs, 0, poolSize);
		return new ConstantPool(cs);
	}
}