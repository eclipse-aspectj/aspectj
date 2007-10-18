package test;

import java.util.ArrayList;
import java.util.List;

public aspect ListFactoryAspect {

	private ListFactory listFactory = new ListFactory() {
		public <T> List<T> createList() {
			return new ArrayList<T>();
		};
		public <T> List<T> createList(int initialCapacity) {
			return new ArrayList<T>();
		};
	};
	
	declare parents: Processor implements ListFactoryConsumer;
	
	public ListFactory ListFactoryConsumer.getListFactory() {
		return ListFactoryAspect.aspectOf().listFactory;
	}
	
	public <T> List<T> ListFactoryConsumer.createList() {
		return getListFactory().<T>createList();
	}
	
	public <T> List<T> ListFactoryConsumer.createList(int initialCapacity) {
		return getListFactory().<T>createList(initialCapacity);
	}
}
