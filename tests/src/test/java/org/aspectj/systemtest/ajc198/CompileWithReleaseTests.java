/*******************************************************************************
 * Copyright (c) 2021 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc198;

import junit.framework.Test;
import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCaseForJava9OrLater;

import java.util.Objects;

/**
 * @author Alexander Kriegisch
 */
public class CompileWithReleaseTests extends XMLBasedAjcTestCaseForJava9OrLater {

  /**
   * In order to avoid a complicated test involving two different JDKs (9+ for compilation, 8 for runtime), we inspect
   * the byte code of test class {@code Buffers} with BCEL, simply grepping on the disassembled byte code. If compiled
   * correctly with {@code --release 8}, the byte code should contain the equivalent of a {@code Buffer.flip()} call,
   * not a {@code ByteBuffer.flip()} one.
   */
  public void testCompileToOlderJDKRelease() {
    runTest("compile to older JDK release");

    // Check compiled byte code version
    String className = "Buffers";
    checkVersion(className, Constants.ClassFileVersion.of(8).MAJOR, Constants.ClassFileVersion.of(8).MINOR);

    // Disassemble method and check if Java 8 API is used as expected
    JavaClass javaClass;
    try {
      javaClass = getClassFrom(ajc.getSandboxDirectory(), className);
    }
    catch (ClassNotFoundException e) {
      throw new IllegalStateException("Cannot find class " + className, e);
    }
    Method method = Objects.requireNonNull(getMethodFromClass(javaClass, "flip"));
    String disassembledMethod = method.getCode().toString();

    final String JAVA8_API_CALL = "invokevirtual\tjava.nio.ByteBuffer.flip ()Ljava/nio/Buffer;";
    final String JAVA9_API_CALL = "invokevirtual\tjava.nio.ByteBuffer.flip ()Ljava/nio/ByteBuffer;";
    if (disassembledMethod.contains(JAVA9_API_CALL))
      fail(
        "Class '" + className + "' was compiled against Java 9+ API. " +
        "There seems to be a problem with the '--release' compiler option.\n" +
        "Disassembled method:\n" + disassembledMethod
      );
    else if (!disassembledMethod.contains(JAVA8_API_CALL))
      fail(
        "Cannot determine if class '" + className + "' was compiled against Java 8 or 9+ API. " +
        "This should never happen.\n" +
        "Disassembled method:\n" + disassembledMethod
      );
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(CompileWithReleaseTests.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc198.xml");
  }

}
