
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.objectweb.asm.*;

public class Generator implements Opcodes {

	private String generatedRunnerTypename,linkClassName,dynMethodName,dynMethodDescriptor;
	private byte[] bytes;
	
	/**
	 * Main entry point generates a default thing, in this case a class called 'Invoker' that will use the bootstrap method on Code1 to run 'foo()'
	 * @param args
	 * @throws Throwable
	 */
	public static void main(String[] args) throws Throwable {
		Generator g = new Generator("Invoker","Code1","foo","()V");
		g.dump();
	}
	
	public byte[] getBytes() {
		return bytes;
	}

	public Generator(String generatedRunnerTypename, String linkClassName, String dynMethodName, String dynMethodDescriptor) {
		this.generatedRunnerTypename = generatedRunnerTypename;
		this.linkClassName = linkClassName;
		this.dynMethodName = dynMethodName;
		this.dynMethodDescriptor = dynMethodDescriptor;
		this.bytes = generateClass();
	}

	public void dump() {
		try {
			FileOutputStream fos
			 = new FileOutputStream(new File(generatedRunnerTypename+".class"));
			fos.write(bytes);
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public byte[] generateClass() {
		ClassWriter cw = new ClassWriter(0);
		cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER, generatedRunnerTypename, null, "java/lang/Object", null);
		createConstructor(cw);
		createMain(cw);
		cw.visitEnd();
		return cw.toByteArray();
	}

	private void createMain(ClassWriter cw) {
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		mv.visitCode();
		MethodType mt = MethodType.methodType(CallSite.class, MethodHandles.Lookup.class, String.class,
				MethodType.class);
		Handle bootstrap = new Handle(Opcodes.H_INVOKESTATIC, this.linkClassName, "bootstrap",
				mt.toMethodDescriptorString());
		int maxStackSize = 0;//addMethodParameters(mv);
		mv.visitInvokeDynamicInsn(dynMethodName, dynMethodDescriptor, bootstrap);
		mv.visitInsn(RETURN);
		mv.visitMaxs(maxStackSize, 1);
		mv.visitEnd();
	}
	
//	public byte[] dump(String dynamicInvokerClassName, String dynamicLinkageClassName, String bootstrapMethodName, String targetMethodDescriptor)
//			throws Exception {
//		ClassWriter cw = new ClassWriter(0);
//		cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER, dynamicInvokerClassName, null, "java/lang/Object", null);
//		createConstructor(cw);
//		createMain(dynamicLinkageClassName, bootstrapMethodName, targetMethodDescriptor, cw);
//		cw.visitEnd();
//		return cw.toByteArray();
//	}



//	protected int addMethodParameters(MethodVisitor mv) {
//		return 0;
//	}


	private void createConstructor(ClassWriter cw) {
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
		mv.visitInsn(RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}
}