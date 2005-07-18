// "public ITD-on-itself field with declare @field"

import java.lang.annotation.*;
import java.lang.reflect.Field;

@Retention(RetentionPolicy.RUNTIME)
@interface anInterface{}

aspect B28 {
	
	public int B28.a;
	declare @field : int B28.a : @anInterface;
	
	public static void main(String [] args){
		Class c = B28.class;
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
