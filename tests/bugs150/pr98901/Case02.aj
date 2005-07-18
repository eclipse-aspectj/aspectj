//"public method on the aspect that declares @method on it"

import java.lang.annotation.*;
import java.lang.reflect.Method;

@Retention(RetentionPolicy.RUNTIME)
@interface anInterface{}

aspect B02 {
	public void a(){}
	declare @method : void B02.a(..) : @anInterface;
	
	public static void main(String [] args){
		Class c = B02.class;
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
