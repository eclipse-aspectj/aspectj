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
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.Message;
import org.aspectj.weaver.BytecodeWeaver;
import org.aspectj.weaver.Clazz;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.LazyClass;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ReferenceTypeDelegate;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.UnwovenClassFile;
import org.aspectj.weaver.patterns.PerClause.Kind;

/**
 * BCEL implementation of the BytecodeWeaver.
 *
 * @author PARC
 * @author Andy Clement
 * @author Alexandre Vasseur
 */
public class BcelWeaver extends BytecodeWeaver {

	public BcelWeaver(BcelWorld world) {
		super(world);
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
			if (isXmlExcluded(resolvedClassType)) {
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
				try {
					boolean isChanged = false;
					if (mightNeedToWeave) {
						isChanged = BcelClassWeaver.weave(getWorld(), clazz, shadowMungers, typeMungers, lateTypeMungerList,
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
			tryRemoveFromModel(classType);

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
	
	public BcelWorld getWorld() {
		return (BcelWorld)world;
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
	
	public BcelTypeMunger makeTypeMunger(ResolvedTypeMunger resolvedTypeMunger, ResolvedType aspect) {
		return new BcelTypeMunger(resolvedTypeMunger, aspect);
	}

	@Override
	protected ConcreteTypeMunger makePerClauseAspectAdder(ResolvedType theType, Kind kind, LazyClass clazz, boolean checkAlreadyThere) {
		BcelPerClauseAspectAdder bcelPerClauseAspectAdder = new BcelPerClauseAspectAdder(theType, kind);
		bcelPerClauseAspectAdder.forceMunge((LazyClassGen)clazz, checkAlreadyThere);
		return bcelPerClauseAspectAdder;
	}

}
