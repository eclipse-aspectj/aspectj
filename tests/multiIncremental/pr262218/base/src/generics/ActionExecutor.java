package generics;

public class ActionExecutor {
	public static void main(String[] args) {
		DeleteAction<String> d = new DeleteAction<String>() {
			public String getSelected() {
				throw new RuntimeException();
			}
			
		};
		d.delete2++; 
		d.delete3.add(null);
	}
	
	void nothing2(DeleteAction<String> g) {
		g.delete2++;
	}
} 