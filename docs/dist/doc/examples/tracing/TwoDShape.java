/*
Copyright (c) Xerox Corporation 1998-2002.  All rights reserved.

Use and copying of this software and preparation of derivative works based
upon this software are permitted.  Any distribution of this software or
derivative works must comply with all applicable United States export control
laws.

This software is made available AS IS, and Xerox Corporation makes no warranty
about the software, its performance or its conformity to any specification.
*/

package tracing;

/**
 * TwoDShape is an abstract class that defines generic functionality
 * for 2D shapes.
 */
public abstract class TwoDShape {
    /**
     * Coordinates of the center of the shape.
     */
    protected double x, y;

    protected TwoDShape(double x, double y) {
        this.x = x; this.y = y;
    }

    /**
     * Returns the x coordinate of the shape.
     */
    public double getX() { return x; }

    /**
     * Returns the y coordinate of the shape.
     */
    public double getY() { return y; }

    /**
     * Returns the distance between this shape and the shape given as
     * parameter.
     */
    public double distance(TwoDShape s) {
        double dx = Math.abs(s.getX() - x);
        double dy = Math.abs(s.getY() - y);
        return Math.sqrt(dx*dx + dy*dy);
    }

    /**
     * Returns the perimeter of this shape. Must be defined in
     * subclasses.
     */
    public abstract double perimeter();

    /**
     * Returns the area of this shape. Must be defined in
     * subclasses.
     */
    public abstract double area();

    /**
     * Returns a string representation of 2D shapes -- simply its
     * coordinates.
     */
    public String toString() {
        return (" @ (" + String.valueOf(x) + ", " + String.valueOf(y) + ") ");
    }
}

