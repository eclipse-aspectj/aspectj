/*
 * Created on Sep 4, 2008
 */
package test1;

public aspect SelectActionAspect {
	
	private T SelectAction<T>.selected;
	
	public void SelectAction<T>.select(T object){
		this.selected = object;
	}

	public T SelectAction<T>.getSelected(){
		return selected;
	}

	public void SelectAction<T>.setSelected(T object){
		this.selected = object;
	}

	public void SelectAction<T>.deselect(){
		this.selected = null;
	}

}
