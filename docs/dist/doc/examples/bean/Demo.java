/*
Copyright (c) Xerox Corporation 1998-2002.  All rights reserved.

Use and copying of this software and preparation of derivative works based
upon this software are permitted.  Any distribution of this software or
derivative works must comply with all applicable United States export control
laws.
*/

package bean;

import java.beans.*;
import java.io.*;

public class Demo implements PropertyChangeListener {

    static final String fileName = "test.tmp";

    /**
     * when Demo is playing the listener role,
     * this method reports that a propery has changed
     */
    public void propertyChange(PropertyChangeEvent e){
        System.out.println("Property " + e.getPropertyName() + " changed from " +
                           e.getOldValue() + " to " + e.getNewValue() );
    }

    /**
     * main: test the program
     */
    public static void main(String[] args){
        Point p1 = new Point();
        p1.addPropertyChangeListener(new Demo());
        System.out.println("p1 =" + p1);
        p1.setRectangular(5,2);
        System.out.println("p1 =" + p1);
        p1.setX( 6 );
        p1.setY( 3 );
        System.out.println("p1 =" + p1);
        p1.offset(6,4);
        System.out.println("p1 =" + p1);
        save(p1, fileName);
        Point p2 = (Point) restore(fileName);
        System.out.println("Had: " + p1);
        System.out.println("Got: " + p2);
    }

    /**
     * Save a serializable object to a file
     */
    static void save(Serializable p, String fn){
        try {
            System.out.println("Writing to file: " + p);
            FileOutputStream fo = new FileOutputStream(fn);
            ObjectOutputStream so = new ObjectOutputStream(fo);
            so.writeObject(p);
            so.flush();
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
    }

    /**
     * Restore a serializable object from the file
     */
    static Object restore(String fn){
        try {
            Object result;
            System.out.println("Reading from file: " + fn);
            FileInputStream fi = new FileInputStream(fn);
            ObjectInputStream si = new ObjectInputStream(fi);
            return si.readObject();
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
        return null;
    }
}
