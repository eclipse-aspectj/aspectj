// "public ITD method onto an Interface with declare @method"

import java.lang.annotation.*;
import java.lang.reflect.Method;

@Retention(RetentionPolicy.RUNTIME)
@interface anInterface{}

interface A10{
}

aspect B10 {
	
	public void A10.a(){}
	declare @method : void A10.a(..) : @anInterface;
	
	public static void main(String [] args){
		Class c = A10.class;
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
