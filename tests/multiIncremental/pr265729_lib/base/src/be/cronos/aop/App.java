package be.cronos.aop;

import be.cronos.aop.InterTypeAspectSupport;

@InterTypeAspectSupport
public class App 
{
    public static void main( String[] args )
    {
        //System.out.println( "Hello World!" ); //should throw compiler error, OK
	App app = new App();
	app.foo(42,null,null);

	
    }
}
