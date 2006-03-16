import java.util.List;

aspect Slide74 {
	
	public X Bar<X>.getFirst() {
		return lts.get(0);
    }

	<T> Foo<T>.new(List<T> elements) { this(); }
	
	private List<C> Bar<C>.children;// = new ArrayList<C>();
	
    static class Bar<T> {
        List<T> lts;
    }   
    
}

class Foo<T> {
	
}
