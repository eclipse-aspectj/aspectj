/* -*- Mode: JDE; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This file is part of the debugger and core tools for the AspectJ(tm)
 * programming language; see http://aspectj.org
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * either http://www.mozilla.org/MPL/ or http://aspectj.org/MPL/.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is AspectJ.
 *
 * The Initial Developer of the Original Code is Xerox Corporation. Portions
 * created by Xerox Corporation are Copyright (C) 1999-2002 Xerox Corporation.
 * All Rights Reserved.
 */
package org.aspectj.tools.ajdoc;

import org.aspectj.ajdoc.IntroducedSuperDoc;
import org.aspectj.ajdoc.PointcutDoc;
import org.aspectj.compiler.base.ast.ClassDec;
import org.aspectj.compiler.base.ast.CodeDec;
import org.aspectj.compiler.base.ast.CompilationUnit;
import org.aspectj.compiler.base.ast.Constructor;
import org.aspectj.compiler.base.ast.ConstructorDec;
import org.aspectj.compiler.base.ast.Dec;
import org.aspectj.compiler.base.ast.Field;
import org.aspectj.compiler.base.ast.FieldDec;
import org.aspectj.compiler.base.ast.Import;
import org.aspectj.compiler.base.ast.Imports;
import org.aspectj.compiler.base.ast.InterfaceDec;
import org.aspectj.compiler.base.ast.Method;
import org.aspectj.compiler.base.ast.NameType;
import org.aspectj.compiler.base.ast.SourceLocation;
import org.aspectj.compiler.base.ast.TextSourceLocation;
import org.aspectj.compiler.base.ast.Type;
import org.aspectj.compiler.base.ast.TypeDec;
import org.aspectj.compiler.crosscuts.AspectJCompiler;
import org.aspectj.compiler.crosscuts.ast.AspectDec;
import org.aspectj.compiler.crosscuts.ast.IntroducedSuperDec;
import org.aspectj.compiler.crosscuts.ast.PointcutSO;

import org.aspectj.ajdoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This implements ClassDoc and acts as a factory for ClassDocImpl
 * and associated DocImpl.
 * The factory will excluded new superclasses and introduction classes
 * so the caller should take care to include those classes explicitly
 * by adding them directly before they are sought indirectly or
 * by post-processing them to enable inclusion as appropriate.
 */
public class ClassDocImpl
    extends ProgramElementDocImpl
    implements org.aspectj.ajdoc.ClassDoc {

    /**
     * Returns an instance of ClassDoc represented by
     * the passed in TypeDec.
     *
     * @param typeDec instance of TypeDec representing
     *                the ClassDoc that will be returned.
     * @return        an instance of ClassDoc mirroring
     *                the passed in TypeDec.
     */    
    public final static ClassDocImpl getInstance(TypeDec typeDec) {
        return factory.getInstance(typeDec);
    }

    /**
     * Returns an instance of ClassDoc represented by
     * the passed in TypeDec and containing ClassDoc (may be null).
     *
     * @param outerDoc the containing ClassDoc -- may be null.
     * @param typeDec  instance of TypeDec representing
     *                 the ClassDoc that will be returned.
     * @return         an instance of ClassDoc mirroring
     *                 the passed in TypeDec.
     */  
    public final static ClassDocImpl getInstance(ClassDoc outerDoc, TypeDec typeDec) {
        return factory.getInstance(outerDoc, typeDec);
    }

    /**
     * Returns the known ClassDocImpl for a given String --
     * the returned value may be null.
     *
     * @return the known ClassDocImpl for a given String --
     *         the returned value may be null.
     */
    public final static ClassDocImpl find(String qualifiedName) {
        return factory.find(qualifiedName);
    }

    /**The factory used to create instances of this class. */
    private final static Factory factory = new Factory();

    /** The ClassDec to which is delegated. */
    private final TypeDec typeDec;

    // todo: we know these Collections are FilteredDecList, so declare that?
    /** The introductions that affect a ClassDoc. */
    private Collection introducers;

    /** The array of fields visible in this type. */
    private Collection fieldDocs;

    /** The array of methods visible in this type. */
    private Collection methodDocs;

    /** The array of constructors visible in this type. 
      * The implementation must support iterator().remove().
      */
    private Collection constructorDocs;

    /** The array of inner classes visible in thie type. */
    private Collection innerclassDocs;

    /** The array of interfaces this type implements. */
    private Collection interfaceDocs;

    /** The array of classes this type imports on demand. */
    private Collection importedClasses;

    /** The array of package this type imports on demand. */
    private Collection importedPackages;

    /** The array of pointcuts visible in this type. */
    private Collection pointcutDocs;

    /** cached variant of <code>((AjdocCompiler)ajc()).getFilter()</code> */
    private AccessChecker filter;
    
    /**
     * Constructs a representation of an AspectJ-compiled class
     * using the underlying TypeDec and containning ClassDoc.
     * NOTE: This is protected (and maybe should be private)
     * because the static method {@link #getInstance(TypeDec)}
     * should always be used to get instances of this type.  It
     * ensures that enclosing types are created before their
     * enclosed types.
     *
     * @param containingClass ClassDoc that encloses this.
     * @param typeDec         The underlying TypeDec.
     */
    protected ClassDocImpl(com.sun.javadoc.ClassDoc containingClass,
                           TypeDec typeDec) {
        super(containingClass);
        this.typeDec = typeDec;
        //AccessChecker check = getFilter();
        // RootDocImpl sets inclusion of world classes
        setIncluded(false); 
        // have to install before creating imports to avoid cycles
        factory.put(this, typeDec);
        createImports(importedClasses  = new ArrayList(),
                      importedPackages = new ArrayList());
    }

    /**
     * Maps Decs to their counterpart by testing with
     * <code>instanceof</code>
     *
     * @return a MemberDocImpl that has an underlying Dec dec.
     */
    public MemberDocImpl docForDec(Dec dec) {
        if (dec instanceof FieldDec) {
            return docForDec((FieldDec)dec);
        }
        if (dec instanceof CodeDec) {
            return docForDec((CodeDec)dec);
        }
        // todo: map for inner classes, Type, etc?
        return null; //TODO error ???
    }

    /**
     * Returns a FieldDocImpl that has an underlying FieldDec dec.
     *
     * @return a FieldDocImpl that has an underlying FieldDec dec.
     */
    public FieldDocImpl docForDec(FieldDec dec) {
        FieldDoc[] fs = fields();
        for (int i = 0; i < fs.length; i++) {
            FieldDocImpl fd = (FieldDocImpl)fs[i];
            if (fd.dec() == dec) return fd;
        }
        return null;
    }

    /**
     * Returns a CodeDocImpl that has an underlying CodeDec dec.
     *
     * @return a CodeDocImpl that has an underlying CodeDec dec.
     */
    public CodeDocImpl docForDec(CodeDec dec) {
        MethodDoc[] ms = methods();
        for (int i = 0; i < ms.length; i++) {
            CodeDocImpl cd = (CodeDocImpl)ms[i];
            if (cd.dec() == dec) return cd;
        }
        ConstructorDoc[] cs = constructors();
        for (int i = 0; i < cs.length; i++) {
            CodeDocImpl cd = (CodeDocImpl)cs[i];
            if (cd.dec() == dec) return cd;
        }
        return null;
    }

    /**
     * @todo ??????
     */
    public TypeDec nonNullTypeDec() {
        if (typeDec().getLexicalType() == null) return typeDec();
        return super.nonNullTypeDec();
    }

    /**
     * Returns a Collection of ClassDocImpls that have corresponding
     * ClassDecs declared within classDec().
     *
     * @return a Collection of ClassDocImpls that have corresponding
     *         ClassDecs declared within classDec().
     */
    private Collection createInnerTypes() {
        Collection items = ((NameType)typeDec.getType()).getInnerTypes();
        FilteredDecList result = 
            new FilteredDecList(getFilter(), this);
        if (items != null) {
            for (Iterator i = items.iterator(); i.hasNext();) {
                result.add(((NameType)i.next()).getTypeDec());
            }
        }
        Collections.sort(result);
        return result;
    }

    /**
     * Creates the two collection of imports -- class and packages --
     * used by the file in which classDec() was declared.
     *
     * @param importClasses the classes to fill.
     * @param importPkgs    the packages to fill.
     */
    private void createImports(final Collection importClasses,
                               final Collection importPkgs) {
        CompilationUnit cu = typeDec.getCompilationUnit();
        if (cu != null) {
            Imports imports = cu.getImports();
            if (imports != null) {
                for (int i = 0; i < imports.size(); i++) {
                    Import imprt = imports.get(i);
                    if (imprt != null) {
                        if (imprt.getStar()) {
                            PackageDoc importedPkg =
                                PackageDocImpl.getPackageDoc
                                (imprt.getName());
                            if (importedPkg != null) {
                                importPkgs.add(importedPkg);
                            }
                        } else {
                            com.sun.javadoc.ClassDoc importedClass =
                                findClass(imprt.getName());
                            if (importedClass != null) {
                                importClasses.add(importedClass);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns a Collection of ClassDocImpl representing the
     * interfaces the underlying TypeDec implements.
     *
     * @return a Collection of ClassDocImpl representing the
     *         interfaces the underlying TypeDec implements.
     */
    private Collection createInterfaces() {
        //NameType type = (NameType)typeDec.getType();
        Collection items = typeDec.getSuperInterfaceTypes();
        FilteredDecList result = 
            new FilteredDecList(getFilter(), this);
        if (items != null) {
            for (Iterator i = items.iterator(); i.hasNext();) {
                result.add(((NameType)i.next()).getTypeDec());
            }
        }
        Collections.sort(result);
        return result;
    }

    /** 
     * Called by AjdocCompiler to do pruning once the whole world is created.
     * This avoids using aspect before initialized when 
     * this classdoc constructed as a result of aspect
     * initialization.
     */
    void postProcess() {
        // prune compiler-generated default constructor if unadvised.
        // can't do this on construction since advising aspect may exist but be uninitialized
        final SourceLocation parentLoc = dec().getSourceLocation();
        final int parentLine = (null == parentLoc ? -1 : parentLoc.getBeginLine());
        final int parentColumn = (null == parentLoc ? -1 : parentLoc.getBeginColumn());
        if (null == constructorDocs) {
            if (null == constructors()) {
                // XXX harmless error System.err.println("Unable to post-process");
                return; 
            }
        }
        try {
            //ArrayList removed = new ArrayList();
            for (Iterator i = constructorDocs.iterator(); i.hasNext(); ) {
                ConstructorDocImpl cdi = (ConstructorDocImpl) i.next();
                CodeDec cd = cdi.codeDec();
                SourceLocation sl = (null == cd ? null : cd.getSourceLocation());
                // if ajc changes so typedec start/end not equal to constructor, then can't recognize
                if ((null != sl) 
                    && (parentColumn == sl.getBeginColumn())
                    && (parentLine == sl.getBeginLine())) {
                    Object[] advice = cdi.advice();
                    if ((null != advice) && (1 > advice.length)) {
                        i.remove();                    
                        //removed.add(cdi);
                        //System.err.println("removing unadvised generated constructor: " + cdi);
                    } else {
                        //System.err.println("keeping advised generated constructor: " + cdi);
                    }
                } else {
                     //System.err.println("keeping ungenerated constructor: " + cdi);
                }
            }
//            for (Iterator i = removed.iterator(); i.hasNext();) {
//                Object dec = i.next();
//				if (constructorDocs.contains(dec)) {
//                    throw new Error("remove failed for " + dec);
//                }				
//			}
        } catch (UnsupportedOperationException e) {
            System.err.println("Warning: ClassDocImpl.constructorDocs not removable");
        }
    }
    
    /**
     * Returns a Collection of ConstructorDocImpl representing the
     * constructors the underlying TypeDec declares.
     *
     * @return a Collection of ConstructorDocImpl representing the
     *         constructors the underlying TypeDec declares.
     */
    private Collection createConstructors() {
        NameType type = (NameType)typeDec.getType();
        Collection items = type.getConstructors();
        final SourceLocation parentLoc = dec().getSourceLocation();
        final int parentLine = (null == parentLoc ? -1 : parentLoc.getBeginLine());
        final int parentColumn = (null == parentLoc ? -1 : parentLoc.getBeginColumn());
        FilteredDecList result = 
            new FilteredDecList(getFilter(), this);
        if (items != null) {
            for (Iterator i = items.iterator(); i.hasNext();) {
                Constructor c = (Constructor) i.next();
                ConstructorDec cd = c.getConstructorDec();
                ConstructorDocImpl impl = new ConstructorDocImpl(this, cd);
                // XXX workaround for ajc bug of default constructor source location
                SourceLocation sl = (null == cd ? null : cd.getSourceLocation());
                // if line/column starts the same, then a generated constructor
                if ((null != sl) 
                    && (parentColumn == sl.getBeginColumn())
                    && (parentLine == sl.getBeginLine())) {
                        // use source location clone without comment from class                        
                        TextSourceLocation tsl = new TextSourceLocation(sl.getCompilationUnit(),
                            sl.getStartPosition(), sl.getEndPosition());
                        tsl.clearComment();
                        cd.setSourceLocation(tsl);
                } 
                result.add(impl);
            }
        }
        Collections.sort(result);
        return result;
    }
    
    /**
     * Returns a Collection of PointcutDocImpl representing the
     * pointcuts the underlying TypeDec declares.
     *
     * @return a Collection of PointcutDocImpl representing the
     *         pointcuts the underlying TypeDec declares.
     */
    private Collection createPointcuts() {
        NameType type = (NameType)typeDec.getType();
        Collection items = type.getPointcuts();
        FilteredDecList result = 
            new FilteredDecList(getFilter(), this);
        if (items != null) {
            for (Iterator i = items.iterator(); i.hasNext();) {
                result.add(((PointcutSO)i.next()).getPointcutDec());
            }
        }
        Collections.sort(result);
        return result;
    }

    /**
     * Returns a Collection of MethodDocImpl representing the
     * methods the underlying TypeDec declares.
     *
     * @return a Collection of MethodDocImpl representing the
     *         methods the underlying TypeDec declares.
     */
    private Collection createMethods() {
        NameType type = (NameType)typeDec.getType();
        Collection methods = type.getMethods();
        FilteredDecList result = 
            new FilteredDecList(getFilter(), this);
        if (methods != null) {
            for (Iterator i = methods.iterator(); i.hasNext();) {
                result.add(((Method)i.next()).getMethodDec());
            }
        }
        Collections.sort(result);
        return result;
    }

    /**
     * Returns a Collection of FieldDocImpl representing the
     * fields the underlying TypeDec declares.
     *
     * @return a Collection of FieldDocImpl representing the
     *         fields the underlying TypeDec declares.
     */
    private Collection createFields() {
        NameType type = (NameType)typeDec.getType();
        Collection fields = type.getFields();
        FilteredDecList result = 
            new FilteredDecList(getFilter(), this);
        if (fields != null) {
            for (Iterator i = fields.iterator(); i.hasNext();) {
                result.add(((Field)i.next()).getFieldDec());
            }
        }
        Collections.sort(result);
        return result;
    }

    /** return (and cache) filter from ajc() */
    protected AccessChecker getFilter() {
        // nb: called during construction
        if (null == filter) {
            AspectJCompiler ajc = ajc();
            if (ajc instanceof AjdocCompiler) {
                filter = ((AjdocCompiler) ajc).getFilter();
            }
        }
        return filter; // still may be null
    }

    /**
     * Returns the underlying Dec -- a TypeDec.
     *
     * @return the underlying Dec -- a TypeDec.
     */
    protected final Dec dec() {
        return typeDec();
    }
    
    /**
     * Returns the underlying TypeDec.
     *
     * @return the underlying TypeDec.
     */
    public final TypeDec typeDec() {
        return typeDec;
    }

    /**
     * Returns the fully-qualified name of this TypeDec including
     * the package and any enclosing classes.
     *
     * @return the fully-qualified name of this TypeDec including
     *         the package and any inclosing classes.
     */
    public String qualifiedName() {
        return qualifiedTypeName().replace('$','.');
    }

    /**
     * Returns the fully-qualfied name of this class.
     *
     * @return the fully-qualfied name of this class.
     */
    public String toString() {
        return qualifiedName();
    }

    /**
     * Returns the single name of this TypeDec excluding the package
     * but including enclosing classes.  NOTE:  All dollar signs
     * are replaced by periods.
     *
     * @return the single name of this TypeDec excluding the package
     *         but including enclosing classes.
     */
    public String name() {
        return ((NameType)typeDec().getType()).
            getExtendedId().replace('$','.');
    }

    /**
     * Returns the class specified by <code>classname</code>
     * from the context of <code>this</code>. This method may return
     * <code>null</code> denoting the class wasn't found.
     * Search proceeds in the following order:
     * 
     * <ul><ll>
     * <li>qualified name</li>
     * <li>in this class (inner)</li>
     * <li>in this package</li>
     * <li>in the class imports</li>
     * <li>in the package imports</li>
     * </ll></ul>
     *
     * @return the type specified by <code>classname</code>
     *         from the context of <code>this</code>.
     * @see    Util#findClass(ClassDoc,String,JavaCompiler)
     * @see    <a href="http://java.sun.com/products/jdk/1.2/docs/tooldocs">
     *         Javadoc Tool Homepage</a>
     */
    public com.sun.javadoc.ClassDoc findClass(String classname) {
        // Sanity check
        if (classname == null || classname.length() < 1) {
            return null;
        }

        // The result
        com.sun.javadoc.ClassDoc desired;

        // [0] The trivial case, the classname is this class
        if (classname.equals(name()) ||
            classname.equals(qualifiedName())) {
            return this;
        }

        // [1] Look up the fully qualified name.
        if ((desired = ClassDocImpl.find(classname)) != null) {
            return desired;
        }

        // [2] Search the inner classes.  We can assume that if
        // classname refers to an inner class it is unqualified
        // with respect to its package, because step [1] would have
        // picked it up, then.  First look to see if the name
        // matches, then search the inner class itself.  We check two
        // values:
        //  [1] innername: the unqualified inner class name
        //  [2] classname: the qualified (with outer class) name
        //  Example:
        //           /**
        //            * @see Inner
        //            *   // classname == Inner              (f'cked)
        //            *   // innername == Outer.Inner          (ok)
        //            * @see Outer.Inner
        //            *   // classname == Outer.Inner          (ok)
        //            *   // innername == Outer.Outer.Inner  (f'cked)
        //           class Outer {
        //             static class Inner {}
        //           }
        String innername = name() + '.' + classname;
        com.sun.javadoc.ClassDoc[] inners = innerClasses();
        if (inners != null) {
            for (int i = 0; i < inners.length; i++) {
                if (classname.equals(inners[i].name()) ||
                    innername.equals(inners[i].name())) {
                    return inners[i];
                }
            }
        }

        // [3] Search in this package
        if ((desired = containingPackage().findClass(classname)) != null) {
            return desired;
        }

        // [4] Search the class imports. The order for this is specified
        // by the compiler -- if you don't believe me read for yourself:
        // http://java.sun.com/products/jdk/1.2/docs/tooldocs/win32/ (cont't)
        // javadoc.html#seesearchorder
        // We don't look in other package, so we assume classname
        // is full package-qualified.
        com.sun.javadoc.ClassDoc[] imports = importedClasses();
        if (imports != null) {
            for (int i = 0; i < imports.length; i++) {
                if (classname.equals(imports[i].name())) {
                    return imports[i];
                }
            }
        }

        // [5] Search the package imports for the fully-qualified name.
        PackageDoc[] pkgs = importedPackages();
        if (pkgs != null) {
            for (int i = 0; i < pkgs.length; i++) {
                if ((desired = pkgs[i].findClass(classname)) != null) {
                    return desired;
                }
            }
        }

        // [5 1/2] OK, I lied above, we do search a couple packages,
        // it should be java.lang, but we're aspectj, so we'll look in
        // org.aspectj.lang, too.  We assume the names are package-unqualified
        // in this step.
        // TODO: check that this is made final, if not, make it static
        String[] pkgnames = {"java.lang", "org.aspectj.lang"};
        for (int i = 0; i < pkgnames.length; i++) {
            PackageDoc pkg = PackageDocImpl.getPackageDoc(pkgnames[i]);
            if (pkg != null) {
                if ((desired = pkg.findClass(classname)) != null) {
                    return desired;
                }
            }
        }
        // Found nothing.
        return null;
    }

    /**
     * Returns the fields visible in this type.
     *
     * @return an array of FieldDoc representing
     *         the fields visible in this type.
     */
    public FieldDoc[] fields() {
        if (fieldDocs == null) {
            fieldDocs = createFields();
        }
        return (FieldDoc[])fieldDocs.toArray
            (new org.aspectj.ajdoc.FieldDoc[fieldDocs.size()]);
    }

    /**
     * Returns <code>true</code> is this type is externalizable.
     *
     * @return <code>true</code> is this type is externalizable.
     */
    public boolean isExternalizable() {
        return false; //TODO
    }

    /**
     * Returns <code>true</code> if this type is serializable.
     *
     * @return <code>true</code> if this type is serializable.
     */
    public boolean isSerializable() {
        for (Iterator i = typeDec().getSuperInterfaceTypes().iterator();
             i.hasNext();) {
            if (((Type)i.next()).getId().equals("java.io.Serializable")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the methods visible in this type.
     *
     * @return an array of MethodDoc representing the
     *         methods visible in this type.
     */
    public MethodDoc[] methods() {
        if (methodDocs == null) {
            methodDocs = createMethods();
        }
        return (MethodDoc[])methodDocs.toArray
            (new org.aspectj.ajdoc.MethodDoc[methodDocs.size()]);
    }

    /**
     * Returns the serializable methods visible in this type.
     *
     * @return an array of MethodDoc representing the
     *         serializable methods visible in this type.
     */
    public MethodDoc[] serializationMethods() {
        List ser = new ArrayList();
        MethodDoc[] mds = methods();
        for (int i = 0, N = mds.length; i < N; i++) {
            if (mds[i].tags("@serialized").length > 1) ser.add(mds[i]);
        }
        return (MethodDoc[])ser.toArray(new MethodDoc[ser.size()]);
    }

    /**
     * Returns the serializable fields visible in this type.
     *
     * @return an array of MethodDoc representing the
     *         serializable fields visible in this type.
     */
    public FieldDoc[] serializableFields() {
        List ser = new ArrayList();
        FieldDoc[] fds = fields();
        for (int i = 0, N = fds.length; i < N; i++) {
            if (fds[i].serialFieldTags().length > 1) ser.add(fds[i]);
        }
        return (FieldDoc[])ser.toArray(new FieldDoc[ser.size()]);
    }

    /**
     * Returns <code>true</code> is this type contains
     * visible serializable fields.
     *
     * @return <code>true</code> is this type contains
     *         visible serializable fields.
     */
    public boolean definesSerializableFields() {
        return serializableFields().length > 0;
    }

    /**
     * Returns the super type of this type.  The return value
     * is guaranteed to be non-null unless this represents
     * java.lang.Object.
     *
     * @return a ClassDoc representing the super type of this type.
     *         or null if this represents java.lang.Object.
     */
    public com.sun.javadoc.ClassDoc superclass() {
        if ("java.lang.Object".equals(qualifiedTypeName())) {
            return null;
        } else {
            TypeDec superType = typeDec().getSuperClassType().getTypeDec();
            return ClassDocImpl.getInstance(superType);
        }
    }

    /**
     * Returns <code>true</code> is <code>c</code> is a
     * subtype of <code>this</code>.
     *
     * @return  <code>true</code> is <code>c</code> is a
     *          subtype of <code>this</code>.
     */
    public boolean subclassOf(com.sun.javadoc.ClassDoc c) {
        return c != null && c.equals(superclass());
    }

    /**
     * Returns the interfaces this type implements.
     *
     * @return an array of ClassDoc representing the
     *         interfaces this type implements.
     */
    public com.sun.javadoc.ClassDoc[] interfaces() {
        if (interfaceDocs == null) {
            interfaceDocs = createInterfaces();
        }
        return (ClassDoc[])interfaceDocs.toArray
            (new org.aspectj.ajdoc.ClassDoc[interfaceDocs.size()]);
    }

    /**
     * Returns the constructors visible in this type.
     *
     * @return an array of ConstructorDoc representing the
     *         visible constructors in this type.
     */
    public ConstructorDoc[] constructors() {
        if (constructorDocs == null) {
            constructorDocs = createConstructors();
        }
        return (ConstructorDoc[])constructorDocs.toArray
            (new org.aspectj.ajdoc.ConstructorDoc[constructorDocs.size()]);
    }

    /**
     * Returns the inner class visible in this type.
     *
     * @return an array of ClassDoc representing the inner
     *         classes visible in this type.
     */
    public com.sun.javadoc.ClassDoc[] innerClasses() {
        if (innerclassDocs == null) {
            innerclassDocs = createInnerTypes();
        }
        final int size = innerclassDocs.size();
        return (ClassDoc[])innerclassDocs.toArray
            (new org.aspectj.ajdoc.ClassDoc[size]);
    }

        /**
     * Returns the types imported on demand by this type.
     *
     * @return an array of ClassDoc representing the
     *         types imported on demand by this type.
     */
    public com.sun.javadoc.ClassDoc[] importedClasses() {
        return (ClassDoc[])importedClasses.toArray
            (new org.aspectj.ajdoc.ClassDoc[importedClasses.size()]);
    }

    /**
     * Returns the packages imported on demand by this type.
     *
     * @return an array of PackageDoc representing the
     *         packages imported on demand by this type.
     */
    public PackageDoc[] importedPackages() {
        return (PackageDoc[])importedPackages.toArray
            (new org.aspectj.ajdoc.PackageDoc[importedPackages.size()]);
    }

    /**
     * Returns the pointcuts visible in this type.
     *
     * @return an array of PointcutDoc representing the
     *         pointcuts visible in this type.
     */
    public PointcutDoc[] pointcuts() {
        if (pointcutDocs == null) {
            pointcutDocs = createPointcuts();
        }
        return (PointcutDoc[])pointcutDocs.toArray
            (new PointcutDoc[pointcutDocs.size()]);
    }

    /**
     * Returns <code>true</code> is this type is <code>abstract</code>.
     *
     * @return <code>true</code> is this type is <code>abstract</code>.
     */
    public boolean isAbstract() {
        return typeDec().isAbstract();
    }

    /**
     * Returns <code>true</code> is this type is as exception.
     *
     * @return <code>true</code> is this type is an instance
     *         of java.lang.Exception.
     */
    public boolean isException() {
        //TODO: make lazy
        for (com.sun.javadoc.ClassDoc superclass = superclass();
             superclass != null &&
                 !superclass.qualifiedTypeName().equals("java.lang.Object");
             superclass = superclass.superclass()) {
            if (superclass.qualifiedTypeName().equals("java.lang.Exception")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns <code>true</code> is this type is an error.
     *
     * @return <code>true</code> is this type is an instance
     *         of java.lang.Error.
     */
    public boolean isError() {
        //TODO: make lazy
        for (com.sun.javadoc.ClassDoc superclass = superclass();
             superclass != null &&
                 !superclass.qualifiedTypeName().equals("java.lang.Object");
             superclass = superclass.superclass()) {
            if (superclass.qualifiedTypeName().equals("java.lang.Error")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the introductions affecting a ClassDoc's
     * type hierarchy.
     *
     * @return an array of IntroducedSuperDoc representing the
     *         introductions of this ClassDoc that affect its
     *         type hierarchy.
     */
    public IntroducedSuperDoc[] introducers() {
        if (introducers == null) {
            introducers = createIntroducers();
        }
        return (IntroducedSuperDoc[])introducers.toArray
            (new IntroducedSuperDoc[introducers.size()]);
    }
    
    /**
     * Returns <code>true</code> is this is an <code>interface</code>.
     *
     * @return <code>true</code> is this is an <code>interface</code>.
     */
    public boolean isInterface() {
        return typeDec() instanceof InterfaceDec;
    }

    /**
     * Returns <code>true</code> is this is a <code>class</code>.
     *
     * @return <code>true</code> is this is a <code>class</code>.
     */
    public boolean isClass() {
        return typeDec() instanceof ClassDec;
    }

    /**
     * Returns <code>true</code> is this is an <code>aspect</code>.
     *
     * @return <code>true</code> is this is an <code>aspect</code>.
     */
    public boolean isAspect() {
        return typeDec() instanceof AspectDec;
    }

    /**
     * Returns <code>true</code> is this class is neither
     * an error nor an exception, but this still could be
     * an aspect.
     *
     * @return <code>true</code> is this class is neither
     *         an error nor an exception, but this still could be
     *         an aspect.
     */
    public boolean isOrdinaryClass() {
        return isClass() && !(isError() || isException());
    }

    /**
     * Returns int modifiers with the 'interface' bit set.
     *
     * @return  int modifiers with the 'interface' bit set.
     * @see     java.lang.reflect.Modifier.
     */
    public int modifierSpecifier() {
        return super.modifierSpecifier()
            |  (isInterface() ? Modifier.INTERFACE : 0);
    }

    
    /* ------------------------------------------------------------
     * Implementation of Type
     * ------------------------------------------------------------
     */

    /**
     * Returns the declaration of this type -- null if
     * this isn't included.
     *
     * @return the ClassDoc represented by this Type or null
     *         if this isn't included.
     */
    public com.sun.javadoc.ClassDoc asClassDoc() {
        return isIncluded() ? this : null;
    }

    /**
     * Returns this type's dimension information as a String.
     *
     * @return this type's dimension information as a String.
     */
    public String dimension() {
        return "";
    }

    /**
     * Returns qualified name of type excluding
     * any dimension information.
     *
     * @return qualified name of type excluding
     *         any dimension information.
     */
    public String qualifiedTypeName() {
        return typeDec().getFullName().replace('$', '.');
    }

    /**
     * Returns unqualified name of type excluding
     * any dimension information.
     *
     * @return unqualified name of type excluding
     *         any dimension information.
     */
    public String typeName() {
        return typeDec().getId().replace('$', '.');
    }

    /**
     * Returns the Collection of IntroducedSuperDec that
     * introduce a type intro this's type hierarchy.  At the
     * same time, the method makes sure <code>this</code> is
     * added to every IntroducedSuperDec's list of targets.
     *
     * @return Collection of IntroducedSuperDec that
     *         introduce a type intro this's type hierarchy.
     */
    private Collection createIntroducers() {
        Set affectedBy = ajc().getCorrespondences().getAffectedBy(typeDec);
        if (affectedBy.size() < 1) {
            return Collections.EMPTY_LIST;
        }
        Collection list = new ArrayList();
        for (Iterator i = affectedBy.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof IntroducedSuperDec) {
                IntroducedSuperDec dec = (IntroducedSuperDec)o;
                TypeDec owner = ((NameType)dec.getDeclaringType()).getTypeDec();
                AspectDocImpl ad = (AspectDocImpl)ClassDocImpl.getInstance(owner);
                IntroducedSuperDocImpl id = (IntroducedSuperDocImpl)ad.introDocForDec(dec);
                list.add(id);
                id.addTarget(this);
                
            }
        }
        return list;
    }

    
    /* ------------------------------------------------------------
     * Factory instantiation
     * ------------------------------------------------------------
     */

    /**
     * Inner class in charge of creating instances of ClassDocImpl.
     */
    private final static class Factory {

        private final Map typeDecsToClassDocs = new HashMap();
        private final Map qualifiedNamesToClassDocs = new HashMap();

        public final ClassDocImpl find(String qualifiedName) {
            return (ClassDocImpl)qualifiedNamesToClassDocs.get(qualifiedName);
        }

        public final ClassDocImpl getInstance(TypeDec typeDec) {
            if (typeDec == null) return null;
            ClassDocImpl outerDoc = getInstance(typeDec.getEnclosingTypeDec());

            return getInstance(outerDoc, typeDec);
        }
        public final ClassDocImpl getInstance(ClassDoc outerDoc, 
                                              TypeDec typeDec) {
            if (typeDec == null) return null;
            ClassDocImpl cd = (ClassDocImpl)typeDecsToClassDocs.get(typeDec);
            if (cd == null) {
                cd = makeInstance(outerDoc, typeDec);
                /*
                Object o = typeDecsToClassDocs.put(typeDec, cd);
                if (null != o) {
                    throw new Error("new " + cd + " displaced " + o + " for " + typeDec);
                }
                */
            }
            return cd;
        }
        private final ClassDocImpl makeInstance(ClassDoc outerDoc, 
                                                TypeDec typeDec) {
            ClassDocImpl result = null;
            if (typeDec instanceof AspectDec) {
                result = new AspectDocImpl(outerDoc, (AspectDec)typeDec);
            } else {
                result = new ClassDocImpl(outerDoc, typeDec);
            }
            if (null == result.containingPackage()) {
                System.err.println("Warning: unable to add " 
                                   + result + " to package");
            }
            return result;
        }

        /** 
         * constructor installs itself here before generating imports.
         * fyi: not Thread-safe since available from factory before 
         * construction completes
         * @return object displaced, if any - error if not null
         */
        private final Object put(ClassDocImpl classdoc, TypeDec typeDec) {
            Object result  = typeDecsToClassDocs.put(typeDec, classdoc);
            if (null == result) {
                result = qualifiedNamesToClassDocs.put(classdoc.qualifiedName(), classdoc);
            }
            return result;
        }
    } // factory
}
