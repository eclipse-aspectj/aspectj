/*
 * Created on 22.10.2004
 */

/**
 * @author Thomas Knauth
 */
public class PR79554
{
    public static void main(String[] args)
    {
        try
        {
	    	if ( args.length < 0 )
	    	{
	    		System.out.println("Usage!");
	    		return;
	    	}

	    	throw new Exception();
        }
        catch ( Throwable e )
        {
            System.out.println( "exception caught!" );
            //e.printStackTrace();
        }
        finally
        {
        	System.out.println("finally block entered!");
        }
    }
}

aspect Aspect {

	pointcut main(): execution(void main(String[]));
  
	after(): main(){
	  System.out.println("Aspect calling after main!");
	}
}
