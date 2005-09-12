/**
 * 
 */
package com.designpattern.decorator;

public aspect HeaderDecorator extends OrderDecorator 
{
	
	void around(Order order) : print(order)
	{
		printHeader(order);
		proceed(order);
	}

	private void printHeader(Order order)
	{
		System.out.println("XYZ Incorporated\nDate of Sale:");
	}
}
