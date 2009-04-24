package p;


public class AspectTargetClass
{
    @MonitorableMethod(ApiDescriptor.TARGET_CLASS_TARGET_METHOD)
    public void aspectTargetMethod()
    {
	System.out.println("In target method");
    }
}
