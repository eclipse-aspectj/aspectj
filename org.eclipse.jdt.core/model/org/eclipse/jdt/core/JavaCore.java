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
package org.eclipse.jdt.core;

import java.io.File;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jdt.internal.core.Assert;
import org.eclipse.jdt.internal.core.BufferManager;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.Region;
import org.eclipse.jdt.internal.core.SetClasspathOperation;
import org.eclipse.jdt.internal.core.Util;

/**
 * The plug-in runtime class for the Java model plug-in containing the core
 * (UI-free) support for Java projects.
 * <p>
 * Like all plug-in runtime classes (subclasses of <code>Plugin</code>), this
 * class is automatically instantiated by the platform when the plug-in gets
 * activated. Clients must not attempt to instantiate plug-in runtime classes
 * directly.
 * </p>
 * <p>
 * The single instance of this class can be accessed from any plug-in declaring
 * the Java model plug-in as a prerequisite via 
 * <code>JavaCore.getJavaCore()</code>. The Java model plug-in will be activated
 * automatically if not already active.
 * </p>
 */
public final class JavaCore extends Plugin implements IExecutableExtension {

	private static Plugin JAVA_CORE_PLUGIN = null; 
	/**
	 * The plug-in identifier of the Java core support
	 * (value <code>"org.eclipse.jdt.core"</code>).
	 */
	public static final String PLUGIN_ID = "org.eclipse.jdt.core" ; //$NON-NLS-1$

	/**
	 * The identifier for the Java builder
	 * (value <code>"org.eclipse.jdt.core.javabuilder"</code>).
	 */
	public static final String BUILDER_ID = PLUGIN_ID + ".javabuilder" ; //$NON-NLS-1$

	/**
	 * The identifier for the Java model
	 * (value <code>"org.eclipse.jdt.core.javamodel"</code>).
	 */
	public static final String MODEL_ID = PLUGIN_ID + ".javamodel" ; //$NON-NLS-1$

	/**
	 * The identifier for the Java nature
	 * (value <code>"org.eclipse.jdt.core.javanature"</code>).
	 * The presence of this nature on a project indicates that it is 
	 * Java-capable.
	 *
	 * @see org.eclipse.core.resources.IProject#hasNature(java.lang.String)
	 */
	public static final String NATURE_ID = PLUGIN_ID + ".javanature" ; //$NON-NLS-1$

	/**
	 * Name of the handle id attribute in a Java marker
	 */
	protected static final String ATT_HANDLE_ID =
		"org.eclipse.jdt.internal.core.JavaModelManager.handleId" ; //$NON-NLS-1$

	// *************** Possible IDs for configurable options. ********************

	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 */
	public static final String COMPILER_LOCAL_VARIABLE_ATTR = PLUGIN_ID + ".compiler.debug.localVariable"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 */
	public static final String COMPILER_LINE_NUMBER_ATTR = PLUGIN_ID + ".compiler.debug.lineNumber"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 */
	public static final String COMPILER_SOURCE_FILE_ATTR = PLUGIN_ID + ".compiler.debug.sourceFile"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 */
	public static final String COMPILER_CODEGEN_UNUSED_LOCAL = PLUGIN_ID + ".compiler.codegen.unusedLocal"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 */
	public static final String COMPILER_CODEGEN_TARGET_PLATFORM = PLUGIN_ID + ".compiler.codegen.targetPlatform"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 */
	public static final String COMPILER_PB_UNREACHABLE_CODE = PLUGIN_ID + ".compiler.problem.unreachableCode"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 */
	public static final String COMPILER_PB_INVALID_IMPORT = PLUGIN_ID + ".compiler.problem.invalidImport"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 */
	public static final String COMPILER_PB_OVERRIDING_PACKAGE_DEFAULT_METHOD = PLUGIN_ID + ".compiler.problem.overridingPackageDefaultMethod"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 */
	public static final String COMPILER_PB_METHOD_WITH_CONSTRUCTOR_NAME = PLUGIN_ID + ".compiler.problem.methodWithConstructorName"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 */
	public static final String COMPILER_PB_DEPRECATION = PLUGIN_ID + ".compiler.problem.deprecation"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 */
	public static final String COMPILER_PB_HIDDEN_CATCH_BLOCK = PLUGIN_ID + ".compiler.problem.hiddenCatchBlock"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 */
	public static final String COMPILER_PB_UNUSED_LOCAL = PLUGIN_ID + ".compiler.problem.unusedLocal"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 */
	public static final String COMPILER_PB_UNUSED_PARAMETER = PLUGIN_ID + ".compiler.problem.unusedParameter"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String COMPILER_PB_UNUSED_IMPORT = PLUGIN_ID + ".compiler.problem.unusedImport"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 */
	public static final String COMPILER_PB_SYNTHETIC_ACCESS_EMULATION = PLUGIN_ID + ".compiler.problem.syntheticAccessEmulation"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String COMPILER_PB_NON_NLS_STRING_LITERAL = PLUGIN_ID + ".compiler.problem.nonExternalizedStringLiteral"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String COMPILER_PB_ASSERT_IDENTIFIER = PLUGIN_ID + ".compiler.problem.assertIdentifier"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String COMPILER_PB_MAX_PER_UNIT = PLUGIN_ID + ".compiler.maxProblemPerUnit"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String COMPILER_SOURCE = PLUGIN_ID + ".compiler.source"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String COMPILER_COMPLIANCE = PLUGIN_ID + ".compiler.compliance"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 */
	public static final String CORE_JAVA_BUILD_ORDER = PLUGIN_ID + ".computeJavaBuildOrder"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String CORE_JAVA_BUILD_RESOURCE_COPY_FILTER = PLUGIN_ID + ".builder.resourceCopyExclusionFilter"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String CORE_JAVA_BUILD_INVALID_CLASSPATH = PLUGIN_ID + ".builder.invalidClasspath"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String CORE_ENCODING = PLUGIN_ID + ".encoding"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String FORMATTER_NEWLINE_OPENING_BRACE = PLUGIN_ID + ".formatter.newline.openingBrace"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String FORMATTER_NEWLINE_CONTROL = PLUGIN_ID + ".formatter.newline.controlStatement"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String FORMATTER_NEWLINE_ELSE_IF = PLUGIN_ID + ".formatter.newline.elseIf"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String FORMATTER_NEWLINE_EMPTY_BLOCK = PLUGIN_ID + ".formatter.newline.emptyBlock"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String FORMATTER_CLEAR_BLANK_LINES = PLUGIN_ID + ".formatter.newline.clearAll"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String FORMATTER_LINE_SPLIT = PLUGIN_ID + ".formatter.lineSplit"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String FORMATTER_COMPACT_ASSIGNMENT = PLUGIN_ID + ".formatter.style.assignment"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String FORMATTER_TAB_CHAR = PLUGIN_ID + ".formatter.tabulation.char"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String FORMATTER_TAB_SIZE = PLUGIN_ID + ".formatter.tabulation.size"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String CODEASSIST_VISIBILITY_CHECK = PLUGIN_ID + ".codeComplete.visibilityCheck"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String CODEASSIST_IMPLICIT_QUALIFICATION = PLUGIN_ID + ".codeComplete.forceImplicitQualification"; //$NON-NLS-1$

	// *************** Possible values for configurable options. ********************
	
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions
	 */
	public static final String GENERATE = "generate"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions
	 */
	public static final String DO_NOT_GENERATE = "do not generate"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions
	 */
	public static final String PRESERVE = "preserve"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions
	 */
	public static final String OPTIMIZE_OUT = "optimize out"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions
	 */
	public static final String VERSION_1_1 = "1.1"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions
	 */
	public static final String VERSION_1_2 = "1.2"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String VERSION_1_3 = "1.3"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String VERSION_1_4 = "1.4"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String ABORT = "abort"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions
	 */
	public static final String ERROR = "error"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions
	 */
	public static final String WARNING = "warning"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions
	 */
	public static final String IGNORE = "ignore"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions
	 */
	public static final String COMPUTE = "compute"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String INSERT = "insert"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String DO_NOT_INSERT = "do not insert"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String PRESERVE_ONE = "preserve one"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String CLEAR_ALL = "clear all"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String NORMAL = "normal"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String COMPACT = "compact"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String TAB = "tab"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String SPACE = "space"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String ENABLED = "enabled"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions
	 * @since 2.0
	 */
	public static final String DISABLED = "disabled"; //$NON-NLS-1$
	
	/**
	 * Creates the Java core plug-in.
	 */
	public JavaCore(IPluginDescriptor pluginDescriptor) {
		super(pluginDescriptor);
		JAVA_CORE_PLUGIN = this;
	}

	/**
	 * Adds the given listener for changes to Java elements.
	 * Has no effect if an identical listener is already registered.
	 *
	 * This listener will only be notified during the POST_CHANGE resource change notification
	 * and any reconcile operation (POST_RECONCILE).
	 * For finer control of the notification, use <code>addElementChangedListener(IElementChangedListener,int)</code>,
	 * which allows to specify a different eventMask.
	 * 
	 * @see ElementChangedEvent
	 * @param listener the listener
	 */
	public static void addElementChangedListener(IElementChangedListener listener) {
		addElementChangedListener(listener, ElementChangedEvent.POST_CHANGE | ElementChangedEvent.POST_RECONCILE);
	}

	/**
	 * Adds the given listener for changes to Java elements.
	 * Has no effect if an identical listener is already registered.
	 * After completion of this method, the given listener will be registered for exactly
	 * the specified events.  If they were previously registered for other events, they
	 * will be deregistered.  
	 * <p>
	 * Once registered, a listener starts receiving notification of changes to
	 * java elements in the model. The listener continues to receive 
	 * notifications until it is replaced or removed. 
	 * </p>
	 * <p>
	 * Listeners can listen for several types of event as defined in <code>ElementChangeEvent</code>.
	 * Clients are free to register for any number of event types however if they register
	 * for more than one, it is their responsibility to ensure they correctly handle the
	 * case where the same java element change shows up in multiple notifications.  
	 * Clients are guaranteed to receive only the events for which they are registered.
	 * </p>
	 * 
	 * @param listener the listener
	 * @param eventMask the bit-wise OR of all event types of interest to the listener
	 * @see IElementChangedListener
	 * @see ElementChangedEvent
	 * @see #removeElementChangedListener(IElementChangedListener)
	 * @since 2.0
	 */
	public static void addElementChangedListener(IElementChangedListener listener, int eventMask) {
		JavaModelManager.getJavaModelManager().addElementChangedListener(listener, eventMask);
	}

	/**
	 * Configures the given marker attribute map for the given Java element.
	 * Used for markers, which denote a Java element rather than a resource.
	 *
	 * @param attributes the mutable marker attribute map (key type: <code>String</code>,
	 *   value type: <code>String</code>)
	 * @param element the Java element for which the marker needs to be configured
	 */
	public static void addJavaElementMarkerAttributes(
		Map attributes,
		IJavaElement element) {
		if (element instanceof IMember)
			element = ((IMember) element).getClassFile();
		if (attributes != null && element != null)
			attributes.put(ATT_HANDLE_ID, element.getHandleIdentifier());
	}
	
	/**
	 * Configures the given marker for the given Java element.
	 * Used for markers, which denote a Java element rather than a resource.
	 *
	 * @param marker the marker to be configured
	 * @param element the Java element for which the marker needs to be configured
	 * @exception CoreException if the <code>IMarker.setAttribute</code> on the marker fails
	 */
	public void configureJavaElementMarker(IMarker marker, IJavaElement element)
		throws CoreException {
		if (element instanceof IMember)
			element = ((IMember) element).getClassFile();
		if (marker != null && element != null)
			marker.setAttribute(ATT_HANDLE_ID, element.getHandleIdentifier());
	}
	
	/**
	 * Returns the Java model element corresponding to the given handle identifier
	 * generated by <code>IJavaElement.getHandleIdentifier()</code>, or
	 * <code>null</code> if unable to create the associated element.
	 */
	public static IJavaElement create(String handleIdentifier) {
		if (handleIdentifier == null) {
			return null;
		}
		try {
			return JavaModelManager.getJavaModelManager().getHandleFromMemento(handleIdentifier);
		} catch (JavaModelException e) {
			return null;
		}
	}
	/**
	 * Returns the Java element corresponding to the given file, or
	 * <code>null</code> if unable to associate the given file
	 * with a Java element.
	 *
	 * <p>The file must be one of:<ul>
	 *	<li>a <code>.java</code> file - the element returned is the corresponding <code>ICompilationUnit</code></li>
	 *	<li>a <code>.class</code> file - the element returned is the corresponding <code>IClassFile</code></li>
	 *	<li>a <code>.jar</code> file - the element returned is the corresponding <code>IPackageFragmentRoot</code></li>
	 *	</ul>
	 * <p>
	 * Creating a Java element has the side effect of creating and opening all of the
	 * element's parents if they are not yet open.
	 * 
	 * @param the given file
	 * @return the Java element corresponding to the given file, or
	 * <code>null</code> if unable to associate the given file
	 * with a Java element
	 */
	public static IJavaElement create(IFile file) {
		return JavaModelManager.create(file, null);
	}
	/**
	 * Returns the package fragment or package fragment root corresponding to the given folder, or
	 * <code>null</code> if unable to associate the given folder with a Java element.
	 * <p>
	 * Note that a package fragment root is returned rather than a default package.
	 * <p>
	 * Creating a Java element has the side effect of creating and opening all of the
	 * element's parents if they are not yet open.
	 * 
	 * @param the given folder
	 * @return the package fragment or package fragment root corresponding to the given folder, or
	 * <code>null</code> if unable to associate the given folder with a Java element
	 */
	public static IJavaElement create(IFolder folder) {
		return JavaModelManager.create(folder, null);
	}
	/**
	 * Returns the Java project corresponding to the given project.
	 * <p>
	 * Creating a Java Project has the side effect of creating and opening all of the
	 * project's parents if they are not yet open.
	 * <p>
	 * Note that no check is done at this time on the existence or the java nature of this project.
	 * 
	 * @param project the given project
	 * @return the Java project corresponding to the given project, null if the given project is null
	 */
	public static IJavaProject create(IProject project) {
		if (project == null) {
			return null;
		}
		JavaModel javaModel = JavaModelManager.getJavaModelManager().getJavaModel();
		return javaModel.getJavaProject(project);
	}
	/**
	 * Returns the Java element corresponding to the given resource, or
	 * <code>null</code> if unable to associate the given resource
	 * with a Java element.
	 * <p>
	 * The resource must be one of:<ul>
	 *	<li>a project - the element returned is the corresponding <code>IJavaProject</code></li>
	 *	<li>a <code>.java</code> file - the element returned is the corresponding <code>ICompilationUnit</code></li>
	 *	<li>a <code>.class</code> file - the element returned is the corresponding <code>IClassFile</code></li>
	 *	<li>a <code>.jar</code> file - the element returned is the corresponding <code>IPackageFragmentRoot</code></li>
	 *  <li>a folder - the element returned is the corresponding <code>IPackageFragmentRoot</code>
	 *			or <code>IPackageFragment</code></li>
	 *  <li>the workspace root resource - the element returned is the <code>IJavaModel</code></li>
	 *	</ul>
	 * <p>
	 * Creating a Java element has the side effect of creating and opening all of the
	 * element's parents if they are not yet open.
	 * 
	 * @param resource the given resource
	 * @return the Java element corresponding to the given resource, or
	 * <code>null</code> if unable to associate the given resource
	 * with a Java element
	 */
	public static IJavaElement create(IResource resource) {
		return JavaModelManager.create(resource, null);
	}
	/**
	 * Returns the Java model.
	 * 
	 * @param root the given root
	 * @return the Java model, or <code>null</code> if the root is null
	 */
	public static IJavaModel create(IWorkspaceRoot root) {
		if (root == null) {
			return null;
		}
		return JavaModelManager.getJavaModelManager().getJavaModel();
	}
	/**
	 * Creates and returns a class file element for
	 * the given <code>.class</code> file. Returns <code>null</code> if unable
	 * to recognize the class file.
	 * 
	 * @param file the given <code>.class</code> file
	 * @return a class file element for the given <code>.class</code> file, or <code>null</code> if unable
	 * to recognize the class file
	 */
	public static IClassFile createClassFileFrom(IFile file) {
		return JavaModelManager.createClassFileFrom(file, null);
	}
	/**
	 * Creates and returns a compilation unit element for
	 * the given <code>.java</code> file. Returns <code>null</code> if unable
	 * to recognize the compilation unit.
	 * 
	 * @param file the given <code>.java</code> file
	 * @return a compilation unit element for the given <code>.java</code> file, or <code>null</code> if unable
	 * to recognize the compilation unit
	 */
	public static ICompilationUnit createCompilationUnitFrom(IFile file) {
		return JavaModelManager.createCompilationUnitFrom(file, null);
	}
	/**
	 * Creates and returns a handle for the given JAR file.
	 * The Java model associated with the JAR's project may be
	 * created as a side effect. 
	 * 
	 * @param file the given JAR file
	 * @return a handle for the given JAR file, or <code>null</code> if unable to create a JAR package fragment root.
	 * (for example, if the JAR file represents a non-Java resource)
	 */
	public static IPackageFragmentRoot createJarPackageFragmentRootFrom(IFile file) {
		return JavaModelManager.createJarPackageFragmentRootFrom(file, null);
	}

	/** 
	 * Answers the project specific value for a given classpath container.
	 * In case this container path could not be resolved, then will answer <code>null</code>.
	 * Both the container path and the project context are supposed to be non-null.
	 * <p>
	 * The containerPath is a formed by a first ID segment followed with extra segments, which can be 
	 * used as additional hints for resolution. If no container was ever recorded for this container path 
	 * onto this project (using <code>setClasspathContainer</code>, then a 
	 * <code>ClasspathContainerInitializer</code> will be activated if any was registered for this container 
	 * ID onto the extension point "org.eclipse.jdt.core.classpathContainerInitializer".
	 * <p>
	 * There is no assumption that the returned container must answer the exact same containerPath
	 * when requested <code>IClasspathContainer#getPath</code>. 
	 * Indeed, the containerPath is just an indication for resolving it to an actual container object.
	 * <p>
	 * Classpath container values are persisted locally to the workspace, but 
	 * are not preserved from a session to another. It is thus highly recommended to register a 
	 * <code>ClasspathContainerInitializer</code> for each referenced container 
	 * (through the extension point "org.eclipse.jdt.core.ClasspathContainerInitializer").
	 * <p>
	 * @param containerPath the name of the container, which needs to be resolved
	 * @param project a specific project in which the container is being resolved
	 * @return the corresponding classpath container or <code>null</code> if unable to find one.
	 * 
	 * @exception JavaModelException if an exception occurred while resolving the container, or if the resolved container
	 *   contains illegal entries (contains CPE_CONTAINER entries or null entries).	 
	 * 
	 * @see ClasspathContainerInitializer
	 * @see IClasspathContainer
	 * @see #setClasspathContainer(IPath, IJavaProject[], IClasspathContainer[], IProgressMonitor)
	 * @since 2.0
	 */
	public static IClasspathContainer getClasspathContainer(final IPath containerPath, final IJavaProject project) throws JavaModelException {

		Map projectContainers = (Map)JavaModelManager.Containers.get(project);
		if (projectContainers == null){
			projectContainers = new HashMap(1);
			JavaModelManager.Containers.put(project, projectContainers);
		}
		IClasspathContainer container = (IClasspathContainer)projectContainers.get(containerPath);

		if (container == JavaModelManager.ContainerInitializationInProgress) return null; // break cycle
		if (container == null){
			final ClasspathContainerInitializer initializer = JavaModelManager.getClasspathContainerInitializer(containerPath.segment(0));
			if (initializer != null){
				projectContainers.put(containerPath, JavaModelManager.ContainerInitializationInProgress); // avoid initialization cycles
				boolean ok = false;
				try {
					// wrap initializer call with Safe runnable in case initializer would be causing some grief
					Platform.run(new ISafeRunnable() {
						public void handleException(Throwable exception) {
							Util.log(exception, "Exception occurred in classpath container initializer: "+initializer); //$NON-NLS-1$
						}
						public void run() throws Exception {
							initializer.initialize(containerPath, project);
						}
					});
					
					// retrieve value (if initialization was successful)
					container = (IClasspathContainer)projectContainers.get(containerPath);
					if (container == JavaModelManager.ContainerInitializationInProgress) return null; // break cycle
					ok = true;
				} finally {
					if (!ok) JavaModelManager.Containers.put(project, null); // flush cache
				}
				if (container != null){
					projectContainers.put(containerPath, container);
				}
				if (JavaModelManager.CP_RESOLVE_VERBOSE){
					System.out.print("CPContainer INIT - after resolution: " + containerPath + " --> "); //$NON-NLS-2$//$NON-NLS-1$
					if (container != null){
						System.out.print("container: "+container.getDescription()+" {"); //$NON-NLS-2$//$NON-NLS-1$
						IClasspathEntry[] entries = container.getClasspathEntries();
						if (entries != null){
							for (int i = 0; i < entries.length; i++){
								if (i > 0) System.out.println(", ");//$NON-NLS-1$
								System.out.println(entries[i]);
							}
						}
						System.out.println("}");//$NON-NLS-1$
					} else {
						System.out.println("{unbound}");//$NON-NLS-1$
					}
				}
			}
		}
		return container;			
	}

	/**
	 * Returns the path held in the given classpath variable.
	 * Returns <node>null</code> if unable to bind.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and 
	 * are preserved from session to session.
	 * <p>
	 * Note that classpath variables can be contributed registered initializers for,
	 * using the extension point "org.eclipse.jdt.core.classpathVariableInitializer".
	 *
	 * @param variableName the name of the classpath variable
	 * @return the path, or <code>null</code> if none 
	 * @see #setClasspathVariable
	 */
	public static IPath getClasspathVariable(final String variableName) {

		IPath variablePath = (IPath) JavaModelManager.variableGet(variableName);
		if (variablePath == JavaModelManager.VariableInitializationInProgress) return null; // break cycle
		
		if (variablePath == null){
			final ClasspathVariableInitializer initializer = getClasspathVariableInitializer(variableName);
			if (initializer != null){
				JavaModelManager.variablePut(variableName, JavaModelManager.VariableInitializationInProgress); // avoid initialization cycles
				// wrap initializer call with Safe runnable in case initializer would be causing some grief
				Platform.run(new ISafeRunnable() {
					public void handleException(Throwable exception) {
						Util.log(exception, "Exception occurred in classpath variable initializer: "+initializer); //$NON-NLS-1$
					}
					public void run() throws Exception {
						initializer.initialize(variableName);
					}
				});
				variablePath = (IPath) JavaModelManager.variableGet(variableName); // retry
				if (variablePath == JavaModelManager.VariableInitializationInProgress) return null; // break cycle
				if (JavaModelManager.CP_RESOLVE_VERBOSE){
					System.out.println("CPVariable INIT - after initialization: " + variableName + " --> " + variablePath); //$NON-NLS-2$//$NON-NLS-1$
				}
			}
		}
		return variablePath;
	}

	/**
 	 * Retrieve the client classpath variable initializer registered for a given variable if any
 	 * 
 	 * @param the given variable
 	 * @return the client classpath variable initializer registered for a given variable, <code>null</code> if none
 	 */
	private static ClasspathVariableInitializer getClasspathVariableInitializer(String variable){
		
		Plugin jdtCorePlugin = JavaCore.getPlugin();
		if (jdtCorePlugin == null) return null;

		IExtensionPoint extension = jdtCorePlugin.getDescriptor().getExtensionPoint(JavaModelManager.CPVARIABLE_INITIALIZER_EXTPOINT_ID);
		if (extension != null) {
			IExtension[] extensions =  extension.getExtensions();
			for(int i = 0; i < extensions.length; i++){
				IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
					IPluginDescriptor plugin = extension.getDeclaringPluginDescriptor();
					if (plugin.isPluginActivated()) {
						for(int j = 0; j < configElements.length; j++){
							try {
								String varAttribute = configElements[j].getAttribute("variable"); //$NON-NLS-1$
								if (variable.equals(varAttribute)) {
									if (JavaModelManager.CP_RESOLVE_VERBOSE) {
										System.out.println("CPVariable INIT - found initializer: "+variable+" --> " + configElements[j].getAttribute("class"));//$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$
									}						
									Object execExt = configElements[j].createExecutableExtension("class"); //$NON-NLS-1$
									if (execExt instanceof ClasspathVariableInitializer){
										return (ClasspathVariableInitializer)execExt;
									}
								}
							} catch(CoreException e){
							}
						}
					}
			}	
		}
		return null;
	}	
	
	/**
	 * Returns the names of all known classpath variables.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and 
	 * are preserved from session to session.
	 * <p>
	 *
	 * @return the list of classpath variable names
	 * @see #setClasspathVariable
	 */
	public static String[] getClasspathVariableNames() {
		return JavaModelManager.variableNames();
	}

	/**
	 * Returns a table of all known configurable options with their default values.
	 * These options allow to configure the behaviour of the underlying components.
	 * The client may safely use the result as a template that they can modify and
	 * then pass to <code>setOptions</code>.
	 * 
	 * Helper constants have been defined on JavaCore for each of the option ID and 
	 * their possible constant values.
	 * 
	 * Note: more options might be added in further releases.
	 * <pre>
	 * RECOGNIZED OPTIONS:
	 *	COMPILER / Generating Local Variable Debug Attribute
 	 *		When generated, this attribute will enable local variable names 
	 *		to be displayed in debugger, only in place where variables are 
	 *		definitely assigned (.class file is then bigger)
	 *     		- option id:		"org.eclipse.jdt.core.compiler.debug.localVariable"
	 *     		- possible values:	{ "generate", "do not generate" }
	 *     		- default:			"generate"
	 *
	 *	COMPILER / Generating Line Number Debug Attribute 
	 *		When generated, this attribute will enable source code highlighting in debugger 
	 *		(.class file is then bigger).
	 *     		- option id:		"org.eclipse.jdt.core.compiler.debug.lineNumber"
	 *     		- possible values:	{ "generate", "do not generate" }
	 *     		- default:			"generate"
	 *		
	 *	COMPILER / Generating Source Debug Attribute 
	 *		When generated, this attribute will enable the debugger to present the 
	 *		corresponding source code.
	 *     		- option id:		"org.eclipse.jdt.core.compiler.debug.sourceFile"
	 *     		- possible values:	{ "generate", "do not generate" }
	 *     		- default:			"generate"
	 *		
	 *	COMPILER / Preserving Unused Local Variables
	 *		Unless requested to preserve unused local variables (i.e. never read), the 
	 *		compiler will optimize them out, potentially altering debugging
	 *     		- option id:		"org.eclipse.jdt.core.compiler.codegen.unusedLocal"
	 *     		- possible values:	{ "preserve", "optimize out" }
	 *     		- default:			"preserve"
	 * 
	 *	COMPILER / Defining Target Java Platform
	 *		For binary compatibility reason, .class files can be tagged to with certain VM versions and later.
	 *		Note that "1.4" target require to toggle compliance mode to "1.4" too.
	 *     		- option id:		"org.eclipse.jdt.core.compiler.codegen.targetPlatform"
	 *     		- possible values:	{ "1.1", "1.2", "1.3", "1.4" }
	 *     		- default:			"1.1"
	 *
	 *	COMPILER / Reporting Unreachable Code
	 *		Unreachable code can optionally be reported as an error, warning or simply 
	 *		ignored. The bytecode generation will always optimized it out.
	 *     		- option id:		"org.eclipse.jdt.core.compiler.problem.unreachableCode"
	 *     		- possible values:	{ "error", "warning", "ignore" }
	 *     		- default:			"error"
	 *
	 *	COMPILER / Reporting Invalid Import
	 *		An import statement that cannot be resolved might optionally be reported 
	 *		as an error, as a warning or ignored.
	 *     		- option id:		"org.eclipse.jdt.core.compiler.problem.invalidImport"
	 *     		- possible values:	{ "error", "warning", "ignore" }
	 *     		- default:			"error"
	 *
	 *	COMPILER / Reporting Attempt to Override Package-Default Method
	 *		A package default method is not visible in a different package, and thus 
	 *		cannot be overridden. When enabling this option, the compiler will signal 
	 *		such scenarii either as an error or a warning.
	 *     		- option id:		"org.eclipse.jdt.core.compiler.problem.overridingPackageDefaultMethod"
	 *     		- possible values:	{ "error", "warning", "ignore" }
	 *     		- default:			"warning"
	 *
	 *	COMPILER / Reporting Method With Constructor Name
	 *		Naming a method with a constructor name is generally considered poor 
	 *		style programming. When enabling this option, the compiler will signal such 
	 *		scenarii either as an error or a warning.
	 *     		- option id:		"org.eclipse.jdt.core.compiler.problem.methodWithConstructorName"
	 *     		- possible values:	{ "error", "warning", "ignore" }
	 *     		- default:			"warning"
	 *
	 *	COMPILER / Reporting Deprecation
	 *		When enabled, the compiler will signal use of deprecated API either as an 
	 *		error or a warning.
	 *     		- option id:		"org.eclipse.jdt.core.compiler.problem.deprecation"
	 *     		- possible values:	{ "error", "warning", "ignore" }
	 *     		- default:			"warning"
	 *
	 *	COMPILER / Reporting Hidden Catch Block
	 *		Locally to a try statement, some catch blocks may hide others , e.g.
	 *			try {	throw new java.io.CharConversionException();
	 *			} catch (java.io.CharConversionException e) {
	 *  	    } catch (java.io.IOException e) {}. 
	 *		When enabling this option, the compiler will issue an error or a warning for hidden 
	 *		catch blocks corresponding to checked exceptions
	 *     		- option id:		"org.eclipse.jdt.core.compiler.problem.hiddenCatchBlock"
	 *     		- possible values:	{ "error", "warning", "ignore" }
	 *     		- default:			"warning"
	 *
	 *	COMPILER / Reporting Unused Local
	 *		When enabled, the compiler will issue an error or a warning for unused local 
	 *		variables (i.e. variables never read from)
	 *     		- option id:		"org.eclipse.jdt.core.compiler.problem.unusedLocal"
	 *     		- possible values:	{ "error", "warning", "ignore" }
	 *     		- default:			"ignore"
	 *
	 *	COMPILER / Reporting Unused Parameter
	 *		When enabled, the compiler will issue an error or a warning for unused method 
	 *		parameters (i.e. parameters never read from)
	 *     		- option id:		"org.eclipse.jdt.core.compiler.problem.unusedParameter"
	 *     		- possible values:	{ "error", "warning", "ignore" }
	 *     		- default:			"ignore"
	 *
	 *	COMPILER / Reporting Unused Import
	 *		When enabled, the compiler will issue an error or a warning for unused import 
	 *		reference 
	 *     		- option id:		"org.eclipse.jdt.core.compiler.problem.unusedImport"
	 *     		- possible values:	{ "error", "warning", "ignore" }
	 *     		- default:			"ignore"
	 *
	 *	COMPILER / Reporting Synthetic Access Emulation
	 *		When enabled, the compiler will issue an error or a warning whenever it emulates 
	 *		access to a non-accessible member of an enclosing type. Such access can have
	 *		performance implications.
	 *     		- option id:		"org.eclipse.jdt.core.compiler.problem.syntheticAccessEmulation"
	 *     		- possible values:	{ "error", "warning", "ignore" }
	 *     		- default:			"ignore"
	 *
	 *	COMPILER / Reporting Non-Externalized String Literal
	 *		When enabled, the compiler will issue an error or a warning for non externalized 
	 *		String literal (i.e. non tagged with //$NON-NLS-<n>$). 
	 *     		- option id:		"org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral"
	 *     		- possible values:	{ "error", "warning", "ignore" }
	 *     		- default:			"ignore"
	 * 
	 * COMPILER / Reporting Usage of 'assert' Identifier
	 *    When enabled, the compiler will issue an error or a warning whenever 'assert' is 
	 *    used as an identifier (reserved keyword in 1.4)
	 *     - option id:			"org.eclipse.jdt.core.compiler.problem.assertIdentifier"
	 *     - possible values:	{ "error", "warning", "ignore" }
	 *     - default:			"ignore"
	 * 
	 * COMPILER / Setting Source Compatibility Mode
	 *    Specify whether source is 1.3 or 1.4 compatible. From 1.4 on, 'assert' is a keyword
	 *    reserved for assertion support. Also note, than when toggling to 1.4 mode, the target VM
	 *   level should be set to "1.4" and the compliance mode should be "1.4".
	 *     - option id:			"org.eclipse.jdt.core.compiler.source"
	 *     - possible values:	{ "1.3", "1.4" }
	 *     - default:			"1.3"
	 * 
	 * COMPILER / Setting Compliance Level
	 *    Select the compliance level for the compiler. In "1.3" mode, source and target settings
	 *    should not go beyond "1.3" level.
	 *     - option id:			"org.eclipse.jdt.core.compiler.compliance"
	 *     - possible values:	{ "1.3", "1.4" }
	 *     - default:			"1.3"
	 * 
	 * COMPILER / Maximum number of problems reported per compilation unit
	 *    Specify the maximum number of problems reported on each compilation unit.
	 *     - option id:			"org.eclipse.jdt.core.compiler.maxProblemPerUnit"
	 *     - possible values:	"<n>" where <n> is zero or a positive integer (if zero then all problems are reported).
	 *     - default:			"100"
	 * 
	 * BUILDER / Specifying Filters for Resource Copying Control
	 *    Allow to specify some filters to control the resource copy process.
	 *     - option id:			"org.eclipse.jdt.core.builder.resourceCopyExclusionFilter"
	 *     - possible values:	{ "<name>[,<name>]* } where <name> is a file name pattern (only * wild-cards allowed)
	 *       or the name of a folder which ends with '/'
	 *     - default:			""
	 * 
	 * BUILDER / Abort if Invalid Classpath
	 *    Allow to toggle the builder to abort if the classpath is invalid
	 *     - option id:			"org.eclipse.jdt.core.builder.invalidClasspath"
	 *     - possible values:	{ "abort", "ignore" }
	 *     - default:			"ignore"
	 * 
	 *	JAVACORE / Computing Project Build Order
	 *    Indicate whether JavaCore should enforce the project build order to be based on
	 *    the classpath prerequisite chain. When requesting to compute, this takes over
	 *    the platform default order (based on project references).
	 *     - option id:			"org.eclipse.jdt.core.computeJavaBuildOrder"
	 *     - possible values:	{ "compute", "ignore" }
	 *     - default:			"ignore"	 
	 * 
	 * JAVACORE / Specify Default Source Encoding Format
	 *    Get the encoding format for compiled sources. This setting is read-only, it is equivalent
	 *    to 'ResourcesPlugin.getEncoding()'.
	 *     - option id:			"org.eclipse.jdt.core.encoding"
	 *     - possible values:	{ any of the supported encoding name}.
	 *     - default:			<platform default>
	 * 
	 *	FORMATTER / Inserting New Line Before Opening Brace
	 *    When Insert, a new line is inserted before an opening brace, otherwise nothing
	 *    is inserted
	 *     - option id:			"org.eclipse.jdt.core.formatter.newline.openingBrace"
	 *     - possible values:	{ "insert", "do not insert" }
	 *     - default:			"do not insert"
	 * 
	 *	FORMATTER / Inserting New Line Inside Control Statement
	 *    When Insert, a new line is inserted between } and following else, catch, finally
	 *     - option id:			"org.eclipse.jdt.core.formatter.newline.controlStatement"
	 *     - possible values:	{ "insert", "do not insert" }
	 *     - default:			"do not insert"
	 * 
	 *	FORMATTER / Clearing Blank Lines
	 *    When Clear all, all blank lines are removed. When Preserve one, only one is kept
	 *    and all others removed.
	 *     - option id:			"org.eclipse.jdt.core.formatter.newline.clearAll"
	 *     - possible values:	{ "clear all", "preserve one" }
	 *     - default:			"preserve one"
	 * 
	 *	FORMATTER / Inserting New Line Between Else/If 
	 *    When Insert, a blank line is inserted between an else and an if when they are 
	 *    contiguous. When choosing to not insert, else-if will be kept on the same
	 *    line when possible.
	 *     - option id:			"org.eclipse.jdt.core.formatter.newline.elseIf"
	 *     - possible values:	{ "insert", "do not insert" }
	 *     - default:			"do not insert"
	 * 
	 *	FORMATTER / Inserting New Line In Empty Block
	 *    When insert, a line break is inserted between contiguous { and }, if } is not followed
	 *    by a keyword.
	 *     - option id:			"org.eclipse.jdt.core.formatter.newline.emptyBlock"
	 *     - possible values:	{ "insert", "do not insert" }
	 *     - default:			"insert"
	 * 
	 *	FORMATTER / Splitting Lines Exceeding Length
	 *    Enable splitting of long lines (exceeding the configurable length). Length of 0 will
	 *    disable line splitting
	 *     - option id:			"org.eclipse.jdt.core.formatter.lineSplit"
	 *     - possible values:	"<n>", where n is zero or a positive integer
	 *     - default:			"80"
	 * 
	 *	FORMATTER / Compacting Assignment
	 *    Assignments can be formatted asymmetrically, e.g. 'int x= 2;', when Normal, a space
	 *    is inserted before the assignment operator
	 *     - option id:			"org.eclipse.jdt.core.formatter.style.assignment"
	 *     - possible values:	{ "compact", "normal" }
	 *     - default:			"normal"
	 * 
	 *	FORMATTER / Defining Indentation Character
	 *    Either choose to indent with tab characters or spaces
	 *     - option id:			"org.eclipse.jdt.core.formatter.tabulation.char"
	 *     - possible values:	{ "tab", "space" }
	 *     - default:			"tab"
	 * 
	 *	FORMATTER / Defining Space Indentation Length
	 *    When using spaces, set the amount of space characters to use for each 
	 *    indentation mark.
	 *     - option id:			"org.eclipse.jdt.core.formatter.tabulation.size"
	 *     - possible values:	"<n>", where n is a positive integer
	 *     - default:			"4"
	 * 
	 *	CODEASSIST / Activate Visibility Sensitive Completion
	 *    When active, completion doesn't show that you can not see
	 *    (e.g. you can not see private methods of a super class).
	 *     - option id:			"org.eclipse.jdt.core.codeComplete.visibilityCheck"
	 *     - possible values:	{ "enabled", "disabled" }
	 *     - default:			"disabled"
	 * 
	 *	CODEASSIST / Automatic Qualification of Implicit Members
	 *    When active, completion automatically qualifies completion on implicit
	 *    field references and message expressions.
	 *     - option id:			"org.eclipse.jdt.core.codeComplete.forceImplicitQualification"
	 *     - possible values:	{ "enabled", "disabled" }
	 *     - default:			"disabled"
	 * </pre>
	 * 
	 * @return a mutable table containing the default settings of all known options
	 *   (key type: <code>String</code>; value type: <code>String</code>)
	 * @see #setOptions
	 */
 	public static Hashtable getDefaultOptions(){
	
		Hashtable defaultOptions = new Hashtable(10);

		// see #initializeDefaultPluginPreferences() for changing default settings
		Preferences preferences = getPlugin().getPluginPreferences();
		HashSet optionNames = JavaModelManager.getJavaModelManager().OptionNames;
		
		// get preferences set to their default
		String[] defaultPropertyNames = preferences.defaultPropertyNames();
		for (int i = 0; i < defaultPropertyNames.length; i++){
			String propertyName = defaultPropertyNames[i];
			if (optionNames.contains(propertyName)) {
				defaultOptions.put(propertyName, preferences.getDefaultString(propertyName));
			}
		}		
		// get preferences not set to their default
		String[] propertyNames = preferences.propertyNames();
		for (int i = 0; i < propertyNames.length; i++){
			String propertyName = propertyNames[i];
			if (optionNames.contains(propertyName)) {
				defaultOptions.put(propertyName, preferences.getDefaultString(propertyName));
			}
		}		
		// get encoding through resource plugin
		defaultOptions.put(CORE_ENCODING, ResourcesPlugin.getEncoding()); 
		
		return defaultOptions;
	}

	/**
	 * Returns the single instance of the Java core plug-in runtime class.
	 * Equivalent to <code>(JavaCore) getPlugin()</code>.
	 * 
	 * @return the single instance of the Java core plug-in runtime class
	 */
	public static JavaCore getJavaCore() {
		return (JavaCore) getPlugin();
	}
	/**
	 * Returns the <code>IJavaProject</code> associated with the
	 * given <code>IProject</code>, or <code>null</code> if the
	 * project does not have a Java nature.
	 * 
	 * @param the given <code>IProject</code>
	 * @return the <code>IJavaProject</code> associated with the
	 * given <code>IProject</code>, or <code>null</code> if the
	 * project does not have a Java nature
	 */
	private IJavaProject getJavaProject(IProject project) {
		try {
			if (project.hasNature(NATURE_ID)) {
				JavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();
				if (model != null) {
					return model.getJavaProject(project);
				}
			}
		} catch (CoreException e) {
		}
		return null;
	}
	
	/**
	 * Helper method for returning one option value only. Equivalent to <code>(String)JavaCore.getOptions().get(optionName)</code>
	 * Note that it may answer <code>null</code> if this option does not exist.
	 * <p>
	 * For a complete description of the configurable options, see <code>getDefaultOptions</code>.
	 * </p>
	 * 
	 * @param optionName the name of an option
	 * @return the String value of a given option
	 * @see JavaCore#getDefaultOptions
	 * @since 2.0
	 */
	public static String getOption(String optionName) {
		
		if (CORE_ENCODING.equals(optionName)){
			return ResourcesPlugin.getEncoding();
		}
		if (JavaModelManager.getJavaModelManager().OptionNames.contains(optionName)){
			Preferences preferences = getPlugin().getPluginPreferences();
			return preferences.getString(optionName);
		}
		return null;
	}
	
	/**
	 * Returns the table of the current options. Initially, all options have their default values,
	 * and this method returns a table that includes all known options.
	 * <p>
	 * For a complete description of the configurable options, see <code>getDefaultOptions</code>.
	 * </p>
	 * 
	 * @return table of current settings of all options 
	 *   (key type: <code>String</code>; value type: <code>String</code>)
	 * @see JavaCore#getDefaultOptions
	 */
	public static Hashtable getOptions() {
		
		Hashtable options = new Hashtable(10);

		// see #initializeDefaultPluginPreferences() for changing default settings
		Preferences preferences = getPlugin().getPluginPreferences();
		HashSet optionNames = JavaModelManager.getJavaModelManager().OptionNames;
		
		// get preferences set to their default
		String[] defaultPropertyNames = preferences.defaultPropertyNames();
		for (int i = 0; i < defaultPropertyNames.length; i++){
			String propertyName = defaultPropertyNames[i];
			if (optionNames.contains(propertyName)){
				options.put(propertyName, preferences.getString(propertyName));
			}
		}		
		// get preferences not set to their default
		String[] propertyNames = preferences.propertyNames();
		for (int i = 0; i < propertyNames.length; i++){
			String propertyName = propertyNames[i];
			if (optionNames.contains(propertyName)){
				options.put(propertyName, preferences.getString(propertyName));
			}
		}		
		// get encoding through resource plugin
		options.put(CORE_ENCODING, ResourcesPlugin.getEncoding());

		return options;
	}
		
	/**
	 * Returns the single instance of the Java core plug-in runtime class.
	 * 
	 * @return the single instance of the Java core plug-in runtime class
	 */
	public static Plugin getPlugin() {
		return JAVA_CORE_PLUGIN;
	}

	/**
	 * This is a helper method, which returns the resolved classpath entry denoted 
	 * by a given entry (if it is a variable entry). It is obtained by resolving the variable 
	 * reference in the first segment. Returns <node>null</code> if unable to resolve using 
	 * the following algorithm:
	 * <ul>
	 * <li> if variable segment cannot be resolved, returns <code>null</code></li>
	 * <li> finds a project, JAR or binary folder in the workspace at the resolved path location</li>
	 * <li> if none finds an external JAR file or folder outside the workspace at the resolved path location </li>
	 * <li> if none returns <code>null</code></li>
	 * </ul>
	 * <p>
	 * Variable source attachment path and root path are also resolved and recorded in the resulting classpath entry.
	 * <p>
	 * NOTE: This helper method does not handle classpath containers, for which should rather be used
	 * <code>JavaCore#getResolvedClasspathContainer(IPath, IJavaProject)</code>.
	 * <p>
	 * 
	 * @param entry the given variable entry
	 * @return the resolved library or project classpath entry, or <code>null</code>
	 *   if the given variable entry could not be resolved to a valid classpath entry
	 */
	public static IClasspathEntry getResolvedClasspathEntry(IClasspathEntry entry) {

		if (entry.getEntryKind() != IClasspathEntry.CPE_VARIABLE)
			return entry;

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IPath resolvedPath = JavaCore.getResolvedVariablePath(entry.getPath());
		if (resolvedPath == null)
			return null;

		Object target = JavaModel.getTarget(workspaceRoot, resolvedPath, false);
		if (target == null)
			return null;

		// inside the workspace
		if (target instanceof IResource) {
			IResource resolvedResource = (IResource) target;
			if (resolvedResource != null) {
				switch (resolvedResource.getType()) {
					
					case IResource.PROJECT :  
						// internal project
						return JavaCore.newProjectEntry(resolvedPath, entry.isExported());
						
					case IResource.FILE : 
						String extension = resolvedResource.getFileExtension();
						if ("jar".equalsIgnoreCase(extension)  //$NON-NLS-1$
							 || "zip".equalsIgnoreCase(extension)) {  //$NON-NLS-1$
							// internal binary archive
							return JavaCore.newLibraryEntry(
									resolvedPath,
									getResolvedVariablePath(entry.getSourceAttachmentPath()),
									getResolvedVariablePath(entry.getSourceAttachmentRootPath()),
									entry.isExported());
						}
						break;
						
					case IResource.FOLDER : 
						// internal binary folder
						return JavaCore.newLibraryEntry(
								resolvedPath,
								getResolvedVariablePath(entry.getSourceAttachmentPath()),
								getResolvedVariablePath(entry.getSourceAttachmentRootPath()),
								entry.isExported());
				}
			}
		}
		// outside the workspace
		if (target instanceof File) {
			File externalFile = (File) target;
			if (externalFile.isFile()) {
				String fileName = externalFile.getName().toLowerCase();
				if (fileName.endsWith(".jar"  //$NON-NLS-1$
					) || fileName.endsWith(".zip"  //$NON-NLS-1$
					)) { // external binary archive
					return JavaCore.newLibraryEntry(
							resolvedPath,
							getResolvedVariablePath(entry.getSourceAttachmentPath()),
							getResolvedVariablePath(entry.getSourceAttachmentRootPath()),
							entry.isExported());
				}
			} else { // external binary folder
				if (resolvedPath.isAbsolute()){
					return JavaCore.newLibraryEntry(
							resolvedPath,
							getResolvedVariablePath(entry.getSourceAttachmentPath()),
							getResolvedVariablePath(entry.getSourceAttachmentRootPath()),
							entry.isExported());
				}
			}
		}
		return null;
	}


	/**
	 * Resolve a variable path (helper method)
	 * 
	 * @param variablePath the given variable path
	 * @return the resolved variable path or <code>null</code> if none
	 */
	public static IPath getResolvedVariablePath(IPath variablePath) {

		if (variablePath == null)
			return null;
		int count = variablePath.segmentCount();
		if (count == 0)
			return null;

		// lookup variable	
		String variableName = variablePath.segment(0);
		IPath resolvedPath = JavaCore.getClasspathVariable(variableName);
		if (resolvedPath == null)
			return null;

		// append path suffix
		if (count > 1) {
			resolvedPath = resolvedPath.append(variablePath.removeFirstSegments(1));
		}
		return resolvedPath; 
	}

	/**
	 * Answers the shared working copies currently registered for this buffer factory. 
	 * Working copies can be shared by several clients using the same buffer factory,see 
	 * <code>IWorkingCopy##getSharedWorkingCopy</code>.
	 * 
	 * @param factory the given buffer factory
	 * @return the list of shared working copies for a given buffer factory
	 * @see IWorkingCopy
	 * @since 2.0
	 */
	public static IWorkingCopy[] getSharedWorkingCopies(IBufferFactory factory){
		
		// if factory is null, default factory must be used
		if (factory == null) factory = BufferManager.getDefaultBufferManager().getDefaultBufferFactory();
		Map sharedWorkingCopies = JavaModelManager.getJavaModelManager().sharedWorkingCopies;
		
		Map perFactoryWorkingCopies = (Map) sharedWorkingCopies.get(factory);
		if (perFactoryWorkingCopies == null) return JavaModelManager.NoWorkingCopy;
		Collection copies = perFactoryWorkingCopies.values();
		IWorkingCopy[] result = new IWorkingCopy[copies.size()];
		copies.toArray(result);
		return result;
	}
	
	/**
	 * Initializes the default preferences settings for this plug-in.
	 */
	protected void initializeDefaultPluginPreferences() {
		
		Preferences preferences = getPluginPreferences();
		HashSet optionNames = JavaModelManager.getJavaModelManager().OptionNames;
		
		// Compiler settings
		preferences.setDefault(COMPILER_LOCAL_VARIABLE_ATTR, GENERATE);
		optionNames.add(COMPILER_LOCAL_VARIABLE_ATTR);

		preferences.setDefault(COMPILER_LINE_NUMBER_ATTR, GENERATE); 
		optionNames.add(COMPILER_LINE_NUMBER_ATTR);

		preferences.setDefault(COMPILER_SOURCE_FILE_ATTR, GENERATE); 
		optionNames.add(COMPILER_SOURCE_FILE_ATTR);

		preferences.setDefault(COMPILER_CODEGEN_UNUSED_LOCAL, PRESERVE); 
		optionNames.add(COMPILER_CODEGEN_UNUSED_LOCAL);

		preferences.setDefault(COMPILER_CODEGEN_TARGET_PLATFORM, VERSION_1_1); 
		optionNames.add(COMPILER_CODEGEN_TARGET_PLATFORM);

		preferences.setDefault(COMPILER_PB_UNREACHABLE_CODE, ERROR); 
		optionNames.add(COMPILER_PB_UNREACHABLE_CODE);

		preferences.setDefault(COMPILER_PB_INVALID_IMPORT, ERROR); 
		optionNames.add(COMPILER_PB_INVALID_IMPORT);

		preferences.setDefault(COMPILER_PB_OVERRIDING_PACKAGE_DEFAULT_METHOD, WARNING); 
		optionNames.add(COMPILER_PB_OVERRIDING_PACKAGE_DEFAULT_METHOD);

		preferences.setDefault(COMPILER_PB_METHOD_WITH_CONSTRUCTOR_NAME, WARNING); 
		optionNames.add(COMPILER_PB_METHOD_WITH_CONSTRUCTOR_NAME);

		preferences.setDefault(COMPILER_PB_DEPRECATION, WARNING);
		optionNames.add(COMPILER_PB_DEPRECATION);

		preferences.setDefault(COMPILER_PB_HIDDEN_CATCH_BLOCK, WARNING); 
		optionNames.add(COMPILER_PB_HIDDEN_CATCH_BLOCK);

		preferences.setDefault(COMPILER_PB_UNUSED_LOCAL, IGNORE); 
		optionNames.add(COMPILER_PB_UNUSED_LOCAL);

		preferences.setDefault(COMPILER_PB_UNUSED_PARAMETER, IGNORE); 
		optionNames.add(COMPILER_PB_UNUSED_PARAMETER);

		preferences.setDefault(COMPILER_PB_UNUSED_IMPORT, IGNORE); 
		optionNames.add(COMPILER_PB_UNUSED_IMPORT);

		preferences.setDefault(COMPILER_PB_SYNTHETIC_ACCESS_EMULATION, IGNORE); 
		optionNames.add(COMPILER_PB_SYNTHETIC_ACCESS_EMULATION);

		preferences.setDefault(COMPILER_PB_NON_NLS_STRING_LITERAL, IGNORE); 
		optionNames.add(COMPILER_PB_NON_NLS_STRING_LITERAL);

		preferences.setDefault(COMPILER_PB_ASSERT_IDENTIFIER, IGNORE); 
		optionNames.add(COMPILER_PB_ASSERT_IDENTIFIER);

		preferences.setDefault(COMPILER_SOURCE, VERSION_1_3);
		optionNames.add(COMPILER_SOURCE);

		preferences.setDefault(COMPILER_COMPLIANCE, VERSION_1_3); 
		optionNames.add(COMPILER_COMPLIANCE);

		preferences.setDefault(COMPILER_PB_MAX_PER_UNIT, "100"); //$NON-NLS-1$
		optionNames.add(COMPILER_PB_MAX_PER_UNIT);
		
		// Builder settings
		preferences.setDefault(CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, ""); //$NON-NLS-1$
		optionNames.add(CORE_JAVA_BUILD_RESOURCE_COPY_FILTER);

		preferences.setDefault(CORE_JAVA_BUILD_INVALID_CLASSPATH, ABORT); 
		optionNames.add(CORE_JAVA_BUILD_INVALID_CLASSPATH);
		
		// JavaCore settings
		preferences.setDefault(CORE_JAVA_BUILD_ORDER, IGNORE); //$NON-NLS-1$
		optionNames.add(CORE_JAVA_BUILD_ORDER);
	
		// Formatter settings
		preferences.setDefault(FORMATTER_NEWLINE_OPENING_BRACE, DO_NOT_INSERT); 
		optionNames.add(FORMATTER_NEWLINE_OPENING_BRACE);

		preferences.setDefault(FORMATTER_NEWLINE_CONTROL, DO_NOT_INSERT);
		optionNames.add(FORMATTER_NEWLINE_CONTROL);

		preferences.setDefault(FORMATTER_CLEAR_BLANK_LINES, PRESERVE_ONE); 
		optionNames.add(FORMATTER_CLEAR_BLANK_LINES);

		preferences.setDefault(FORMATTER_NEWLINE_ELSE_IF, DO_NOT_INSERT);
		optionNames.add(FORMATTER_NEWLINE_ELSE_IF);

		preferences.setDefault(FORMATTER_NEWLINE_EMPTY_BLOCK, INSERT); 
		optionNames.add(FORMATTER_NEWLINE_EMPTY_BLOCK);

		preferences.setDefault(FORMATTER_LINE_SPLIT, "80"); //$NON-NLS-1$
		optionNames.add(FORMATTER_LINE_SPLIT);

		preferences.setDefault(FORMATTER_COMPACT_ASSIGNMENT, NORMAL); 
		optionNames.add(FORMATTER_COMPACT_ASSIGNMENT);

		preferences.setDefault(FORMATTER_TAB_CHAR, TAB); 
		optionNames.add(FORMATTER_TAB_CHAR);

		preferences.setDefault(FORMATTER_TAB_SIZE, "4"); //$NON-NLS-1$ 
		optionNames.add(FORMATTER_TAB_SIZE);
		
		// CodeAssist settings
		preferences.setDefault(CODEASSIST_VISIBILITY_CHECK, DISABLED); //$NON-NLS-1$
		optionNames.add(CODEASSIST_VISIBILITY_CHECK);

		preferences.setDefault(CODEASSIST_IMPLICIT_QUALIFICATION, DISABLED); //$NON-NLS-1$
		optionNames.add(CODEASSIST_IMPLICIT_QUALIFICATION);
		
	}
	
	/**
	 * Returns whether the given marker references the given Java element.
	 * Used for markers, which denote a Java element rather than a resource.
	 *
	 * @param element the element
	 * @param marker the marker
	 * @return <code>true</code> if the marker references the element, false otherwise
	 * @exception CoreException if the <code>IMarker.getAttribute</code> on the marker fails 	 
	 */
	public static boolean isReferencedBy(IJavaElement element, IMarker marker) throws CoreException {
		
		// only match units or classfiles
		if (element instanceof IMember){
			IMember member = (IMember) element;
			if (member.isBinary()){
				element = member.getClassFile();
			} else {
				element = member.getCompilationUnit();
			}
		}
		if (element == null) return false;			
		if (marker == null) return false;

		String markerHandleId = (String)marker.getAttribute(ATT_HANDLE_ID);
		if (markerHandleId == null) return false;
		
		IJavaElement markerElement = JavaCore.create(markerHandleId);
		while (true){
			if (element.equals(markerElement)) return true; // external elements may still be equal with different handleIDs.
			
			// cycle through enclosing types in case marker is associated with a classfile (15568)
			if (markerElement instanceof IClassFile){
				IType enclosingType = ((IClassFile)markerElement).getType().getDeclaringType();
				if (enclosingType != null){
					markerElement = enclosingType.getClassFile(); // retry with immediate enclosing classfile
					continue;
				}
			}
			break;
		}
		return false;
	}

	/**
	 * Returns whether the given marker delta references the given Java element.
	 * Used for markers deltas, which denote a Java element rather than a resource.
	 *
	 * @param element the element
	 * @param markerDelta the marker delta
	 * @return <code>true</code> if the marker delta references the element
	 * @exception CoreException if the  <code>IMarkerDelta.getAttribute</code> on the marker delta fails 	 
	 */
	public static boolean isReferencedBy(IJavaElement element, IMarkerDelta markerDelta) throws CoreException {
		
		// only match units or classfiles
		if (element instanceof IMember){
			IMember member = (IMember) element;
			if (member.isBinary()){
				element = member.getClassFile();
			} else {
				element = member.getCompilationUnit();
			}
		}
		if (element == null) return false;			
		if (markerDelta == null) return false;

		String markerDeltarHandleId = (String)markerDelta.getAttribute(ATT_HANDLE_ID);
		if (markerDeltarHandleId == null) return false;
		
		IJavaElement markerElement = JavaCore.create(markerDeltarHandleId);
		while (true){
			if (element.equals(markerElement)) return true; // external elements may still be equal with different handleIDs.
			
			// cycle through enclosing types in case marker is associated with a classfile (15568)
			if (markerElement instanceof IClassFile){
				IType enclosingType = ((IClassFile)markerElement).getType().getDeclaringType();
				if (enclosingType != null){
					markerElement = enclosingType.getClassFile(); // retry with immediate enclosing classfile
					continue;
				}
			}
			break;
		}
		return false;
	}

	/**
	 * Creates and returns a new classpath entry of kind <code>CPE_CONTAINER</code>
	 * for the given path. The path of the container will be used during resolution so as to map this
	 * container entry to a set of other classpath entries the container is acting for.
	 * <p>
	 * A container entry allows to express indirect references to a set of libraries, projects and variable entries,
	 * which can be interpreted differently for each Java project where it is used.
	 * A classpath container entry can be resolved using <code>JavaCore#getResolvedClasspathContainer</code>,
	 * and updated with <code>JavaCore#classpathContainerChanged</code>
	 * <p>
	 * A container is exclusively resolved by a <code>ClasspathContainerInitializer</code> registered onto the
	 * extension point "org.eclipse.jdt.core.classpathContainerInitializer".
	 * <p>
	 * A container path must be formed of at least one segment, where: <ul>
	 * <li> the first segment is a unique ID identifying the target container, there must be a container initializer registered
	 * 	onto this ID through the extension point  "org.eclipse.jdt.core.classpathContainerInitializer". </li>
	 * <li> the remaining segments will be passed onto the initializer, and can be used as additional
	 * 	hints during the initialization phase. </li>
	 * </ul>
	 * <p>
	 * Example of an ClasspathContainerInitializer for a classpath container denoting a default JDK container:
	 * 
	 * containerEntry = JavaCore.newContainerEntry(new Path("MyProvidedJDK/default"));
	 * 
	 * <extension
	 *    point="org.eclipse.jdt.core.classpathContainerInitializer">
	 *    <containerInitializer
	 *       id="MyProvidedJDK"
	 *       class="com.example.MyInitializer"/> 
	 * <p>
	 * Note that this operation does not attempt to validate classpath containers
	 * or access the resources at the given paths.
	 * <p>
	 * The resulting entry is not exported to dependent projects. This method is equivalent to
	 * <code>newContainerEntry(-,false)</code>.
	 * <p>
	 * @param containerPath the path identifying the container, it must be formed of two
	 * 	segments
	 * @return a new container classpath entry
	 * 
	 * @see JavaCore#getClasspathContainer(IPath, IJavaProject)
	 * @see JavaCore#newContainerEntry(IPath, boolean)
	 * @since 2.0
	 */
	public static IClasspathEntry newContainerEntry(IPath containerPath) {
			
		return newContainerEntry(containerPath, false);
	}

	/**
	 * Creates and returns a new classpath entry of kind <code>CPE_CONTAINER</code>
	 * for the given path. The path of the container will be used during resolution so as to map this
	 * container entry to a set of other classpath entries the container is acting for.
	 * <p>
	 * A container entry allows to express indirect references to a set of libraries, projects and variable entries,
	 * which can be interpreted differently for each Java project where it is used.
	 * A classpath container entry can be resolved using <code>JavaCore#getResolvedClasspathContainer</code>,
	 * and updated with <code>JavaCore#classpathContainerChanged</code>
	 * <p>
	 * A container is exclusively resolved by a <code>ClasspathContainerInitializer</code> registered onto the
	 * extension point "org.eclipse.jdt.core.classpathContainerInitializer".
	 * <p>
	 * A container path must be formed of at least one segment, where: <ul>
	 * <li> the first segment is a unique ID identifying the target container, there must be a container initializer registered
	 * 	onto this ID through the extension point  "org.eclipse.jdt.core.classpathContainerInitializer". </li>
	 * <li> the remaining segments will be passed onto the initializer, and can be used as additional
	 * 	hints during the initialization phase. </li>
	 * </ul>
	 * <p>
	 * Example of an ClasspathContainerInitializer for a classpath container denoting a default JDK container:
	 * 
	 * containerEntry = JavaCore.newContainerEntry(new Path("MyProvidedJDK/default"));
	 * 
	 * <extension
	 *    point="org.eclipse.jdt.core.classpathContainerInitializer">
	 *    <containerInitializer
	 *       id="MyProvidedJDK"
	 *       class="com.example.MyInitializer"/> 
	 * <p>
	 * Note that this operation does not attempt to validate classpath containers
	 * or access the resources at the given paths.
	 * <p>
	 * @param containerPath the path identifying the container, it must be formed of at least
	 * 	one segment (ID+hints)
	 * @param isExported a boolean indicating whether this entry is contributed to dependent
	 *		projects in addition to the output location
	 * @return a new container classpath entry
	 * 
	 * @see JavaCore#getClasspathContainer(IPath, IJavaProject)
	 * @see JavaCore#setClasspathContainer(IPath, IJavaProject[], IClasspathContainer[], IProgressMonitor)
	 * @see JavaCore#newContainerEntry(IPath, boolean)
	 * @since 2.0
	 */
	public static IClasspathEntry newContainerEntry(IPath containerPath, boolean isExported) {
			
		Assert.isTrue(
			containerPath != null && containerPath.segmentCount() >= 1,
			Util.bind("classpath.illegalContainerPath" )); //$NON-NLS-1$
			
		return new ClasspathEntry(
			IPackageFragmentRoot.K_SOURCE,
			IClasspathEntry.CPE_CONTAINER,
			containerPath,
			null,
			null,
			isExported);
	}

	/**
	 * Creates and returns a new non-exported classpath entry of kind <code>CPE_LIBRARY</code> for the 
	 * JAR or folder identified by the given absolute path. This specifies that all package fragments 
	 * within the root will have children of type <code>IClassFile</code>.
	 * <p>
	 * A library entry is used to denote a prerequisite JAR or root folder containing binaries.
	 * The target JAR or folder can either be defined internally to the workspace (absolute path relative
	 * to the workspace root) or externally to the workspace (absolute path in the file system).
	 * <p>
	 * e.g. Here are some examples of binary path usage<ul>
	 *	<li><code> "c:/jdk1.2.2/jre/lib/rt.jar" </code> - reference to an external JAR</li>
	 *	<li><code> "/Project/someLib.jar" </code> - reference to an internal JAR </li>
	 *	<li><code> "c:/classes/" </code> - reference to an external binary folder</li>
	 * </ul>
	 * Note that this operation does not attempt to validate or access the 
	 * resources at the given paths.
	 * <p>
	 * The resulting entry is not exported to dependent projects. This method is equivalent to
	 * <code>newLibraryEntry(-,-,-,false)</code>.
	 * <p>
	 * 
	 * @param path the absolute path of the binary archive
	 * @param sourceAttachmentPath the absolute path of the corresponding source archive, 
	 *    or <code>null</code> if none
	 * @param sourceAttachmentRootPath the location of the root within the source archive
	 *    or <code>null</code> if <code>archivePath</code> is also <code>null</code>
	 * @return a new library classpath entry
	 * 
	 * @see #newLibraryEntry(IPath, IPath, IPath, boolean)
	 */
	public static IClasspathEntry newLibraryEntry(
		IPath path,
		IPath sourceAttachmentPath,
		IPath sourceAttachmentRootPath) {
			
		return newLibraryEntry(path, sourceAttachmentPath, sourceAttachmentRootPath, false);
	}

	/**
	 * Creates and returns a new classpath entry of kind <code>CPE_LIBRARY</code> for the JAR or folder
	 * identified by the given absolute path. This specifies that all package fragments within the root 
	 * will have children of type <code>IClassFile</code>.
	 * <p>
	 * A library entry is used to denote a prerequisite JAR or root folder containing binaries.
	 * The target JAR or folder can either be defined internally to the workspace (absolute path relative
	 * to the workspace root) or externally to the workspace (absolute path in the file system).
	 *	<p>
	 * e.g. Here are some examples of binary path usage<ul>
	 *	<li><code> "c:/jdk1.2.2/jre/lib/rt.jar" </code> - reference to an external JAR</li>
	 *	<li><code> "/Project/someLib.jar" </code> - reference to an internal JAR </li>
	 *	<li><code> "c:/classes/" </code> - reference to an external binary folder</li>
	 * </ul>
	 * Note that this operation does not attempt to validate or access the 
	 * resources at the given paths.
	 * <p>
	 * 
	 * @param path the absolute path of the binary archive
	 * @param sourceAttachmentPath the absolute path of the corresponding source archive, 
	 *    or <code>null</code> if none
	 * @param sourceAttachmentRootPath the location of the root within the source archive
	 *    or <code>null</code> if <code>archivePath</code> is also <code>null</code>
	 * @param isExported indicates whether this entry is contributed to dependent
	 * 	  projects in addition to the output location
	 * @return a new library classpath entry
	 * @since 2.0
	 */
	public static IClasspathEntry newLibraryEntry(
		IPath path,
		IPath sourceAttachmentPath,
		IPath sourceAttachmentRootPath,
		boolean isExported) {
			
		Assert.isTrue(
			path.isAbsolute(),
			Util.bind("classpath.needAbsolutePath" )); //$NON-NLS-1$
			
		return new ClasspathEntry(
			IPackageFragmentRoot.K_BINARY,
			IClasspathEntry.CPE_LIBRARY,
			JavaProject.canonicalizedPath(path),
			sourceAttachmentPath,
			sourceAttachmentRootPath,
			isExported);
	}

	/**
	 * Creates and returns a new non-exported classpath entry of kind <code>CPE_PROJECT</code>
	 * for the project identified by the given absolute path.
	 * <p>
	 * A project entry is used to denote a prerequisite project on a classpath.
	 * The referenced project will be contributed as a whole, either as sources (in the Java Model, it
	 * contributes all its package fragment roots) or as binaries (when building, it contributes its 
	 * whole output location).
	 * <p>
	 * A project reference allows to indirect through another project, independently from its internal layout. 
	 * <p>
	 * The prerequisite project is referred to using an absolute path relative to the workspace root.
	 * <p>
	 * The resulting entry is not exported to dependent projects. This method is equivalent to
	 * <code>newProjectEntry(_,false)</code>.
	 * <p>
	 * 
	 * @param path the absolute path of the binary archive
	 * @return a new project classpath entry
	 * 
	 * @see JavaCore#newProjectEntry(IPath, boolean)
	 */
	public static IClasspathEntry newProjectEntry(IPath path) {
		return newProjectEntry(path, false);
	}
	
	/**
	 * Creates and returns a new classpath entry of kind <code>CPE_PROJECT</code>
	 * for the project identified by the given absolute path.
	 * <p>
	 * A project entry is used to denote a prerequisite project on a classpath.
	 * The referenced project will be contributed as a whole, either as sources (in the Java Model, it
	 * contributes all its package fragment roots) or as binaries (when building, it contributes its 
	 * whole output location).
	 * <p>
	 * A project reference allows to indirect through another project, independently from its internal layout. 
	 * <p>
	 * The prerequisite project is referred to using an absolute path relative to the workspace root.
	 * <p>
	 * 
	 * @param path the absolute path of the prerequisite project
	 * @param isExported indicates whether this entry is contributed to dependent
	 * 	  projects in addition to the output location
	 * @return a new project classpath entry
	 * @since 2.0
	 */
	public static IClasspathEntry newProjectEntry(IPath path, boolean isExported) {
		Assert.isTrue(
			path.isAbsolute(),
			Util.bind("classpath.needAbsolutePath" )); //$NON-NLS-1$
		return new ClasspathEntry(
			IPackageFragmentRoot.K_SOURCE,
			IClasspathEntry.CPE_PROJECT,
			path,
			null,
			null,
			isExported);
	}

	/**
	 * Returns a new empty region.
	 * 
	 * @return a new empty region
	 */
	public static IRegion newRegion() {
		return new Region();
	}

	/**
	 * Creates and returns a new classpath entry of kind <code>CPE_SOURCE</code> for the project's source folder 
	 * identified by the given absolute path. This specifies that all package fragments within the root will 
	 * have children of type <code>ICompilationUnit</code>.
	 * <p>
	 * The source folder is referred to using an absolute path relative to the workspace root, e.g. <code>"/Project/src"</code>.
	 * </p>
	 * <p>
	 * A source entry is used to set up the internal source layout of a project, and cannot be used out of the
	 * context of the containing project (a source entry "Proj1/src" cannot be used on the classpath of Proj2).
	 * </p>
	 * <p>
	 * A particular source entry cannot be exported to other projects. All sources/binaries inside a project are
	 * contributed as a whole through a project entry (see <code>JavaCore.newProjectEntry</code>).
	 * </p>
	 * 
	 * @param path the absolute path of a source folder
	 * @return a new source classpath entry
	 */
	public static IClasspathEntry newSourceEntry(IPath path) {
		Assert.isTrue(
			path.isAbsolute(),
			Util.bind("classpath.needAbsolutePath" )); //$NON-NLS-1$
		return new ClasspathEntry(
			IPackageFragmentRoot.K_SOURCE,
			IClasspathEntry.CPE_SOURCE,
			path,
			null,
			null,
			false);
	}

	/**
	 * Creates and returns a new non-exported classpath entry of kind <code>CPE_VARIABLE</code>
	 * for the given path. The first segment of the path is the name of a classpath variable.
	 * The trailing segments of the path will be appended to resolved variable path.
	 * <p>
	 * A variable entry allows to express indirect references on a classpath to other projects or libraries,
	 * depending on what the classpath variable is referring.
	 * <p>
	 *	It is possible to register an automatic initializer (<code>ClasspathVariableInitializer</code>),
	 * which will be invoked through the extension point "org.eclipse.jdt.core.classpathVariableInitializer".
	 * After resolution, a classpath variable entry may either correspond to a project or a library entry. </li>	 
	 * <p>
	 * e.g. Here are some examples of variable path usage<ul>
	 * <li> "JDTCORE" where variable <code>JDTCORE</code> is 
	 *		bound to "c:/jars/jdtcore.jar". The resolved classpath entry is denoting the library "c:\jars\jdtcore.jar"</li>
	 * <li> "JDTCORE" where variable <code>JDTCORE</code> is 
	 *		bound to "/Project_JDTCORE". The resolved classpath entry is denoting the project "/Project_JDTCORE"</li>
	 * <li> "PLUGINS/com.example/example.jar" where variable <code>PLUGINS</code>
	 *      is bound to "c:/eclipse/plugins". The resolved classpath entry is denoting the library "c:/eclipse/plugins/com.example/example.jar"</li>
	 * </ul>
	 * Note that this operation does not attempt to validate classpath variables
	 * or access the resources at the given paths.
	 * <p>
	 * The resulting entry is not exported to dependent projects. This method is equivalent to
	 * <code>newVariableEntry(-,-,-,false)</code>.
	 * <p>
	 * 
	 * @param variablePath the path of the binary archive; first segment is the
	 *   name of a classpath variable
	 * @param variableSourceAttachmentPath the path of the corresponding source archive, 
	 *    or <code>null</code> if none; if present, the first segment is the
	 *    name of a classpath variable (not necessarily the same variable
	 *    as the one that begins <code>variablePath</code>)
	 * @param sourceAttachmentRootPath the location of the root within the source archive
	 *    or <code>null</code> if <code>archivePath</code> is also <code>null</code>
	 * @return a new library classpath entry
	 * 
	 * @see JavaCore#newVariableEntry(IPath, IPath, IPath, boolean)
	 */
	public static IClasspathEntry newVariableEntry(
		IPath variablePath,
		IPath variableSourceAttachmentPath,
		IPath sourceAttachmentRootPath) {
		Assert.isTrue(
			variablePath != null && variablePath.segmentCount() >= 1,
			Util.bind("classpath.illegalVariablePath" )); //$NON-NLS-1$
		return newVariableEntry(variablePath, variableSourceAttachmentPath, sourceAttachmentRootPath, false);
	}

	/**
	 * Creates and returns a new non-exported classpath entry of kind <code>CPE_VARIABLE</code>
	 * for the given path. The first segment of the path is the name of a classpath variable.
	 * The trailing segments of the path will be appended to resolved variable path.
	 * <p>
	 * A variable entry allows to express indirect references on a classpath to other projects or libraries,
	 * depending on what the classpath variable is referring.
	 * <p>
	 *	It is possible to register an automatic initializer (<code>ClasspathVariableInitializer</code>),
	 * which will be invoked through the extension point "org.eclipse.jdt.core.classpathVariableInitializer".
	 * After resolution, a classpath variable entry may either correspond to a project or a library entry. </li>	 
	 * <p>
	 * e.g. Here are some examples of variable path usage<ul>
	 * <li> "JDTCORE" where variable <code>JDTCORE</code> is 
	 *		bound to "c:/jars/jdtcore.jar". The resolved classpath entry is denoting the library "c:\jars\jdtcore.jar"</li>
	 * <li> "JDTCORE" where variable <code>JDTCORE</code> is 
	 *		bound to "/Project_JDTCORE". The resolved classpath entry is denoting the project "/Project_JDTCORE"</li>
	 * <li> "PLUGINS/com.example/example.jar" where variable <code>PLUGINS</code>
	 *      is bound to "c:/eclipse/plugins". The resolved classpath entry is denoting the library "c:/eclipse/plugins/com.example/example.jar"</li>
	 * </ul>
	 * Note that this operation does not attempt to validate classpath variables
	 * or access the resources at the given paths.
	 * <p>
	 *
	 * @param variablePath the path of the binary archive; first segment is the
	 *   name of a classpath variable
	 * @param variableSourceAttachmentPath the path of the corresponding source archive, 
	 *    or <code>null</code> if none; if present, the first segment is the
	 *    name of a classpath variable (not necessarily the same variable
	 *    as the one that begins <code>variablePath</code>)
	 * @param sourceAttachmentRootPath the location of the root within the source archive
	 *    or <code>null</code> if <code>archivePath</code> is also <code>null</code>
	 * @param isExported indicates whether this entry is contributed to dependent
	 * 	  projects in addition to the output location
	 * @return a new variable classpath entry
	 * @since 2.0
	 */
	public static IClasspathEntry newVariableEntry(
		IPath variablePath,
		IPath variableSourceAttachmentPath,
		IPath sourceAttachmentRootPath,
		boolean isExported) {
			
		Assert.isTrue(
			variablePath != null && variablePath.segmentCount() >= 1,
			Util.bind("classpath.illegalVariablePath" )); //$NON-NLS-1$
			
		return new ClasspathEntry(
			IPackageFragmentRoot.K_SOURCE,
			IClasspathEntry.CPE_VARIABLE,
			variablePath,
			variableSourceAttachmentPath,
			sourceAttachmentRootPath,
			isExported);
	}

	/**
	 * Removed the given classpath variable. Does nothing if no value was
	 * set for this classpath variable.
	 * <p>
	 * This functionality cannot be used while the resource tree is locked.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and 
	 * are preserved from session to session.
	 * <p>
	 *
	 * @param variableName the name of the classpath variable
	 * @see #setClasspathVariable
	 *
	 * @deprecated - use version with extra IProgressMonitor
	 */
	public static void removeClasspathVariable(String variableName) {
		removeClasspathVariable(variableName, null);
	}

	/**
	 * Removed the given classpath variable. Does nothing if no value was
	 * set for this classpath variable.
	 * <p>
	 * This functionality cannot be used while the resource tree is locked.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and 
	 * are preserved from session to session.
	 * <p>
	 *
	 * @param variableName the name of the classpath variable
	 * @param monitor the progress monitor to report progress
	 * @see #setClasspathVariable
	 */
	public static void removeClasspathVariable(
		String variableName,
		IProgressMonitor monitor) {

		try {
			updateVariableValues(new String[]{ variableName}, new IPath[]{ null }, monitor);
		} catch (JavaModelException e) {
		}
	}

	/**
	 * Removes the given element changed listener.
	 * Has no affect if an identical listener is not registered.
	 *
	 * @param listener the listener
	 */
	public static void removeElementChangedListener(IElementChangedListener listener) {
		JavaModelManager.getJavaModelManager().removeElementChangedListener(listener);
	}

	/** 
	 * Bind a container reference path to some actual containers (<code>IClasspathContainer</code>).
	 * This API must be invoked whenever changes in container need to be reflected onto the JavaModel.
	 * Containers can have distinct values in different projects, therefore this API considers a
	 * set of projects with their respective containers.
	 * <p>
	 * <code>containerPath</code> is the path under which these values can be referenced through
	 * container classpath entries (<code>IClasspathEntry#CPE_CONTAINER</code>). A container path 
	 * is formed by a first ID segment followed with extra segments, which can be used as additional hints
	 * for the resolution. The container ID is used to identify a <code>ClasspathContainerInitializer</code> 
	 * registered on the extension point "org.eclipse.jdt.core.classpathContainerInitializer".
	 * <p>
	 * There is no assumption that each individual container value passed in argument 
	 * (<code>respectiveContainers</code>) must answer the exact same path when requested 
	 * <code>IClasspathContainer#getPath</code>. 
	 * Indeed, the containerPath is just an indication for resolving it to an actual container object. It can be 
	 * delegated to a <code>ClasspathContainerInitializer</code>, which can be activated through the extension
	 * point "org.eclipse.jdt.core.ClasspathContainerInitializer"). 
	 * <p>
	 * In reaction to changing container values, the JavaModel will be updated to reflect the new
	 * state of the updated container. 
	 * <p>
	 * This functionality cannot be used while the resource tree is locked.
	 * <p>
	 * Classpath container values are persisted locally to the workspace, but 
	 * are not preserved from a session to another. It is thus highly recommended to register a 
	 * <code>ClasspathContainerInitializer</code> for each referenced container 
	 * (through the extension point "org.eclipse.jdt.core.ClasspathContainerInitializer").
	 * <p>
	 * 
	 * @param containerPath - the name of the container reference, which is being updated
	 * @param affectedProjects - the set of projects for which this container is being bound
	 * @param respectiveContainers - the set of respective containers for the affected projects
	 * @param monitor a monitor to report progress
	 * 
	 * @see ClasspathContainerInitializer
	 * @see #getClasspathContainer(IPath, IJavaProject)
	 * @see IClasspathContainer
	 * @since 2.0
	 */
	public static void setClasspathContainer(IPath containerPath, IJavaProject[] affectedProjects, IClasspathContainer[] respectiveContainers, IProgressMonitor monitor) throws JavaModelException {

		Assert.isTrue(affectedProjects.length == respectiveContainers.length, Util.bind("classpath.mismatchProjectsContainers" )); //$NON-NLS-1$

		if (monitor != null && monitor.isCanceled()) return;

		int projectLength = affectedProjects.length;
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		IClasspathEntry[][] oldResolvedPaths = new IClasspathEntry[projectLength][];

		// filter out unmodified project containers
		int remaining = 0;
		for (int i = 0; i < projectLength; i++){

			if (monitor != null && monitor.isCanceled()) return;

			IJavaProject affectedProject = affectedProjects[i];
			IClasspathContainer newContainer = respectiveContainers[i];
			
			boolean found = false;
			if (affectedProject.getProject().exists()){
				IClasspathEntry[] rawClasspath = affectedProject.getRawClasspath();
				for (int j = 0, cpLength = rawClasspath.length; j <cpLength; j++) {
					IClasspathEntry entry = rawClasspath[j];
					if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER && entry.getPath().equals(containerPath)){
						found = true;
						break;
					}
				}
			}
			if (!found){
				affectedProjects[i] = null; // filter out this project - does not reference the container path
			}
			
			Map perProjectContainers = (Map)JavaModelManager.Containers.get(affectedProject);
			if (perProjectContainers == null){
				perProjectContainers = new HashMap();
				JavaModelManager.Containers.put(affectedProject, perProjectContainers);
			} else {
				IClasspathContainer oldContainer = (IClasspathContainer) perProjectContainers.get(containerPath);
				if (oldContainer != null && oldContainer.equals(respectiveContainers[i])){
					affectedProjects[i] = null; // filter out this project - container did not change
					continue;
				}
			}
			if (found){
				remaining++;
				oldResolvedPaths[i] = affectedProject.getResolvedClasspath(true);
			}
			perProjectContainers.put(containerPath, newContainer);
		}
		
		if (remaining == 0) return;
		
		// trigger model refresh
		boolean wasFiring = manager.isFiring();
		int count = 0;
		try {
			if (wasFiring)
				manager.stopDeltas();
				
			for(int i = 0; i < projectLength; i++){

				if (monitor != null && monitor.isCanceled()) return;

				JavaProject affectedProject = (JavaProject)affectedProjects[i];
				if (affectedProject == null) continue; // was filtered out
				
				if (++count == remaining) { // re-enable firing for the last operation
					if (wasFiring) {
						wasFiring = false;
						manager.startDeltas();
					}
				}
			
				// force a refresh of the affected project (will compute deltas)
				affectedProject.setRawClasspath(
						affectedProject.getRawClasspath(),
						SetClasspathOperation.ReuseOutputLocation,
						monitor,
						!JavaModelManager.IsResourceTreeLocked, // can save resources
						!JavaModelManager.IsResourceTreeLocked && affectedProject.getWorkspace().isAutoBuilding(), // force save?
						oldResolvedPaths[i],
						remaining == 1, // no individual cycle check if more than 1 project
						false); // updating - no validation
			}
			if (remaining > 1){
				// use workspace runnable so as to allow marker creation - workaround bug 14733
//				ResourcesPlugin.getWorkspace().run(
//					new IWorkspaceRunnable() {
//						public void run(IProgressMonitor monitor) throws CoreException {
							JavaProject.updateAllCycleMarkers(); // update them all at once
//						}
//					}, 
//					monitor);					
			}
		} finally {
			if (wasFiring) {
				manager.startDeltas();
				// in case of exception traversing, deltas may be fired only in the next #fire() iteration
			}
		}
					
	}

	/**
	 * Sets the value of the given classpath variable.
	 * The path must have at least one segment.
	 * <p>
	 * This functionality cannot be used while the resource tree is locked.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and 
	 * are preserved from session to session.
	 * <p>
	 *
	 * @param variableName the name of the classpath variable
	 * @param path the path
	 * @see #getClasspathVariable
	 *
	 * @deprecated - use API with IProgressMonitor
	 */
	public static void setClasspathVariable(String variableName, IPath path)
		throws JavaModelException {

		setClasspathVariable(variableName, path, null);
	}

	/**
	 * Sets the value of the given classpath variable.
	 * The path must not be null.
	 * <p>
	 * This functionality cannot be used while the resource tree is locked.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and 
	 * are preserved from session to session.
	 * <p>
	 * Updating a variable with the same value has no effect.
	 *
	 * @param variableName the name of the classpath variable
	 * @param path the path
	 * @param monitor a monitor to report progress
	 * @see #getClasspathVariable
	 */
	public static void setClasspathVariable(
		String variableName,
		IPath path,
		IProgressMonitor monitor)
		throws JavaModelException {

		Assert.isTrue(path != null, Util.bind("classpath.nullVariablePath" )); //$NON-NLS-1$
		setClasspathVariables(new String[]{variableName}, new IPath[]{ path }, monitor);
	}

	/**
	 * Sets the values of all the given classpath variables at once.
	 * Null paths can be used to request corresponding variable removal.
	 * <p>
	 * This functionality cannot be used while the resource tree is locked.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and 
	 * are preserved from session to session.
	 * <p>
	 * Updating a variable with the same value has no effect.
	 * 
	 * @param variableNames an array of names for the updated classpath variables
	 * @param paths an array of path updates for the modified classpath variables (null
	 *       meaning that the corresponding value will be removed
	 * @param monitor a monitor to report progress
	 * @see #getClasspathVariable
	 * @since 2.0
	 */
	public static void setClasspathVariables(
		String[] variableNames,
		IPath[] paths,
		IProgressMonitor monitor)
		throws JavaModelException {

		Assert.isTrue(variableNames.length == paths.length, Util.bind("classpath.mismatchNamePath" )); //$NON-NLS-1$
		updateVariableValues(variableNames, paths, monitor);
	}

	/* (non-Javadoc)
	 * Method declared on IExecutableExtension.
	 * Record any necessary initialization data from the plugin.
	 */
	public void setInitializationData(
		IConfigurationElement cfig,
		String propertyName,
		Object data)
		throws CoreException {
	}

	/**
	 * Sets the current table of options. All and only the options explicitly included in the given table 
	 * are remembered; all previous option settings are forgotten, including ones not explicitly
	 * mentioned.
	 * <p>
	 * For a complete description of the configurable options, see <code>getDefaultOptions</code>.
	 * </p>
	 * 
	 * @param newOptions the new options (key type: <code>String</code>; value type: <code>String</code>),
	 *   or <code>null</code> to reset all options to their default values
	 * @see JavaCore#getDefaultOptions
	 */
	public static void setOptions(Hashtable newOptions) {
		
		// see #initializeDefaultPluginPreferences() for changing default settings
		Preferences preferences = getPlugin().getPluginPreferences();

		if (newOptions == null){
			newOptions = JavaCore.getDefaultOptions();
		}
		Enumeration keys = newOptions.keys();
		while (keys.hasMoreElements()){
			String key = (String)keys.nextElement();
			if (key.equals(CORE_ENCODING)) continue; // skipped, contributed by resource prefs
			String value = (String)newOptions.get(key);
			preferences.setValue(key, value);
		}
		
		// persist options
		getPlugin().savePluginPreferences();
	}
	
	/**
	 * Shutdown the JavaCore plugin
	 * <p>
	 * De-registers the JavaModelManager as a resource changed listener and save participant.
	 * <p>
	 * @see org.eclipse.core.runtime.Plugin#shutdown()
	 */
	public void shutdown() {

		savePluginPreferences();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(JavaModelManager.getJavaModelManager().deltaProcessor);
		workspace.removeSaveParticipant(this);

		((JavaModelManager) JavaModelManager.getJavaModelManager()).shutdown();
	}

	/**
	 * Initiate the background indexing process.
	 * This should be deferred after the plugin activation.
	 */
	private void startIndexing() {

		JavaModelManager.getJavaModelManager().getIndexManager().reset();
	}

	/**
	 * Startup of the JavaCore plugin
	 * <p>
	 * Registers the JavaModelManager as a resource changed listener and save participant.
	 * Starts the background indexing, and restore saved classpath variable values.
	 * <p>
	 * @see org.eclipse.core.runtime.Plugin#startup()
	 */
	public void startup() {
		
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		try {
			manager.configurePluginDebugOptions();

			// request state folder creation (workaround 19885)
			JavaCore.getPlugin().getStateLocation();

			// retrieve variable values
			JavaCore.getPlugin().getPluginPreferences().addPropertyChangeListener(new JavaModelManager.PluginPreferencesListener());
			manager.loadVariables();

			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			workspace.addResourceChangeListener(
				manager.deltaProcessor,
				IResourceChangeEvent.PRE_AUTO_BUILD
					| IResourceChangeEvent.POST_CHANGE
					| IResourceChangeEvent.PRE_DELETE
					| IResourceChangeEvent.PRE_CLOSE);

			startIndexing();
			workspace.addSaveParticipant(this, manager);
			
		} catch (CoreException e) {
		} catch (RuntimeException e) {
			manager.shutdown();
			throw e;
		}
	}


	/**
	 * Internal updating of a variable values (null path meaning removal), allowing to change multiple variable values at once.
	 */
	private static void updateVariableValues(
		String[] variableNames,
		IPath[] variablePaths,
		IProgressMonitor monitor) throws JavaModelException {

		if (monitor != null && monitor.isCanceled()) return;
		
		boolean needCycleCheck = false;
		int varLength = variableNames.length;
		
		// gather classpath information for updating
		HashMap affectedProjects = new HashMap(5);
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		IJavaModel model = manager.getJavaModel();

		// filter out unmodified variables
		int discardCount = 0;
		for (int i = 0; i < varLength; i++){
			IPath oldPath = (IPath)JavaModelManager.variableGet(variableNames[i]);
			if (oldPath == JavaModelManager.VariableInitializationInProgress) oldPath = null;
			if (oldPath != null && oldPath.equals(variablePaths[i])){
				variableNames[i] = null;
				discardCount++;
			}
		}
		if (discardCount > 0){
			if (discardCount == varLength) return;
			int changedLength = varLength - discardCount;
			String[] changedVariableNames = new String[changedLength];
			IPath[] changedVariablePaths = new IPath[changedLength];
			for (int i = 0, index = 0; i < varLength; i++){
				if (variableNames[i] != null){
					changedVariableNames[index] = variableNames[i];
					changedVariablePaths[index] = variablePaths[i];
					index++;
				}
			}
			variableNames = changedVariableNames;
			variablePaths = changedVariablePaths;
			varLength = changedLength;
		}
		
		if (monitor != null && monitor.isCanceled()) return;

		if (model != null) {
			IJavaProject[] projects = model.getJavaProjects();
			nextProject : for (int i = 0, projectLength = projects.length; i < projectLength; i++){
				IJavaProject project = projects[i];
						
				// check to see if any of the modified variables is present on the classpath
				IClasspathEntry[] classpath = project.getRawClasspath();
				for (int j = 0, cpLength = classpath.length; j < cpLength; j++){
					
					IClasspathEntry entry = classpath[j];
					for (int k = 0; k < varLength; k++){

						String variableName = variableNames[k];						
						if (entry.getEntryKind() ==  IClasspathEntry.CPE_VARIABLE){

							if (variableName.equals(entry.getPath().segment(0))){
								affectedProjects.put(project, ((JavaProject)project).getResolvedClasspath(true));
								
								// also check whether it will be necessary to update proj references and cycle markers
								if (!needCycleCheck && entry.getPath().segmentCount() ==  1){
									IPath oldPath = (IPath)JavaModelManager.variableGet(variableName);
									if (oldPath == JavaModelManager.VariableInitializationInProgress) oldPath = null;
									if (oldPath != null && oldPath.segmentCount() == 1) {
										needCycleCheck = true;
									} else {
										IPath newPath = variablePaths[k];
										if (newPath != null && newPath.segmentCount() == 1) {
											needCycleCheck = true;
										}
									}
								}
								continue nextProject;
							}
							IPath sourcePath, sourceRootPath;
							if (((sourcePath = entry.getSourceAttachmentPath()) != null	&& variableName.equals(sourcePath.segment(0)))
								|| ((sourceRootPath = entry.getSourceAttachmentRootPath()) != null	&& variableName.equals(sourceRootPath.segment(0)))) {

								affectedProjects.put(project, ((JavaProject)project).getResolvedClasspath(true));
								continue nextProject;
							}
						}												
					}
				}
			}
		}
		// update variables
		for (int i = 0; i < varLength; i++){
			IPath path = variablePaths[i];
			JavaModelManager.variablePut(variableNames[i], path);
		}
				
		// update affected project classpaths
		int size = affectedProjects.size();
		
		if (!affectedProjects.isEmpty()) {
			boolean wasFiring = manager.isFiring();
			try {
				if (wasFiring)
					manager.stopDeltas();
				// propagate classpath change
				Iterator projectsToUpdate = affectedProjects.keySet().iterator();
				while (projectsToUpdate.hasNext()) {

					if (monitor != null && monitor.isCanceled()) return;

					JavaProject project = (JavaProject) projectsToUpdate.next();
					
					if (!projectsToUpdate.hasNext()) {
						// re-enable firing for the last operation
						if (wasFiring) {
							wasFiring = false;
							manager.startDeltas();
						}
					}
					project
						.setRawClasspath(
							project.getRawClasspath(),
							SetClasspathOperation.ReuseOutputLocation,
							monitor,
							!JavaModelManager.IsResourceTreeLocked, // can change resources
							!JavaModelManager.IsResourceTreeLocked && project.getWorkspace().isAutoBuilding(),// force build if in auto build mode
							(IClasspathEntry[]) affectedProjects.get(project),
							size == 1 && needCycleCheck, // no individual check if more than 1 project to update
							false); // updating - no validation
				}
				if (size > 1 && needCycleCheck){
					// use workspace runnable for protecting marker manipulation
//					ResourcesPlugin.getWorkspace().run(
//						new IWorkspaceRunnable() {
//							public void run(IProgressMonitor monitor) throws CoreException {
								JavaProject.updateAllCycleMarkers(); // update them all at once
//							}
//						}, 
//						monitor);					
				}
			} finally {
				if (wasFiring) {
					manager.startDeltas();
					// in case of exception traversing, deltas may be fired only in the next #fire() iteration
				}
			}
		}
	}
}