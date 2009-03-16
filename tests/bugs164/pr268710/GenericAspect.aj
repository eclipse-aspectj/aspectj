/**
 * 
 */
package none;

/**
 * @author Dawid Pytel
 * 
 */
public abstract aspect GenericAspect<T> {

	interface SomeInterface {
	}

	pointcut SomeConstructor(SomeInterface var) : execution(SomeInterface+.new(..)) && this(var);
}
