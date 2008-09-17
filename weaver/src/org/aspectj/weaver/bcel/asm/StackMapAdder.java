package org.aspectj.weaver.bcel.asm;

import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * Uses asm to add the stack map attribute to methods in a class.  The class is passed in as pure byte data
 * and then a reader/writer process it.  The writer is wired into the world so that types can be resolved
 * and getCommonSuperClass() can be implemented without class loading using the context class loader.
 * 
 * @author Andy Clement
 */
public class StackMapAdder {

	public static byte[] addStackMaps(World world, byte[] data) {
		try {
			ClassReader cr = new ClassReader(data);
			ClassWriter cw = new AspectJConnectClassWriter(world);
			cr.accept(cw,0);
			return  cw.toByteArray();
		} catch (Throwable t) {
			System.err.println("AspectJ Internal Error: unable to add stackmap attributes. "+t.getMessage());
			AsmDetector.isAsmAround=false;
			return data;
		}
	}
	
	private static class AspectJConnectClassWriter extends ClassWriter {
		private World world;

		public AspectJConnectClassWriter(World w) {
			super(ClassWriter.COMPUTE_FRAMES);
			this.world = w;
		}

		// Implementation of getCommonSuperClass() that avoids Class.forName()
		protected String getCommonSuperClass(final String type1, final String type2) {
			  
		  ResolvedType resolvedType1 = world.resolve(UnresolvedType.forName(type1.replace('/','.')));
		  ResolvedType resolvedType2 = world.resolve(UnresolvedType.forName(type2.replace('/','.')));
		  
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
			  } while (!resolvedType1.isAssignableFrom(resolvedType2));
			  return resolvedType1.getName().replace('.','/');
		  }
	    }
	}
}
