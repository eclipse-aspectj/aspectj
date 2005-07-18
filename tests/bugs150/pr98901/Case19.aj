//"public abstract method on an Interface with declare @method"

import java.lang.annotation.*;
import java.lang.reflect.Method;

@Retention(RetentionPolicy.RUNTIME)
@interface anInterface{}

interface A19 {
	public abstract void a();
}

aspect B19 {
	
	declare @method : abstract void A19.a(..) : @anInterface;
	
	public static void main(String [] args){
		Class c = A19.class;
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
