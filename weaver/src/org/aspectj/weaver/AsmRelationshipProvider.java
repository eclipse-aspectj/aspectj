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


package org.aspectj.weaver;

import java.util.Iterator;
import java.util.List;

import org.aspectj.apache.bcel.classfile.Field;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.Utility;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;
import org.aspectj.asm.IRelationshipMap;
import org.aspectj.asm.internal.ProgramElement;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.weaver.bcel.BcelAdvice;


public class AsmRelationshipProvider {
	
    protected static AsmRelationshipProvider INSTANCE = new AsmRelationshipProvider();
    
	public static final String ADVISES = "advises";
	public static final String ADVISED_BY = "advised by";
	public static final String DECLARES_ON = "declares on";
	public static final String DECLAREDY_BY = "declared by";
    public static final String SOFTENS = "softens";
    public static final String SOFTENED_BY = "softened by"; 
    public static final String MATCHED_BY = "matched by";
	public static final String MATCHES_DECLARE = "matches declare";
	public static final String INTER_TYPE_DECLARES = "declared on";
	public static final String INTER_TYPE_DECLARED_BY = "aspect declarations";
	
	public static final String ANNOTATES = "annotates";
	public static final String ANNOTATED_BY = "annotated by";
	
	public void checkerMunger(IHierarchy model, Shadow shadow, Checker checker) {
	  if (!AsmManager.isCreatingModel()) return;
		if (shadow.getSourceLocation() == null || checker.getSourceLocation() == null) return;

		if (World.createInjarHierarchy) {
			checker.createHierarchy();
		}
		
		// Ensure a node for the target exists
		IProgramElement targetNode = getNode(AsmManager.getDefault().getHierarchy(),shadow);
		if (targetNode == null) return;
		String targetHandle = AsmManager.getDefault().getHandleProvider()
				.createHandleIdentifier(targetNode);
		if (targetHandle == null) return;
		
		IProgramElement sourceNode = AsmManager.getDefault().getHierarchy()
				.findElementForSourceLine(checker.getSourceLocation());
		String sourceHandle = AsmManager.getDefault().getHandleProvider()
				.createHandleIdentifier(sourceNode);
		if (sourceHandle == null) return;
				
		IRelationshipMap mapper = AsmManager.getDefault().getRelationshipMap();
		IRelationship foreward = mapper.get(sourceHandle, IRelationship.Kind.DECLARE, MATCHED_BY,false,true);
		foreward.addTarget(targetHandle);
				
		IRelationship back = mapper.get(targetHandle, IRelationship.Kind.DECLARE, MATCHES_DECLARE,false,true);
		if (back != null && back.getTargets() != null) {
			back.addTarget(sourceHandle);
		}
	}

    // For ITDs
	public void addRelationship(
		ResolvedType onType,
		ResolvedTypeMunger munger,
		ResolvedType originatingAspect) {

	  if (!AsmManager.isCreatingModel()) return;
		if (originatingAspect.getSourceLocation() != null) {
			String sourceHandle = "";
			if (munger.getSourceLocation()!=null && munger.getSourceLocation().getOffset()!=-1) {
				IProgramElement sourceNode = AsmManager.getDefault().getHierarchy()
						.findElementForSourceLine(munger.getSourceLocation());
				sourceHandle = AsmManager.getDefault().getHandleProvider()
						.createHandleIdentifier(sourceNode);
			} else {
				IProgramElement sourceNode = AsmManager.getDefault().getHierarchy()
						.findElementForSourceLine(originatingAspect.getSourceLocation());
				sourceHandle = AsmManager.getDefault().getHandleProvider()
						.createHandleIdentifier(sourceNode);
			}
			if (sourceHandle == null) return;
			IProgramElement targetNode = AsmManager.getDefault().getHierarchy()
					.findElementForSourceLine(onType.getSourceLocation());
			String targetHandle = AsmManager.getDefault().getHandleProvider()
					.createHandleIdentifier(targetNode);
			if (targetHandle == null) return;
			
			IRelationshipMap mapper = AsmManager.getDefault().getRelationshipMap();
			IRelationship foreward = mapper.get(sourceHandle, IRelationship.Kind.DECLARE_INTER_TYPE, INTER_TYPE_DECLARES,false,true);
			foreward.addTarget(targetHandle);
			
			IRelationship back = mapper.get(targetHandle, IRelationship.Kind.DECLARE_INTER_TYPE, INTER_TYPE_DECLARED_BY,false,true);
			back.addTarget(sourceHandle);
		}
	}
	
	public void addDeclareParentsRelationship(ISourceLocation decp,ResolvedType targetType, List newParents) {
	    if (!AsmManager.isCreatingModel()) return;

		IProgramElement sourceNode = AsmManager.getDefault().getHierarchy()
				.findElementForSourceLine(decp);
		String sourceHandle = AsmManager.getDefault().getHandleProvider()
				.createHandleIdentifier(sourceNode);
		if (sourceHandle == null) return;
		
		IProgramElement targetNode = AsmManager.getDefault().getHierarchy()
				.findElementForSourceLine(targetType.getSourceLocation());
		String targetHandle = AsmManager.getDefault().getHandleProvider()
				.createHandleIdentifier(targetNode);
		if (targetHandle == null) return;
		
		IRelationshipMap mapper = AsmManager.getDefault().getRelationshipMap();
		IRelationship foreward = mapper.get(sourceHandle, IRelationship.Kind.DECLARE_INTER_TYPE, INTER_TYPE_DECLARES,false,true);
		foreward.addTarget(targetHandle);
				
		IRelationship back = mapper.get(targetHandle, IRelationship.Kind.DECLARE_INTER_TYPE, INTER_TYPE_DECLARED_BY,false,true);
		back.addTarget(sourceHandle);
	}
	
	/**
	 * Adds a declare annotation relationship, sometimes entities don't have source locs (methods/fields) so use other
	 * variants of this method if that is the case as they will look the entities up in the structure model.
	 */
	public void addDeclareAnnotationRelationship(ISourceLocation declareAnnotationLocation,ISourceLocation annotatedLocation) {
	    if (!AsmManager.isCreatingModel()) return;
	    
	    IProgramElement sourceNode = AsmManager.getDefault().getHierarchy()
	    		.findElementForSourceLine(declareAnnotationLocation);
	    String sourceHandle = AsmManager.getDefault().getHandleProvider()
	    		.createHandleIdentifier(sourceNode);
	    if (sourceHandle == null) return;

	    IProgramElement targetNode = AsmManager.getDefault().getHierarchy()
				.findElementForSourceLine(annotatedLocation);
	    String targetHandle = AsmManager.getDefault().getHandleProvider()
				.createHandleIdentifier(targetNode);
		if (targetHandle == null) return;
	    
		IRelationshipMap mapper = AsmManager.getDefault().getRelationshipMap();
		IRelationship foreward = mapper.get(sourceHandle, IRelationship.Kind.DECLARE_INTER_TYPE, ANNOTATES,false,true);
		foreward.addTarget(targetHandle);
				
		IRelationship back = mapper.get(targetHandle, IRelationship.Kind.DECLARE_INTER_TYPE, ANNOTATED_BY,false,true);
		back.addTarget(sourceHandle);
	}
	
	public void adviceMunger(IHierarchy model, Shadow shadow, ShadowMunger munger) {
	  if (!AsmManager.isCreatingModel()) return;
		if (munger instanceof Advice) {
			Advice advice = (Advice)munger;
			
			if (advice.getKind().isPerEntry() || advice.getKind().isCflow()) {
				// TODO: might want to show these in the future
				return;
			}
			
			if (World.createInjarHierarchy) {
				munger.createHierarchy();
			}

			IRelationshipMap mapper = AsmManager.getDefault().getRelationshipMap();
			IProgramElement targetNode = getNode(AsmManager.getDefault().getHierarchy(), shadow);
			if (targetNode == null) return;
			boolean runtimeTest = ((BcelAdvice)munger).hasDynamicTests();
			
			// Work out extra info to inform interested UIs !
			IProgramElement.ExtraInformation ai = new IProgramElement.ExtraInformation();

			String adviceHandle = advice.getHandle(); 
			if (adviceHandle == null) return;
			
			// What kind of advice is it?
			// TODO: Prob a better way to do this but I just want to
			// get it into CVS !!!
			AdviceKind ak = ((Advice)munger).getKind();
			ai.setExtraAdviceInformation(ak.getName());
			IProgramElement adviceElement = AsmManager.getDefault().getHierarchy().findElementForHandle(adviceHandle);
			if (adviceElement != null) {
				adviceElement.setExtraInfo(ai);	
			}
            String targetHandle = targetNode.getHandleIdentifier(); 
            if (advice.getKind().equals(AdviceKind.Softener)) {
            	IRelationship foreward = mapper.get(adviceHandle, IRelationship.Kind.DECLARE_SOFT, SOFTENS,runtimeTest,true);
                if (foreward != null) foreward.addTarget(targetHandle);//foreward.getTargets().add(targetHandle);
                        
                IRelationship back = mapper.get(targetHandle, IRelationship.Kind.DECLARE, SOFTENED_BY,runtimeTest,true);
                if (back != null) back.addTarget(adviceHandle);//back.getTargets().add(adviceHandle);
            } else {
            	IRelationship foreward = mapper.get(adviceHandle, IRelationship.Kind.ADVICE, ADVISES,runtimeTest,true);
    			if (foreward != null) foreward.addTarget(targetHandle);//foreward.getTargets().add(targetHandle);
    					
    			IRelationship back = mapper.get(targetHandle, IRelationship.Kind.ADVICE, ADVISED_BY,runtimeTest,true);
    			if (back != null)     back.addTarget(adviceHandle);//back.getTargets().add(adviceHandle);
            }
		}
	}

	protected IProgramElement getNode(IHierarchy model, Shadow shadow) {
		Member enclosingMember = shadow.getEnclosingCodeSignature();
		
		IProgramElement enclosingNode = lookupMember(model, enclosingMember);
		if (enclosingNode == null) {
			Lint.Kind err = shadow.getIWorld().getLint().shadowNotInStructure;
			if (err.isEnabled()) {
				err.signal(shadow.toString(), shadow.getSourceLocation());
			}
			return null;
		}
		
		Member shadowSig = shadow.getSignature();
		if (!shadowSig.equals(enclosingMember)) {
			IProgramElement bodyNode = findOrCreateCodeNode(enclosingNode, shadowSig, shadow);
			return bodyNode;
		} else {
			return enclosingNode;
		}
	}
	
	private boolean sourceLinesMatch(ISourceLocation loc1,ISourceLocation loc2) {
		if (loc1.getLine()!=loc2.getLine()) return false;
		return true;
	}
	
	/**
	 * Finds or creates a code IProgramElement for the given shadow. 
	 * 
	 * The byteCodeName of the created node is set to 'shadowSig.getName() + "!" + counter',
	 * eg "println!3". The counter is the occurence count of children within
	 * the enclosingNode which have the same name. So, for example, if a method 
	 * contains two System.out.println statements, the first one will have 
	 * byteCodeName 'println!1' and the second will have byteCodeName 'println!2'.
	 * This is to ensure the two nodes have unique handles when the handles 
	 * do not depend on sourcelocations. 
	 * 
	 * Currently the shadows are examined in the sequence they appear in the 
	 * source file. This means that the counters are consistent over incremental 
	 * builds. All aspects are compiled up front and any new aspect created will force
	 * a full build. Moreover, if the body of the enclosingShadow is changed, then
	 * the model for this is rebuilt from scratch. 
	 */
	private IProgramElement findOrCreateCodeNode(IProgramElement enclosingNode, Member shadowSig, Shadow shadow)
	{
		for (Iterator it = enclosingNode.getChildren().iterator(); it.hasNext(); ) {
			IProgramElement node = (IProgramElement)it.next();
			int excl = node.getBytecodeName().lastIndexOf('!');
			if (((excl != -1 
					&& shadowSig.getName().equals(node.getBytecodeName().substring(0,excl)))
					|| shadowSig.getName().equals(node.getBytecodeName()))
					&& shadowSig.getSignature().equals(node.getBytecodeSignature()) 
					&& sourceLinesMatch(node.getSourceLocation(),shadow.getSourceLocation())){
				return node;
			}
		}
		
		ISourceLocation sl = shadow.getSourceLocation();
		
//		XXX why not use shadow file? new SourceLocation(sl.getSourceFile(), sl.getLine()),
		SourceLocation peLoc = new SourceLocation(enclosingNode.getSourceLocation().getSourceFile(),sl.getLine());
		peLoc.setOffset(sl.getOffset());
		IProgramElement peNode = new ProgramElement(
			shadow.toString(),
			IProgramElement.Kind.CODE,
			peLoc,0,null,null);
		
		// check to see if the enclosing shadow already has children with the 
		// same name. If so we want to add a counter to the byteCodeName otherwise
		// we wont get unique handles
		int numberOfChildrenWithThisName = 0;
    	for (Iterator it = enclosingNode.getChildren().iterator(); it.hasNext(); ) {
    		IProgramElement child = (IProgramElement)it.next();
			if (child.getName().equals(shadow.toString())) {
				numberOfChildrenWithThisName++;
			}
    	}
		peNode.setBytecodeName(shadowSig.getName() + "!" + String.valueOf(numberOfChildrenWithThisName+1));
		peNode.setBytecodeSignature(shadowSig.getSignature());
		enclosingNode.addChild(peNode);
		return peNode;
	}
	
	protected IProgramElement lookupMember(IHierarchy model, Member member) {
		UnresolvedType declaringType = member.getDeclaringType();
		IProgramElement classNode =
			model.findElementForType(declaringType.getPackageName(), declaringType.getClassName());
		return findMemberInClass(classNode, member);
	}
 
	protected IProgramElement findMemberInClass(
		IProgramElement classNode,
		Member member)
	{
		if (classNode == null) return null; // XXX remove this check
		for (Iterator it = classNode.getChildren().iterator(); it.hasNext(); ) {
			IProgramElement node = (IProgramElement)it.next();
			if (member.getName().equals(node.getBytecodeName()) &&
				member.getSignature().equals(node.getBytecodeSignature()))
			{
				return node;
			}
		}
	 	// if we can't find the member, we'll just put it in the class
		return classNode;
	}
	
//	private static IProgramElement.Kind genShadowKind(Shadow shadow) {
//		IProgramElement.Kind shadowKind;
//		if (shadow.getKind() == Shadow.MethodCall
//			|| shadow.getKind() == Shadow.ConstructorCall
//			|| shadow.getKind() == Shadow.FieldGet
//			|| shadow.getKind() == Shadow.FieldSet
//			|| shadow.getKind() == Shadow.ExceptionHandler) {
//			return IProgramElement.Kind.CODE;
//			
//		} else if (shadow.getKind() == Shadow.MethodExecution) {
//			return IProgramElement.Kind.METHOD;
//			
//		} else if (shadow.getKind() == Shadow.ConstructorExecution) {
//			return IProgramElement.Kind.CONSTRUCTOR;
//			
//		} else if (shadow.getKind() == Shadow.PreInitialization
//			|| shadow.getKind() == Shadow.Initialization) {
//			return IProgramElement.Kind.CLASS;
//			
//		} else if (shadow.getKind() == Shadow.AdviceExecution) {
//			return IProgramElement.Kind.ADVICE;
//			
//		} else {
//			return IProgramElement.Kind.ERROR;
//		}
//	}

    public static AsmRelationshipProvider getDefault() {
        return INSTANCE;
    }
    
    /**
     * Reset the instance of this class, intended for extensibility.
     * This enables a subclass to become used as the default instance.
     */
    public static void setDefault(AsmRelationshipProvider instance) {
        INSTANCE = instance;
    }

    /**
     * Add a relationship to the known set for a declare @method/@constructor construct.  
     * Locating the method is a messy (for messy read 'fragile') bit of code that could break at any moment
     * but it's working for my simple testcase.  Currently just fails silently if any of the lookup code
     * doesn't find anything...
     */
	public void addDeclareAnnotationRelationship(ISourceLocation sourceLocation, String typename,Method method) {
	  if (!AsmManager.isCreatingModel()) return;
	    
	  String pkg  = null;
	  String type = typename;
	  int packageSeparator = typename.lastIndexOf(".");
	  if (packageSeparator!=-1) {
	 	pkg = typename.substring(0,packageSeparator);
	  	type = typename.substring(packageSeparator+1);
	  }
		
	  IProgramElement typeElem = AsmManager.getDefault().getHierarchy().findElementForType(pkg,type);
	  if (typeElem == null) return;


	  StringBuffer parmString = new StringBuffer("(");
	  Type[] args = method.getArgumentTypes();
	  for (int i = 0; i < args.length; i++) {
			Type type2 = args[i];
			String s = Utility.signatureToString(type2.getSignature(),false);
			parmString.append(s);
			if ((i+1)<args.length) parmString.append(",");
	  }
	  parmString.append(")");
	  IProgramElement methodElem = null;
		
	  if (method.getName().startsWith("<init>")) {
	    // its a ctor
	  	methodElem = AsmManager.getDefault().getHierarchy().findElementForSignature(typeElem,IProgramElement.Kind.CONSTRUCTOR,type+parmString);
		if (methodElem == null && args.length==0) methodElem = typeElem; // assume default ctor
	  } else {
	    // its a method
	  	methodElem = AsmManager.getDefault().getHierarchy().findElementForSignature(typeElem,IProgramElement.Kind.METHOD,method.getName()+parmString);
	  }
	  
	  if (methodElem == null) return;
	  
	  try {
		  String targetHandle = methodElem.getHandleIdentifier();
		  if (targetHandle == null) return; 
		  
		  IProgramElement sourceNode = AsmManager.getDefault().getHierarchy().findElementForSourceLine(sourceLocation);
		  String sourceHandle = AsmManager.getDefault().getHandleProvider().createHandleIdentifier(sourceNode);
		  if (sourceHandle == null) return;	
		  
		  IRelationshipMap mapper = AsmManager.getDefault().getRelationshipMap();
		  IRelationship foreward = mapper.get(sourceHandle, IRelationship.Kind.DECLARE_INTER_TYPE, ANNOTATES,false,true);
		  foreward.addTarget(targetHandle);
					
		  IRelationship back = mapper.get(targetHandle, IRelationship.Kind.DECLARE_INTER_TYPE, ANNOTATED_BY,false,true);
		  back.addTarget(sourceHandle);
       } catch (Throwable t) { // I'm worried about that code above, this will make sure we don't explode if it plays up
    	   t.printStackTrace(); // I know I know .. but I don't want to lose it!
       }
	}
	
    /**
     * Add a relationship to the known set for a declare @field construct.  Locating the field is trickier than
     * it might seem since we have no line number info for it, we have to dig through the structure model under
     * the fields' type in order to locate it.  Currently just fails silently if any of the lookup code
     * doesn't find anything...
     */
	public void addDeclareAnnotationRelationship(ISourceLocation sourceLocation, String typename,Field field) {
	    if (!AsmManager.isCreatingModel()) return;
	    
  	    String pkg  = null;
	    String type = typename;
	    int packageSeparator = typename.lastIndexOf(".");
	    if (packageSeparator!=-1) {
	  	  pkg  = typename.substring(0,packageSeparator);
	  	  type = typename.substring(packageSeparator+1);
	    }
	    
        IProgramElement typeElem = AsmManager.getDefault().getHierarchy().findElementForType(pkg,type);
        if (typeElem == null) return;
        
        IProgramElement fieldElem = AsmManager.getDefault().getHierarchy().findElementForSignature(typeElem,IProgramElement.Kind.FIELD,field.getName());
        if (fieldElem== null) return;

		String targetHandle = fieldElem.getHandleIdentifier();
		if (targetHandle == null) return;
        
		IProgramElement sourceNode = AsmManager.getDefault().getHierarchy().findElementForSourceLine(sourceLocation);
		String sourceHandle = AsmManager.getDefault().getHandleProvider().createHandleIdentifier(sourceNode);
		if (sourceHandle == null) return;  	
		
        IRelationshipMap mapper = AsmManager.getDefault().getRelationshipMap();
		IRelationship foreward = mapper.get(sourceHandle, IRelationship.Kind.DECLARE_INTER_TYPE, ANNOTATES,false,true);
		foreward.addTarget(targetHandle);
				
		IRelationship back = mapper.get(targetHandle, IRelationship.Kind.DECLARE_INTER_TYPE, ANNOTATED_BY,false,true);
		back.addTarget(sourceHandle);
	}
	
}
