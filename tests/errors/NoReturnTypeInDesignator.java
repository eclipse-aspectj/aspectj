
// PR#130

public aspect NoReturnTypeInDesignator
{
    static after(): this(Point) && call(!static *(..)) { 
            System.out.println( "after" );
    }

}       

class Point {}
