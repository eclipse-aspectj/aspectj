// "public field on the aspect that declares @field on it"

import java.lang.annotation.*;
import java.lang.reflect.Field;

@Retention(RetentionPolicy.RUNTIME)
@interface anInterface{}

aspect B24 {
	
	public int a;
	declare @field : int B24.a : @anInterface;
	
	public static void main(String [] args){
		Class c = B24.class;
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
