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

package org.aspectj.internal.tools.ant.taskdefs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Mkdir;

public class CopyAndInlineStylesheet extends Task {

    private File file;
    public void setFile(String file) {
        this.file = project.resolveFile(file);
    }

    private File todir;
    public void setTodir(String todir) {
        this.todir = project.resolveFile(todir);
    }
    

    public void execute() throws BuildException {
        try {
            if (todir == null) {
                throw new BuildException("must set 'todir' attribute");
            }
            if (file == null) {
                throw new BuildException("must set 'file' attribute");
            }
            log("copying html from" + file + " to " + todir.getAbsolutePath());
	    
            File toFile = new File(todir, file.getName());

            Mkdir mkdir = (Mkdir) project.createTask("mkdir");
            mkdir.setDir(todir);
            mkdir.execute();

            BufferedReader in = new BufferedReader(new FileReader(file));
            PrintStream out = new PrintStream(new FileOutputStream(toFile));

        outer:
            while (true) {
                String line = in.readLine();
                if (line == null) break;
                if (isStyleSheet(line)) {
                    doStyleSheet(line, out, file);
                    while (true) {
                        String line2 = in.readLine();
                        if (line2 == null) break outer;
                        out.println(line2);
                    }
                } else {
                    out.println(line);
                }
            }
            
            in.close();
            out.close();
        } catch (IOException e) {
            throw new BuildException(e.getMessage());
        }
    }

    private static void doStyleSheet(String line, PrintStream out, File file) throws IOException {
        int srcIndex = line.indexOf("href");
        int startQuotIndex = line.indexOf('"', srcIndex);
        int endQuotIndex = line.indexOf('"', startQuotIndex + 1);

        String stylesheetLocation = line.substring(startQuotIndex + 1, endQuotIndex);

        File styleSheetFile = new File(file.getParent(), stylesheetLocation);

        out.println("<style type=\"text/css\">");
        out.println("<!--");
        
        BufferedReader inStyle = new BufferedReader(new FileReader(styleSheetFile));

        while (true) {
            String line2 = inStyle.readLine();
            if (line2 == null) break;
            out.println(line2);
        }
        inStyle.close();

        out.println("-->");
        out.println("</style>");
    }


    private static boolean isStyleSheet(String line) throws IOException {
        line = line.toLowerCase();
        int len = line.length();
        int i = 0; 

        while (true) {
            if (i == len) return false;
            if (! Character.isWhitespace(line.charAt(i))) break;
        }

        return line.startsWith("<link", i);
    }
}
