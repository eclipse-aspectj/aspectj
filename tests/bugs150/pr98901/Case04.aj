// "public ITD method with declare @method"

import java.lang.annotation.*;
import java.lang.reflect.Method;

@Retention(RetentionPolicy.RUNTIME)
@interface anInterface{}

class A04{
}

aspect B04 {
	
	public void A04.a(){}
	declare @method : void A04.a(..) : @anInterface;
	
	public static void main(String [] args){
		Class c = A04.class;
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
