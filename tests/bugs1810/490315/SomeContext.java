package test;

public class SomeContext
{
	public SomeContext getPermissionDetails()
	{
		return this;
	}
	
    public boolean checkAccess(String tag, SomeEnum accessType) {
    	return false;
    }
}
