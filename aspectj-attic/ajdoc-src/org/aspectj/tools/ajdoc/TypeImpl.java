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

import org.aspectj.compiler.base.ast.ArrayType;
import org.aspectj.compiler.base.ast.NameType;
import org.aspectj.compiler.base.ast.PrimitiveType;
import org.aspectj.compiler.base.ast.Type;

import com.sun.javadoc.ClassDoc;

public class TypeImpl { //implements org.aspectj.ajdoc.Type {

    public static com.sun.javadoc.Type getInstance(Type type) {
        return factory.getInstance(type);
    }

    public static com.sun.javadoc.Type getInstance(String spec,
                                                   ClassDoc where) {
        return factory.getInstance(spec, where);
    }

    private final static class Primitive implements org.aspectj.ajdoc.Type {



        public final static Primitive getInstance(PrimitiveType type) {
            return getInstance(type.getName());
        }
        
        public final static Primitive getInstance(String name) {
            if ("void".equals(name))     return    voidType;
            if ("boolean".equals(name))  return booleanType;
            if ("byte".equals(name))     return    byteType;
            if ("char".equals(name))     return    charType;
            if ("short".equals(name))    return   shortType;
            if ("int".equals(name))      return     intType;
            if ("long".equals(name))     return    longType;
            if ("float".equals(name))    return   floatType;
            if ("double".equals(name))   return  doubleType;
            return null;
        }

        public final static boolean isPrimitive(String name) {
            return name.equals("boolean")
                || name.equals("byte")
                || name.equals("char")
                || name.equals("short")
                || name.equals("int")
                || name.equals("long")
                || name.equals("float")
                || name.equals("double");
        }
        
        private final String name;
        private Primitive(String name) { this.name = name; }
        
        public String          toString() { return name; }
        public String          typeName() { return name; }
        public String qualifiedTypeName() { return name; }
        public String         dimension() { return   ""; }
        public ClassDoc      asClassDoc() { return null; }
        
        private final static Primitive    voidType = new Primitive("void");
        private final static Primitive booleanType = new Primitive("boolean");
        private final static Primitive    byteType = new Primitive("byte");
        private final static Primitive    charType = new Primitive("char");
        private final static Primitive   shortType = new Primitive("short");
        private final static Primitive     intType = new Primitive("int");
        private final static Primitive    longType = new Primitive("long");
        private final static Primitive   floatType = new Primitive("float");
        private final static Primitive  doubleType = new Primitive("double");
    }

    private final static class Array implements org.aspectj.ajdoc.Type {
        protected final com.sun.javadoc.Type type;
        protected final int dimension;
        private Array(com.sun.javadoc.Type type, int dimension) {
            this.type = type;
            //TODO: tune this later
            this.dimension = dimension;
        }

        public String          toString() { return type.toString();          }
        public String          typeName() { return type.typeName();          }
        public String qualifiedTypeName() { return type.qualifiedTypeName(); }
        public ClassDoc      asClassDoc() { return type.asClassDoc();        }
        public String         dimension() {
            String str = "";
            for (int i = 0; i < dimension; i++) str += "[]";
            return str;
        }

        public boolean equals(Object other) {
            if (!(other instanceof Array)) {
                return super.equals(other);
            }
            Array array = (Array)other;
            return array.type.equals(type)
                && array.dimension == dimension;
        }
    }
        

    private static final Factory factory = new Factory();
    private final static class Factory {

        private com.sun.javadoc.Type getInstance(Type type) {
            if (type instanceof PrimitiveType) {
                return Primitive.getInstance((PrimitiveType)type);
            } else if (type instanceof ArrayType) {
                ArrayType arrayType = (ArrayType)type;
                Type component = arrayType.getComponentType();
                while (component instanceof ArrayType) {
                    component = ((ArrayType)component).getComponentType();
                }
                return new Array(getInstance(component),
                                 arrayType.getArrayDimCount());
            } else {
                return ClassDocImpl.getInstance(((NameType)type).getTypeDec());
            }
        }

        private com.sun.javadoc.Type getInstance(String spec,
                                                 ClassDoc where) {
            int ibracket = spec.indexOf('[');
            String name;
            int dimension;
            if (ibracket != -1) {
                name = spec.substring(0, ibracket);
                dimension = spec.substring(ibracket+1).length()/2;
            } else {
                name = spec;
                dimension = 0;
            }
            com.sun.javadoc.Type type = Primitive.getInstance(name);
            if (type == null) {
                type = where.findClass(name); //TODO
            }
            if (dimension > 0) {
                type = new Array(type, dimension);
            }
            return type;
        }
    }
}
