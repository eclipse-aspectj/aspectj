import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public aspect Wibble {

	declare @field: (@III *) Song.*: @Foo;
	
	before(): get((@III *) Song.*) {
		System.out.println();
	} 
  public static void main(String []argv) throws Exception {
    System.out.println(Song.class.getDeclaredField("i").getAnnotation(Foo.class));
  }
}

@III
class XX {
	
}
class Song {

	XX i;
	
	void foo() {
		System.out.println(i);
	}
}

@Retention(RetentionPolicy.RUNTIME)
@interface III {}
@Retention(RetentionPolicy.RUNTIME)
@interface Foo {}
