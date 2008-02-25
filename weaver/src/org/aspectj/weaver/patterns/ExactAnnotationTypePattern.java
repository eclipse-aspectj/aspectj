/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.AnnotatedElement;
import org.aspectj.weaver.AnnotationX;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.TypeVariableReference;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;
import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;

/**
 * Matches an annotation of a given type
 */
public class ExactAnnotationTypePattern extends AnnotationTypePattern {

	protected UnresolvedType annotationType;
	protected String formalName;
	protected boolean resolved = false;
	private boolean bindingPattern = false;
	private Map annotationValues;
	
	/**
	 * @param annotationValues 
	 * 
	 */
	public ExactAnnotationTypePattern(UnresolvedType annotationType, Map annotationValues) {
		this.annotationType = annotationType;
		this.annotationValues = annotationValues;
		this.resolved = (annotationType instanceof ResolvedType);
	}
	
	// Used when deserializing, values will be added
	private ExactAnnotationTypePattern(UnresolvedType annotationType) {
		this.annotationType = annotationType;
		this.resolved = (annotationType instanceof ResolvedType);
	}

	protected ExactAnnotationTypePattern(String formalName) {
		this.formalName = formalName;
		this.resolved = false;
		this.bindingPattern = true;
		// will be turned into BindingAnnotationTypePattern during resolution
	}

	public ResolvedType getResolvedAnnotationType() {
		if (!resolved) throw new IllegalStateException("I need to be resolved first!");
		return (ResolvedType) annotationType;
	}
	
    public UnresolvedType getAnnotationType() {
        return annotationType;
    }
    
    public Map getAnnotationValues() {
    	return annotationValues;
    }

	public FuzzyBoolean fastMatches(AnnotatedElement annotated) {
		if (annotated.hasAnnotation(annotationType) && annotationValues == null) {
			return FuzzyBoolean.YES;
		} else {
			// could be inherited, but we don't know that until we are 
			// resolved, and we're not yet...
			return FuzzyBoolean.MAYBE;
		}
	}
	
	public FuzzyBoolean matches(AnnotatedElement annotated) {
		return matches(annotated,null);
	}
	
	public FuzzyBoolean matches(AnnotatedElement annotated,ResolvedType[] parameterAnnotations) {
		if (!isForParameterAnnotationMatch()) {
			boolean checkSupers = false;
			if (getResolvedAnnotationType().hasAnnotation(UnresolvedType.AT_INHERITED)) {
				if (annotated instanceof ResolvedType) {
					checkSupers = true;
				}
			}
			
			if (annotated.hasAnnotation(annotationType)) {
				if (annotationType instanceof ReferenceType) {
					ReferenceType rt = (ReferenceType)annotationType;
					if (rt.getRetentionPolicy()!=null && rt.getRetentionPolicy().equals("SOURCE")) {
						rt.getWorld().getMessageHandler().handleMessage(
						  MessageUtil.warn(WeaverMessages.format(WeaverMessages.NO_MATCH_BECAUSE_SOURCE_RETENTION,annotationType,annotated),getSourceLocation()));
						return FuzzyBoolean.NO;
					}
				}
				
				
				// Are we also matching annotation values?
				if (annotationValues!=null) {
					AnnotationX theAnnotation = annotated.getAnnotationOfType(annotationType);
					
					// Check each one
					Set keys = annotationValues.keySet();
					for (Iterator keyIter = keys.iterator(); keyIter.hasNext();) {
						String k = (String) keyIter.next();
						String v = (String)annotationValues.get(k);
						if (theAnnotation.hasNamedValue(k)) {
							// Simple case, value is 'name=value' and the annotation specified the same thing
							if (!theAnnotation.hasNameValuePair(k,v)) {
								return FuzzyBoolean.NO;
							}	
						} else {
							// Complex case, look at the default value
							ResolvedMember[] ms = ((ResolvedType)annotationType).getDeclaredMethods();
							boolean foundMatch = false;
							for (int i=0; i<ms.length && !foundMatch;i++) {
								if (ms[i].isAbstract() && ms[i].getParameterTypes().length==0 && ms[i].getName().equals(k)) {
									// we might be onto something
									String s= ms[i].getAnnotationDefaultValue();
									if (s!=null && s.equals(v)) foundMatch=true;;
								}
							}
							if (!foundMatch)
							return FuzzyBoolean.NO;
						}
					}
				}
				return FuzzyBoolean.YES;
			} else if (checkSupers) {
				ResolvedType toMatchAgainst = ((ResolvedType) annotated).getSuperclass();
				while (toMatchAgainst != null) {
					if (toMatchAgainst.hasAnnotation(annotationType)) {
						// Are we also matching annotation values?
						if (annotationValues!=null) {
							AnnotationX theAnnotation = toMatchAgainst.getAnnotationOfType(annotationType);
							
							// Check each one
							Set keys = annotationValues.keySet();
							for (Iterator keyIter = keys.iterator(); keyIter.hasNext();) {
								String k = (String) keyIter.next();
								String v = (String)annotationValues.get(k);
								if (theAnnotation.hasNamedValue(k)) {
									// Simple case, value is 'name=value' and the annotation specified the same thing
									if (!theAnnotation.hasNameValuePair(k,v)) {
										return FuzzyBoolean.NO;
									}	
								} else {
									// Complex case, look at the default value
									ResolvedMember[] ms = ((ResolvedType)annotationType).getDeclaredMethods();
									boolean foundMatch = false;
									for (int i=0; i<ms.length && !foundMatch;i++) {
										if (ms[i].isAbstract() && ms[i].getParameterTypes().length==0 && ms[i].getName().equals(k)) {
											// we might be onto something
											String s= ms[i].getAnnotationDefaultValue();
											if (s!=null && s.equals(v)) foundMatch=true;;
										}
									}
									if (!foundMatch)
									return FuzzyBoolean.NO;
								}
							}
						}
						return FuzzyBoolean.YES;
					}
					toMatchAgainst = toMatchAgainst.getSuperclass();
				}
			} 
		} else {
			// check parameter annotations
			if (parameterAnnotations==null) return FuzzyBoolean.NO;
			for (int i = 0; i < parameterAnnotations.length; i++) {
				if (annotationType.equals(parameterAnnotations[i])) {
					// Are we also matching annotation values?
					if (annotationValues!=null) {
						parameterAnnotations[i].getWorld().getMessageHandler().handleMessage(
								MessageUtil.error("Compiler limitation: annotation value matching for parameter annotations not yet supported"));
						return FuzzyBoolean.NO;
					}
					return FuzzyBoolean.YES;
				}
			}
		}
		
		
		return FuzzyBoolean.NO;
	}
	
	// this version should be called for @this, @target, @args
	public FuzzyBoolean matchesRuntimeType(AnnotatedElement annotated) {
		if (getResolvedAnnotationType().hasAnnotation(UnresolvedType.AT_INHERITED)) {
			// a static match is good enough
			if (matches(annotated).alwaysTrue()) {
				return FuzzyBoolean.YES;
			} 
		}
		// a subtype could match at runtime
		return FuzzyBoolean.MAYBE;
	}

	
	public void resolve(World world) {
		if (!resolved) {
			annotationType = annotationType.resolve(world);
		}
		resolved = true;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.AnnotationTypePattern#resolveBindings(org.aspectj.weaver.patterns.IScope, org.aspectj.weaver.patterns.Bindings, boolean)
	 */
	public AnnotationTypePattern resolveBindings(IScope scope,
			Bindings bindings, boolean allowBinding) {
		if (resolved) return this;
		resolved = true;
		String simpleName = maybeGetSimpleName();
		if (simpleName != null) {
			FormalBinding formalBinding = scope.lookupFormal(simpleName);
			if (formalBinding != null) {
				if (bindings == null) {
					scope.message(IMessage.ERROR, this, "negation doesn't allow binding");
					return this;
				}
				if (!allowBinding) {
					scope.message(IMessage.ERROR, this, 
						"name binding only allowed in @pcds, args, this, and target");
					return this;
				}
				formalName = simpleName;
				bindingPattern = true;
				verifyIsAnnotationType(formalBinding.getType().resolve(scope.getWorld()),scope);
				BindingAnnotationTypePattern binding = new BindingAnnotationTypePattern(formalBinding);
				binding.copyLocationFrom(this);
				bindings.register(binding, scope);
				binding.resolveBinding(scope.getWorld());
				if (isForParameterAnnotationMatch()) binding.setForParameterAnnotationMatch();
				
				return binding;
			} 
		}

		// Non binding case
		String cleanname = annotationType.getName();
		annotationType = scope.getWorld().resolve(annotationType,true);
		
		// We may not have found it if it is in a package, lets look it up...
		if (ResolvedType.isMissing(annotationType)) {
			UnresolvedType type = null;
			while (ResolvedType.isMissing(type = scope.lookupType(cleanname,this))) {
				int lastDot = cleanname.lastIndexOf('.');
				if (lastDot == -1) break;
				cleanname = cleanname.substring(0,lastDot)+"$"+cleanname.substring(lastDot+1);
			}
			annotationType = scope.getWorld().resolve(type,true);
		}
		
		verifyIsAnnotationType((ResolvedType)annotationType,scope);
		return this;
	}
	
	public AnnotationTypePattern parameterizeWith(Map typeVariableMap,World w) {
		UnresolvedType newAnnotationType = annotationType;
		if (annotationType.isTypeVariableReference()) {
			TypeVariableReference t = (TypeVariableReference) annotationType;
			String key = t.getTypeVariable().getName();
			if (typeVariableMap.containsKey(key)) {
				newAnnotationType = (UnresolvedType) typeVariableMap.get(key);
			}
		} else if (annotationType.isParameterizedType()) {
			newAnnotationType = annotationType.parameterize(typeVariableMap);
		}
		ExactAnnotationTypePattern ret = new ExactAnnotationTypePattern(newAnnotationType,annotationValues);
		ret.formalName = formalName;
		ret.bindingPattern = bindingPattern;
		ret.copyLocationFrom(this);
		if (isForParameterAnnotationMatch()) ret.setForParameterAnnotationMatch();
		return ret;
	}
	
	private String maybeGetSimpleName() {
		if (formalName != null) return formalName;
		String ret = annotationType.getName();
		return (ret.indexOf('.') == -1) ? ret : null;
	}
	
	/**
	 * @param scope
	 */
	private void verifyIsAnnotationType(ResolvedType type,IScope scope) {
		if (!type.isAnnotation()) {
			IMessage m = MessageUtil.error(
					WeaverMessages.format(WeaverMessages.REFERENCE_TO_NON_ANNOTATION_TYPE,type.getName()),
					getSourceLocation());
			scope.getWorld().getMessageHandler().handleMessage(m);
			resolved = false;
		}
	}

	private static byte VERSION = 1; // rev if serialisation form changes
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PatternNode#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream s) throws IOException {
		s.writeByte(AnnotationTypePattern.EXACT);
		s.writeByte(VERSION);
		s.writeBoolean(bindingPattern);
		if (bindingPattern) {
			s.writeUTF(formalName);
		} else {
			annotationType.write(s);
		}
		writeLocation(s);
		s.writeBoolean(isForParameterAnnotationMatch());
		if (annotationValues==null) {
			s.writeInt(0);
		} else {
			s.writeInt(annotationValues.size());
			Set key = annotationValues.keySet();
			for (Iterator keys = key.iterator(); keys.hasNext();) {
				String k = (String) keys.next();
				s.writeUTF(k);
				s.writeUTF((String)annotationValues.get(k));
			}
		}
	}

	public static AnnotationTypePattern read(VersionedDataInputStream s,ISourceContext context) throws IOException {
		ExactAnnotationTypePattern ret;
		byte version = s.readByte();
		if (version > VERSION) {
			throw new BCException("ExactAnnotationTypePattern was written by a newer version of AspectJ");
		}
		boolean isBindingPattern = s.readBoolean();
		if (isBindingPattern) {
			ret = new ExactAnnotationTypePattern(s.readUTF());
		} else {
			ret = new ExactAnnotationTypePattern(UnresolvedType.read(s));			
		}
		ret.readLocation(context,s);
		if (s.getMajorVersion()>=WeaverVersionInfo.WEAVER_VERSION_MAJOR_AJ160) {
			if (s.readBoolean()) ret.setForParameterAnnotationMatch();
		}
		if (s.getMajorVersion()>=WeaverVersionInfo.WEAVER_VERSION_MAJOR_AJ160M2) {
			int annotationValueCount = s.readInt();
			if (annotationValueCount>0) {
				Map aValues = new HashMap();
				for (int i=0;i<annotationValueCount;i++) {
					String key = s.readUTF();
					String val = s.readUTF();
					aValues.put(key,val);
				}
				ret.annotationValues = aValues;
			}
		}
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof ExactAnnotationTypePattern)) return false;
		ExactAnnotationTypePattern other = (ExactAnnotationTypePattern) obj;
		return (other.annotationType.equals(annotationType)) && isForParameterAnnotationMatch()==other.isForParameterAnnotationMatch();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return annotationType.hashCode()*37+(isForParameterAnnotationMatch()?0:1);
	}
	
	public String toString() {
	    if (!resolved && formalName != null) return formalName;
		String ret = "@" + annotationType.toString();
		if (formalName != null) ret = ret + " " + formalName;
		return ret;
	}

    public Object accept(PatternNodeVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
