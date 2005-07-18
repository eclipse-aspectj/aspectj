// "public abstract ITD method with declare @method"

import java.lang.annotation.*;
import java.lang.reflect.Method;

@Retention(RetentionPolicy.RUNTIME)
@interface anInterface{}

abstract class A15{
}

aspect B15 {
	
	public abstract void A15.a();
	declare @method : abstract void A15.a(..) : @anInterface;
	
	public static void main(String [] args){
		Class c = A15.class;
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
