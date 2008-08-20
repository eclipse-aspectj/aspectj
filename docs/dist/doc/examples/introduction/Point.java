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

public class Point {

   protected double x = 0;
   protected double y = 0;
   protected double theta = 0;
   protected double rho = 0;

   protected boolean polar = true;
   protected boolean rectangular = true;

   public double getX(){
      makeRectangular();
      return x;
   }

   public double getY(){
      makeRectangular();
      return y;
   }

   public double getTheta(){
      makePolar();
      return theta;
   }

   public double getRho(){
      makePolar();
      return rho;
   }

   public void setRectangular(double newX, double newY){
      x = newX;
      y = newY;
      rectangular = true;
      polar = false;
   }

   public void setPolar(double newTheta, double newRho){
      theta = newTheta;
      rho = newRho;
      rectangular = false;
      polar = true;
   }

   public void rotate(double angle){
      setPolar(theta + angle, rho);
   }

   public void offset(double deltaX, double deltaY){
      setRectangular(x + deltaX, y + deltaY);
   }

   protected void makePolar(){
      if (!polar){
	 theta = Math.atan2(y,x);
	 rho = y / Math.sin(theta);
	 polar = true;
      }
   }

   protected void makeRectangular(){
      if (!rectangular) {
		 y = rho * Math.sin(theta);
		 x = rho * Math.cos(theta);
		 rectangular = true;
      }
   }

   public String toString(){
      return "(" + getX() + ", " + getY() + ")[" 
	 + getTheta() + " : " + getRho() + "]";
   }

   public static void main(String[] args){
      Point p1 = new Point();
      System.out.println("p1 =" + p1);
      p1.setRectangular(5,2);
      System.out.println("p1 =" + p1);
      p1.setPolar( Math.PI / 4.0 , 1.0);
      System.out.println("p1 =" + p1);
      p1.setPolar( 0.3805 , 5.385);
      System.out.println("p1 =" + p1);
   }
}
