/*

Copyright (c) Xerox Corporation 1998-2002.  All rights reserved.

Use and copying of this software and preparation of derivative works based
upon this software are permitted.  Any distribution of this software or
derivative works must comply with all applicable United States export control
laws.

This software is made available AS IS, and Xerox Corporation makes no warranty
about the software, its performance or its conformity to any specification.

*/
package introduction;

public aspect CloneablePoint {

   declare parents: Point implements Cloneable;

   public Object Point.clone() throws CloneNotSupportedException {
      // we choose to bring all fields up to date before cloning.
      makeRectangular();
      makePolar();
      return super.clone();
   }

   public static void main(String[] args){
      Point p1 = new Point();
      Point p2 = null;

      p1.setPolar(Math.PI, 1.0);
      try {
         p2 = (Point)p1.clone();
      } catch (CloneNotSupportedException e) {}
      System.out.println("p1 =" + p1 );
      System.out.println("p2 =" + p2 );

      p1.rotate(Math.PI / -2);
      System.out.println("p1 =" + p1 );
      System.out.println("p2 =" + p2 );
   }
}
