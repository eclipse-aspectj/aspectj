/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.bcel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.Type;
import org.apache.bcel.util.ClassPath;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.weaver.Advice;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.FormalBinding;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.SimpleScope;

public class BcelWorld extends World {
	private ClassPathManager classPath;
	
	//private ClassPathManager aspectPath = null;
	private List aspectPathEntries;
	
    // ---- constructors

	public BcelWorld() {
		this("");		
	}
	
	public BcelWorld(String cp) {
		this(makeDefaultClasspath(cp), IMessageHandler.THROW);
	}
	
	private static List makeDefaultClasspath(String cp) {
		List classPath = new ArrayList();
		classPath.addAll(getPathEntries(cp));
		classPath.addAll(getPathEntries(ClassPath.getClassPath()));
		//System.err.println("classpath: " + classPath);
		return classPath;
		
	}
		
	private static List getPathEntries(String s) {
		List ret = new ArrayList();
		StringTokenizer tok = new StringTokenizer(s, File.pathSeparator);

      	while(tok.hasMoreTokens()) ret.add(tok.nextToken());
      	
      	return ret;
	}
	
	public BcelWorld(List classPath, IMessageHandler handler) {
		//this.aspectPath = new ClassPathManager(aspectPath, handler);
		this.classPath = new ClassPathManager(classPath, handler);
		setMessageHandler(handler);	
	}
	
	public void addPath (String name) {
		classPath.addPath(name, this.getMessageHandler());
	}

    /**
     * Parse a string into advice.
     * 
     * <blockquote><pre>
     * Kind ( Id , ... ) : Pointcut -> MethodSignature
     * </pre></blockquote>
     */
    public Advice shadowMunger(String str, int extraFlag) {
        str = str.trim();
        int start = 0;
        int i = str.indexOf('(');
        AdviceKind kind = 
            AdviceKind.stringToKind(str.substring(start, i));
        start = ++i;
        i = str.indexOf(')', i);
        String[] ids = parseIds(str.substring(start, i).trim());
        //start = ++i;
        
        
        
        i = str.indexOf(':', i);        
        start = ++i;        
        i = str.indexOf("->", i);
        Pointcut pointcut = Pointcut.fromString(str.substring(start, i).trim());
        Member m = Member.methodFromString(str.substring(i+2, str.length()).trim());

        // now, we resolve
        TypeX[] types = m.getParameterTypes();
        FormalBinding[] bindings = new FormalBinding[ids.length];
        for (int j = 0, len = ids.length; j < len; j++) {
            bindings[j] = new FormalBinding(types[j], ids[j], j, 0, 0, "fromString");
        }

        Pointcut p =
        	pointcut.resolve(new SimpleScope(this, bindings));

        return new BcelAdvice(kind, p, m, extraFlag, 0, 0, null, null);
    }
    
    private String[] parseIds(String str) {
        if (str.length() == 0) return ZERO_STRINGS;
        List l = new ArrayList();
        int start = 0;
        while (true) {
            int i = str.indexOf(',', start);
            if (i == -1) {
                l.add(str.substring(start).trim());
                break;
            }
            l.add(str.substring(start, i).trim());
            start = i+1;
        }
        return (String[]) l.toArray(new String[l.size()]);
    }
    
    // ---- various interactions with bcel

    public static Type makeBcelType(TypeX type) {
        return Type.getType(type.getSignature());
    }

    static Type[] makeBcelTypes(TypeX[] types) {
        Type[] ret = new Type[types.length];
        for (int i = 0, len = types.length; i < len; i++) {
            ret[i] = makeBcelType(types[i]);
        }
        return ret;
    }

    public static TypeX fromBcel(Type t) {
        return TypeX.forSignature(t.getSignature());
    }

    static TypeX[] fromBcel(Type[] ts) {
        TypeX[] ret = new TypeX[ts.length];
        for (int i = 0, len = ts.length; i < len; i++) {
            ret[i] = fromBcel(ts[i]);
        }
        return ret;
    }
    
    public ResolvedTypeX resolve(Type t) {
        return resolve(fromBcel(t));
    }       


	protected ResolvedTypeX.ConcreteName resolveObjectType(ResolvedTypeX.Name ty) {
        String name = ty.getName();
        JavaClass jc = null;
        //UnwovenClassFile classFile = (UnwovenClassFile)sourceJavaClasses.get(name);
        //if (classFile != null) jc = classFile.getJavaClass();
//		if (jc == null) {
//		    jc = lookupJavaClass(aspectPath, name);
//		}
        if (jc == null) {
        	jc = lookupJavaClass(classPath, name);
        }       
        if (jc == null) {
        	return null;
        } else {
        	return makeBcelObjectType(ty, jc, false);
        }
	}
	
	private BcelObjectType makeBcelObjectType(ResolvedTypeX.Name resolvedTypeX, JavaClass jc, boolean exposedToWeaver) {
		BcelObjectType ret = new BcelObjectType(resolvedTypeX, jc, exposedToWeaver);
		resolvedTypeX.setDelegate(ret);
		return ret;
	}
	
	
	private JavaClass lookupJavaClass(ClassPathManager classPath, String name) {
		if (classPath == null) return null;
		try {
	        ClassPathManager.ClassFile file = classPath.find(TypeX.forName(name));
	        if (file == null) return null;
	        
	        ClassParser parser = new ClassParser(file.getInputStream(), file.getPath());
	        
	        JavaClass jc = parser.parse();
			
			return jc;
		} catch (IOException ioe) {
			return null;
		}
	}
	
	
	public BcelObjectType addSourceObjectType(JavaClass jc) {
		String signature = TypeX.forName(jc.getClassName()).getSignature();
        ResolvedTypeX.Name nameTypeX = (ResolvedTypeX.Name)typeMap.get(signature);

        if (nameTypeX == null) {
        	nameTypeX = new ResolvedTypeX.Name(signature, this);
        }
        BcelObjectType ret = makeBcelObjectType(nameTypeX, jc, true);
        nameTypeX.setDelegate(ret);
        typeMap.put(signature, nameTypeX);
		return ret;
	}
	
	void deleteSourceObjectType(TypeX ty) {
		typeMap.remove(ty.getSignature());
	}

    public static Member makeFieldSignature(LazyClassGen cg, FieldInstruction fi) {
    	ConstantPoolGen cpg = cg.getConstantPoolGen();
        return 
            Member.field(
                fi.getClassName(cpg),
                (fi instanceof GETSTATIC || fi instanceof PUTSTATIC)
                ? Modifier.STATIC
                : 0, 
                fi.getName(cpg),
                fi.getSignature(cpg));
    }
	
    public static Member makeFieldSetSignature(LazyClassGen cg, FieldInstruction fi) {
    	ConstantPoolGen cpg = cg.getConstantPoolGen();
        return 
            Member.field(
                fi.getClassName(cpg),
                (fi instanceof GETSTATIC || fi instanceof PUTSTATIC)
                ? Modifier.STATIC
                : 0, 
                fi.getName(cpg),
                "(" + fi.getSignature(cpg) + ")" +fi.getSignature(cpg));
    }

	public Member makeMethodSignature(LazyMethodGen mg) {
		return makeMethodSignature(mg, null);
	}

	
	public Member makeMethodSignature(LazyMethodGen mg, Member.Kind kind) {
		ResolvedMember ret = mg.getMemberView();
		if (ret == null) {
	        int mods = mg.getAccessFlags();
	        if (mg.getEnclosingClass().isInterface()) {
	            mods |= Modifier.INTERFACE;
	        }
	        if (kind == null) {
		        if (mg.getName().equals("<init>")) {
		        	kind = Member.CONSTRUCTOR;
		        } else if (mg.getName().equals("<clinit>")) {
		        	kind = Member.STATIC_INITIALIZATION;
		        } else {
		        	kind = Member.METHOD;
		        }
	        }
	        return new ResolvedMember(kind,
	                TypeX.forName(mg.getClassName()), 
	                mods,
	                fromBcel(mg.getReturnType()),
	                mg.getName(),
	                fromBcel(mg.getArgumentTypes())
	                );
		} else {
			return ret;
		}
        
    }

    public static Member makeMethodSignature(LazyClassGen cg, InvokeInstruction ii) {
    	ConstantPoolGen cpg = cg.getConstantPoolGen();
        String declaring = ii.getClassName(cpg);
        String name = ii.getName(cpg);
        String signature = ii.getSignature(cpg);
        
        int modifier = 
            (ii instanceof INVOKEINTERFACE)
            ? Modifier.INTERFACE
            : (ii instanceof INVOKESTATIC)
              ? Modifier.STATIC
              : (ii instanceof INVOKESPECIAL && ! name.equals("<init>"))
                ? Modifier.PRIVATE
                : 0;
        return Member.method(TypeX.forName(declaring), modifier, name, signature);
    }  

    public static Member makeMungerMethodSignature(JavaClass javaClass, Method method) {
        int mods = 0;
        if (method.isStatic()) mods = Modifier.STATIC;
        else if (javaClass.isInterface()) mods = Modifier.INTERFACE;
        else if (method.isPrivate()) mods = Modifier.PRIVATE;
        return Member.method(
            TypeX.forName(javaClass.getClassName()), mods, method.getName(), method.getSignature()); 
    }
    
    private static final String[] ZERO_STRINGS = new String[0];
    
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("BcelWorld(");
		//buf.append(shadowMungerMap);
		buf.append(")");
		return buf.toString();
	}

    public Advice concreteAdvice(
       	AjAttribute.AdviceAttribute attribute,
    	Pointcut pointcut,
        Member signature)
    {
    	//System.err.println("concrete advice: " + signature + " context " + sourceContext);
        return new BcelAdvice(attribute, pointcut, signature, null);
    }
    
    public ConcreteTypeMunger concreteTypeMunger(
        ResolvedTypeMunger munger, ResolvedTypeX aspectType) 
    {
        return new BcelTypeMunger(munger, aspectType);
    }
    
	public ConcreteTypeMunger makeCflowStackFieldAdder(ResolvedMember cflowField) {
		return new BcelCflowStackFieldAdder(cflowField);
	}

	public static BcelObjectType getBcelObjectType(ResolvedTypeX concreteAspect) {
		//XXX need error checking
		return (BcelObjectType) ((ResolvedTypeX.Name)concreteAspect).getDelegate();
	}

}
