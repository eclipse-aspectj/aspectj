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
 * Circle is a 2D shape. It extends the TwoDShape class with the radius
 * variable, and it implements TwoDShape's abstract methods for 
 * correctly computing a circle's area and distance.
 *
 */
public class Circle extends TwoDShape {
    protected double r;    // radius

    /*
     * All sorts of constructors
     */
    public Circle(double x, double y, double r) {
        super(x, y); this.r = r;
    }

    public Circle(double x, double y) {
        this(x, y, 1.0);
    }

    public Circle(double r) {
        this(0.0, 0.0, r);
    }

    public Circle() {
        this(0.0, 0.0, 1.0);
    }

    /**
     * Returns the perimeter of this circle
     */
    public double perimeter() {
        return 2 * Math.PI * r;
    }

    /**
     * Returns the area of this circle
     */
    public double area() {
        return Math.PI * r*r;
    }

    /**
     * This method overrides the one in the superclass. It adds some
     * circle-specific information.
     */
    public String toString() {
        return ("Circle radius = " + String.valueOf(r) + super.toString());
    }
}
