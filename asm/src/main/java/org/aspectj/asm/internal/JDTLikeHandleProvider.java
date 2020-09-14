/********************************************************************
 * Copyright (c) 2006 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version
 *******************************************************************/
package org.aspectj.asm.internal;

import java.io.File;
import java.util.List;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IElementHandleProvider;
import org.aspectj.asm.IProgramElement;
import org.aspectj.bridge.ISourceLocation;

/**
 * Creates JDT-like handles, for example
 * 
 * method with string argument: &lt;tjp{Demo.java[Demo~main~\[QString; method with generic argument:
 * &lt;pkg{MyClass.java[MyClass~myMethod~QList\&lt;QString;&gt;; an aspect: &lt;pkg*A1.aj}A1 advice with Integer arg:
 * &lt;pkg*A8.aj}A8&amp;afterReturning&amp;QInteger; method call: &lt;pkg*A10.aj[C~m1?method-call(void pkg.C.m2())
 * 
 */
public class JDTLikeHandleProvider implements IElementHandleProvider {

	private final AsmManager asm;

	private static final char[] empty = new char[] {};
	private static final char[] countDelim = new char[] { HandleProviderDelimiter.COUNT.getDelimiter() };

	private static final String backslash = "\\";
	private static final String emptyString = "";

	public JDTLikeHandleProvider(AsmManager asm) {
		this.asm = asm;
	}

	public void initialize() {
		// nothing to do
	}

	public String createHandleIdentifier(IProgramElement ipe) {
		// AjBuildManager.setupModel --> top of the tree is either
		// <root> or the .lst file
		if (ipe == null || (ipe.getKind().equals(IProgramElement.Kind.FILE_JAVA) && ipe.getName().equals("<root>"))) {
			return "";
		} else if (ipe.getHandleIdentifier(false) != null) {
			// have already created the handle for this ipe
			// therefore just return it
			return ipe.getHandleIdentifier();
		} else if (ipe.getKind().equals(IProgramElement.Kind.FILE_LST)) {
			String configFile = asm.getHierarchy().getConfigFile();
			int start = configFile.lastIndexOf(File.separator);
			int end = configFile.lastIndexOf(".lst");
			if (end != -1) {
				configFile = configFile.substring(start + 1, end);
			} else {
				configFile = new StringBuffer("=").append(configFile.substring(start + 1)).toString();
			}
			ipe.setHandleIdentifier(configFile);
			return configFile;
		} else if (ipe.getKind() == IProgramElement.Kind.SOURCE_FOLDER) {
			StringBuffer sb = new StringBuffer();
			sb.append(createHandleIdentifier(ipe.getParent())).append("/");
			// pr249216 - escape any embedded slashes
			String folder = ipe.getName();
			if (folder.endsWith("/")) {
				folder = folder.substring(0, folder.length() - 1);
			}
			if (folder.contains("/")) {
				folder = folder.replace("/", "\\/");
			}
			sb.append(folder);
			String handle = sb.toString();
			ipe.setHandleIdentifier(handle);
			return handle;
		}
		IProgramElement parent = ipe.getParent();
		if (parent != null && parent.getKind().equals(IProgramElement.Kind.IMPORT_REFERENCE)) {
			// want to miss out '#import declaration' in the handle
			parent = ipe.getParent().getParent();
		}

		StringBuffer handle = new StringBuffer();
		// add the handle for the parent
		handle.append(createHandleIdentifier(parent));
		// add the correct delimiter for this ipe
		handle.append(HandleProviderDelimiter.getDelimiter(ipe));
		// add the name and any parameters unless we're an initializer
		// (initializer's names are '...')
		if (!ipe.getKind().equals(IProgramElement.Kind.INITIALIZER)) {
			if (ipe.getKind() == IProgramElement.Kind.CLASS && ipe.getName().endsWith("{..}")) {
				// format: 'new Runnable() {..}' but its anon-y-mouse
				// dont append anything, there may be a count to follow though (!<n>)
			} else {
				if (ipe.getKind() == IProgramElement.Kind.INTER_TYPE_CONSTRUCTOR) {
					handle.append(ipe.getName()).append("_new").append(getParameters(ipe));
				} else {
					// if (ipe.getKind() == IProgramElement.Kind.PACKAGE && ipe.getName().equals("DEFAULT")) {
					// // the delimiter will be in there, but skip the word DEFAULT as it is just a placeholder
					// } else {
					if (ipe.getKind().isDeclareAnnotation()) {
						// escape the @ (pr249216c9)
						handle.append("declare \\@").append(ipe.getName().substring(9)).append(getParameters(ipe));
					} else {
						if (ipe.getFullyQualifiedName() != null) {
							handle.append(ipe.getFullyQualifiedName());
						} else {
							handle.append(ipe.getName());
						}
						handle.append(getParameters(ipe));
					}
				}
				// }
			}
		}
		// add the count, for example '!2' if its the second ipe of its
		// kind in the aspect
		handle.append(getCount(ipe));

		ipe.setHandleIdentifier(handle.toString());
		return handle.toString();
	}

	private String getParameters(IProgramElement ipe) {
		if (ipe.getParameterSignatures() == null || ipe.getParameterSignatures().isEmpty()) {
			return "";
		}
		List<String> sourceRefs = ipe.getParameterSignaturesSourceRefs();
		List<char[]> parameterTypes = ipe.getParameterSignatures();
		StringBuffer sb = new StringBuffer();
		if (sourceRefs != null) {
			for (String sourceRef : sourceRefs) {
				sb.append(HandleProviderDelimiter.getDelimiter(ipe));
				sb.append(sourceRef);
			}
		} else {
			for (char[] element : parameterTypes) {
				sb.append(HandleProviderDelimiter.getDelimiter(ipe));
				sb.append(NameConvertor.createShortName(element, false, false));
			}
		}
		return sb.toString();
	}

	/**
	 * Determine a count to be suffixed to the handle, this is only necessary for identical looking entries at the same level in the
	 * model (for example two anonymous class declarations). The format is !<n> where n will be greater than 2.
	 * 
	 * @param ipe the program element for which the handle is being constructed
	 * @return a char suffix that will either be empty or of the form "!<n>"
	 */
	private char[] getCount(IProgramElement ipe) {
		// TODO could optimize this code
		char[] byteCodeName = ipe.getBytecodeName().toCharArray();

		if (ipe.getKind().isInterTypeMember()) {
			int count = 1;
			List<IProgramElement> kids = ipe.getParent().getChildren();
			for (IProgramElement object : kids) {
				if (object.equals(ipe)) {
					break;
				}
				if (object.getKind().isInterTypeMember()) {
					if (object.getName().equals(ipe.getName()) && getParameters(object).equals(getParameters(ipe))) {
						String existingHandle = object.getHandleIdentifier();
						int suffixPosition = existingHandle.indexOf('!');
						if (suffixPosition != -1) {
							count = new Integer(existingHandle.substring(suffixPosition + 1)) + 1;
						} else {
							if (count == 1) {
								count = 2;
							}
						}
					}
				}
			}
			if (count > 1) {
				return CharOperation.concat(countDelim, new Integer(count).toString().toCharArray());
			}
		} else if (ipe.getKind().isDeclare()) {
			// // look at peer declares
			int count = computeCountBasedOnPeers(ipe);
			if (count > 1) {
				return CharOperation.concat(countDelim, new Integer(count).toString().toCharArray());
			}
		} else if (ipe.getKind().equals(IProgramElement.Kind.ADVICE)) {
			// Look at any peer advice
			int count = 1;
			List<IProgramElement> kids = ipe.getParent().getChildren();
			String ipeSig = ipe.getBytecodeSignature();
			// remove return type from the signature - it should not be included in the comparison
			int idx = 0;
			ipeSig = shortenIpeSig(ipeSig);
			for (IProgramElement object : kids) {
				if (object.equals(ipe)) {
					break;
				}
				if (object.getKind() == ipe.getKind()) {
					if (object.getName().equals(ipe.getName())) {
						String sig1 = object.getBytecodeSignature();
						if (sig1 != null && (idx = sig1.indexOf(")")) != -1) {
							sig1 = sig1.substring(0, idx);
						}
						// this code needs a speed overhaul... and some proper tests
						// Two static parts because one may be enclosing jpsp (269522)
						if (sig1 != null) {
							if (sig1.contains("Lorg/aspectj/lang")) {
								if (sig1.endsWith("Lorg/aspectj/lang/JoinPoint$StaticPart;")) {
									sig1 = sig1.substring(0, sig1.lastIndexOf("Lorg/aspectj/lang/JoinPoint$StaticPart;"));
								}
								if (sig1.endsWith("Lorg/aspectj/lang/JoinPoint;")) {
									sig1 = sig1.substring(0, sig1.lastIndexOf("Lorg/aspectj/lang/JoinPoint;"));
								}
								if (sig1.endsWith("Lorg/aspectj/lang/JoinPoint$StaticPart;")) {
									sig1 = sig1.substring(0, sig1.lastIndexOf("Lorg/aspectj/lang/JoinPoint$StaticPart;"));
								}
							}
						}

						if (sig1 == null && ipeSig == null || (sig1 != null && sig1.equals(ipeSig))) {
							String existingHandle = object.getHandleIdentifier();
							int suffixPosition = existingHandle.indexOf('!');
							if (suffixPosition != -1) {
								count = new Integer(existingHandle.substring(suffixPosition + 1)) + 1;
							} else {
								if (count == 1) {
									count = 2;
								}
							}
						}
					}
				}
			}
			if (count > 1) {
				return CharOperation.concat(countDelim, new Integer(count).toString().toCharArray());
			}
		} else if (ipe.getKind().equals(IProgramElement.Kind.INITIALIZER)) {
			// return String.valueOf(++initializerCounter).toCharArray();
			// Look at any peer advice
			int count = 1;
			List<IProgramElement> kids = ipe.getParent().getChildren();
			String ipeSig = ipe.getBytecodeSignature();
			// remove return type from the signature - it should not be included in the comparison
			int idx = 0;
			ipeSig = shortenIpeSig(ipeSig);
			for (IProgramElement object : kids) {
				if (object.equals(ipe)) {
					break;
				}
				if (object.getKind() == ipe.getKind()) {
					if (object.getName().equals(ipe.getName())) {
						String sig1 = object.getBytecodeSignature();
						if (sig1 != null && (idx = sig1.indexOf(")")) != -1) {
							sig1 = sig1.substring(0, idx);
						}
						// this code needs a speed overhaul... and some proper tests
						// Two static parts because one may be enclosing jpsp (269522)
						if (sig1 != null) {
							if (sig1.contains("Lorg/aspectj/lang")) {
								if (sig1.endsWith("Lorg/aspectj/lang/JoinPoint$StaticPart;")) {
									sig1 = sig1.substring(0, sig1.lastIndexOf("Lorg/aspectj/lang/JoinPoint$StaticPart;"));
								}
								if (sig1.endsWith("Lorg/aspectj/lang/JoinPoint;")) {
									sig1 = sig1.substring(0, sig1.lastIndexOf("Lorg/aspectj/lang/JoinPoint;"));
								}
								if (sig1.endsWith("Lorg/aspectj/lang/JoinPoint$StaticPart;")) {
									sig1 = sig1.substring(0, sig1.lastIndexOf("Lorg/aspectj/lang/JoinPoint$StaticPart;"));
								}
							}
						}

						if (sig1 == null && ipeSig == null || (sig1 != null && sig1.equals(ipeSig))) {
							String existingHandle = object.getHandleIdentifier();
							int suffixPosition = existingHandle.indexOf('!');
							if (suffixPosition != -1) {
								count = new Integer(existingHandle.substring(suffixPosition + 1)) + 1;
							} else {
								if (count == 1) {
									count = 2;
								}
							}
						}
					}
				}
			}
			// if (count > 1) {
			return new Integer(count).toString().toCharArray();
			// return CharOperation.concat(countDelim, new Integer(count).toString().toCharArray());
			// }
		} else if (ipe.getKind().equals(IProgramElement.Kind.CODE)) {
			int index = CharOperation.lastIndexOf('!', byteCodeName);
			if (index != -1) {
				return convertCount(CharOperation.subarray(byteCodeName, index + 1, byteCodeName.length));
			}
		} else if (ipe.getKind() == IProgramElement.Kind.CLASS) {
			// depends on previous children
			int count = 1;
			List<IProgramElement> kids = ipe.getParent().getChildren();
			if (ipe.getName().endsWith("{..}")) {
				// only depends on previous anonymous children, name irrelevant
				for (IProgramElement object : kids) {
					if (object.equals(ipe)) {
						break;
					}
					if (object.getKind() == ipe.getKind()) {
						if (object.getName().endsWith("{..}")) {
							String existingHandle = object.getHandleIdentifier();
							int suffixPosition = existingHandle.lastIndexOf('!');
							int lastSquareBracket = existingHandle.lastIndexOf('['); // type delimiter
							if (suffixPosition != -1 && lastSquareBracket < suffixPosition) { // pr260384
								count = new Integer(existingHandle.substring(suffixPosition + 1)) + 1;
							} else {
								if (count == 1) {
									count = 2;
								}
							}
						}
					}
				}
			} else {
				for (IProgramElement object : kids) {
					if (object.equals(ipe)) {
						break;
					}
					if (object.getKind() == ipe.getKind()) {
						if (object.getName().equals(ipe.getName())) {
							String existingHandle = object.getHandleIdentifier();
							int suffixPosition = existingHandle.lastIndexOf('!');
							int lastSquareBracket = existingHandle.lastIndexOf('['); // type delimiter
							if (suffixPosition != -1 && lastSquareBracket < suffixPosition) { // pr260384
								count = new Integer(existingHandle.substring(suffixPosition + 1)) + 1;
							} else {
								if (count == 1) {
									count = 2;
								}
							}
						}
					}
				}
			}
			if (count > 1) {
				return CharOperation.concat(countDelim, new Integer(count).toString().toCharArray());
			}
		}
		return empty;
	}

	private String shortenIpeSig(String ipeSig) {
		int idx;
		if (ipeSig != null && ((idx = ipeSig.indexOf(")")) != -1)) {
			ipeSig = ipeSig.substring(0, idx);
		}
		if (ipeSig != null) {
			if (ipeSig.contains("Lorg/aspectj/lang")) {
				if (ipeSig.endsWith("Lorg/aspectj/lang/JoinPoint$StaticPart;")) {
					ipeSig = ipeSig.substring(0, ipeSig.lastIndexOf("Lorg/aspectj/lang/JoinPoint$StaticPart;"));
				}
				if (ipeSig.endsWith("Lorg/aspectj/lang/JoinPoint;")) {
					ipeSig = ipeSig.substring(0, ipeSig.lastIndexOf("Lorg/aspectj/lang/JoinPoint;"));
				}
				if (ipeSig.endsWith("Lorg/aspectj/lang/JoinPoint$StaticPart;")) {
					ipeSig = ipeSig.substring(0, ipeSig.lastIndexOf("Lorg/aspectj/lang/JoinPoint$StaticPart;"));
				}
			}
		}
		return ipeSig;
	}

	private int computeCountBasedOnPeers(IProgramElement ipe) {
		int count = 1;
		for (IProgramElement object : ipe.getParent().getChildren()) {
			if (object.equals(ipe)) {
				break;
			}
			if (object.getKind() == ipe.getKind()) {
				if (object.getKind().toString().equals(ipe.getKind().toString())) {
					String existingHandle = object.getHandleIdentifier();
					int suffixPosition = existingHandle.indexOf('!');
					if (suffixPosition != -1) {
						count = new Integer(existingHandle.substring(suffixPosition + 1)) + 1;
					} else {
						if (count == 1) {
							count = 2;
						}
					}
				}
			}
		}
		return count;
	}

	/**
	 * Only returns the count if it's not equal to 1
	 */
	private char[] convertCount(char[] c) {
		if ((c.length == 1 && c[0] != ' ' && c[0] != '1') || c.length > 1) {
			return CharOperation.concat(countDelim, c);
		}
		return empty;
	}

	public String getFileForHandle(String handle) {
		IProgramElement node = asm.getHierarchy().getElement(handle);
		if (node != null) {
			return asm.getCanonicalFilePath(node.getSourceLocation().getSourceFile());
		} else if (handle.charAt(0) == HandleProviderDelimiter.ASPECT_CU.getDelimiter()
				|| handle.charAt(0) == HandleProviderDelimiter.COMPILATIONUNIT.getDelimiter()) {
			// it's something like *MyAspect.aj or {MyClass.java. In other words
			// it's a file node that's been created with no children and no
			// parent
			return backslash + handle.substring(1);
		}
		return emptyString;
	}

	public int getLineNumberForHandle(String handle) {
		IProgramElement node = asm.getHierarchy().getElement(handle);
		if (node != null) {
			return node.getSourceLocation().getLine();
		} else if (handle.charAt(0) == HandleProviderDelimiter.ASPECT_CU.getDelimiter()
				|| handle.charAt(0) == HandleProviderDelimiter.COMPILATIONUNIT.getDelimiter()) {
			// it's something like *MyAspect.aj or {MyClass.java. In other words
			// it's a file node that's been created with no children and no
			// parent
			return 1;
		}
		return -1;
	}

	public int getOffSetForHandle(String handle) {
		IProgramElement node = asm.getHierarchy().getElement(handle);
		if (node != null) {
			return node.getSourceLocation().getOffset();
		} else if (handle.charAt(0) == HandleProviderDelimiter.ASPECT_CU.getDelimiter()
				|| handle.charAt(0) == HandleProviderDelimiter.COMPILATIONUNIT.getDelimiter()) {
			// it's something like *MyAspect.aj or {MyClass.java. In other words
			// it's a file node that's been created with no children and no
			// parent
			return 0;
		}
		return -1;
	}

	public String createHandleIdentifier(ISourceLocation location) {
		IProgramElement node = asm.getHierarchy().findElementForSourceLine(location);
		if (node != null) {
			return createHandleIdentifier(node);
		}
		return null;
	}

	public String createHandleIdentifier(File sourceFile, int line, int column, int offset) {
		IProgramElement node = asm.getHierarchy().findElementForOffSet(sourceFile.getAbsolutePath(), line, offset);
		if (node != null) {
			return createHandleIdentifier(node);
		}
		return null;
	}

	public boolean dependsOnLocation() {
		// handles are independent of soureLocations therefore return false
		return false;
	}

}
