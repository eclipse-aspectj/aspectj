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

package org.aspectj.tools.doclets.standard;

import com.sun.javadoc.RootDoc;
import com.sun.tools.doclets.MessageRetriever;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * A customized configuration.
 *
 * @author Jeff Palm
 */
public class ConfigurationStandard
    extends com.sun.tools.doclets.standard.ConfigurationStandard
{
    
    /** It true we don't print crosscut information. */
    public boolean nocrosscuts = false;

    /** If true we don't print crosscut summary information. */
    public boolean nosummarycrosscuts = false;

    /** If true we log each pass in the doclet. */
    public boolean log = false;
    
    public ConfigurationStandard() {
//        standardmessage = new MessageRetriever
//            ("org.aspectj.tools.doclets.standard.resources.standard");

        String loc = "org.aspectj.tools.doclets.standard.resources.standard";
        final ClassLoader loader = getClass().getClassLoader();
        // XXX move persistant resource loader to util
        ResourceBundle bundle = null;
        for (int i = 0; ((null == bundle) && (i < 4)); i++) {
			
            try {
                switch (i) {
                    case 0: 
                        bundle = ResourceBundle.getBundle(loc);
                        standardmessage = new MessageRetriever(bundle);
                    break;
                    case 1: 
                        Locale locale = Locale.getDefault();            
                        bundle = ResourceBundle.getBundle(loc, locale, loader);
                        standardmessage = new MessageRetriever(bundle);
                    break;
                    case 2: 
                        standardmessage = new MessageRetriever(loc);
                    break;
                    case 3:
                        URL pURL = loader.getResource(loc + ".properties");
                        bundle = new PropertyResourceBundle(pURL.openStream());
                        standardmessage = new MessageRetriever(loc);
                    break;
                }
                break; // from for loop
            } catch (MissingResourceException e) { } // error below
            catch (IOException ie) { } // error below
        }
        if (null == bundle) {
            throw new Error("unable to load resource: " +   loc);
        }
    }
    
    //TODO: Document the new options in help

    public void setSpecificDocletOptions(RootDoc root) {
        String[][] options = root.options();
        for (int i = 0; i < options.length; ++i) {
            String opt = options[i][0].toLowerCase();
            if (opt.equals("-nocrosscuts")) {
                nocrosscuts = true;
                nosummarycrosscuts = true;
            } else if (opt.equals("-nosummarycrosscuts")) {
                nosummarycrosscuts = true;
            } else if (opt.equals("-log")) {
                log = true;
            }
        }
        super.setSpecificDocletOptions(root);
    }

    public int specificDocletOptionLength(String opt) {
        if (opt.equals("-nocrosscuts") ||
            opt.equals("-nosummarycrosscuts") ||
            opt.equals("-log")) {
            return 1;
        }
        return super.specificDocletOptionLength(opt);
    }
}
        

