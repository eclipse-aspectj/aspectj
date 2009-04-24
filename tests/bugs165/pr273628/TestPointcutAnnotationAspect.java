package p;

public class TestPointcutAnnotationAspect
{
    public static void main(String[] args)
    {
	AspectTargetClass c = new AspectTargetClass();
	try
	{
	    c.aspectTargetMethod();
	}
	catch ( Exception e )
	{
	    e.printStackTrace();
	}
//	System.exit(0);
    }
}
