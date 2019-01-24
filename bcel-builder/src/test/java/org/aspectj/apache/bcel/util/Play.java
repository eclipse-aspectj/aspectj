package org.aspectj.apache.bcel.util;

import java.io.File;
import java.io.FileInputStream;

import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.ClassParser;
import org.aspectj.apache.bcel.classfile.Field;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeAnnos;

public class Play {
	
	public static void printBytes(byte[] bs) {
		StringBuilder sb = new StringBuilder("Bytes:"+bs.length+"[");
		for (int i=0;i<bs.length;i++) {
			if (i>0) sb.append(" ");
			sb.append(bs[i]);
		}
		sb.append("]");
		System.out.println(sb);
	}
	
	public static void main(String[] args) throws Exception {
		if (args==null || args.length==0 ) {
			System.out.println("Specify a file");
			return;
		}
		if (!args[0].endsWith(".class")) {
			args[0] = args[0]+".class";
		}
		FileInputStream fis = new FileInputStream(new File(args[0]));
		ClassParser cp = new ClassParser(fis,args[0]);
		JavaClass jc = cp.parse();
		Attribute[] attributes = jc.getAttributes();
		printUsefulAttributes(attributes);
		System.out.println("Fields");
		Field[] fs = jc.getFields();
		if (fs!=null) {
			for (Field f: fs) {
				System.out.println(f);
				printUsefulAttributes(f.getAttributes());
			}
		}
		System.out.println("Methods");
		Method[] ms = jc.getMethods();
		if (ms!=null) {
			for (Method m: ms) {
				System.out.println(m);
				printUsefulAttributes(m.getAttributes());
				System.out.println("Code attributes:");
				printUsefulAttributes(m.getCode().getAttributes());
			}
		}
//		Method[] ms = jc.getMethods();
//		for (Method m: ms) {
//			System.out.println("==========");
//			System.out.println("Method: "+m.getName()+" modifiers=0x"+Integer.toHexString(m.getModifiers()));
//			Attribute[] as = m.getAttributes();
//			for (Attribute a: as) {
//				if (a.getName().toLowerCase().contains("synthetic")) {
//					System.out.println("> "+a.getName());
//				}
//			}
//		}
	}

	private static void printUsefulAttributes(Attribute[] attributes) throws Exception {
		for (Attribute attribute: attributes) {
			String n = attribute.getName();
			if (n.equals("RuntimeInvisibleAnnotations") ||
				n.equals("RuntimeVisibleAnnotations")) {
				RuntimeAnnos ra = (RuntimeAnnos)attribute;
				// private byte[] annotation_data;
				java.lang.reflect.Field f = RuntimeAnnos.class.getDeclaredField("annotation_data");
				f.setAccessible(true);
				byte[] bs = (byte[])f.get(ra);
//				byte[] bs = unknown.getBytes();
				printBytes(bs);
			}
		}
	}
}
