/* *******************************************************************
 * Copyright (c) 2005 Contributors.
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

/**
 * Represents a prospective build result and any requirements for it. Used for
 * [testing|normal][jar|assembled-jar|classesDir?].
 */
public class Result {
    public static final boolean NORMAL = true;

    public static final boolean ASSEMBLE = true;

    static final Kind RELEASE = new Kind("RELEASE", NORMAL, !ASSEMBLE);

    static final Kind RELEASE_ALL = new Kind("RELEASE_ALL", NORMAL, ASSEMBLE);

    static final Kind TEST = new Kind("TEST", !NORMAL, !ASSEMBLE);

    static final Kind TEST_ALL = new Kind("TEST_ALL", !NORMAL, ASSEMBLE);

    private static final Kind[] KINDS = { RELEASE, TEST, RELEASE_ALL, TEST_ALL };

    private static final HashMap<String,Result> nameToResult = new HashMap<>();

    public static boolean isTestingJar(String name) {
        name = name.toLowerCase();
        return "junit.jar".equals(name);
    }

    public static boolean isTestingDir(String name) {
        name = name.toLowerCase();
        return (Util.Constants.TESTSRC.equals(name) || Util.Constants.JAVA5_TESTSRC
                .equals(name));
    }

    public static boolean isTestingModule(Module module) {
        String name = module.name.toLowerCase();
        return name.startsWith("testing") || "tests".equals(name);
    }

    public static synchronized Result getResult(String name) {
        if (null == name) {
            throw new IllegalArgumentException("null name");
        }
        return nameToResult.get(name);
    }

    public static Result[] getResults(String[] names) {
        if (null == names) {
            return new Result[0];
        }
        Result[] results = new Result[names.length];

        for (int i = 0; i < results.length; i++) {
            String name = names[i];
            if (null == name) {
                String m = "no name at " + i + ": " + Arrays.asList(names);
                throw new IllegalArgumentException(m);
            }
            Result r = Result.getResult(name);
            if (null == r) {
                String m = "no result [" + i + "]: " + name + ": "
                        + Arrays.asList(names);
                throw new IllegalArgumentException(m);
            }
            results[i] = r;
        }
        return results;

    }

    public static Kind[] KINDS() {
        Kind[] result = new Kind[KINDS.length];
        System.arraycopy(KINDS, 0, result, 0, result.length);
        return result;
    }

    public static void iaxUnlessNormal(Result result) {
        if ((null == result) || !result.getKind().normal) {
            throw new IllegalArgumentException("not normal: " + result);
        }
    }

    public static void iaxUnlessAssembly(Result result) {
        if ((null == result) || !result.getKind().assemble) {
            throw new IllegalArgumentException("not assembly: " + result);
        }
    }

    public static Kind kind(boolean normal, boolean assemble) {
        return (normal == NORMAL ? (assemble == ASSEMBLE ? RELEASE_ALL
                : RELEASE) : (assemble == ASSEMBLE ? TEST_ALL : TEST));
    }

    public static class Kind {
        final String name;

        final boolean normal;

        final boolean assemble;

        private Kind(String name, boolean normal, boolean assemble) {
            this.name = name;
            this.normal = normal;
            this.assemble = assemble;
        }

        public final boolean isAssembly() {
            return assemble;
        }

        public final boolean isNormal() {
            return normal;
        }

        public final String toString() {
            return name;
        }
    }

    /** path to output jar - may not exist */
    private final File outputFile;

    /** List of required Result */
    private final List<Result> requiredResults;

    /** List of library jars */
    private final List<File> libJars;

    /** List of classpath variables */
    private final List<String> classpathVariables;

    transient String toLongString;

    /**
     * List of library jars exported to clients (duplicates some libJars
     * entries)
     */
    private final List<File> exportedLibJars;

    /** List of source directories */
    private final List<File> srcDirs;

    /** true if this has calculated List fields. */
    private boolean requiredDone;

    /** true if this has been found to be out of date */
    private boolean outOfDate;

    /** true if we have calculated whether this is out of date */
    private boolean outOfDateSet;

    private final Kind kind;

    private final Module module;

    private final String name;

    Result(Kind kind, Module module, File jarDir) {
        this.kind = kind;
        this.module = module;
        this.libJars = new ArrayList<>();
        this.exportedLibJars = new ArrayList<>();
        this.srcDirs = new ArrayList<>();
        this.classpathVariables = new ArrayList<>();
        this.requiredResults = new ArrayList<>();
        String name = module.name;
        if (!kind.normal) {
            name += "-test";
        }
        if (kind.assemble) {
            name += "-all";
        }
        this.name = name;
        this.outputFile = new File(jarDir, name + ".jar");
        nameToResult.put(name, this);
    }

    public String getName() {
        return name;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void clearOutOfDate() {
        outOfDateSet = false;
        outOfDate = false;
    }

    public boolean outOfDate() {
        if (!outOfDateSet) {
            outOfDate = Module.outOfDate(this);
            outOfDateSet = true;
        }
        return outOfDate;
    }

    /** @return List (File) of jar's required */
    public List<File> findJarRequirements() {
        List<File> result = new ArrayList<>();
        Module.doFindJarRequirements(this, result);
        return result;
    }

    /** @return unmodifiable List of String classpath variables */
    public List<String> getClasspathVariables() {
        return safeList(classpathVariables);
    }

    //
    /** @return unmodifiable List of required modules String names */
    public Result[] getRequired() {
        return safeResults(requiredResults);
    }

    /**
     * @return unmodifiable list of exported library files, guaranteed readable
     */
    public List<File> getExportedLibJars() {
        return safeList(exportedLibJars);
    }

    /**
     * @return unmodifiable list of required library files, guaranteed readable
     */
    public List<File> getLibJars() {
        requiredDone();
        return safeList(libJars);
    }

    /**
     * @return unmodifiable list of required library files, guaranteed readable
     */
    // public List getMerges() {
    // requiredDone();
    // return safeList(merges);
    // }
    /** @return unmodifiable list of source directories, guaranteed readable */
    public List<File> getSrcDirs() {
        return safeList(srcDirs);
    }

    public Module getModule() {
        return module;
    }

    public Kind getKind() {
        return kind;
    }

    public String toLongString() {
        if (null == toLongString) {
            toLongString = name + "[outputFile=" + outputFile
                    + ", requiredResults=" + requiredResults + ", srcDirs="
                    + srcDirs + ", libJars=" + libJars + "]";
        }
        return toLongString;
    }

    public String toString() {
        return name;
    }

    private <T> List<T> safeList(List<T> l) {
        requiredDone();
        return Collections.unmodifiableList(l);
    }

    private Result[] safeResults(List<Result> list) {
        requiredDone();
        if (null == list) {
            return new Result[0];
        }
        return list.toArray(new Result[0]);
    }

    private void initSrcDirs() {
        srcDirs.addAll(getModule().srcDirs(this));
        if (getKind().normal) {
            // trim testing source directories
            for (ListIterator<File> iter = srcDirs.listIterator(); iter.hasNext();) {
                File srcDir = iter.next();
                if (isTestingDir(srcDir.getName())) {
                    iter.remove();
                }
            }
        }
    }

    private void initLibJars() {
        libJars.addAll(getModule().libJars(this));
        if (getKind().normal && !isTestingModule(getModule())) {
            // trim testing libraries
            for (ListIterator<File> iter = libJars.listIterator(); iter.hasNext();) {
                File libJar = iter.next();
                if (isTestingJar(libJar.getName())) {
                    iter.remove();
                }
            }
        }
    }

    private void assertKind(Kind kind) {
        if (kind != getKind()) {
            throw new IllegalArgumentException("expected " + getKind()
                    + " got " + kind);
        }
    }

    private void initRequiredResults() {
        final Module module = getModule();
        final Kind kind = getKind();
        if (kind.assemble) {
            if (kind.normal) {
                assertKind(RELEASE_ALL);
                requiredResults.add(module.getResult(RELEASE));
            } else {
                assertKind(TEST_ALL);
                requiredResults.add(module.getResult(TEST));
                requiredResults.add(module.getResult(RELEASE));
            }
        } else if (!kind.normal) {
            assertKind(TEST);
            requiredResults.add(module.getResult(RELEASE));
        } else {
            assertKind(RELEASE);
        }
        // externally-required:
        List<Module> modules = module.requiredModules(this);
        final boolean adoptTests = !kind.normal || isTestingModule(module);
        for (Module required: modules) {
            if (adoptTests) {
                // testing builds can rely on other release and test results
                requiredResults.add(required.getResult(TEST));
                requiredResults.add(required.getResult(RELEASE));
            } else if (!isTestingModule(required)){
                // release builds can only rely on non-testing results
                // from non-testing modules
                requiredResults.add(required.getResult(RELEASE));
            } // else skip release dependencies on testing-* (testing-util)
        }
    }

    private void initClasspathVariables() {
        // no difference
        classpathVariables.addAll(getModule().classpathVariables(this));
    }

    private void initExportedLibJars() {
        // no difference
        exportedLibJars.addAll(getModule().exportedLibJars(this));
    }

    private synchronized void requiredDone() {
        if (!requiredDone) {
            initSrcDirs();
            initLibJars();
            initRequiredResults();
            initClasspathVariables();
            initExportedLibJars();
            requiredDone = true;
        }
    }

}
