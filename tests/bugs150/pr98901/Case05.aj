// "public annotated ITD method"

import java.lang.annotation.*;
import java.lang.reflect.Method;

@Retention(RetentionPolicy.RUNTIME)
@interface anInterface{}

class A05{
}

aspect B05 {
	
	@anInterface
	public void A05.a(){}
	
	public static void main(String [] args){
		Class c = A05.class;
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
