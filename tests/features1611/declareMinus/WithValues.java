import java.lang.annotation.*;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {
	String foo();
}

public aspect WithValues {
	@Anno(foo="anc")
	int i;
	
	declare @field: int i: -@Anno;
    declare @field: int j:  @Banno(hoo="abc");
    int j;
	
	public static void main(String[] args) throws Exception {
		if (WithValues.class.getDeclaredField("i").getAnnotation(Anno.class)==null) {
			System.out.println("i does not have Anno");
		} else {
			System.out.println("i has Anno");
		}	
		Annotation a = WithValues.class.getDeclaredField("j").getAnnotation(Banno.class);
		if (a==null) {
			System.out.println("j does not have Banno");
		} else {
			System.out.println("j has Banno:"+a);
		}
		a = WithValues.class.getDeclaredField("j").getAnnotation(Anno.class);
		if (a==null) {
			System.out.println("j does not have Anno");
		} else {
			System.out.println("j has Anno:"+a);
		}
	}
}
@Retention(RetentionPolicy.RUNTIME)
@interface Banno {
	String hoo();
}
