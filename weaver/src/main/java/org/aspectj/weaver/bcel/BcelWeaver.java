/* *******************************************************************
 * Copyright (c) 2002-2010 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 * ******************************************************************/
package org.aspectj.weaver.bcel;

import java.io.IOException;
import java.util.List;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.internal.AspectJElementHierarchy;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.weaver.BytecodeWeaver;
import org.aspectj.weaver.BytecodeWorld;
import org.aspectj.weaver.Clazz;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ReferenceTypeDelegate;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.UnwovenClassFile;
import org.aspectj.weaver.WeaverStateInfo;
import org.aspectj.weaver.patterns.PerClause.Kind;

/**
 *
 * @author PARC
 * @author Andy Clement
 * @author Alexandre Vasseur
 */
public class BcelWeaver extends BytecodeWeaver {

	private transient final BcelWorld world;

	public BcelWeaver(BcelWorld world) {
		super();
		this.world = world;
		this.xcutSet = world.getCrosscuttingMembersSet();
	}

	public BytecodeWorld getWorld() {
		return world;
	}

	protected Clazz makeClazz(String filename, byte[] bytes) {
		JavaClass bcelJavaClass = Utility.makeJavaClass(filename, bytes);
		return BcelClazz.asBcelClazz(bcelJavaClass);
	}

	public BcelUnwovenClassFile makeUnwovenClassFile(String filename, byte[] bytes) {
		return new BcelUnwovenClassFile(filename, bytes);
	}

	public BcelUnwovenClassFile makeUnwovenClassFile(String filename, String classname, byte[] bytes) {
		return new BcelUnwovenClassFile(filename, classname, bytes);
	}
	
	/**
	 * Add the given aspect to the weaver. The type is resolved to support DOT for static inner classes as well as DOLLAR
	 *
	 * @param aspectName
	 * @return aspect
	 */
	public ResolvedType addLibraryAspect(String aspectName) {
		if (trace.isTraceEnabled()) {
			trace.enter("addLibraryAspect", this, aspectName);
		}
		// 1 - resolve as is
		UnresolvedType unresolvedT = UnresolvedType.forName(aspectName);
		unresolvedT.setNeedsModifiableDelegate(true);
		ResolvedType type = world.resolve(unresolvedT, true);
		if (type.isMissing()) {
			// fallback on inner class lookup mechanism
			String fixedName = aspectName;
			int hasDot = fixedName.lastIndexOf('.');
			while (hasDot > 0) {
				// System.out.println("BcelWeaver.addLibraryAspect " + fixedName);
				char[] fixedNameChars = fixedName.toCharArray();
				fixedNameChars[hasDot] = '$';
				fixedName = new String(fixedNameChars);
				hasDot = fixedName.lastIndexOf('.');
				UnresolvedType ut = UnresolvedType.forName(fixedName);
				ut.setNeedsModifiableDelegate(true);
				type = world.resolve(ut, true);
				if (!type.isMissing()) {
					break;
				}
			}
		}

		// System.out.println("type: " + type + " for " + aspectName);
		if (type.isAspect()) {
			// Bug 119657 ensure we use the unwoven aspect
			WeaverStateInfo wsi = type.getWeaverState();
			if (wsi != null && wsi.isReweavable()) {
				ReferenceTypeDelegate classType2 = getClassType(type.getName());
//				byte[] bytes = wsi.getUnwovenClassFileData(classType2.getBytes());
//				BcelObjectType classType = getClassType(type.getName());
//				JavaClass wovenJavaClass = classType.getJavaClass();
				byte[] bytes = wsi.getUnwovenClassFileData(classType2.getBytes());
				Clazz unwovenClass = makeClazz(classType2.getFilename(), bytes);
//				JavaClass unwovenJavaClass = Utility.makeJavaClass(wovenJavaClass.getFileName(), bytes);
				world.storeClass(unwovenClass);
				classType2.setJavaClass(unwovenClass, true);
//				classType.setJavaClass(unwovenJavaClass, true);
			}

			// TODO AV - happens to reach that a lot of time: for each type
			// flagged reweavable X for each aspect in the weaverstate
			// => mainly for nothing for LTW - pbly for something in incremental
			// build...
			xcutSet.addOrReplaceAspect(type);
			if (trace.isTraceEnabled()) {
				trace.exit("addLibraryAspect", type);
			}
			if (type.getSuperclass().isAspect()) {
				// If the supertype includes ITDs and the user has not included
				// that aspect in the aop.xml, they will
				// not get picked up, which can give unusual behaviour! See bug
				// 223094
				// This change causes us to pick up the super aspect regardless
				// of what was said in the aop.xml - giving
				// predictable behaviour. If the user also supplied it, there
				// will be no problem other than the second
				// addition overriding the first
				addLibraryAspect(type.getSuperclass().getName());
			}
			return type;
		} else {
			if (type.isMissing()) {
				// May not be found if not visible to the classloader that can see the aop.xml during LTW
				IMessage message = new Message("The specified aspect '"+aspectName+"' cannot be found", null, true);
				world.getMessageHandler().handleMessage(message);
			} else {
				IMessage message = new Message("Cannot register '"+aspectName+"' because the type found with that name is not an aspect", null, true);
				world.getMessageHandler().handleMessage(message);
			}
			return null;
		}
	}
	
	/**
	 * Should be addOrReplace
	 */
	public ReferenceType addClassFile(UnwovenClassFile classFile, boolean fromInpath) {
		addedClasses.add(classFile);
		ReferenceType type = world.addSourceObjectType(classFile.getJavaClass(), false).getResolvedTypeX();
		if (fromInpath) {
			type.setBinaryPath(classFile.getFilename());
		}
		return type;
	}

	public void deleteClassFile(String typename) {
		deletedTypenames.add(typename);
		world.deleteSourceObjectType(UnresolvedType.forName(typename));
	}

	// FOR TESTING
	LazyClassGen weave(BcelUnwovenClassFile classFile, BcelObjectType classType) throws IOException {
		LazyClassGen ret = weave(classFile, classType, true);
		return ret;
	}

	protected LazyClassGen weave(UnwovenClassFile classFile, ReferenceTypeDelegate classType, boolean dump) throws IOException {
		try {
			if (classType.isSynthetic()) { // Don't touch synthetic classes
				if (dump) {
					dumpUnchanged(classFile);
				}
				return null;
			}
			ReferenceType resolvedClassType = classType.getResolvedTypeX();

			if (world.isXmlConfigured() && world.getXmlConfiguration().excludesType(resolvedClassType)) {
				if (!world.getMessageHandler().isIgnoring(IMessage.INFO)) {
					world.getMessageHandler().handleMessage(
							MessageUtil.info("Type '" + resolvedClassType.getName()
									+ "' not woven due to exclusion via XML weaver exclude section"));

				}
				if (dump) {
					dumpUnchanged(classFile);
				}
				return null;
			}

			List<ShadowMunger> shadowMungers = fastMatch(shadowMungerList, resolvedClassType);
			List<ConcreteTypeMunger> typeMungers = classType.getResolvedTypeX().getInterTypeMungers();

			resolvedClassType.checkInterTypeMungers();

			// Decide if we need to do actual weaving for this class
			boolean mightNeedToWeave = shadowMungers.size() > 0 || typeMungers.size() > 0 || classType.isAspect()
					|| world.getDeclareAnnotationOnMethods().size() > 0 || world.getDeclareAnnotationOnFields().size() > 0;

			// May need bridge methods if on 1.5 and something in our hierarchy is
			// affected by ITDs
			boolean mightNeedBridgeMethods = world.isInJava5Mode() && !classType.isInterface()
					&& resolvedClassType.getInterTypeMungersIncludingSupers().size() > 0;

			LazyClassGen clazz = null;
			if (mightNeedToWeave || mightNeedBridgeMethods) {
				clazz = (LazyClassGen)classType.getLazyClassGen();
				// System.err.println("got lazy gen: " + clazz + ", " +
				// clazz.getWeaverState());
				try {
					boolean isChanged = false;

					if (mightNeedToWeave) {
						isChanged = BcelClassWeaver.weave(world, clazz, shadowMungers, typeMungers, lateTypeMungerList,
								inReweavableMode);
					}
					checkDeclareTypeErrorOrWarning(world, classType);
					if (mightNeedBridgeMethods) {
						isChanged = BcelClassWeaver.calculateAnyRequiredBridgeMethods(world, clazz) || isChanged;
					}
					if (isChanged) {
						if (dump) {
							dump(classFile, clazz);
						}
						return clazz;
					}
				} catch (RuntimeException re) {
					String classDebugInfo = null;
					try {
						classDebugInfo = clazz.toLongString();
					} catch (Throwable e) {
						new RuntimeException("Crashed whilst crashing with this exception: " + e, e).printStackTrace();
						// recover from crash whilst producing debug string
						classDebugInfo = clazz.getClassName();
					}
					String messageText = "trouble in: \n" + classDebugInfo;
					getWorld().getMessageHandler().handleMessage(new Message(messageText, IMessage.ABORT, re, null));
				} catch (Error re) {
					String classDebugInfo = null;
					try {
						classDebugInfo = clazz.toLongString();
					} catch (OutOfMemoryError oome) {
						System.err.println("Ran out of memory creating debug info for an error");
						re.printStackTrace(System.err);
						// recover from crash whilst producing debug string
						classDebugInfo = clazz.getClassName();
					} catch (Throwable e) {
						// recover from crash whilst producing debug string
						classDebugInfo = clazz.getClassName();
					}
					String messageText = "trouble in: \n" + classDebugInfo;
					getWorld().getMessageHandler().handleMessage(new Message(messageText, IMessage.ABORT, re, null));
				}
			} else {
				checkDeclareTypeErrorOrWarning(world, classType);
			}
			// this is very odd return behavior trying to keep everyone happy

			// can we remove it from the model now? we know it contains no relationship endpoints...
			AsmManager model = world.getModelAsAsmManager();
			if (world.isMinimalModel() && model != null && !classType.isAspect()) {
				AspectJElementHierarchy hierarchy = (AspectJElementHierarchy) model.getHierarchy();
				String pkgname = classType.getResolvedTypeX().getPackageName();
				String tname = classType.getResolvedTypeX().getSimpleBaseName();
				IProgramElement typeElement = hierarchy.findElementForType(pkgname, tname);
				if (typeElement != null && hasInnerType(typeElement)) {
					// Cannot remove it right now (has inner type), schedule it
					// for possible deletion later if all inner types are
					// removed
					candidatesForRemoval.add(typeElement);
				}
				if (typeElement != null && !hasInnerType(typeElement)) {
					IProgramElement parent = typeElement.getParent();
					// parent may have children: PACKAGE DECL, IMPORT-REFERENCE, TYPE_DECL
					if (parent != null) {
						// if it was the only type we should probably remove
						// the others too.
						parent.removeChild(typeElement);
						if (parent.getKind().isSourceFile()) {
							removeSourceFileIfNoMoreTypeDeclarationsInside(hierarchy, typeElement, parent);
						} else {
							hierarchy.forget(null, typeElement);
							// At this point, the child has been removed. We should now check if the parent is in our
							// 'candidatesForRemoval' set. If it is then that means we were going to remove it but it had a
							// child. Now we can check if it still has a child - if it doesn't it can also be removed!
							walkUpRemovingEmptyTypesAndPossiblyEmptySourceFile(hierarchy, tname, parent);
						}

					}
				}
			}

			if (dump) {
				dumpUnchanged(classFile);
				return clazz;
			} else {
				// the class was not woven, but since it gets there early it may have new generated inner classes
				// attached to it to support LTW perX aspectOf support (see BcelPerClauseAspectAdder)
				// that aggressively defines the inner <aspect>$mayHaveAspect interface.
				if (clazz != null && !clazz.getChildClasses(world).isEmpty()) {
					return clazz;
				}
				return null;
			}
		} finally {
			world.demote();
		}
	}

	public BcelTypeMunger makeTypeMunger(ResolvedTypeMunger resolvedTypeMunger, ResolvedType aspect) {
		return new BcelTypeMunger(resolvedTypeMunger, aspect);
	}

	@Override
	protected ConcreteTypeMunger makePerClauseAspectAdder(ResolvedType theType, Kind kind, LazyClassGen clazz, boolean checkAlreadyThere) {
		BcelPerClauseAspectAdder bcelPerClauseAspectAdder = new BcelPerClauseAspectAdder(theType, kind);
		bcelPerClauseAspectAdder.forceMunge(clazz, checkAlreadyThere);
		return bcelPerClauseAspectAdder;
	}

}
