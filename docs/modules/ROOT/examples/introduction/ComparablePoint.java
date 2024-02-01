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

public aspect ComparablePoint {

   declare parents: Point implements Comparable;

   public int Point.compareTo(Object o) {
      return (int) (this.getRho() - ((Point)o).getRho());
   }

   public static void main(String[] args){
      Point p1 = new Point();
      Point p2 = new Point();

      System.out.println("p1 =?= p2 :" + p1.compareTo(p2));

      p1.setRectangular(2,5);
      p2.setRectangular(2,5);
      System.out.println("p1 =?= p2 :" + p1.compareTo(p2));

      p2.setRectangular(3,6);
      System.out.println("p1 =?= p2 :" + p1.compareTo(p2));

      p1.setPolar(Math.PI, 4);
      p2.setPolar(Math.PI, 4);
      System.out.println("p1 =?= p2 :" + p1.compareTo(p2));

      p1.rotate(Math.PI / 4.0);
      System.out.println("p1 =?= p2 :" + p1.compareTo(p2));

      p1.offset(1,1);
      System.out.println("p1 =?= p2 :" + p1.compareTo(p2));
   }
}
