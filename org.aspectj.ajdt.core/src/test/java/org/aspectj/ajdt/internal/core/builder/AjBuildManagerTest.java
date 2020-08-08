/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     PARC     initial implementation
 * ******************************************************************/

package org.aspectj.ajdt.internal.core.builder;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.aspectj.ajdt.StreamPrintWriter;
import org.aspectj.ajdt.ajc.BuildArgParser;
import org.aspectj.ajdt.ajc.Constants;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.bridge.MessageWriter;
import org.aspectj.testing.util.TestUtil;

import junit.framework.TestCase;

public class AjBuildManagerTest extends TestCase {

	private final StreamPrintWriter outputWriter = new StreamPrintWriter(new PrintWriter(System.out));
	private final MessageWriter messageWriter = new MessageWriter(outputWriter, false);
	public static File source1 = new File(Constants.TESTDATA_DIR, "src1/A.java");
	public static File source2 = new File(Constants.TESTDATA_DIR, "src1/Hello.java");
	public static File source3 = new File(Constants.TESTDATA_DIR, "src1/X.java");

	/**
	 * @throws AssertionFailedError unless handler has 0 messages worse than warning, or the one message is a warning about
	 *         aspectjrt.jar
	 */
	public static void assertCompileMessagesValid(MessageHandler handler) {
		assertTrue("null handler", null != handler);
		final int numMessages = handler.numMessages(IMessage.WARNING, true);
		if (1 == numMessages) { // permit aspectjrt.jar warning
			IMessage m = handler.getMessages(IMessage.WARNING, true)[0];
			if (!(m.isWarning() && (m.getMessage().contains("aspectjrt.jar")))) {
				assertTrue(handler.toString(), false);
			}
		} else if (0 != numMessages) {
			assertTrue(handler.toString(), false);
		}
	}

	public AjBuildManagerTest(String name) {
		super(name);
	}

	public void testSimpleStructure() throws IOException {
		AjBuildManager manager = new AjBuildManager(messageWriter);
		BuildArgParser parser = new BuildArgParser(messageWriter);
		String javaClassPath = System.getProperty("java.class.path");
		String sandboxName = TestUtil.createEmptySandbox().getAbsolutePath();
		AjBuildConfig buildConfig = parser.genBuildConfig(new String[] { "-d", sandboxName, "-1.4", "-classpath", javaClassPath,
				Constants.TESTDATA_PATH + "/src1/A.java",
		// EajcModuleTests.TESTDATA_PATH + "/src1/Hello.java",
				});
		String err = parser.getOtherMessages(true);
		assertTrue(err, null == err || err.startsWith("incorrect classpath"));
		// manager.setStructureModel(AsmManager.getDefault().getHierarchy());
		MessageHandler handler = new MessageHandler();
		manager.batchBuild(buildConfig, handler);
		assertCompileMessagesValid(handler);
	}

	// XXX add test for resource deltas
	//
	// public void testUpdateBuildConfig() {
	// final File FILE_1 = new File("testdata/testclasses/Temp1.java");
	// final File FILE_2 = new File("testdata/testclasses/Temp2.java");
	// final File FILE_3 = new File("testdata/testclasses/Temp3.java");
	// List files = new ArrayList();
	// files.add(FILE_1);
	// files.add(FILE_2);
	//
	// AjBuildManager manager = new AjBuildManager(messageWriter);
	// AjBuildConfig buildConfig = new AjBuildConfig();
	// manager.buildConfig = buildConfig;
	// buildConfig.setFiles(files);
	//
	// manager.updateBuildConfig(buildConfig);
	// assertTrue("no change", manager.deletedFiles.isEmpty());
	//
	// AjBuildConfig newConfig = new AjBuildConfig();
	// newConfig.getFiles().add(FILE_1);
	// newConfig.getFiles().add(FILE_2);
	// newConfig.getFiles().add(FILE_3);
	// manager.updateBuildConfig(newConfig);
	// assertTrue("added file", manager.deletedFiles.isEmpty());
	// assertTrue(manager.addedFiles.size() == 1);
	// assertTrue(manager.addedFiles.contains(FILE_3));
	//
	// newConfig = new AjBuildConfig();
	// newConfig.getFiles().add(FILE_3);
	// manager.updateBuildConfig(newConfig);
	// assertTrue("deleted 2 files", manager.addedFiles.isEmpty());
	// assertTrue(manager.deletedFiles.size() == 2);
	// assertTrue(manager.deletedFiles.contains(FILE_1));
	//
	// newConfig = new AjBuildConfig();
	// newConfig.getFiles().add(FILE_2);
	// manager.updateBuildConfig(newConfig);
	// assertTrue("added file", manager.addedFiles.size() == 1);
	// assertTrue("deleted file", manager.deletedFiles.size() == 1);
	// assertTrue(manager.deletedFiles.size() == 1);
	// assertTrue(manager.addedFiles.contains(FILE_2));
	// assertTrue(manager.deletedFiles.contains(FILE_3));
	// }
	//
	// /**
	// * Pretends that the files 'have been' modified in the future and waits.
	// * Tests:
	// * 1) no change,
	// * 2) added file,
	// * 3) removed file
	// *
	// * XXX should just test modified
	// */
	// public void testGetModifiedFiles() throws IOException, InterruptedException {
	// final File TEMP_1 = new File("testdata/testclasses/TempChanged.java");
	// final File EXISTS_2 = new File("testdata/testclasses/p1/Foo.java");
	// final File NEW = new File("testdata/testclasses/TempNew.java");
	// NEW.delete();
	// touch(TEMP_1, false);
	// List files = new ArrayList();
	// files.add(TEMP_1);
	// files.add(EXISTS_2);
	//
	// assertTrue("input files", TEMP_1.exists() && EXISTS_2.exists());
	// assertTrue("new file", !NEW.exists());
	//
	// Thread.sleep(100);
	// long lastBuildTime = System.currentTimeMillis();
	//
	// AjBuildManager manager = new AjBuildManager(messageWriter);
	// manager.buildConfig = new AjBuildConfig();
	// manager.buildConfig.setFiles(files);
	// Collection changedFiles = manager.getModifiedFiles(lastBuildTime);
	// assertTrue("nothing changed: " + changedFiles, changedFiles.isEmpty());
	//
	// lastBuildTime = System.currentTimeMillis();
	// Thread.sleep(100);
	//
	// touch(NEW, false);
	//
	// //NEW.createNewFile();
	// files.add(NEW);
	// changedFiles = manager.getModifiedFiles(lastBuildTime);
	// assertTrue("new file: " + changedFiles, changedFiles.contains(NEW));
	//
	// lastBuildTime = System.currentTimeMillis();
	// Thread.sleep(100);
	//
	// files.remove(NEW);
	// changedFiles = manager.getModifiedFiles(lastBuildTime);
	// assertTrue("nothing changed", changedFiles.isEmpty());
	//
	// lastBuildTime = System.currentTimeMillis();
	// Thread.sleep(100);
	//
	// touch(TEMP_1, true);
	// changedFiles = manager.getModifiedFiles(lastBuildTime);
	// assertTrue("touched file: " + changedFiles, changedFiles.contains(TEMP_1));
	//
	// lastBuildTime = System.currentTimeMillis();
	// Thread.sleep(100);
	//
	// files.remove(NEW);
	// changedFiles = manager.getModifiedFiles(lastBuildTime);
	// assertTrue("nothing changed", changedFiles.isEmpty());
	//
	// TEMP_1.delete();
	// NEW.delete();
	// }

	// don't do delta's anymore
	// public void testMakeDeltas() throws IOException, InterruptedException {
	// AjBuildManager manager = new AjBuildManager(messageWriter);
	// manager.buildConfig = new AjBuildConfig();
	// List sourceRoots = new ArrayList();
	// sourceRoots.add(new File("out"));
	// manager.buildConfig.setSourceRoots(sourceRoots);
	// assertTrue(manager.testInit(messageWriter));
	// List modified = Arrays.asList(new File[] { new File("A.java"), new File("B.java") });
	// List deleted = Arrays.asList(new File[] { new File("X.java") });
	// SimpleLookupTable deltas = new SimpleLookupTable();
	// manager.makeDeltas(
	// deltas,
	// modified,
	// deleted,
	// ((File)manager.buildConfig.getSourceRoots().get(0)).getPath());
	//
	// ResourceDelta d = (ResourceDelta)deltas.get(manager.getJavaBuilder().currentProject);
	// assertNotNull(d);
	//
	// assertEquals(d.getAffectedChildren().length, 3);
	// //XXX do more testing of children
	// }
	//
	// // XXX should this be working??
	// public void testDeleteRealFiles() throws CoreException, IOException {
	// AjBuildManager manager = new AjBuildManager(messageWriter);
	// manager.buildConfig = new AjBuildConfig();
	// List sourceRoots = new ArrayList();
	// sourceRoots.add(new File("testdata/src1"));
	// manager.buildConfig.setSourceRoots(sourceRoots);
	// manager.buildConfig.setOutputDir(new File("out"));
	// assertTrue(manager.testInit(messageWriter));
	//
	// File realClassFile = new File("out/X.class");
	// touch(realClassFile, false);
	//
	// assertTrue(realClassFile.exists());
	//
	// IFile classfile = manager.classFileCache.getFile(new Path("X.class"));
	// classfile.create(FileUtil.getStreamFromZip("testdata/testclasses.jar", "Hello.class"), true, null);
	// assertTrue(classfile.exists());
	//
	// manager.addAspectClassFilesToWeaver();
	//
	// classfile.delete(true, false, null);
	// assertTrue(realClassFile.exists());
	//
	// manager.addAspectClassFilesToWeaver();
	//
	// assertTrue(!realClassFile.exists());
	//
	// }

	// !!!
	// public void testIncrementalCompilerCall() throws IOException, InterruptedException, CoreException {
	// AjBuildManager manager = new AjBuildManager(messageWriter);
	//
	// manager.buildConfig = new AjBuildConfig();
	// List roots = new ArrayList();
	// roots.add(new File("testdata/src1"));
	// manager.testInit(messageWriter);
	// manager.buildConfig.setSourceRoots(roots);
	// assertTrue(manager.testInit(messageWriter));
	// List modified = Arrays.asList(new File[] { source1, source2 });
	// List deleted = Arrays.asList(new File[] { source3 });
	// SimpleLookupTable deltas = new SimpleLookupTable();
	// manager.makeDeltas(
	// deltas,
	// modified,
	// deleted,
	// ((File)manager.buildConfig.getSourceRoots().get(0)).getAbsolutePath());
	//
	// JavaBuilder jbuilder = manager.getJavaBuilder();
	// jbuilder.lastState = new State(jbuilder);
	// jbuilder.binaryLocationsPerProject = new SimpleLookupTable();
	//
	// AjBuildManager.IncrementalBuilder builder
	// = manager.getIncrementalBuilder(messageWriter); // XXX trap errors
	// TestNotifier testNotifier = new TestNotifier(builder, jbuilder.currentProject);
	// jbuilder.notifier = testNotifier;
	//
	// IContainer[] sourceFolders = new IContainer[] {
	// new FilesystemFolder(((File)manager.buildConfig.getSourceRoots().get(0)).getAbsolutePath())
	// };
	// builder.setSourceFolders(sourceFolders);
	// testNotifier.builder = builder;
	//
	// IFile classfile = manager.classFileCache.getFile(new Path("X.class"));
	// classfile.create(new ByteArrayInputStream(new byte[] {1,2,3}), true, null);
	//
	// assertTrue(classfile.exists());
	//
	//
	// try {
	// manager.testSetHandler(messageWriter);
	// boolean succeeded = builder.build(deltas);
	// } catch (NonLocalExit nle) {
	// assertEquals(nle.getExitCode(), 0);
	// } finally {
	// manager.testSetHandler(null);
	// }
	//
	// assertTrue(!classfile.exists());
	// }
	//
	// static class TestNotifier extends BuildNotifier {
	// int state = 0;
	// AjBuildManager.IncrementalBuilder builder;
	//
	// public TestNotifier(AjBuildManager.IncrementalBuilder builder, IProject project) {
	// super(null, project);
	// this.builder = builder;
	// }
	//
	//
	// public void updateProgressDelta(float percentWorked) {
	// switch(state) {
	// case 0:
	// checkInitialConfig();
	// break;
	// case 1:
	// checkBinaryResources();
	// break;
	// case 2:
	// checkAffectedFiles();
	// break;
	// }
	// state += 1;
	// }
	//
	// private void checkBinaryResources() {
	// }
	//
	//
	// private void checkInitialConfig() {
	// Collection files = builder.getLocations();
	// //System.out.println("initial: " + files);
	// }
	//
	// private void checkAffectedFiles() {
	// Collection files = builder.getLocations();
	// TestUtil.assertSetEquals(Arrays.asList(new String[] {
	// source1.getAbsolutePath().replace(File.separatorChar, '/'),
	// source2.getAbsolutePath().replace(File.separatorChar, '/') }), files);
	// throw new NonLocalExit(0);
	// }
	// }

	// private void touch(File file, boolean isAppend) throws IOException {
	// FileOutputStream s = new FileOutputStream(file.getAbsolutePath(), isAppend);
	// s.write(new byte[] {1,2,3});
	// s.close();
	// }

	/*
	 * jar directory source directory container
	 */
	// public void testMakeClasspathLocations() {
	// List classpath = new ArrayList();
	// classpath.add(
	//
	// AjBuildConfig config = new AjBuildConfig();
	// config.setClasspath()
	// }
	// private void testClasspathLocation(String loca
}
