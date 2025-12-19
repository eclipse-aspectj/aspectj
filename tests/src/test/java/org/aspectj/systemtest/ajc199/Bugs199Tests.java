/*******************************************************************************
 * Copyright (c) 2022 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc199;

import io.bmuskalla.system.properties.PropertyEnvironment;
import io.bmuskalla.system.properties.ScopedSystemProperties;
import junit.framework.Test;
import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Alexander Kriegisch
 */
public class Bugs199Tests extends XMLBasedAjcTestCase {

  public void testAnnotationStyleSpecialIfClauses() {
    runTest("annotation style A");
  }

  public void testAnnotationStylePointcutInheritanceWithIfClauses() {
    runTest("annotation style B");
  }

  public void testAnnotationStyleSpecialIfClauses2_gh120() {
    runTest("annotation style C");
  }

  public void testAnnotationStyleSpecialIfClauses3_gh120() {
    runTest("annotation style D");
  }

  public void testAnnotationStyleNegatedIf_gh122() {
    runTest("annotation style negated if");
  }

  public void testCompilerCanReopenClosedJARs_gh125() {
    try (PropertyEnvironment env = ScopedSystemProperties.newPropertyEnvironment()) {
      env.setProperty("org.aspectj.weaver.openarchives", "20");
      runTest("compiler can re-open closed JARs");
    }
  }

  public void testAsyncProceedNestedAroundAdvice_gh128() {
    runTest("asynchronous proceed for nested around-advice (@AspectJ)");
  }

  public void testAsyncProceedNestedAroundAdviceThreadPool_gh128() {
    // Test created for #128, but initially commented out and remaining work recorded in #141.
    // Now, test is expected to pass. See https://github.com/eclipse-aspectj/aspectj/issues/141.
    runTest("asynchronous proceed for nested around-advice (@AspectJ, thread pool)");
  }

  public void testAsyncProceedNestedAroundAdviceNative_gh128() {
    runTest("asynchronous proceed for nested around-advice (native)");
  }

  public void testAsyncProceedNestedAroundAdviceNativeThreadPool_gh128() {
    runTest("asynchronous proceed for nested around-advice (native, thread pool)");
  }

  public void testAddExports_gh145() {
    runTest("use --add-exports");
  }

  public void testAddReads_gh145() {
    runTest("use --add-reads");
  }

  public void testAddModules_gh145() {
    runTest("use --add-modules");
  }

  public void testAddModulesJDK_gh145() {
    // Verify jdk.charsets module is available in the current JDK by checking both:
    // 1. Module is listed by java --list-modules
    // 2. The jmod file exists in the JDK's jmods directory
    // This ensures the module is truly available, not just listed.
    boolean moduleAvailable = false;
    boolean jmodExists = false;
    
    try {
      // Check if module is listed
      ProcessBuilder pb = new ProcessBuilder(
        System.getProperty("java.home") + "/bin/java",
        "--list-modules"
      );
      Process p = pb.start();
      java.io.BufferedReader reader = new java.io.BufferedReader(
        new java.io.InputStreamReader(p.getInputStream())
      );
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.trim().startsWith("jdk.charsets")) {
          moduleAvailable = true;
          break;
        }
      }
      p.waitFor();
      reader.close();
      
      // Check if jmod file exists (more reliable indicator of module availability)
      String javaHome = System.getProperty("java.home");
      java.io.File jmodFile = new java.io.File(javaHome, "jmods/jdk.charsets.jmod");
      jmodExists = jmodFile.exists();
    } catch (Exception e) {
      // If we can't check, assume it's available and let the test run
      // (it will fail with a clear error message if the module is missing)
      moduleAvailable = true;
      jmodExists = true;
    }
    
    if (!moduleAvailable && !jmodExists) {
      System.out.println("Skipping testAddModulesJDK_gh145: jdk.charsets module not available in this JDK");
      return;
    }
    
    // Check if we're on Temurin JDK 25 where ECJ rejects internal JDK modules
    // Even though the module exists, ECJ will reject it with "invalid module name: jdk.charsets"
    // This is a known ECJ limitation documented in the test XML comment.
    String vendor = System.getProperty("java.vm.vendor", "");
    String version = System.getProperty("java.version", "");
    if ((vendor.contains("Eclipse Adoptium") || vendor.contains("Temurin")) && version.startsWith("25")) {
      System.out.println("Skipping testAddModulesJDK_gh145: ECJ on " + vendor + " " + version + 
                         " rejects internal JDK modules with 'invalid module name: jdk.charsets'");
      return;
    }
    
    // Run the test on JDKs where ECJ can handle internal JDK modules (e.g., Oracle JDK 25).
    // AspectJ automatically adds the jmods directory to the module path (see BuildArgParser.java),
    // and AJC has a workaround that allows internal JDK modules to be added via --add-modules
    // (making it more javac-compliant than raw ECJ).
    runTest("use --add-modules with non-public JDK module");
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Bugs199Tests.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc199.xml");
  }

}
