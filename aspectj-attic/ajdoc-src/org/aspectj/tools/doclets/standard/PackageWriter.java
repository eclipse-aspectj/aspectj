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

public class PackageWriter
    extends com.sun.tools.doclets.standard.PackageWriter
{

    protected final PackageDoc packagedoc;

    public PackageWriter(String path,
                         String filename,
                         PackageDoc packagedoc,
                         PackageDoc prev,
                         PackageDoc next) 
        throws IOException, DocletAbortException {
        super(path, filename, packagedoc, prev, next);
        this.packagedoc = packagedoc;
    }

    public static void generate(PackageDoc pkg,
                                PackageDoc prev,
                                PackageDoc next) throws DocletAbortException {
        PackageWriter pw;
        String path = DirectoryManager.getDirectoryPath(pkg);
        String filename = "package-summary.html";
        try {
            (pw = new PackageWriter(path, filename, pkg, prev, next)).
                generatePackageFile();
            pw.close();
            pw.copyDocFiles(path);
        } catch (IOException e) {
            Standard.configuration().standardmessage.
                error("doclet.exception_encountered", e+"", filename);
            throw new DocletAbortException();
        }
    }

    protected void generateClassListing() {
        generateClassKindListing(packagedoc.interfaces(), 
                                 getText("doclet.Interface_Summary"));
        generateClassKindListing(Statics.classes(packagedoc.ordinaryClasses()),
                                   getText("doclet.Class_Summary"));
        generateClassKindListing(((org.aspectj.ajdoc.PackageDoc)packagedoc).aspects(),
                                 getText("doclet.Aspect_Summary"));
        generateClassKindListing(packagedoc.exceptions(),
                                 getText("doclet.Exception_Summary"));
        generateClassKindListing(packagedoc.errors(),
                                 getText("doclet.Error_Summary"));
    }
}



