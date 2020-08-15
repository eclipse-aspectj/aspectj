/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation,
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/


package org.aspectj.internal.tools.build;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

/**
 * Build-only utilities.
 * Many mirror utils module APIs.
 */
public class Util {
    public static class Constants {
        public static final String TESTSRC = "testsrc";
        public static final String JAVA5_SRC = "java5-src";
        public static final String JAVA5_TESTSRC = "java5-testsrc";
    }
    // XXX quick hack for Java 5 support
    public static final boolean JAVA5_VM;
    static {
        boolean java5VM = false;
        try {
            java5VM = (null != Class.forName("java.lang.annotation.Annotation"));
        } catch (Throwable t) {
            // ignore
        }
        JAVA5_VM = java5VM;
    }

    /**
     * Map version in long form to short,
     * e.g., replacing "alpha" with "a"
     */
    public static String shortVersion(String version) {
        version = Util.replace(version, "alpha", "a");
        version = Util.replace(version, "beta", "b");
        version = Util.replace(version, "candidate", "rc");
        version = Util.replace(version, "development", "d");
        version = Util.replace(version, "dev", "d");
        return version;
    }

    /**
     * Replace any instances of {replace} in {input} with {with}.
     * @param input the String to search/replace
     * @param replace the String to search for in input
     * @param with the String to replace with in input
     * @return input if it has no replace, otherwise a new String
     */
    public static String replace(String input, String replace, String with) {
        int loc = input.indexOf(replace);
        if (-1 != loc) {
            String result = input.substring(0, loc);
            result += with;
            int start = loc + replace.length();
            if (start < input.length()) {
                result += input.substring(start);
            }
            input = result;
        }
        return input;
    }

    /** @return false if filter returned false for any file in baseDir subtree */
    public static boolean visitFiles(File baseDir, FileFilter filter) {
        Util.iaxIfNotCanReadDir(baseDir, "baseDir");
        Util.iaxIfNull(filter, "filter");
        File[] files = baseDir.listFiles();
        boolean passed = true;
        for (int i = 0; passed && (i < files.length); i++) {
			passed = files[i].isDirectory()
                ? visitFiles(files[i], filter)
                : filter.accept(files[i]);
		}
        return passed;
    }

    /** @throws IllegalArgumentException if cannot read dir */
    public static void iaxIfNotCanReadDir(File dir, String name) {
        if (!canReadDir(dir)) {
            throw new IllegalArgumentException(name + " dir not readable: " + dir);
        }
    }

    /** @throws IllegalArgumentException if cannot read file */
    public static void iaxIfNotCanReadFile(File file, String name) {
        if (!canReadFile(file)) {
            throw new IllegalArgumentException(name + " file not readable: " + file);
        }
    }

    /** @throws IllegalArgumentException if cannot write dir */
    public static void iaxIfNotCanWriteDir(File dir, String name) {
        if (!canWriteDir(dir)) {
            throw new IllegalArgumentException(name + " dir not writeable: " + dir);
        }
    }

    /** @throws IllegalArgumentException if input is null */
    public static void iaxIfNull(Object input, String name) {
        if (null == input) {
            throw new IllegalArgumentException("null " + name);
        }
    }

    /** render exception to String */
    public static String renderException(Throwable thrown) {
        if (null == thrown) {
            return "(Throwable) null";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        pw.println(thrown.getMessage());
        thrown.printStackTrace(pw);
        pw.flush();
        return sw.getBuffer().toString();
    }

    /** @return true if dir is a writable directory */
    public static boolean canWriteDir(File dir) {
        return (null != dir) && dir.canWrite() && dir.isDirectory();
    }

    public static String path(String first, String second) {
        return first + File.separator + second;
    }

    public static String path(String[] segments) {
        StringBuffer sb = new StringBuffer();
        if ((null != segments)) {
            for (int i = 0; i < segments.length; i++) {
                if (0 < i) {
                    sb.append(File.separator);
                }
                sb.append(segments[i]);
            }
        }
        return sb.toString();
    }

    /** @return true if dir is a readable directory */
    public static boolean canReadDir(File dir) {
        return (null != dir) && dir.canRead() && dir.isDirectory();
    }

    /** @return true if dir is a readable file */
    public static boolean canReadFile(File file) {
        return (null != file) && file.canRead() && file.isFile();
    }

    /**
     * Delete file or directory.
     * @param dir the File file or directory to delete.
     * @return true if all contents of dir were deleted
     */
    public static boolean delete(File dir) {
        return deleteContents(dir) && dir.delete();
    }

    /**
     * Delete contents of directory.
     * The directory itself is not deleted.
     * @param dir the File directory whose contents should be deleted.
     * @return true if all contents of dir were deleted
     */
    public static boolean deleteContents(File dir) {
        if ((null == dir) || !dir.canWrite()) {
            return false;
        } else if (dir.isDirectory()) {
            File[] files = dir.listFiles();
			for (File file : files) {
				if (!deleteContents(file) || !file.delete()) {
					return false;
				}
			}
        }
        return true;
    }

    /** @return File temporary directory with the given prefix */
    public static File makeTempDir(String prefix) {
        if (null == prefix) {
            prefix = "tempDir";
        }
        File tempFile = null;
        for (int i = 0; i < 10; i++) {
            try {
                tempFile =  File.createTempFile(prefix,"tmp");
                tempFile.delete();
                if (tempFile.mkdirs()) {
                    break;
                }
                tempFile = null;
            } catch (IOException e) {
            }
        }
        return tempFile;
    }
    /**
     * Close stream with the usual checks.
     * @param stream the InputStream to close - ignored if null
     * @return null if closed without IOException, message otherwise
     */
    public static String close(Writer stream) {
        String result = null;
        if (null != stream) {
            try {
                stream.close();
            } catch(IOException e) {
                result = e.getMessage();
            }
        }
        return result;
    }

    /**
     * @param list the Object[] to test
     * @return true if list is null or empty
     */
    public static boolean isEmpty(Object[] list) {
        return ((null == list) || (0 == list.length));
    }

    public static void closeSilently(InputStream in) {
        if (null != in) {
            try {
                in.close();
            } catch (IOException e) {
                // do nothing
            }
        }
    }

    public static void closeSilently(Reader in) {
        if (null != in) {
            try {
                in.close();
            } catch (IOException e) {
                // do nothing
            }
        }
    }

    /**
     * Report whether actual has different members than expected
     * @param expected the String[] of expected members (none null)
     * @param actual the String[] of actual members
     * @param sb StringBuffer sink for any differences in membership
     * @return true if any diffs found and sink updated
     */
    public static final boolean reportMemberDiffs(String[] expected, String[] actual, StringBuffer sb) {
        expected = copy(expected);
        actual = copy(actual);
        int hits = 0;
        for (int i = 0; i < expected.length; i++) {
            int curHit = hits;
            for (int j = 0; (curHit == hits) && (j < actual.length); j++) {
                if (null == expected[i]) {
                    throw new IllegalArgumentException("null at " + i);
                }
                if (expected[i].equals(actual[j])) {
                    expected[i] = null;
                    actual[j] = null;
                    hits++;
                }
            }
        }
        if ((hits != expected.length) || (hits != actual.length)) {
            sb.append("unexpected [");
            String prefix = "";
			for (String value : actual) {
				if (null != value) {
					sb.append(prefix);
					prefix = ", ";
					sb.append("\"");
					sb.append(value);
					sb.append("\"");
				}
			}
            sb.append("] missing [");
            prefix = "";
			for (String s : expected) {
				if (null != s) {
					sb.append(prefix);
					prefix = ", ";
					sb.append("\"");
					sb.append(s);
					sb.append("\"");
				}
			}
            sb.append("]");
            return true;
        }
        return false;
    }

    private static final String[] copy(String[] ra) {
        if (null == ra) {
            return new String[0];
        }
        String[] result = new String[ra.length];
        System.arraycopy(ra, 0, result, 0, ra.length);
        return result;
    }

    /**
     * Support for OSGI bundles read from manifest files.
     * Currently very limited, and will only support the subset of
     * features that we use.
     * sources:
     * http://www-128.ibm.com/developerworks/library/os-ecl-osgi/index.html
     * http://help.eclipse.org/help30/index.jsp?topic=/org.eclipse.platform.doc.isv/reference/osgi/org/osgi/framework/Constants.html
     */
    public static class OSGIBundle {
        public static final Name BUNDLE_NAME = new Name("Bundle-Name");

        public static final Name BUNDLE_SYMBOLIC_NAME = new Name(
                "Bundle-SymbolicName");

        public static final Name BUNDLE_VERSION = new Name("Bundle-Version");

        public static final Name BUNDLE_ACTIVATOR = new Name("Bundle-Activator");

        public static final Name BUNDLE_VENDOR = new Name("Bundle-Vendor");

        public static final Name REQUIRE_BUNDLE = new Name("Require-Bundle");

        public static final Name IMPORT_PACKAGE = new Name("Import-Package");

        public static final Name BUNDLE_CLASSPATH = new Name("Bundle-ClassPath");

        /** unmodifiable list of all valid OSGIBundle Name's */
        public static final List<Name> NAMES;
        static {
            List<Name> names = new ArrayList<>();
            names.add(BUNDLE_NAME);
            names.add(BUNDLE_SYMBOLIC_NAME);
            names.add(BUNDLE_VERSION);
            names.add(BUNDLE_ACTIVATOR);
            names.add(BUNDLE_VENDOR);
            names.add(REQUIRE_BUNDLE);
            names.add(IMPORT_PACKAGE);
            names.add(BUNDLE_CLASSPATH);
            NAMES = Collections.unmodifiableList(names);
        }

        private final Manifest manifest;

        private final Attributes attributes;

        /**
         *
         * @param manifestInputStream
         *            the InputStream of the manifest.mf - will be closed.
         * @throws IOException
         *             if unable to read or close the manifest input stream.
         */
        public OSGIBundle(InputStream manifestInputStream) throws IOException {
            manifest = new Manifest();
            manifest.read(manifestInputStream);
            manifestInputStream.close();
            attributes = manifest.getMainAttributes();
        }

        public String getAttribute(Name attributeName) {
            return attributes.getValue(attributeName);
        }

        public String[] getClasspath() {
            String cp = getAttribute(OSGIBundle.BUNDLE_CLASSPATH);
            if (null == cp) {
                return new String[0];
            }
            StringTokenizer st = new StringTokenizer(cp, " ,");
            String[] result = new String[st.countTokens()];
            int i = 0;
            while (st.hasMoreTokens()) {
                result[i++] = st.nextToken();
            }
            return result;
        }

        /**
         * XXX ugly/weak hack only handles a single version comma
         * {name};bundle-version="[1.5.0,1.5.5]";resolution:=optional
         * @return
         */
        public RequiredBundle[] getRequiredBundles() {
            String value = getAttribute(OSGIBundle.REQUIRE_BUNDLE);
            if (null == value) {
                return new RequiredBundle[0];
            }
            StringTokenizer st = new StringTokenizer(value, " ,");
            RequiredBundle[] result = new RequiredBundle[st.countTokens()];
            int i = 0;
            int skips = 0;
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                int first = token.indexOf("\"");
                if (-1 != first) {
                    if (!st.hasMoreTokens()) {
                        throw new IllegalArgumentException(token);
                    }
                    // just assume only one quoted "," for version?
                    token += "," + st.nextToken();
                    skips++;
                }
                result[i++] = new RequiredBundle(token);
            }
            if (skips > 0) {
                RequiredBundle[] patch = new RequiredBundle[result.length-skips];
                System.arraycopy(result, 0, patch, 0, patch.length);
                result = patch;
            }
            return result;
        }

        /**
         * Wrap each dependency on another bundle
         */
        public static class RequiredBundle {

            /** unparsed entry text, for debugging */
            final String text;

            /** Symbolic name of the required bundle */
            final String name;

            /** if not null, then start/end versions of required bundle
             * in the format of the corresponding manifest entry
             */
            final String versions;

            /** if true, then required bundle is optional */
            final boolean optional;

            private RequiredBundle(String entry) {
                text = entry;
                StringTokenizer st = new StringTokenizer(entry, ";");
                name = st.nextToken();
                String vers = null;
                String opt = null;
                // bundle-version="[1.5.0,1.5.5]";resolution:=optiona
                final String RESOLUTION = "resolution:=";
                final String VERSION = "bundle-version=\"";
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    if (token.startsWith(VERSION)) {
                        int start = VERSION.length();
                        int end = token.lastIndexOf("\"");
                        vers = token.substring(start, end);
                        // e.g., [1.5.0,1.5.5)
                    } else if (token.startsWith(RESOLUTION)) {
                        int start = RESOLUTION.length();
                        int end = token.length();
                        opt = token.substring(start, end);
                    }
                }
                versions = vers;
                optional = "optional".equals(opt);
            }
        }
    }
}

