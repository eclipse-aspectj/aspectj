import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.EventObject;
import java.util.Vector;

public class InterfaceAndObject {
    Timer t = null;
    public static void main(String[] args) {
        I i = new I() {};
        System.out.println(i);
        //new I() {}.toString();
    }
}

class Root {
    public String toString() { return "root"; }
}

class C extends Root implements I {}

class C1 extends Root implements I1 {
    public Object clone() { return null; }
}

interface I0 {
    public Object clone();
}

interface I {
    //public void toString();
    public boolean equals(Object o);
}


interface I1 extends I, I0 {
    //public Object clone();
}
