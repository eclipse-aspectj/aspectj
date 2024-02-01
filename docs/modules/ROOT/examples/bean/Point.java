/*
Copyright (c) Xerox Corporation 1998-2002.  All rights reserved.

Use and copying of this software and preparation of derivative works based
upon this software are permitted.  Any distribution of this software or
derivative works must comply with all applicable United States export control
laws.

This software is made available AS IS, and Xerox Corporation makes no warranty
about the software, its performance or its conformity to any specification.
*/

package bean;

class Point {

    protected int x = 0;
    protected int y = 0;

    /**
     * Return the X coordinate
     */
    public int getX(){
        return x;
    }

    /**
     * Return the y coordinate
     */
    public int getY(){
        return y;
    }

    /**
     * Set the x and y coordinates
     */
    public void setRectangular(int newX, int newY){
        setX(newX);
        setY(newY);
    }

    /**
     * Set the X coordinate
     */
    public void setX(int newX) {
        x = newX;
    }

    /**
     * set the y coordinate
     */
    public void setY(int newY) {
        y = newY;
    }

    /**
     * Move the point by the specified x and y offset
     */
    public void offset(int deltaX, int deltaY){
        setRectangular(x + deltaX, y + deltaY);
    }

    /**
     * Make a string of this
     */
    public String toString(){
        return "(" + getX() + ", " + getY() + ")" ;
    }
}
