package client;
import lib.AbstractA;

aspect MyAspect1 extends AbstractA {
	protected pointcut scope(): within(client.*);
}