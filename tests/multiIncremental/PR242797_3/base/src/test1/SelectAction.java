/*
 * Created on Mar 7, 2008
 */
package test1;

public interface SelectAction<T>{

	public void select(T object);

	public T getSelected();

	public void setSelected(T object);

	public void deselect();

}