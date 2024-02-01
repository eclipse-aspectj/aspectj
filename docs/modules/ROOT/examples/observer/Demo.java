/*
Copyright (c) Xerox Corporation 1998-2002.  All rights reserved.

Use and copying of this software and preparation of derivative works based
upon this software are permitted.  Any distribution of this software or
derivative works must comply with all applicable United States export control
laws.

This software is made available AS IS, and Xerox Corporation makes no warranty
about the software, its performance or its conformity to any specification.
*/

package observer;

public class Demo {
    public static void main(String[] args) {

        Display display = new Display();
        Button b1 = new Button(display);
        Button b2 = new Button(display);
        ColorLabel c1 = new ColorLabel(display);
        ColorLabel c2 = new ColorLabel(display);
        ColorLabel c3 = new ColorLabel(display);

        b1.addObserver(c1);
        b1.addObserver(c2);
        b2.addObserver(c3);
    }
}
