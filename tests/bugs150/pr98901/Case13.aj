// "public abstract method on the aspect that declares @method on it"

import java.lang.annotation.*;
import java.lang.reflect.Method;

@Retention(RetentionPolicy.RUNTIME)
@interface anInterface{}

abstract aspect A13{
	public abstract void a();
	declare @method : abstract void A13.a(..) : @anInterface;
}

aspect A13Concrete extends A13 {
	public void a() {};
}

aspect B13 {
	
	public static void main(String [] args){
		Class c = A13.class;
		try {
			Method m = c.getDeclaredMethod("a", new Class [0]);
			Annotation [] anns = m.getDeclaredAnnotations();
			for (int i = 0;i < anns.length;i++){
				System.out.println(anns[i]);
			}
		} catch (Exception e){
			System.out.println("exceptional!");
		}
	}
}
