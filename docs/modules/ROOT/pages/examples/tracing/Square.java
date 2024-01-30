/*

Copyright (c) Xerox Corporation 1998-2002.  All rights reserved.

Use and copying of this software and preparation of derivative works based
upon this software are permitted.  Any distribution of this software or
derivative works must comply with all applicable United States export control
laws.

This software is made available AS IS, and Xerox Corporation makes no warranty
about the software, its performance or its conformity to any specification.

|<---            this code is formatted to fit into 80 columns             --->|
|<---            this code is formatted to fit into 80 columns             --->|
|<---            this code is formatted to fit into 80 columns             --->|

*/

package tracing;

/**
 *
 * Square is a 2D shape. It extends the TwoDShape class with the side
 * variable, and it implements TwoDShape's abstract methods for 
 * correctly computing a square's area and distance.
 *
 */
public class Square extends TwoDShape {
    protected double s;    // side

    /*
     * All sorts of constructors
     */
    public Square(double x, double y, double s) {
        super(x, y); this.s = s;
    }

    public Square(double x, double y) {
        this(x, y, 1.0);
    }

    public Square(double s) {
        this(0.0, 0.0, s);
    }

    public Square() {
        this(0.0, 0.0, 1.0);
    }

    /**
     * Returns the perimeter of this square
     */
    public double perimeter() {
        return 4 * s;
    }

    /**
     * Returns the area of this square
     */
    public double area() {
        return s*s;
    }

    /**
     * This method overrides the one in the superclass. It adds some
     * circle-specific information.
     */
    public String toString() {
        return ("Square side = " + String.valueOf(s) + super.toString());
    }
}
