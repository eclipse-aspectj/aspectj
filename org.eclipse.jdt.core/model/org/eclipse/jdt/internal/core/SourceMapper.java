/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.core.util.ReferenceInfoAdapter;

/**
 * A SourceMapper maps source code in a ZIP file to binary types in
 * a JAR. The SourceMapper uses the fuzzy parser to identify source
 * fragments in a .java file, and attempts to match the source code
 * with children in a binary type. A SourceMapper is associated
 * with a JarPackageFragment by an AttachSourceOperation.
 *
 * @see org.eclipse.jdt.internal.core.JarPackageFragment
 */
public class SourceMapper
	extends ReferenceInfoAdapter
	implements ISourceElementRequestor {

	/**
	 * The binary type source is being mapped for
	 */
	protected BinaryType fType;

	/**
	 * The location of the zip file containing source.
	 */
	protected IPath fZipPath;
	/**
	 * Specifies the location of the package fragment root within
	 * the zip (empty specifies the default root). <code>null</code> is
	 * not a valid root path.
	 */
	protected String fRootPath;

	/**
	 * Used for efficiency
	 */
	protected static String[] fgEmptyStringArray = new String[0];

	/**
	 * Table that maps a binary method to its parameter names.
	 * Keys are the method handles, entries are <code>char[][]</code>.
	 */
	protected HashMap fParameterNames;
	
	/**
	 * Table that maps a binary element to its <code>SourceRange</code>s.
	 * Keys are the element handles, entries are <code>SourceRange[]</code> which
	 * is a two element array; the first being source range, the second
	 * being name range.
	 */
	protected HashMap fSourceRanges;
	

	/**
	 * The unknown source range {-1, 0}
	 */
	protected static SourceRange fgUnknownRange = new SourceRange(-1, 0);

	/**
	 * The position within the source of the start of the
	 * current member element, or -1 if we are outside a member.
	 */
	protected int[] fMemberDeclarationStart;
	/**
	 * The <code>SourceRange</code> of the name of the current member element.
	 */
	protected SourceRange[] fMemberNameRange;
	/**
	 * The name of the current member element.
	 */
	protected String[] fMemberName;
	
	/**
	 * The parameter names for the current member method element.
	 */
	protected char[][][] fMethodParameterNames;
	
	/**
	 * The parameter types for the current member method element.
	 */
	protected char[][][] fMethodParameterTypes;
	

	/**
	 * The element searched for
	 */
	protected IJavaElement searchedElement;

	/**
	 * imports references
	 */
	private HashMap importsTable;
	private HashMap importsCounterTable;

	/**
	 * Enclosing type information
	 */
	IType[] types;
	int[] typeDeclarationStarts;
	SourceRange[] typeNameRanges;
	int typeDepth;
	
	/**
	 *  Anonymous counter in case we want to map the source of an anonymous class.
	 */
	int anonymousCounter;
	int anonymousClassName;
	
	/**
	 * File encoding to be used
	 */
	String encoding;
	
	/**
	 * Creates a <code>SourceMapper</code> that locates source in the zip file
	 * at the given location in the specified package fragment root.
	 */
	public SourceMapper(IPath zipPath, String rootPath) {
		this.fZipPath = zipPath;
		this.fRootPath = rootPath.replace('\\', '/');
		if (this.fRootPath.endsWith("/" )) { //$NON-NLS-1$
			this.fRootPath = this.fRootPath.substring(0, this.fRootPath.lastIndexOf('/'));
		}
		this.fSourceRanges = new HashMap();
		this.fParameterNames = new HashMap();
		this.importsTable = new HashMap();
		this.importsCounterTable = new HashMap();
		
		IResource zipResource = ResourcesPlugin.getWorkspace().getRoot().findMember(zipPath);

		this.encoding = JavaCore.getOption(JavaCore.CORE_ENCODING);
	}
	
	/**
	 * @see ISourceElementRequestor
	 */
	public void acceptImport(
		int declarationStart,
		int declarationEnd,
		char[] name,
		boolean onDemand) {
		char[][] imports = (char[][]) this.importsTable.get(fType);
		int importsCounter;
		if (imports == null) {
			imports = new char[5][];
			importsCounter = 0;
		} else {
			importsCounter = ((Integer) this.importsCounterTable.get(fType)).intValue();
		}
		if (imports.length == importsCounter) {
			System.arraycopy(
				imports,
				0,
				(imports = new char[importsCounter * 2][]),
				0,
				importsCounter);
		}
		if (onDemand) {
			int nameLength = name.length;
			System.arraycopy(name, 0, (name = new char[nameLength + 2]), 0, nameLength);
			name[nameLength] = '.';
			name[nameLength + 1] = '*';
		}
		imports[importsCounter++] = name;
		this.importsTable.put(fType, imports);
		this.importsCounterTable.put(fType, new Integer(importsCounter));
	}
	
	/**
	 * @see ISourceElementRequestor
	 */
	public void acceptLineSeparatorPositions(int[] positions) {
		//do nothing
	}
	
	/**
	 * @see ISourceElementRequestor
	 */
	public void acceptPackage(
		int declarationStart,
		int declarationEnd,
		char[] name) {
		//do nothing
	}
	
	/**
	 * @see ISourceElementRequestor
	 */
	public void acceptProblem(IProblem problem) {
		//do nothing
	}
	
	/**
	 * Closes this <code>SourceMapper</code>'s zip file. Once this is done, this
	 * <code>SourceMapper</code> cannot be used again.
	 */
	public void close() throws JavaModelException {
		fSourceRanges = null;
		fParameterNames = null;
	}

	/**
	 * Converts these type names to unqualified signatures. This needs to be done in order to be consistent
	 * with the way the source range is retrieved.
	 * @see SourceMapper#getUnqualifiedMethodHandle
	 * @see Signature.
	 */
	private String[] convertTypeNamesToSigs(char[][] typeNames) {
		if (typeNames == null)
			return fgEmptyStringArray;
		int n = typeNames.length;
		if (n == 0)
			return fgEmptyStringArray;
		String[] typeSigs = new String[n];
		for (int i = 0; i < n; ++i) {
			String typeSig = Signature.createTypeSignature(typeNames[i], false);
			int lastIndex = typeSig.lastIndexOf('.');
			if (lastIndex == -1) {
				typeSigs[i] = typeSig;
			} else {
				typeSigs[i] = Signature.C_UNRESOLVED + typeSig.substring(lastIndex + 1, typeSig.length());
			}
		}
		return typeSigs;
	}
	
	/**
	 * @see ISourceElementRequestor
	 */
	public void enterClass(
		int declarationStart,
		int modifiers,
		char[] name,
		int nameSourceStart,
		int nameSourceEnd,
		char[] superclass,
		char[][] superinterfaces) {

		this.typeDepth++;
		if (this.typeDepth == this.types.length) { // need to grow
			System.arraycopy(
				this.types,
				0,
				this.types = new IType[this.typeDepth * 2],
				0,
				this.typeDepth);
			System.arraycopy(
				this.typeNameRanges,
				0,
				this.typeNameRanges = new SourceRange[this.typeDepth * 2],
				0,
				this.typeDepth);
			System.arraycopy(
				this.typeDeclarationStarts,
				0,
				this.typeDeclarationStarts = new int[this.typeDepth * 2],
				0,
				this.typeDepth);
			System.arraycopy(
				this.fMemberName,
				0,
				this.fMemberName = new String[this.typeDepth * 2],
				0,
				this.typeDepth);
			System.arraycopy(
				this.fMemberDeclarationStart,
				0,
				this.fMemberDeclarationStart = new int[this.typeDepth * 2],
				0,
				this.typeDepth);							
			System.arraycopy(
				this.fMemberNameRange,
				0,
				this.fMemberNameRange = new SourceRange[this.typeDepth * 2],
				0,
				this.typeDepth);
			System.arraycopy(
				this.fMethodParameterTypes,
				0,
				this.fMethodParameterTypes = new char[this.typeDepth * 2][][],
				0,
				this.typeDepth);
			System.arraycopy(
				this.fMethodParameterNames,
				0,
				this.fMethodParameterNames = new char[this.typeDepth * 2][][],
				0,
				this.typeDepth);					
		}
		if (name.length == 0) {
			this.anonymousCounter++;
			if (this.anonymousCounter == this.anonymousClassName) {
				this.types[typeDepth] = this.getType(fType.getElementName());
			} else {
				this.types[typeDepth] = this.getType(new String(name));				
			}
		} else {
			this.types[typeDepth] = this.getType(new String(name));
		}
		this.typeNameRanges[typeDepth] =
			new SourceRange(nameSourceStart, nameSourceEnd - nameSourceStart + 1);
		this.typeDeclarationStarts[typeDepth] = declarationStart;
	}
	
	/**
	 * @see ISourceElementRequestor
	 */
	public void enterCompilationUnit() {
		// do nothing
	}
	
	/**
	 * @see ISourceElementRequestor
	 */
	public void enterConstructor(
		int declarationStart,
		int modifiers,
		char[] name,
		int nameSourceStart,
		int nameSourceEnd,
		char[][] parameterTypes,
		char[][] parameterNames,
		char[][] exceptionTypes) {
		enterMethod(
			declarationStart,
			modifiers,
			null,
			name,
			nameSourceStart,
			nameSourceEnd,
			parameterTypes,
			parameterNames,
			exceptionTypes);
	}
	
	/**
	 * @see ISourceElementRequestor
	 */
	public void enterField(
		int declarationStart,
		int modifiers,
		char[] type,
		char[] name,
		int nameSourceStart,
		int nameSourceEnd) {
		if (typeDepth >= 0) {
			fMemberDeclarationStart[typeDepth] = declarationStart;
			fMemberNameRange[typeDepth] =
				new SourceRange(nameSourceStart, nameSourceEnd - nameSourceStart + 1);
			fMemberName[typeDepth] = new String(name);
		}
	}
	
	/**
	 * @see ISourceElementRequestor
	 */
	public void enterInitializer(
		int declarationSourceStart,
		int modifiers) {
		//do nothing
	}
	
	/**
	 * @see ISourceElementRequestor
	 */
	public void enterInterface(
		int declarationStart,
		int modifiers,
		char[] name,
		int nameSourceStart,
		int nameSourceEnd,
		char[][] superinterfaces) {
		enterClass(
			declarationStart,
			modifiers,
			name,
			nameSourceStart,
			nameSourceEnd,
			null,
			superinterfaces);
	}
	
	/**
	 * @see ISourceElementRequestor
	 */
	public void enterMethod(
		int declarationStart,
		int modifiers,
		char[] returnType,
		char[] name,
		int nameSourceStart,
		int nameSourceEnd,
		char[][] parameterTypes,
		char[][] parameterNames,
		char[][] exceptionTypes) {
		if (typeDepth >= 0) {
			fMemberName[typeDepth] = new String(name);
			fMemberNameRange[typeDepth] =
				new SourceRange(nameSourceStart, nameSourceEnd - nameSourceStart + 1);
			fMemberDeclarationStart[typeDepth] = declarationStart;
			fMethodParameterTypes[typeDepth] = parameterTypes;
			fMethodParameterNames[typeDepth] = parameterNames;
		}
	}
	
	/**
	 * @see ISourceElementRequestor
	 */
	public void exitClass(int declarationEnd) {
		if (typeDepth >= 0) {
			IType currentType = this.types[typeDepth];
			setSourceRange(
				currentType,
				new SourceRange(
					this.typeDeclarationStarts[typeDepth],
					declarationEnd - this.typeDeclarationStarts[typeDepth] + 1),
				this.typeNameRanges[typeDepth]);
			this.typeDepth--;
		}
	}
	
	/**
	 * @see ISourceElementRequestor
	 */
	public void exitCompilationUnit(int declarationEnd) {
		//do nothing
	}
	
	/**
	 * @see ISourceElementRequestor
	 */
	public void exitConstructor(int declarationEnd) {
		exitMethod(declarationEnd);
	}
	
	/**
	 * @see ISourceElementRequestor
	 */
	public void exitField(int declarationEnd) {
		if (typeDepth >= 0) {
			IType currentType = this.types[typeDepth];
			setSourceRange(
				currentType.getField(fMemberName[typeDepth]),
				new SourceRange(
					fMemberDeclarationStart[typeDepth],
					declarationEnd - fMemberDeclarationStart[typeDepth] + 1),
				fMemberNameRange[typeDepth]);
		}
	}
	
	/**
	 * @see ISourceElementRequestor
	 */
	public void exitInitializer(int declarationEnd) {
	}
	
	/**
	 * @see ISourceElementRequestor
	 */
	public void exitInterface(int declarationEnd) {
		exitClass(declarationEnd);
	}
	
	/**
	 * @see ISourceElementRequestor
	 */
	public void exitMethod(int declarationEnd) {
		if (typeDepth >= 0) {
			IType currentType = this.types[typeDepth];
			SourceRange sourceRange =
				new SourceRange(
					fMemberDeclarationStart[typeDepth],
					declarationEnd - fMemberDeclarationStart[typeDepth] + 1);
			IMethod method = currentType.getMethod(
					fMemberName[typeDepth],
					convertTypeNamesToSigs(fMethodParameterTypes[typeDepth]));
			setSourceRange(
				method,
				sourceRange,
				fMemberNameRange[typeDepth]);
			setMethodParameterNames(
				method,
				fMethodParameterNames[typeDepth]);
		}
	}
	
	/**
	 * Locates and returns source code for the given (binary) type, in this
	 * SourceMapper's ZIP file, or returns <code>null</code> if source
	 * code cannot be found.
	 */
	public char[] findSource(IType type) {
		if (!type.isBinary()) {
			return null;
		}
		BinaryType parent = (BinaryType) type.getDeclaringType();
		BinaryType declType = (BinaryType) type;
		while (parent != null) {
			declType = parent;
			parent = (BinaryType) declType.getDeclaringType();
		}
		IBinaryType info = null;
		try {
			info = (IBinaryType) declType.getRawInfo();
		} catch (JavaModelException e) {
			return null;
		}
		return this.findSource(type, info);
	}
	
	/**
	 * Locates and returns source code for the given (binary) type, in this
	 * SourceMapper's ZIP file, or returns <code>null</code> if source
	 * code cannot be found.
	 */
	public char[] findSource(IType type, IBinaryType info) {
		char[] sourceFileName = info.sourceFileName();
		if (sourceFileName == null)
			return null; // no source file attribute
		String name = new String(sourceFileName);

		IPackageFragment pkgFrag = type.getPackageFragment();
		if (!pkgFrag.isDefaultPackage()) {
			String pkg = type.getPackageFragment().getElementName().replace('.', '/');
			name = pkg + '/' + name;
		}
		// try to get the entry
		ZipEntry entry = null;
		ZipFile zip = null;
		char[] source = null;
		try {
			String fullName;
			//add the root path if specified
			if (!fRootPath.equals(IPackageFragmentRoot.DEFAULT_PACKAGEROOT_PATH)) {
				fullName = fRootPath + '/' + name;
			} else {
				fullName = name;
			}
			zip = getZip();
			entry = zip.getEntry(fullName);
			if (entry != null) {
				// now read the source code
				byte[] bytes = null;
				try {
					bytes = Util.getZipEntryByteContent(entry, zip);
				} catch (IOException e) {
				}
				if (bytes != null) {
					try {
						source = Util.bytesToChar(bytes, this.encoding);
					} catch (IOException e) {
						source = null;
					}
				}
			}
		} catch (CoreException e) {
			return null;
		} finally {
			JavaModelManager.getJavaModelManager().closeZipFile(zip);
		}
		return source;
	}
	
	/**
	 * Returns the SourceRange for the name of the given element, or
	 * {-1, -1} if no source range is known for the name of the element.
	 */
	public SourceRange getNameRange(IJavaElement element) {
		if (element.getElementType() == IJavaElement.METHOD
			&& ((IMember) element).isBinary()) {
			IJavaElement[] el = getUnqualifiedMethodHandle((IMethod) element, false);
			if(el[1] != null && fSourceRanges.get(el[0]) == null) {
				element = getUnqualifiedMethodHandle((IMethod) element, true)[0];
			} else {
				element = el[0];
			}
		}
		SourceRange[] ranges = (SourceRange[]) fSourceRanges.get(element);
		if (ranges == null) {
			return fgUnknownRange;
		} else {
			return ranges[1];
		}
	}
	
	/**
	 * Returns parameters names for the given method, or
	 * null if no parameter names are known for the method.
	 */
	public char[][] getMethodParameterNames(IMethod method) {
		if (((IMember) method).isBinary()) {
			IJavaElement[] el = getUnqualifiedMethodHandle(method, false);
			if(el[1] != null && fParameterNames.get(el[0]) == null) {
				method = (IMethod) getUnqualifiedMethodHandle(method, true)[0];
			} else {
				method = (IMethod) el[0];
			}
		}
		char[][] parameterNames = (char[][]) fParameterNames.get(method);
		if (parameterNames == null) {
			return null;
		} else {
			return parameterNames;
		}
	}
	
	/**
	 * Returns the <code>SourceRange</code> for the given element, or
	 * {-1, -1} if no source range is known for the element.
	 */
	public SourceRange getSourceRange(IJavaElement element) {
		if (element.getElementType() == IJavaElement.METHOD
			&& ((IMember) element).isBinary()) {
			IJavaElement[] el = getUnqualifiedMethodHandle((IMethod) element, false);
			if(el[1] != null && fSourceRanges.get(el[0]) == null) {
				element = getUnqualifiedMethodHandle((IMethod) element, true)[0];
			} else {
				element = el[0];
			}
		}
		SourceRange[] ranges = (SourceRange[]) fSourceRanges.get(element);
		if (ranges == null) {
			return fgUnknownRange;
		} else {
			return ranges[0];
		}
	}
	
	/**
	 * Returns the type with the given <code>typeName</code>.  Returns inner classes
	 * as well.
	 */
	protected IType getType(String typeName) {
		if (fType.getElementName().equals(typeName))
			return fType;
		else
			return fType.getType(typeName);
	}
	
	/**
	 * Creates a handle that has parameter types that are not
	 * fully qualified so that the correct source is found.
	 */
	protected IJavaElement[] getUnqualifiedMethodHandle(IMethod method, boolean noDollar) {
		boolean hasDollar = false;
		String[] qualifiedParameterTypes = method.getParameterTypes();
		String[] unqualifiedParameterTypes = new String[qualifiedParameterTypes.length];
		for (int i = 0; i < qualifiedParameterTypes.length; i++) {
			StringBuffer unqualifiedName = new StringBuffer();
			String qualifiedName = qualifiedParameterTypes[i];
			int count = 0;
			while (qualifiedName.charAt(count) == Signature.C_ARRAY) {
				unqualifiedName.append(Signature.C_ARRAY);
				++count;
			}
			if (qualifiedName.charAt(count) == Signature.C_RESOLVED) {
				unqualifiedName.append(Signature.C_UNRESOLVED);
				String simpleName = Signature.getSimpleName(qualifiedName.substring(count+1));
				if(!noDollar) {
					if(!hasDollar && simpleName.indexOf('$') != -1) {
						hasDollar = true;
					}
					unqualifiedName.append(simpleName);
				} else {
					unqualifiedName.append(CharOperation.lastSegment(simpleName.toCharArray(), '$'));
				}
			} else {
				unqualifiedName.append(qualifiedName.substring(count, qualifiedName.length()));
			}
			unqualifiedParameterTypes[i] = unqualifiedName.toString();
		}
		
		IJavaElement[] result = new IJavaElement[2];
		result[0] = ((IType) method.getParent()).getMethod(
			method.getElementName(),
			unqualifiedParameterTypes);
		if(hasDollar) {
			result[1] = result[0];
		}
		return result;
	}
	
	/**
	 * Returns the <code>ZipFile</code> that source is located in.
	 */
	public ZipFile getZip() throws CoreException {
		return JavaModelManager.getJavaModelManager().getZipFile(fZipPath);
	}
	
	/**
	 * Maps the given source code to the given binary type and its children.
	 */
	public void mapSource(IType type, char[] contents) {
		this.mapSource(type, contents, null);
	}
	
	/**
	 * Maps the given source code to the given binary type and its children.
	 * If a non-null java element is passed, finds the name range for the 
	 * given java element without storing it.
	 */
	public ISourceRange mapSource(
		IType type,
		char[] contents,
		IJavaElement searchedElement) {
			
		fType = (BinaryType) type;
		
		// check whether it is already mapped
		if (this.fSourceRanges.get(type) != null) return (searchedElement != null) ? this.getNameRange(searchedElement) : null;
		
		this.importsTable.remove(fType);
		this.importsCounterTable.remove(fType);
		this.searchedElement = searchedElement;
		this.types = new IType[1];
		this.typeDeclarationStarts = new int[1];
		this.typeNameRanges = new SourceRange[1];
		this.typeDepth = -1;
		this.fMemberDeclarationStart = new int[1];
		this.fMemberName = new String[1];
		this.fMemberNameRange = new SourceRange[1];
		this.fMethodParameterTypes = new char[1][][];
		this.fMethodParameterNames = new char[1][][];
		this.anonymousCounter = 0;
		
		HashMap oldSourceRanges = (HashMap) fSourceRanges.clone();
		try {
			IProblemFactory factory = new DefaultProblemFactory();
			SourceElementParser parser = null;
			boolean isAnonymousClass = false;
			char[] fullName = null;
			this.anonymousClassName = 0;
			try {
				IBinaryType binType = (IBinaryType) fType.getRawInfo();
				isAnonymousClass = binType.isAnonymous();
				fullName = binType.getName();
			} catch(JavaModelException e) {
			}
			if (isAnonymousClass) {
				String eltName = fType.getElementName();
				eltName = eltName.substring(eltName.lastIndexOf('$') + 1, eltName.length());
				try {
					this.anonymousClassName = Integer.parseInt(eltName);
				} catch(NumberFormatException e) {
				}
			}
			boolean doFullParse = hasToRetrieveSourceRangesForLocalClass(fullName);
			parser = new SourceElementParser(this, factory, new CompilerOptions(JavaCore.getOptions()), doFullParse);
			parser.parseCompilationUnit(
				new BasicCompilationUnit(contents, null, type.getElementName() + ".java", encoding), //$NON-NLS-1$
				doFullParse);
			if (searchedElement != null) {
				ISourceRange range = this.getNameRange(searchedElement);
				return range;
			} else {
				return null;
			}
		} finally {
			if (searchedElement != null) {
				fSourceRanges = oldSourceRanges;
			}
			fType = null;
			this.searchedElement = null;
			this.types = null;
			this.typeDeclarationStarts = null;
			this.typeNameRanges = null;
			this.typeDepth = -1;
		}
	}
	
	/** 
	 * Sets the mapping for this method to its parameter names.
	 *
	 * @see fParameterNames
	 */
	protected void setMethodParameterNames(
		IMethod method,
		char[][] parameterNames) {
		if (parameterNames == null) {
			parameterNames = new char[0][];
		}
		fParameterNames.put(method, parameterNames);
	}
	
	/** 
	 * Sets the mapping for this element to its source ranges for its source range
	 * and name range.
	 *
	 * @see fSourceRanges
	 */
	protected void setSourceRange(
		IJavaElement element,
		SourceRange sourceRange,
		SourceRange nameRange) {
		fSourceRanges.put(element, new SourceRange[] { sourceRange, nameRange });
	}

	/**
	 * Return a char[][] array containing the imports of the attached source for the fType binary
	 */
	public char[][] getImports(BinaryType type) {
		char[][] imports = (char[][]) this.importsTable.get(type);
		if (imports != null) {
			int importsCounter = ((Integer) this.importsCounterTable.get(type)).intValue();
			if (imports.length != importsCounter) {
				System.arraycopy(
					imports,
					0,
					(imports = new char[importsCounter][]),
					0,
					importsCounter);
			}
			this.importsTable.put(type, imports);
		}
		return imports;
	}
	
	private boolean hasToRetrieveSourceRangesForLocalClass(char[] eltName) {
		/*
		 * A$1$B$2 : true
		 * A$B$B$2 : true
		 * A$C$B$D : false
		 * A$F$B$D$1$F : true
		 * A$1 : true
		 * A$B : false
		 */
		if (eltName == null) return false;
		int index = 0;
		int dollarIndex = CharOperation.indexOf('$', eltName, index);
		if (dollarIndex != -1) {
			index = dollarIndex + 1;
			dollarIndex = CharOperation.indexOf('$', eltName, index);
			if (dollarIndex == -1) { 
				dollarIndex = eltName.length;
			}
			while (dollarIndex != -1) {
				for (int i = index; i < dollarIndex; i++) {
					if (!Character.isDigit(eltName[i])) {
						index = dollarIndex + 1;
						i = dollarIndex;
						if (index > eltName.length) return false;
						dollarIndex = CharOperation.indexOf('$', eltName, index);
						if (dollarIndex == -1) { 
							dollarIndex = eltName.length;
						}
						continue;
					}
				}
				return true;
			}
		}
		return false;
	}	
}