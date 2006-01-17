package com.test;

public aspect OptionAspect {
	   interface IBadOptionSubtype {};

	    declare parents : (IOption+ && !IOption && !IXOption && !IYOption && !IZOption) && !hasmethod(new(OptionType))
	                             implements IBadOptionSubtype;

	    declare error : staticinitialization(IOption+ && IBadOptionSubtype+)
	        : "IOption implementations must provide a constructor which accepts an OptionType";
}
