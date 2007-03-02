package test;

import java.util.List;

public interface ListFactory {

	<T> List<T> createList();
	<T> List<T> createList(int initialCapacity);
}
