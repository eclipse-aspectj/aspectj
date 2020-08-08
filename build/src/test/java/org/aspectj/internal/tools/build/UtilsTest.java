/* *******************************************************************
 * Copyright (c) 2006 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Wes Isberg       initial implementation 
 * ******************************************************************/

package org.aspectj.internal.tools.build;

import java.io.IOException;
import java.util.jar.Attributes.Name;

import junit.framework.TestCase;

import org.aspectj.internal.tools.build.Util.OSGIBundle;
import org.aspectj.internal.tools.build.Util.OSGIBundle.RequiredBundle;

public class UtilsTest extends TestCase {
    private static final String PREFIX = UtilsTest.class.getName().replace('.',
            '/');

    private static final ClassLoader LOADER = UtilsTest.class.getClassLoader();

    private static final ManifestTest[] TESTS = {
            new ManifestTest("01",
                    new Name[] { OSGIBundle.BUNDLE_CLASSPATH,
                            OSGIBundle.BUNDLE_SYMBOLIC_NAME,
                            OSGIBundle.BUNDLE_VERSION }, new String[] {
                            "lib/commons/commons.jar",
                            "org.aspectj.testing.client", "1.0.0" }),
            new ManifestTest("02",
                    new Name[] { OSGIBundle.BUNDLE_SYMBOLIC_NAME,
                            OSGIBundle.BUNDLE_VERSION }, new String[] {
                            "org.aspectj.testing.client", "1.0.0" },
                    new String[] { "lib/commons/commons.jar",
                            "lib/ant/lib/ant.jar" }, new String[] { "util",
                            "org.aspectj.runtime" }),
            new ManifestTest("03",
                    new Name[] { OSGIBundle.BUNDLE_SYMBOLIC_NAME,
                            OSGIBundle.BUNDLE_VERSION }, new String[] {
                            "org.aspectj.testing.client", "1.0.0" },
                    new String[] { "lib/commons/commons.jar",
                            "lib/ant/lib/ant.jar" }, new String[] { "util",
                            "aspectjrt" }) {
                void checkOthers(OSGIBundle osgiBundle, StringBuffer sb) {
                    RequiredBundle[] bundles = osgiBundle.getRequiredBundles();
					for (RequiredBundle bundle : bundles) {
						if ("aspectjrt".equals(bundle.name)) {
							if (!bundle.optional) {
								sb
										.append("expected required bundle aspectjrt to be optional");
							}
							String version = "[1.5.0,1.5.5]";
							if (!(version.equals(bundle.versions))) {
								sb.append("expected version " + version
										+ " got " + bundle.versions
										+ " for required bundle aspectjrt");
							}
						}
					}

                }
            } };

    private static class ManifestTest {
        final String name;

        final Name[] expectedNames;

        final String[] expectedValues;

        final String[] classpathEntries;

        final String[] requiredBundleNames;

        ManifestTest(String name, Name[] expectedNames, String[] expectedValues) {
            this(name, expectedNames, expectedValues, null, null);
        }

        ManifestTest(String name, Name[] expectedNames,
                String[] expectedValues, String[] classpathEntries,
                String[] requiredBundleNames) {
            this.name = name;
            this.expectedNames = expectedNames;
            this.expectedValues = expectedValues;
            this.classpathEntries = classpathEntries;
            this.requiredBundleNames = requiredBundleNames;
        }

        void run(StringBuffer sb) throws IOException {
            String path = PREFIX + "." + name + ".MF";
            OSGIBundle bundle = new OSGIBundle(LOADER.getResourceAsStream(path));
            int len = sb.length();
            checkNamesAndValues(bundle, sb);
            checkOthers(bundle, sb);
            if (sb.length() != len) {
                sb.append("failure was in test " + name);
            }
        }

        void checkOthers(OSGIBundle bundle, StringBuffer sb) {
        }

        void checkNamesAndValues(OSGIBundle bundle, StringBuffer sb) {
            for (int i = 0; i < expectedNames.length; i++) {
                Name name = expectedNames[i];
                String expected = expectedValues[i];
                String actual = bundle.getAttribute(expectedNames[i]);
                if (!((expected == actual) || expected.equals(actual))) {
                    sb.append(name);
                    sb.append(" ");
                    sb.append("expected ");
                    sb.append(expected);
                    sb.append("actual ");
                    sb.append(actual);
                    sb.append("\n");
                }
            }
            if (null != classpathEntries) {
                String[] cp = bundle.getClasspath();
                Util.reportMemberDiffs(classpathEntries, cp, sb);
            }
            if (null != requiredBundleNames) {
                RequiredBundle[] bundles = bundle.getRequiredBundles();
                String[] names = new String[bundles.length];
                for (int i = 0; i < names.length; i++) {
                    names[i] = bundles[i].name;
                }
                Util.reportMemberDiffs(requiredBundleNames, names, sb);
            }
        }
    }

    /** disabled pending research */
    public void skip_testOSGIManifests() throws Exception {
        StringBuffer sb = new StringBuffer();
		for (ManifestTest test : TESTS) {
			test.run(sb);
		}
        if (0 < sb.length()) {
            fail(sb.toString());
        }
    }

    public void testReportMemberDiffs() {
        StringBuffer sb = new StringBuffer();
        String[] exp = null;
        String[] act = null;
        assertFalse(Util.reportMemberDiffs(exp, act, sb));
        assertEquals("", sb.toString());

        sb.setLength(0);
        exp = new String[] { "" };
        act = null;
        assertTrue(Util.reportMemberDiffs(exp, act, sb));
        assertEquals("unexpected [] missing [\"\"]", sb.toString());

        sb.setLength(0);
        exp = null;
        act = new String[] { "" };
        assertTrue(Util.reportMemberDiffs(exp, act, sb));
        assertEquals("unexpected [\"\"] missing []", sb.toString());

        sb.setLength(0);
        exp = new String[] { "1", "2", "3" };
        act = new String[] { "2", "4" };
        assertTrue(Util.reportMemberDiffs(exp, act, sb));
        assertEquals("unexpected [\"4\"] missing [\"1\", \"3\"]", sb.toString());

    }
    // public void testResourceStream() throws Exception {
    // String path = PREFIX + ".one.mf";
    // System.out.println(path);
    // InputStream in = LOADER.getResourceAsStream(path);
    // int i;
    // while (-1 != (i = in.read())) {
    // System.out.print((char) i);
    // }
    // System.out.println();
    // }
    // Map map = bundle.manifest.getEntries();
    // for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
    // MapEntry entry = (MapEntry) iter.next();
    // System.out.println("entry: " + entry);
    // }
    // System.out.println("main attributes");
    // Attributes attributes = bundle.manifest.getMainAttributes();
    // Set keys = attributes.keySet();
    // for (Iterator iter = keys.iterator(); iter.hasNext();) {
    // Object key = iter.next();
    // System.out.println(" key " + key);
    // System.out.println(" value " + attributes.getValue(key.toString()));
    // }
}
