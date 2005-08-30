package package1;

import java.io.Serializable;

public class Bean implements Serializable{
	
	private String name;

	public String getName() {
		return name;
	}
	
	public void setName( String name ) {
		this.name = name;
	}
}