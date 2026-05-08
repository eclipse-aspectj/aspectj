package org.aspectj.asm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.asm.internal.ProgramElement;
import org.aspectj.bridge.SourceLocation;

import junit.framework.TestCase;

public class AsmManagerTest extends TestCase {

	public void testReadStructureModelRoundTripRestoresHierarchyAndRelationships() throws Exception {
		File configFile = File.createTempFile("asm-manager-test", ".lst");
		File sourceFile = File.createTempFile("asm-manager-source", ".java");
		File structureModelFile = new File(configFile.getParentFile(), configFile.getName().replace(".lst", ".ajsym"));
		try {
			AsmManager asm = buildModel(sourceFile);
			ProgramElement fileNode = (ProgramElement) asm.getHierarchy().getRoot().getChildren().get(0);
			asm.getRelationshipMap().get(fileNode, IRelationship.Kind.ADVICE, "advises", false, true).addTarget("=project/target");
			asm.writeStructureModel(configFile.getAbsolutePath());

			AsmManager restored = AsmManager.createNewStructureModel(Collections.<File, String>emptyMap());
			restored.readStructureModel(configFile.getAbsolutePath());

			assertNotSame(IHierarchy.NO_STRUCTURE, restored.getHierarchy().getRoot());
			assertEquals("project", restored.getHierarchy().getRoot().getName());
			assertNotNull(restored.getHierarchy().findInFileMap(sourceFile.getCanonicalPath()));
			assertEquals(1, restored.getRelationshipMap().get(fileNode.getHandleIdentifier()).size());
		} finally {
			configFile.delete();
			sourceFile.delete();
			structureModelFile.delete();
		}
	}

	public void testPrePatchRawObjectInputStreamWouldExecuteReadObjectCallback() throws Exception {
		File structureModelFile = File.createTempFile("asm-manager-test", ".ajsym");
		try {
			writeStructureModelPayload(structureModelFile, new UnexpectedSerializedType());
			UnexpectedSerializedType.readObjectInvoked = false;
			try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(structureModelFile))) {
				in.readObject();
			}
			assertTrue(UnexpectedSerializedType.readObjectInvoked);
		} finally {
			structureModelFile.delete();
		}
	}

	public void testPatchedReadStructureModelRejectsUnexpectedSerializedTypesBeforeReadObject() throws Exception {
		File configFile = File.createTempFile("asm-manager-test", ".lst");
		File structureModelFile = new File(configFile.getParentFile(), configFile.getName().replace(".lst", ".ajsym"));
		try {
			writeStructureModelPayload(structureModelFile, new UnexpectedSerializedType());
			UnexpectedSerializedType.readObjectInvoked = false;

			AsmManager asm = AsmManager.createNewStructureModel(Collections.<File, String>emptyMap());
			asm.readStructureModel(configFile.getAbsolutePath());

			assertFalse(UnexpectedSerializedType.readObjectInvoked);
			assertSame(IHierarchy.NO_STRUCTURE, asm.getHierarchy().getRoot());
		} finally {
			configFile.delete();
			structureModelFile.delete();
		}
	}

	public void testReadStructureModelSupportsArraysAsList() throws Exception {
		assertModelRoundTripWithCustomData(new ProgramElementMutator() {
			public void mutate(ProgramElement node) {
				node.setParameterNames(Arrays.asList("a", "b"));
			}
		});
	}

	public void testReadStructureModelSupportsUnmodifiableList() throws Exception {
		assertModelRoundTripWithCustomData(new ProgramElementMutator() {
			public void mutate(ProgramElement node) {
				List<String> values = new ArrayList<>();
				values.add("a");
				values.add("b");
				node.setParameterNames(Collections.unmodifiableList(values));
			}
		});
	}

	public void testReadStructureModelSupportsLinkedHashMap() throws Exception {
		assertModelRoundTripWithCustomData(new ProgramElementMutator() {
			public void mutate(ProgramElement node) {
				Map<String, List<String>> map = new LinkedHashMap<>();
				map.put("Type", Arrays.asList("Parent"));
				node.setDeclareParentsMap(map);
			}
		});
	}

	private void assertModelRoundTripWithCustomData(ProgramElementMutator mutator) throws Exception {
		File configFile = File.createTempFile("asm-manager-test", ".lst");
		File sourceFile = File.createTempFile("asm-manager-source", ".java");
		File structureModelFile = new File(configFile.getParentFile(), configFile.getName().replace(".lst", ".ajsym"));
		try {
			AsmManager asm = buildModel(sourceFile);
			ProgramElement fileNode = (ProgramElement) asm.getHierarchy().getRoot().getChildren().get(0);
			mutator.mutate(fileNode);
			asm.writeStructureModel(configFile.getAbsolutePath());

			AsmManager restored = AsmManager.createNewStructureModel(Collections.<File, String>emptyMap());
			restored.readStructureModel(configFile.getAbsolutePath());

			assertNotSame(IHierarchy.NO_STRUCTURE, restored.getHierarchy().getRoot());
			assertNotNull(restored.getHierarchy().findInFileMap(sourceFile.getCanonicalPath()));
		} finally {
			configFile.delete();
			sourceFile.delete();
			structureModelFile.delete();
		}
	}

	private AsmManager buildModel(File sourceFile) throws Exception {
		AsmManager asm = AsmManager.createNewStructureModel(Collections.<File, String>emptyMap());
		ProgramElement root = new ProgramElement(asm, "project", IProgramElement.Kind.PROJECT, null);
		ProgramElement fileNode = new ProgramElement(asm, sourceFile.getName(), IProgramElement.Kind.FILE_JAVA,
				new SourceLocation(sourceFile, 1, 1, 1), 0, null, null);
		fileNode.setHandleIdentifier("=project/" + sourceFile.getName());
		root.addChild(fileNode);
		asm.getHierarchy().setRoot(root);
		HashMap<String, IProgramElement> fileMap = new HashMap<>();
		fileMap.put(sourceFile.getCanonicalPath(), fileNode);
		asm.getHierarchy().setFileMap(fileMap);
		return asm;
	}

	private static void writeStructureModelPayload(File file, Object payload) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
		try {
			out.writeObject(payload);
		} finally {
			out.close();
		}
	}

	private static class UnexpectedSerializedType implements Serializable {
		private static final long serialVersionUID = 1L;
		private static boolean readObjectInvoked;

		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			readObjectInvoked = true;
			in.defaultReadObject();
		}
	}

	private interface ProgramElementMutator {
		void mutate(ProgramElement node);
	}
}
