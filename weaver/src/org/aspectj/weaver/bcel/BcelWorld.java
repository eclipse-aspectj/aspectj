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
 *     Alexandre Vasseur    perClause support for @AJ aspects
 * ******************************************************************/


package org.aspectj.weaver.bcel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Iterator;

import org.aspectj.apache.bcel.classfile.ClassParser;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.generic.ConstantPoolGen;
import org.aspectj.apache.bcel.generic.FieldInstruction;
import org.aspectj.apache.bcel.generic.GETSTATIC;
import org.aspectj.apache.bcel.generic.INVOKEINTERFACE;
import org.aspectj.apache.bcel.generic.INVOKESPECIAL;
import org.aspectj.apache.bcel.generic.INVOKESTATIC;
import org.aspectj.apache.bcel.generic.InvokeInstruction;
import org.aspectj.apache.bcel.generic.PUTSTATIC;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.apache.bcel.util.ClassPath;
import org.aspectj.apache.bcel.util.Repository;
import org.aspectj.apache.bcel.util.ClassLoaderRepository;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.weaver.Advice;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.ICrossReferenceHandler;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.MemberImpl;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ReferenceTypeDelegate;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.FormalBinding;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.SimpleScope;
import org.aspectj.weaver.patterns.PerClause;

public class BcelWorld extends World implements Repository {
	private ClassPathManager classPath;

    private Repository delegate;

	//private ClassPathManager aspectPath = null;
	// private List aspectPathEntries;
	
    // ---- constructors

	public BcelWorld() {
		this("");		
	}
	
	public BcelWorld(String cp) {
		this(makeDefaultClasspath(cp), IMessageHandler.THROW, null);
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
	
	public BcelWorld(List classPath, IMessageHandler handler, ICrossReferenceHandler xrefHandler) {
		//this.aspectPath = new ClassPathManager(aspectPath, handler);
		this.classPath = new ClassPathManager(classPath, handler);
		setMessageHandler(handler);	
		setCrossReferenceHandler(xrefHandler);
		// Tell BCEL to use us for resolving any classes
        delegate = this;
		// TODO Alex do we need to call org.aspectj.apache.bcel.Repository.setRepository(delegate);
        // if so, how can that be safe in J2EE ?? (static stuff in Bcel)
	}
	
	public BcelWorld(ClassPathManager cpm, IMessageHandler handler, ICrossReferenceHandler xrefHandler) {
		this.classPath = cpm;
		setMessageHandler(handler);
		setCrossReferenceHandler(xrefHandler);
		// Tell BCEL to use us for resolving any classes
        delegate = this;
        // TODO Alex do we need to call org.aspectj.apache.bcel.Repository.setRepository(delegate);
        // if so, how can that be safe in J2EE ?? (static stuff in Bcel)
	}

    /**
     * Build a World from a ClassLoader, for LTW support
     *
     * @param loader
     * @param handler
     * @param xrefHandler
     */
    public BcelWorld(ClassLoader loader, IMessageHandler handler, ICrossReferenceHandler xrefHandler) {
        this.classPath = null;
        setMessageHandler(handler);
        setCrossReferenceHandler(xrefHandler);
        // Tell BCEL to use us for resolving any classes
        delegate = new ClassLoaderRepository(loader);
        // TODO Alex do we need to call org.aspectj.apache.bcel.Repository.setRepository(delegate);
        // if so, how can that be safe in J2EE ?? (static stuff in Bcel)
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
        Member m = MemberImpl.methodFromString(str.substring(i+2, str.length()).trim());

        // now, we resolve
        UnresolvedType[] types = m.getParameterTypes();
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

    public static Type makeBcelType(UnresolvedType type) {
        return Type.getType(type.getSignature());
    }

    static Type[] makeBcelTypes(UnresolvedType[] types) {
        Type[] ret = new Type[types.length];
        for (int i = 0, len = types.length; i < len; i++) {
            ret[i] = makeBcelType(types[i]);
        }
        return ret;
    }

    public static UnresolvedType fromBcel(Type t) {
        return UnresolvedType.forSignature(t.getSignature());
    }

    static UnresolvedType[] fromBcel(Type[] ts) {
        UnresolvedType[] ret = new UnresolvedType[ts.length];
        for (int i = 0, len = ts.length; i < len; i++) {
            ret[i] = fromBcel(ts[i]);
        }
        return ret;
    }
    
    public ResolvedType resolve(Type t) {
        return resolve(fromBcel(t));
    }       


	protected ReferenceTypeDelegate resolveDelegate(ReferenceType ty) {
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
	
	protected BcelObjectType makeBcelObjectType(ReferenceType resolvedTypeX, JavaClass jc, boolean exposedToWeaver) {
		BcelObjectType ret = new BcelObjectType(resolvedTypeX, jc, exposedToWeaver);
		return ret;
	}
	
	
	private JavaClass lookupJavaClass(ClassPathManager classPath, String name) {
        if (classPath == null) {
            try {
                return delegate.loadClass(name);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

		try {
	        ClassPathManager.ClassFile file = classPath.find(UnresolvedType.forName(name));
	        if (file == null) return null;
	        
	        ClassParser parser = new ClassParser(file.getInputStream(), file.getPath());
	        
	        JavaClass jc = parser.parse();
			file.close();
			return jc;
		} catch (IOException ioe) {
			return null;
		}
	}
	
	
	public BcelObjectType addSourceObjectType(JavaClass jc) {
		BcelObjectType ret = null;
		String signature = UnresolvedType.forName(jc.getClassName()).getSignature();
        ReferenceType nameTypeX = (ReferenceType)typeMap.get(signature);

        if (nameTypeX == null) {        	
		    if (jc.isGeneric()) {
		    	nameTypeX =  ReferenceType.fromTypeX(UnresolvedType.forRawTypeName(jc.getClassName()),this);
		        ret = makeBcelObjectType(nameTypeX, jc, true);
		    	ReferenceType genericRefType = new ReferenceType(
		    			UnresolvedType.forGenericTypeSignature(signature,ret.getDeclaredGenericSignature()),this);
				nameTypeX.setDelegate(ret);
		    	genericRefType.setDelegate(ret);
		    	nameTypeX.setGenericType(genericRefType);
		       	typeMap.put(signature, nameTypeX);
		    } else {
	        	nameTypeX = new ReferenceType(signature, this);
	            ret = makeBcelObjectType(nameTypeX, jc, true);
	           	typeMap.put(signature, nameTypeX);
		    }
        } else {
            ret = makeBcelObjectType(nameTypeX, jc, true);
        }
		return ret;
	}
	
	void deleteSourceObjectType(UnresolvedType ty) {
		typeMap.remove(ty.getSignature());
	}

    public static Member makeFieldJoinPointSignature(LazyClassGen cg, FieldInstruction fi) {
    	ConstantPoolGen cpg = cg.getConstantPoolGen();
        return            
            	  MemberImpl.field(
            			fi.getClassName(cpg),
            			(fi instanceof GETSTATIC || fi instanceof PUTSTATIC)
            			? Modifier.STATIC: 0, 
            			fi.getName(cpg),
            			fi.getSignature(cpg));
    }
	
//    public static Member makeFieldSetSignature(LazyClassGen cg, FieldInstruction fi) {
//    	ConstantPoolGen cpg = cg.getConstantPoolGen();
//        return 
//            MemberImpl.field(
//                fi.getClassName(cpg),
//                (fi instanceof GETSTATIC || fi instanceof PUTSTATIC)
//                ? Modifier.STATIC
//                : 0, 
//                fi.getName(cpg),
//                "(" + fi.getSignature(cpg) + ")" +fi.getSignature(cpg));
//    }

	public Member makeJoinPointSignature(LazyMethodGen mg) {
		return makeJoinPointSignatureFromMethod(mg, null);
	}

	
	public Member makeJoinPointSignatureFromMethod(LazyMethodGen mg, MemberImpl.Kind kind) {
		Member ret = mg.getMemberView();
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
	        return new ResolvedMemberImpl(kind,
	                UnresolvedType.forName(mg.getClassName()), 
	                mods,
	                fromBcel(mg.getReturnType()),
	                mg.getName(),
	                fromBcel(mg.getArgumentTypes())
	                );
		} else {
			return ret;
		}
        
    }

    public Member makeJoinPointSignatureForMethodInvocation(LazyClassGen cg, InvokeInstruction ii) {
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

        // in Java 1.4 and after, static method call of super class within subclass method appears
        // as declared by the subclass in the bytecode - but they are not
        // see #104212
        if (ii instanceof INVOKESTATIC) {
            ResolvedType appearsDeclaredBy = resolve(declaring);
            // look for the method there
            for (Iterator iterator = appearsDeclaredBy.getMethods(); iterator.hasNext();) {
                ResolvedMember method = (ResolvedMember) iterator.next();
                if (method.isStatic()) {
                    if (name.equals(method.getName()) && signature.equals(method.getSignature())) {
                        // we found it
                        declaring = method.getDeclaringType().getName();
                        break;
                    }
                }

            }
        }
        //FIXME if not found we ll end up again with the bug.. can this happen?

        return MemberImpl.method(UnresolvedType.forName(declaring), modifier, name, signature);
    }  

    public static Member makeMungerMethodSignature(JavaClass javaClass, Method method) {
        int mods = 0;
        if (method.isStatic()) mods = Modifier.STATIC;
        else if (javaClass.isInterface()) mods = Modifier.INTERFACE;
        else if (method.isPrivate()) mods = Modifier.PRIVATE;
        return MemberImpl.method(
            UnresolvedType.forName(javaClass.getClassName()), mods, method.getName(), method.getSignature()); 
    }
    
    private static final String[] ZERO_STRINGS = new String[0];
    
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("BcelWorld(");
		//buf.append(shadowMungerMap);
		buf.append(")");
		return buf.toString();
	}

    public Advice createAdviceMunger(
       	AjAttribute.AdviceAttribute attribute,
    	Pointcut pointcut,
        Member signature)
    {
    	//System.err.println("concrete advice: " + signature + " context " + sourceContext);
        return new BcelAdvice(attribute, pointcut, signature, null);
    }
    
    public ConcreteTypeMunger concreteTypeMunger(
        ResolvedTypeMunger munger, ResolvedType aspectType) 
    {
        return new BcelTypeMunger(munger, aspectType);
    }
    
	public ConcreteTypeMunger makeCflowStackFieldAdder(ResolvedMember cflowField) {
		return new BcelCflowStackFieldAdder(cflowField);
	}
	
	public ConcreteTypeMunger makeCflowCounterFieldAdder(ResolvedMember cflowField) {
		return new BcelCflowCounterFieldAdder(cflowField);
	}

    /**
     * Register a munger for perclause @AJ aspect so that we add aspectOf(..) to them as needed
     *
     * @param aspect
     * @param kind
     * @return
     */
    public ConcreteTypeMunger makePerClauseAspect(ResolvedType aspect, PerClause.Kind kind) {
        return new BcelPerClauseAspectAdder(aspect, kind);
    }

	public static BcelObjectType getBcelObjectType(ResolvedType concreteAspect) {
		//XXX need error checking
		return (BcelObjectType) ((ReferenceType)concreteAspect).getDelegate();
	}

	public void tidyUp() {
	    // At end of compile, close any open files so deletion of those archives is possible
		classPath.closeArchives();
	}

	
	/// The repository interface methods

	public JavaClass findClass(String className) {
		return lookupJavaClass(classPath,className);
	}

	public JavaClass loadClass(String className) throws ClassNotFoundException {
		return lookupJavaClass(classPath,className);
	}

	public void storeClass(JavaClass clazz) {
		throw new RuntimeException("Not implemented");
	}

	public void removeClass(JavaClass clazz) {
		throw new RuntimeException("Not implemented");
	}

	public JavaClass loadClass(Class clazz) throws ClassNotFoundException {
		throw new RuntimeException("Not implemented");
	}

	public void clear() {
		throw new RuntimeException("Not implemented");
	}

}
