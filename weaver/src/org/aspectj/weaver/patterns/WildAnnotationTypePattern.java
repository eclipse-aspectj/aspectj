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
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;
import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;

/**
 * @author colyer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WildAnnotationTypePattern extends AnnotationTypePattern {

	private TypePattern typePattern;
	private boolean resolved = false;
	Map annotationValues;
	
	/**
	 * 
	 */
	public WildAnnotationTypePattern(TypePattern typePattern) {
		super();
		this.typePattern = typePattern;
		this.setLocation(typePattern.getSourceContext(), typePattern.start, typePattern.end);
	}
	
	public WildAnnotationTypePattern(TypePattern typePattern, Map annotationValues) {
		super();
		this.typePattern = typePattern;
		this.annotationValues = annotationValues;
		// PVAL make the location be from start of type pattern to end of values
		this.setLocation(typePattern.getSourceContext(), typePattern.start, typePattern.end);		
	}

    public TypePattern getTypePattern() {
        return typePattern;
    }
    
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.AnnotationTypePattern#matches(org.aspectj.weaver.AnnotatedElement)
	 */
	public FuzzyBoolean matches(AnnotatedElement annotated) {
		return matches(annotated,null);
	}

	/**
	 * Resolve any annotation values specified, checking they are all well formed (valid names, valid values)
	 * @param annotationType the annotation type for which the values have been specified
	 * @param scope the scope within which to resolve type references (eg. Color.GREEN)
	 */
	protected void resolveAnnotationValues(ResolvedType annotationType, IScope scope) {
		if (annotationValues == null) return;
		// Check any values specified are OK:
		// - the value names are for valid annotation fields
		// - the specified values are of the correct type
		// - for enums, check the specified values can be resolved in the specified scope
		Set keys = annotationValues.keySet();
		ResolvedMember[] ms = annotationType.getDeclaredMethods();
		for (Iterator kIter = keys.iterator(); kIter.hasNext();) {
			String k = (String) kIter.next();
			String v = (String) annotationValues.get(k);
			boolean validKey = false;
			for (int i = 0; i < ms.length; i++) {
				ResolvedMember resolvedMember = ms[i];
				if (resolvedMember.getName().equals(k) && resolvedMember.isAbstract()) {
					validKey = true;
					ResolvedType t = resolvedMember.getReturnType().resolve(scope.getWorld());
					if (t.isEnum()) {
						// value must be an enum reference X.Y
						int pos = v.lastIndexOf(".");
						if (pos == -1) {
							IMessage m = MessageUtil.error(
									WeaverMessages.format(WeaverMessages.INVALID_ANNOTATION_VALUE,v,"enum"),
									getSourceLocation());
							scope.getWorld().getMessageHandler().handleMessage(m);							
						} else {
							String typename = v.substring(0,pos);
							ResolvedType rt = scope.lookupType(typename, this).resolve(scope.getWorld());
							v = rt.getSignature()+v.substring(pos+1); // from 'Color.RED' to 'Lp/Color;RED'
							annotationValues.put(k,v);
						}
					} else if (t.isPrimitiveType()) {
						if (t.getSignature()=="I") {
						   try {
							   int value = Integer.parseInt(v);
							   annotationValues.put(k,Integer.toString(value));
						   } catch (NumberFormatException nfe) {
							   IMessage m = MessageUtil.error(
										WeaverMessages.format(WeaverMessages.INVALID_ANNOTATION_VALUE,v,"int"),
										getSourceLocation());
								scope.getWorld().getMessageHandler().handleMessage(m);	
						   }
						} else if (t.getSignature()=="F") {
							   try {
								   float value = Float.parseFloat(v);
								   annotationValues.put(k,Float.toString(value));
							   } catch (NumberFormatException nfe) {
								   IMessage m = MessageUtil.error(
											WeaverMessages.format(WeaverMessages.INVALID_ANNOTATION_VALUE,v,"float"),
											getSourceLocation());
									scope.getWorld().getMessageHandler().handleMessage(m);	
							   }	

						} else if (t.getSignature()=="Z") {
							if (v.equalsIgnoreCase("true") || v.equalsIgnoreCase("false")) {
								// is it ok !
							} else {
								   IMessage m = MessageUtil.error(
											WeaverMessages.format(WeaverMessages.INVALID_ANNOTATION_VALUE,v,"boolean"),
											getSourceLocation());
									scope.getWorld().getMessageHandler().handleMessage(m);	
						   }	
						} else if (t.getSignature()=="S") {
								   try {
									   short value = Short.parseShort(v);
									   annotationValues.put(k,Short.toString(value));
								   } catch (NumberFormatException nfe) {
									   IMessage m = MessageUtil.error(
												WeaverMessages.format(WeaverMessages.INVALID_ANNOTATION_VALUE,v,"short"),
												getSourceLocation());
										scope.getWorld().getMessageHandler().handleMessage(m);	
								   }
						} else if (t.getSignature()=="J") {
							   try {
								   long value = Long.parseLong(v);
								   annotationValues.put(k,Long.toString(value));
							   } catch (NumberFormatException nfe) {
								   IMessage m = MessageUtil.error(
											WeaverMessages.format(WeaverMessages.INVALID_ANNOTATION_VALUE,v,"long"),
											getSourceLocation());
									scope.getWorld().getMessageHandler().handleMessage(m);	
							   }
						} else if (t.getSignature()=="D") {
							   try {
								   double value = Double.parseDouble(v);
								   annotationValues.put(k,Double.toString(value));
							   } catch (NumberFormatException nfe) {
								   IMessage m = MessageUtil.error(
											WeaverMessages.format(WeaverMessages.INVALID_ANNOTATION_VALUE,v,"double"),
											getSourceLocation());
									scope.getWorld().getMessageHandler().handleMessage(m);	
							   }
						} else if (t.getSignature()=="B") {
							   try {
								   byte value = Byte.parseByte(v);
								   annotationValues.put(k,Byte.toString(value));
							   } catch (NumberFormatException nfe) {
								   IMessage m = MessageUtil.error(
											WeaverMessages.format(WeaverMessages.INVALID_ANNOTATION_VALUE,v,"byte"),
											getSourceLocation());
									scope.getWorld().getMessageHandler().handleMessage(m);	
							   }
						} else if (t.getSignature()=="C") {
							if (v.length()!=3) {  // '?'
								  IMessage m = MessageUtil.error(
											WeaverMessages.format(WeaverMessages.INVALID_ANNOTATION_VALUE,v,"char"),
											getSourceLocation());
									scope.getWorld().getMessageHandler().handleMessage(m);	
							} else {
								annotationValues.put(k,v.substring(1,2));
							}
						} else {
							throw new RuntimeException("Not implemented for "+t);
						}
					} else if (t.equals(ResolvedType.JAVA_LANG_STRING)) {
						// nothing to do, it will be OK
					} else {
						throw new RuntimeException("Compiler limitation: annotation value support not implemented for type "+t);
					}
				}
			}
			if (!validKey) {
				IMessage m = MessageUtil.error(
						WeaverMessages.format(WeaverMessages.UNKNOWN_ANNOTATION_VALUE,annotationType,k),
						getSourceLocation());
				scope.getWorld().getMessageHandler().handleMessage(m);	
			}
		}
	}




	public FuzzyBoolean matches(AnnotatedElement annotated,ResolvedType[] parameterAnnotations) {
		if (!resolved) {
			throw new IllegalStateException("Can't match on an unresolved annotation type pattern");
		}
		if (annotationValues!=null) {
			// PVAL improve this restriction, would allow '*(value=Color.RED)'
			throw new IllegalStateException("Cannot use annotationvalues with a wild annotation pattern");
		}
		if (isForParameterAnnotationMatch()) {
			if (parameterAnnotations!=null && parameterAnnotations.length!=0) {
				for (int i = 0; i < parameterAnnotations.length; i++) {
					if (typePattern.matches(parameterAnnotations[i],TypePattern.STATIC).alwaysTrue()) {
						return FuzzyBoolean.YES;
					}
				}
			}
		} else {
			// matches if the type of any of the annotations on the AnnotatedElement is
			// matched by the typePattern.
			ResolvedType[] annTypes = annotated.getAnnotationTypes();
			if (annTypes!=null && annTypes.length!=0) {
				for (int i = 0; i < annTypes.length; i++) {
					if (typePattern.matches(annTypes[i],TypePattern.STATIC).alwaysTrue()) {
						return FuzzyBoolean.YES;
					}
				}
			}
		}
		return FuzzyBoolean.NO;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.AnnotationTypePattern#resolve(org.aspectj.weaver.World)
	 */
	public void resolve(World world) {
		// nothing to do...
	    resolved = true;
	}
	
	/**
	 * This can modify in place, or return a new TypePattern if the type changes.
	 */
    public AnnotationTypePattern resolveBindings(IScope scope, Bindings bindings, 
    								             boolean allowBinding)
    { 
	if (!scope.getWorld().isInJava5Mode()) {
		scope.message(MessageUtil.error(WeaverMessages.format(WeaverMessages.ANNOTATIONS_NEED_JAVA5),
				getSourceLocation()));
		return this;
	}
	if (resolved) return this;
    	this.typePattern = typePattern.resolveBindings(scope,bindings,false,false);
    	resolved = true;
    	if (typePattern instanceof ExactTypePattern) {
    		ExactTypePattern et = (ExactTypePattern)typePattern;
			if (!et.getExactType().resolve(scope.getWorld()).isAnnotation()) {
				IMessage m = MessageUtil.error(
						WeaverMessages.format(WeaverMessages.REFERENCE_TO_NON_ANNOTATION_TYPE,et.getExactType().getName()),
						getSourceLocation());
				scope.getWorld().getMessageHandler().handleMessage(m);
				resolved = false;
			}
			ResolvedType annotationType = et.getExactType().resolve(scope.getWorld());
        	resolveAnnotationValues(annotationType,scope);
    		ExactAnnotationTypePattern eatp = new ExactAnnotationTypePattern(annotationType,annotationValues);
    		eatp.copyLocationFrom(this);
    	    if (isForParameterAnnotationMatch()) eatp.setForParameterAnnotationMatch();
    		return eatp;
    	} else {
    		return this;
    	}
    }
    
    public AnnotationTypePattern parameterizeWith(Map typeVariableMap,World w) {
    	WildAnnotationTypePattern ret = new WildAnnotationTypePattern(typePattern.parameterizeWith(typeVariableMap,w));
    	ret.copyLocationFrom(this);
    	ret.resolved = resolved;
    	return ret;
    }

	private static final byte VERSION = 1; // rev if ser. form changes
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PatternNode#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream s) throws IOException {
		s.writeByte(AnnotationTypePattern.WILD);
		s.writeByte(VERSION);
		typePattern.write(s);
		writeLocation(s);
		s.writeBoolean(isForParameterAnnotationMatch());
		// PVAL
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
		WildAnnotationTypePattern ret;
		byte version = s.readByte();
		if (version > VERSION) {
			throw new BCException("ExactAnnotationTypePattern was written by a newer version of AspectJ");
		}
		TypePattern t = TypePattern.read(s,context);
		ret = new WildAnnotationTypePattern(t);
		ret.readLocation(context,s);
		if (s.getMajorVersion()>=WeaverVersionInfo.WEAVER_VERSION_MINOR_AJ160) {
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
		if (!(obj instanceof WildAnnotationTypePattern)) return false;
		WildAnnotationTypePattern other = (WildAnnotationTypePattern) obj;
		return other.typePattern.equals(typePattern) && this.isForParameterAnnotationMatch()==other.isForParameterAnnotationMatch();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return (17 + 37*typePattern.hashCode())*37+(isForParameterAnnotationMatch()?0:1);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "@(" + typePattern.toString() + ")";
	}

    public Object accept(PatternNodeVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
