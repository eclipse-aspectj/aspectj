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
 * A main function for testing 2D shapes.
 *
 */
public class ExampleMain {
    public static void main(String[] args) {
	Circle c1 = new Circle(3.0, 3.0, 2.0);
	Circle c2 = new Circle(4.0);

	Square s1 = new Square(1.0, 2.0);

	System.out.println("c1.perimeter() = " + c1.perimeter());
	System.out.println("c1.area() = " + c1.area());

	System.out.println("s1.perimeter() = " + s1.perimeter());
	System.out.println("s1.area() = " + s1.area());

	System.out.println("c2.distance(c1) = " + c2.distance(c1));
	System.out.println("s1.distance(c1) = " + s1.distance(c1));

	System.out.println("s1.toString(): " + s1.toString());
    }
}
