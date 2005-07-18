// "public abstract ITD method onto an Interface with declare @method"

import java.lang.annotation.*;
import java.lang.reflect.Method;

@Retention(RetentionPolicy.RUNTIME)
@interface anInterface{}

interface A21 {
}

aspect B21 {
	
	public abstract void A21.a();
	declare @method : abstract void A21.a(..) : @anInterface;
	
	public static void main(String [] args){
		Class c = A21.class;
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
