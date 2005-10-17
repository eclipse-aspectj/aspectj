public interface GenericInterface<O> {

    public O doSomething(Class<? extends O> type);
	
}
