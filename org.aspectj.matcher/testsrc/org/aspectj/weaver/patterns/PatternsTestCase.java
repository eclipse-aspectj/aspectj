package org.aspectj.weaver.patterns;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.TestCase;

import org.aspectj.weaver.World;

public abstract class PatternsTestCase extends TestCase {

	protected World world;

	public void setUp() throws Exception {
		super.setUp();
		world = getWorld();
	}

	protected File getTestDataJar() {
		return new File("../weaver/testdata/testcode.jar");
	}

	public URLClassLoader getClassLoaderForFile(File f) {
		try {
			URLClassLoader ucl = new URLClassLoader(new URL[] { f.toURL() }, this.getClass().getClassLoader());
			return ucl;
		} catch (MalformedURLException mue) {
			throw new RuntimeException(mue);
		}
	}

	public abstract World getWorld();
}
