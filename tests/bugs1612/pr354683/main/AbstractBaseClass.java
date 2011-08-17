package main;

import util.DerivedCommonDataInterfaceImpl;


/**
* NOTE: for iajc failure to occur this class has to be 
* 1st - parametrized
* 2nd - abstract
* whether or not its subclasses specifie the type parameter does not matter
*/
public abstract class AbstractBaseClass <T extends Whatever> implements DerivedCommonDataInterfaceImpl {}
