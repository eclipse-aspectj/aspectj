// "public ITD field with declare @field"

import java.lang.annotation.*;
import java.lang.reflect.Field;

@Retention(RetentionPolicy.RUNTIME)
@interface anInterface{}

class A26{
}

aspect B26 {
	
	public int A26.a;
	declare @field : int A26.a : @anInterface;
	
	public static void main(String [] args){
		Class c = A26.class;
		try {
			Field m = c.getDeclaredField("a");
			Annotation [] anns = m.getDeclaredAnnotations();
			for (int i = 0;i < anns.length;i++){
				System.out.println(anns[i]);
			}
		} catch (Exception e){
			System.out.println("exceptional!");
		}
	}
}
