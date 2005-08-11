// "public abstract ITD-on-itself method with declare @method"

import java.lang.annotation.*;
import java.lang.reflect.Method;

@Retention(RetentionPolicy.RUNTIME)
@interface anInterface{}

abstract aspect A17 {
	public abstract void A17.a();
	declare @method : abstract void A17.a(..) : @anInterface;
}

aspect A17Sub extends A17 {
	public void a() {}
}

aspect B17 {
	
	public static void main(String [] args){
		Class c = A17.class;
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
