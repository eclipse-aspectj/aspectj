/*
 * DiffOutput.java
 * Copyright (c) 2001 Andre Kaplan
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/


package jdiff.util;

import java.io.IOException;
import java.io.Writer;
import java.io.OutputStreamWriter;



/** A base class for printing edit scripts produced by Diff. */
public abstract class DiffOutput {
    /** Set to the lines of the files being compared. */
    protected Object[] file0;
    protected Object[] file1;

    protected String lineSeparator;
    protected Writer out;


    protected DiffOutput(Object[] a,Object[] b) {
        this.lineSeparator = System.getProperty("java.line.separator");
        this.out = new OutputStreamWriter(System.out);
        this.file0 = a;
        this.file1 = b;
    }


    public void setOut(Writer out) {
        this.out = out;
    }


    public void setLineSeparator(String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }


    abstract public void writeScript(Diff.change script) throws IOException;


    protected void writeLine(String prefix, Object line) throws IOException {
        out.write(prefix + line.toString() + this.lineSeparator);
    }


    /** Write a pair of line numbers separated with sepChar.
        If the two numbers are identical, print just one number.

        Args a and b are internal line numbers (ranging from 0)
        We write the translated (real) line numbers ranging from 1).
    */
    protected void writeNumberRange(char sepChar, int a, int b)  throws IOException {
        /* Note: we can have b < a in the case of a range of no lines.
        In this case, we should write the line number before the range,
        which is b.  */
        if (++b > ++a) {
            out.write(Integer.toString(a));
            out.write(sepChar);
            out.write(Integer.toString(b));
        } else {
            out.write(Integer.toString(b));
        }
    }


    public static char changeLetter(int inserts, int deletes) {
        if (inserts == 0) {
            return 'd';
        } else if (deletes == 0) {
            return 'a';
        } else {
            return 'c';
        }
    }
}

