/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

 
package org.aspectj.internal.tools.build;

import java.io.File;
import java.util.Hashtable;

/** 
 * Registration and factory for modules 
 * @see Module
 * @see Builder
 */
public class Modules {
    
    private final Hashtable<String,Module> modules = new Hashtable<>();
    public final File baseDir;
    public final File jarDir;
    private final Messager handler;
    
    public Modules(File baseDir, File jarDir, Messager handler) {
        this.baseDir = baseDir;
        this.jarDir = jarDir;
        this.handler = handler;
        Util.iaxIfNotCanReadDir(baseDir, "baseDir");
        Util.iaxIfNotCanReadDir(jarDir, "jarDir");
        Util.iaxIfNull(handler, "handler");
    }
    
        
    /** 
     * Get module associated with name.
     * @return fail if unable to find or create module {name}.
     */
    public Module getModule(String name) {
        if (null == name) {
            return null;
        }
        Module result = (Module) modules.get(name);
        if (null == result) {
            File moduleDir = new File(baseDir, name);
            if (!Util.canReadDir(moduleDir)) {
                handler.error("not a module: " + name);
            } else {
                result = new Module(moduleDir, jarDir, name, this, handler);
                if (result.valid) {
                    modules.put(name, result);
                } else {
                    handler.error("invalid module: " + result.toLongString());
                }
            }         
        }
        return result;
    }
}