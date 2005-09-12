package com.designpattern.decorator;

public class Main {
	private static Order order;

	public static void main(String[] args) {
		order = new SalesOrder();
		order.print();

	}
}
