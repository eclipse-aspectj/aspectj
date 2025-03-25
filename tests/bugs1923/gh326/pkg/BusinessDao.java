package pkg;

public class BusinessDao<D> {

	public D doSomething() throws SourceException {
		return (D) new BusinessDto();
	}

}
