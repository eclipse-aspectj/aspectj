package client;
import lib.AbstractA;

aspect MyAspect extends AbstractA {
	protected pointcut scope();
}