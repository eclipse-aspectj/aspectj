/* *******************************************************************
 * Copyright (c) 2023 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 * ******************************************************************/
package org.aspectj.weaver.bcel;

import junit.framework.TestCase;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class ExtensibleURLClassLoaderTest extends TestCase {
  /**
   * Simple regression test for <a href="https://github.com/eclipse-aspectj/aspectj/issues/266">GitHub issue 266</a>
   */
  public void testClassNotFoundExceptionHasRootCauseOnIOException() throws URISyntaxException, MalformedURLException {
    ExtensibleURLClassLoader extensibleURLClassLoader = new MockExtensibleURLClassLoader(
      new URL[] { new URI("file://dummy").toURL() },
      null
    );
    ClassNotFoundException classNotFoundException = null;
    try {
      extensibleURLClassLoader.findClass(getClass().getName().replace('.', '/'));
    } catch (ClassNotFoundException e) {
      classNotFoundException = e;
    }
    assertNotNull(classNotFoundException);
    Throwable cause = classNotFoundException.getCause();
    assertNotNull(cause);
    assertTrue(cause instanceof IOException);
    assertEquals("uh-oh", cause.getMessage());
  }

  static class MockExtensibleURLClassLoader extends ExtensibleURLClassLoader {
    public MockExtensibleURLClassLoader(URL[] urls, ClassLoader parent) {
      super(urls, parent);
    }

    @Override
    protected byte[] getBytes(String name) throws IOException {
      throw new IOException("uh-oh");
    }
  }
}
