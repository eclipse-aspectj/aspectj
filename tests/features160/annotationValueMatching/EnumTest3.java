package a;

import p.*;

public aspect EnumTest3 {
	public static void main(String[] argv) {
		
	}
	@q.r.Fruity(q.r.Fruit.APPLE) public void m() {}
	@q.r.Fruity(q.r.Fruit.BANANA) public void n() {}
	@q.r.Fruity public void o() {}
	
	before(): execution(@q.r.Fruity(q.r.Fruit.APPLE) * *(..)) {}; // static import of fruits
}