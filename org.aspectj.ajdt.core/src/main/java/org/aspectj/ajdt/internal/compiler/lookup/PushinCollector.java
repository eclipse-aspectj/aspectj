/* *******************************************************************
 * Copyright (c) 2010 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement - SpringSource
 * ******************************************************************/
package org.aspectj.ajdt.internal.compiler.lookup;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.aspectj.ajdt.internal.compiler.IOutputClassFileNameProvider;
import org.aspectj.asm.internal.CharOperation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.ExactTypePattern;
import org.aspectj.weaver.patterns.TypePattern;

/**
 * Collects up information about the application of ITDs and relevant declares - it can then output source code as if those ITDs had
 * been pushed in. Supports the simulated push-in of:
 * <ul>
 * <li>declare at_type
 * <li>itd method
 * <li>itd field
 * <li>itd ctor
 * <li>declare parents
 * </ul>
 * 
 * @author Andy Clement
 * @since 1.6.9
 */
public class PushinCollector {

	private final static String OPTION_SUFFIX = "suffix";
	private final static String OPTION_DIR = "dir";
	private final static String OPTION_PKGDIRS = "packageDirs";
	private final static String OPTION_DEBUG = "debug";
	private final static String OPTION_LINENUMS = "lineNums";
	private final static String OPTION_DUMPUNCHANGED = "dumpUnchanged";

	private World world;
	private boolean debug = false;
	private boolean dumpUnchanged = false;
	private IOutputClassFileNameProvider outputFileNameProvider;
	private String specifiedOutputDirectory;
	private boolean includePackageDirs;
	private boolean includeLineNumberComments;
	private String suffix;

	// This first collection stores the 'text' for the declarations.
	private Map<AbstractMethodDeclaration, RepresentationAndLocation> codeRepresentation = new HashMap<>();

	// This stores the new annotations
	private Map<SourceTypeBinding, List<String>> additionalAnnotations = new HashMap<>();

	// This stores the new parents
	private Map<SourceTypeBinding, List<ExactTypePattern>> additionalParents = new HashMap<>();

	// This indicates which types are affected by which intertype declarations
	private Map<SourceTypeBinding, List<AbstractMethodDeclaration>> newDeclarations = new HashMap<>();

	private PushinCollector(World world, Properties configuration) {
		this.world = world;

		// Configure the instance based on the input properties
		specifiedOutputDirectory = configuration.getProperty(OPTION_DIR);
		includePackageDirs = configuration.getProperty(OPTION_PKGDIRS, "true").equalsIgnoreCase("true");
		includeLineNumberComments = configuration.getProperty(OPTION_LINENUMS, "false").equalsIgnoreCase("true");
		debug = configuration.getProperty(OPTION_DEBUG, "false").equalsIgnoreCase("true");
		dumpUnchanged = configuration.getProperty(OPTION_DUMPUNCHANGED, "false").equalsIgnoreCase("true");
		String specifiedSuffix = configuration.getProperty(OPTION_SUFFIX, "pushedin");
		if (specifiedSuffix.length() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append(".").append(specifiedSuffix);
			suffix = sb.toString();
		} else {
			suffix = "";
		}
		if (debug) {
			System.out.println("Configured to create pushin side files:" + configuration);
			System.out.println("dumpUnchanged=" + dumpUnchanged + "\nincludePackageDirs=" + includePackageDirs);
		}

	}

	private String getName(CompilationUnitDeclaration cud) {
		if (cud == null) {
			return "UNKNOWN";
		}
		if (cud.scope == null) {
			return "UNKNOWN";
		}
		if (cud.scope.referenceContext == null) {
			return "UNKNOWN";
		}
		return new String(cud.scope.referenceContext.getFileName());
	}

	/**
	 * @return true if the type is affected by something (itd/declare anno/declare parent)
	 */
	private boolean hasChanged(SourceTypeBinding stb) {
		return newDeclarations.get(stb) != null || additionalParents.get(stb) != null || additionalAnnotations.get(stb) != null;
	}

	/**
	 * Produce the modified source that looks like the itds and declares have been applied.
	 */
	public void dump(CompilationUnitDeclaration compilationUnitDeclaration, String outputFileLocation) {
		if (compilationUnitDeclaration.scope.topLevelTypes == null || compilationUnitDeclaration.scope.topLevelTypes.length == 0) {
			return;
		}
		SourceTypeBinding[] types = compilationUnitDeclaration.scope.topLevelTypes;
		if (types == null || types.length == 0) {
			return;
		}

		// Process all types working from end to start as whatever we do (insert-wise) will affect locations later in the file
		StringBuffer sourceContents = new StringBuffer();
		// put the whole original file in the buffer
		boolean changed = false;
		sourceContents.append(compilationUnitDeclaration.compilationResult.compilationUnit.getContents());
		for (int t = types.length - 1; t >= 0; t--) {
			SourceTypeBinding sourceTypeBinding = compilationUnitDeclaration.scope.topLevelTypes[t];
			if (!hasChanged(sourceTypeBinding)) {
				if (debug) {
					System.out.println(getName(compilationUnitDeclaration) + " has nothing applied");
				}
				continue;
			}
			changed = true;
			int bodyEnd = sourceTypeBinding.scope.referenceContext.bodyEnd; // last '}' of the type
			List<AbstractMethodDeclaration> declarations = newDeclarations.get(sourceTypeBinding);
			if (declarations != null) {
				for (AbstractMethodDeclaration md : declarations) {
					RepresentationAndLocation ral = codeRepresentation.get(md);
					if (ral != null) {
						String s = ral.textualRepresentation;
						sourceContents.insert(bodyEnd, "\n" + s + "\n");
						if (includeLineNumberComments && ral.linenumber != -1) {
							sourceContents.insert(bodyEnd, "\n    // " + ral.linenumber);
						}
					}
				}
			}

			// fix up declare parents - may need to attach them to existing ones
			TypeReference sr = sourceTypeBinding.scope.referenceContext.superclass;
			TypeReference[] trs = sourceTypeBinding.scope.referenceContext.superInterfaces;
			List<ExactTypePattern> newParents = additionalParents.get(sourceTypeBinding);
			StringBuffer extendsString = new StringBuffer();
			StringBuffer implementsString = new StringBuffer();
			if (newParents != null && newParents.size() > 0) {
				for (ExactTypePattern newParent : newParents) {
					ResolvedType newParentType = newParent.getExactType().resolve(world);
					if (newParentType.isInterface()) {
						if (implementsString.length() > 0) {
							implementsString.append(",");
						}
						implementsString.append(newParentType.getName());
					} else {
						extendsString.append(newParentType.getName());
					}
				}
				if (trs == null && sr == null) {
					// nothing after the class declaration, let's insert what we need to
					// Find the position just before the type opening '{'
					int beforeOpeningCurly = sourceTypeBinding.scope.referenceContext.bodyStart - 1;
					if (implementsString.length() != 0) {
						implementsString.insert(0, "implements ");
						implementsString.append(" ");
						sourceContents.insert(beforeOpeningCurly, implementsString);
					}
					if (extendsString.length() != 0) {
						extendsString.insert(0, "extends ");
						extendsString.append(" ");
						sourceContents.insert(beforeOpeningCurly, extendsString);
					}
				}
			}
			List<String> annos = additionalAnnotations.get(sourceTypeBinding);
			if (annos != null && annos.size() > 0) {
				for (String anno : annos) {
					sourceContents.insert(sourceTypeBinding.scope.referenceContext.declarationSourceStart, anno + " ");
				}
			}
		}
		if (changed || (!changed && dumpUnchanged)) {
			try {
				if (debug) {
					System.out.println("Pushed in output file being written to " + outputFileLocation);
					System.out.println(sourceContents);
				}
				FileWriter fos = new FileWriter(new File(outputFileLocation));
				fos.write(sourceContents.toString());
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Encapsulates a text representation (source code) for a member and the line where it was declared.
	 */
	private static class RepresentationAndLocation {

		String textualRepresentation;
		int linenumber;

		public RepresentationAndLocation(String textualRepresentation, int linenumber) {
			this.textualRepresentation = textualRepresentation;
			this.linenumber = linenumber;
		}
	}

	public void recordInterTypeMethodDeclarationCode(AbstractMethodDeclaration md, String s, int line) {
		codeRepresentation.put(md, new RepresentationAndLocation(s, line));
	}

	public void recordInterTypeFieldDeclarationCode(AbstractMethodDeclaration md, String s, int line) {
		codeRepresentation.put(md, new RepresentationAndLocation(s, line));
	}

	public void recordInterTypeConstructorDeclarationCode(AbstractMethodDeclaration md, String s, int line) {
		codeRepresentation.put(md, new RepresentationAndLocation(s, line));
	}

	// public void recordDeclareAnnotationDeclarationCode(AbstractMethodDeclaration md, String value) {
	// codeRepresentation.put(md, new RepresentationAndLocation(value, -1));
	// }

	public void tagAsMunged(SourceTypeBinding sourceType, AbstractMethodDeclaration sourceMethod) {
		if (sourceMethod == null) {
			// seen when an ITD field is made onto an interface. It matches, but the sourceMethod is null.
			// can be null for binary weave (there is no source method)
			return;
		}
		List<AbstractMethodDeclaration> amds = newDeclarations.computeIfAbsent(sourceType, k -> new ArrayList<>());
		amds.add(sourceMethod);
	}

	public void tagAsMunged(SourceTypeBinding sourceType, String annotationString) {
		List<String> annos = additionalAnnotations.computeIfAbsent(sourceType, k -> new ArrayList<>());
		annos.add(annotationString);
	}

	public void dump(CompilationUnitDeclaration unit) {
		String outputFile = getOutputFileFor(unit);
		if (debug) {
			System.out
					.println("Output location is " + outputFile + " for " + new String(unit.scope.referenceContext.getFileName()));
		}
		dump(unit, outputFile);
	}

	private String getOutputFileFor(CompilationUnitDeclaration unit) {
		StringBuffer sb = new StringBuffer();

		// Create the directory portion of the output location
		if (specifiedOutputDirectory != null) {
			sb.append(specifiedOutputDirectory).append(File.separator);
		} else {
			String sss = outputFileNameProvider.getOutputClassFileName("A".toCharArray(), unit.compilationResult);
			sb.append(sss, 0, sss.length() - 7);
		}

		// Create the subdirectory structure matching the package declaration
		if (includePackageDirs) {
			char[][] packageName = unit.compilationResult.packageName;
			if (packageName != null) {
				sb.append(CharOperation.concatWith(unit.compilationResult.packageName, File.separatorChar));
				sb.append(File.separator);
			}
		}

		new File(sb.toString()).mkdirs();

		// Create the filename portion
		String filename = new String(unit.getFileName()); // gives 'n:\A.java'
		int index = filename.lastIndexOf('/');
		int index2 = filename.lastIndexOf('\\');
		if (index > index2) {
			sb.append(filename.substring(index + 1));
		} else if (index2 > index) {
			sb.append(filename.substring(index2 + 1));
		} else {
			sb.append(filename);
		}

		// Add the suffix (may be an empty string)
		sb.append(suffix);
		return sb.toString();
	}

	public void tagAsMunged(SourceTypeBinding sourceType, TypePattern typePattern) {
		if (typePattern instanceof ExactTypePattern) {
			List<ExactTypePattern> annos = additionalParents.computeIfAbsent(sourceType, k -> new ArrayList<>());
			annos.add((ExactTypePattern) typePattern);
		}
	}

	/**
	 * Checks if the aspectj.pushin property is set - this is the main condition for triggering the creation of pushed-in source
	 * files. If not set just to 'true', the value of the property is processed as configuration. Configurable options are:
	 * <ul>
	 * <li>dir=XXXX - to set the output directory for the pushed in files
	 * <li>suffix=XXX - to set the suffix, can be blank to get just '.java'
	 * </ul>
	 */
	public static PushinCollector createInstance(World world) {
		try {
			String property = System.getProperty("aspectj.pushin");
			if (property == null) {
				return null;
			}
			Properties configuration = new Properties();
			StringTokenizer tokenizer = new StringTokenizer(property, ",");
			while (tokenizer.hasMoreElements()) {
				String token = tokenizer.nextToken();
				// Simplest thing to do is turn it on 'aspectj.pushin=true'
				if (token.equalsIgnoreCase("true")) {
					continue;
				}
				int positionOfEquals = token.indexOf("=");
				if (positionOfEquals != -1) {
					// it is an option
					String optionName = token.substring(0, positionOfEquals);
					String optionValue = token.substring(positionOfEquals + 1);
					configuration.put(optionName, optionValue);
				} else {
					// it is a flag
					configuration.put(token, "true");
				}
			}
			return new PushinCollector(world, configuration);
		} catch (Exception e) {
			// unable to read system properties...
		}
		return null;
	}

	public void setOutputFileNameProvider(IOutputClassFileNameProvider outputFileNameProvider) {
		this.outputFileNameProvider = outputFileNameProvider;
	}
}
