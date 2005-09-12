/**
 * 
 */
package com.designpattern.decorator;

public abstract aspect OrderDecorator
{
    protected pointcut print(Order order) : target(order) && call(public void print());

    declare parents : SalesOrder extends Order ;

    public void SalesOrder.print()
    {
        super.print();
    }

}
