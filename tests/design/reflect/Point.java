package org.aspectj.examples;

public class Point {
    public int x, y;

    public Point(int _x, int _y) { this.x = _x; this.y = _y; }

    public void move(int dx, int dy) { x += dx; y += dy; }
}

