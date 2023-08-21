import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.FileOutputStream;
import java.io.IOException;

class JpaRepositoryDump implements Opcodes {
  public static void main(String[] args) throws IOException {
    try (FileOutputStream outputStream = new FileOutputStream("JpaRepository.class")) {
      outputStream.write(dump());
    }
  }

  public static byte[] dump() {
    /*
    Write out a class corresponding to this source code:
    ------------------------------------------------------------
    interface JpaRepository<T> extends CrudRepository<T> {
      @Override
      <S extends T> List<S> saveAll(Iterable<S> entities);
    }
    ------------------------------------------------------------
    The only difference to the original class created by Javac or Ajc is that the bridge method is written to the class
    file first, then the overriding method with the return type narrowed from Iterable to List. This has the effect of
    org.aspectj.weaver.ResolvedType.getMethodsIncludingIntertypeDeclarations also finding the bridge method first,
    which helps to reproduce https://github.com/spring-projects/spring-framework/issues/27761 in a regression test.

    The resulting class file can be found in .../gh_spring_27761/JpaRepository_bridge_first.jar and is used during test
    "do not match bridge methods".
    */

    ClassWriter classWriter = new ClassWriter(0);
    classWriter.visit(V1_8, ACC_ABSTRACT | ACC_INTERFACE, "JpaRepository", "<T:Ljava/lang/Object;>Ljava/lang/Object;LCrudRepository<TT;>;", "java/lang/Object", new String[]{"CrudRepository"});
    classWriter.visitSource("RepositoryAspect.aj", null);

    MethodVisitor methodVisitor;
    methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_BRIDGE | ACC_SYNTHETIC, "saveAll", "(Ljava/lang/Iterable;)Ljava/lang/Iterable;", null, null);
    methodVisitor.visitCode();
    Label label0 = new Label();
    methodVisitor.visitLabel(label0);
    methodVisitor.visitLineNumber(1, label0);
    methodVisitor.visitVarInsn(ALOAD, 0);
    methodVisitor.visitVarInsn(ALOAD, 1);
    methodVisitor.visitTypeInsn(CHECKCAST, "java/lang/Iterable");
    methodVisitor.visitMethodInsn(INVOKEINTERFACE, "JpaRepository", "saveAll", "(Ljava/lang/Iterable;)Ljava/util/List;", true);
    methodVisitor.visitInsn(ARETURN);
    methodVisitor.visitMaxs(2, 2);
    methodVisitor.visitEnd();

    methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_ABSTRACT, "saveAll", "(Ljava/lang/Iterable;)Ljava/util/List;", "<S:TT;>(Ljava/lang/Iterable<TS;>;)Ljava/util/List<TS;>;", null);
    methodVisitor.visitEnd();

    classWriter.visitEnd();

    return classWriter.toByteArray();
  }
}
