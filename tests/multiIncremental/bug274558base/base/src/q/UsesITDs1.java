package q;

import p.HasITDs1;
import r.HasITDs2;

public class UsesITDs1 {

	void nothing() {
		new HasITDs1().x++; 
		new HasITDs2().x++;
		new HasITDs1().nothing(1, 1, 1);
		new HasITDs2().nothing(1, 1, 1);
	}
	
}