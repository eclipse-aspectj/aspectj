
public class X implements SelectAction<Long, User>{
	public void setSelected(User user){
		//overriden version
	}
	public static void main(String[] args){
		new X().setSelectedId(1l);
	}
}

interface SelectAction<I, T> {
	public void setSelectedId(I id);
	public void setSelected(T object);
}

aspect SelectActionAspect {
	public void SelectAction<I, T>.setSelected(T object){
		//do nothing
	}
	public void SelectAction<I, T>.setSelectedId(I id){
		setSelected(null);
	}
}

class User {}
