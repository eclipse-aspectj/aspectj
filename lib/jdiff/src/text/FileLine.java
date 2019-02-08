/*
 * FileLine.java
 * Copyright (c) 2001 Andre Kaplan, original version (c) 2000 by Mike Dillon
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


package jdiff.text;


public class FileLine
{
    private String original;
    private String canonical;
    private int cachedHashCode;


    public FileLine(String original, String canonical) {
        this.original  = original;
        this.canonical = canonical;
    }


    public String getOriginalString() {
        return this.original;
    }


    public int hashCode() {
        if (this.cachedHashCode == 0) {
            this.cachedHashCode = this.canonical.hashCode();
        }

        return this.cachedHashCode;
    }


    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null) { return false; }
        if (!(o instanceof FileLine)) {return false; }
        if (this.canonical == ((FileLine) o).canonical) { return true; }

        return this.canonical.equals(((FileLine) o).canonical);
    }


    public String toString() {
        return this.original;
    }
}
