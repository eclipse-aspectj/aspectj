/* *******************************************************************
 * Copyright (c) 2008, 2018 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	 Andy Clement
 * ******************************************************************/
package org.aspectj.weaver.bcel.asm;

import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;

import aj.org.objectweb.asm.ClassReader;
import aj.org.objectweb.asm.ClassVisitor;
import aj.org.objectweb.asm.ClassWriter;
import aj.org.objectweb.asm.MethodVisitor;
import aj.org.objectweb.asm.Opcodes;

/**
 * Uses asm to add the stack map attribute to methods in a class. The class is passed in as pure byte data and then a reader/writer
 * process it. The writer is wired into the world so that types can be resolved and getCommonSuperClass() can be implemented without
 * class loading using the context class loader.
 *
 * It is important that the constant pool is preserved here and asm does not try to remove unused entries.  That is because some
 * entries are refered to from classfile attributes.  Asm cannot see into these attributes so does not realise the constant pool
 * entries are in use.  In order to ensure the copying of cp occurs, we use the variant super constructor call in AspectJConnectClassWriter
 * that passes in the classreader.  However, ordinarily that change causes a further optimization: that if a classreader sees
 * a methodvisitor that has been created by a ClassWriter then it just copies the data across without changing it (and so it
 * fails to attach the stackmapattribute).  In order to avoid this further optimization we use our own minimal MethodVisitor.
 *
 * @author Andy Clement
 */
public class StackMapAdder {

	public static byte[] addStackMaps(World world, byte[] data) {
		try {
			ClassReader cr = new ClassReader(data);
			ClassWriter cw = new AspectJConnectClassWriter(cr, world);
			ClassVisitor cv = new AspectJClassVisitor(cw);
			cr.accept(cv, 0);
			return cw.toByteArray();
		} catch (Throwable t) {
			// If in here fixing an error about version, change the ASMX in class above!
			System.err.println("AspectJ Internal Error: unable to add stackmap attributes. " + t.getMessage());
			t.printStackTrace();
			AsmDetector.isAsmAround = false;
			return data;
		}
	}

	private static class AspectJClassVisitor extends ClassVisitor {

		public AspectJClassVisitor(ClassVisitor classwriter) {
			super(Opcodes.ASM8, classwriter);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
			return new AJMethodVisitor(mv);
		}

		// Minimal pass through MethodVisitor just so that the ClassReader doesn't see one that has been directly
		// created by a ClassWriter (see top level class comment)
		static class AJMethodVisitor extends MethodVisitor {
			public AJMethodVisitor(MethodVisitor mv) {
				super(Opcodes.ASM8,mv);
			}
		}

	}

	private static class AspectJConnectClassWriter extends ClassWriter {
		private final World world;

		public AspectJConnectClassWriter(ClassReader cr, World w) {
			super(cr, ClassWriter.COMPUTE_FRAMES); // passing in cr is necessary so cpool isnt modified (see 2.2.4 of asm doc)
			this.world = w;
		}


		// Implementation of getCommonSuperClass() that avoids Class.forName()
		@Override
		protected String getCommonSuperClass(final String type1, final String type2) {

			ResolvedType resolvedType1 = world.resolve(UnresolvedType.forName(type1.replace('/', '.')));
			ResolvedType resolvedType2 = world.resolve(UnresolvedType.forName(type2.replace('/', '.')));

			if (resolvedType1.isAssignableFrom(resolvedType2)) {
				return type1;
			}

			if (resolvedType2.isAssignableFrom(resolvedType1)) {
				return type2;
			}

			if (resolvedType1.isInterface() || resolvedType2.isInterface()) {
				return "java/lang/Object";
			} else {
				do {
					resolvedType1 = resolvedType1.getSuperclass();
					if (resolvedType1 == null) {
						// This happens if some types are missing, the getSuperclass() call on
						// MissingResolvedTypeWithKnownSignature will return the Missing type which
						// in turn returns a superclass of null. By returning Object here it
						// should surface the cantFindType message raised in the first problematic
						// getSuperclass call
						return "java/lang/Object";
					}
					if (resolvedType1.isParameterizedOrGenericType()) {
						resolvedType1 = resolvedType1.getRawType();
					}
				} while (!resolvedType1.isAssignableFrom(resolvedType2));
				return resolvedType1.getRawName().replace('.', '/');
			}
		}
	}
}
