package generics;

import java.util.List;


public aspect DeleteActionAspect {

    public void DeleteAction<T extends Object>.delete() {
            Object selected = getSelected();
            selected.toString();
            delete3.add("");
    } 
	 
	public int DeleteAction<T extends Object>.delete2;
	
	public List<String> DeleteAction.delete3;
	
	
	public static void main(String[] args) {
		DeleteAction<String> d = new DeleteAction<String>() {
			public String getSelected() {
				throw new RuntimeException();
			}
			
		};
		d.delete2++; 
		d.delete3.add(null);
	}

} 