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

import com.sun.javadoc.PackageDoc;
import com.sun.tools.doclets.DirectoryManager;
import com.sun.tools.doclets.DocletAbortException;

import java.io.IOException;

public class PackageFrameWriter
    extends com.sun.tools.doclets.standard.PackageFrameWriter
{

    protected final PackageDoc packagedoc;

    public PackageFrameWriter(String path,
                              String filename, 
                              PackageDoc packagedoc) 
        throws IOException, DocletAbortException {
        super(path, filename, packagedoc);
        this.packagedoc = packagedoc;
    }

    public static void generate(PackageDoc pkg) throws DocletAbortException {
        PackageFrameWriter pw = null;
        String path = DirectoryManager.getDirectoryPath(pkg);
        String filename = "package-frame" + ".html";
        try {
            (pw = new PackageFrameWriter(path, filename, pkg)).
                generatePackageFile();
        } catch (IOException e) {
            Standard.configuration().standardmessage.
                    error("doclet.exception_encountered", 
                           e+"", filename);
            throw new DocletAbortException();
        } finally {
            if (pw != null) pw.close();
        }
    }

    protected void generateClassListing() {
        generateClassKindListing(packagedoc.interfaces(), 
                                 getText("doclet.Interfaces"));
        generateClassKindListing(Statics.classes(packagedoc.
                                                 ordinaryClasses()),
                                 getText("doclet.Classes"));
        generateClassKindListing(((org.aspectj.ajdoc.PackageDoc)packagedoc).
                                 aspects(),
                                 getText("doclet.Aspects"));
        generateClassKindListing(packagedoc.exceptions(),
                                 getText("doclet.Exceptions"));
        generateClassKindListing(packagedoc.errors(),
                                 getText("doclet.Errors"));
    }
}
