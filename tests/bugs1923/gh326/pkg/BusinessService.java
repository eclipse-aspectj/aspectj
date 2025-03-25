package pkg;

public class BusinessService {

	@HandleSourceException(message="42")
	public BusinessDto doSomething() throws TargetException {
		return new BusinessDao<BusinessDto>().doSomething();
	}


public static void main(String []argv) throws TargetException {
try {
	new BusinessService().doSomething();
} catch (TargetException te) {
	System.out.println(te.getMessage());
}
}
}
