package package2;

import java.io.Serializable;

public class Bean implements Serializable{
	
	private String name;

	public String getName() {
		return name;
	}
	
	@propertyChanger()
	public void setName( String name ) {
		this.name = name;
	}
}