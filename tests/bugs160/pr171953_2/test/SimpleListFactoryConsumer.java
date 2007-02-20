package test;

import java.util.ArrayList;
import java.util.List;

public class SimpleListFactoryConsumer extends AbstractProcessor {

	public void run() {
		//List<List<String>> list1 = getListFactory().createList();
		List<List<String>> list2 = this.createList();
	}
	
	public static void main(String[] args) {
		new SimpleListFactoryConsumer().run();
	}
}
