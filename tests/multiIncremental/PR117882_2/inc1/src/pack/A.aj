package pack;

public aspect A {

	declare @type : C : @Annotation;
	
	declare parents : (@Annotation *) implements I;
	
	public void I.method() {
	}
	
	public static void main(String []argv) {
		new C().method();
          if (C.class.getAnnotation(pack.Annotation.class)==null) 
            throw new RuntimeException("Class C should have @Annotation on it");
	}

}
