import org.aspectj.lang.reflect.*;
import java.lang.reflect.*;
import java.lang.annotation.*;

public aspect DeclareAnnotationTest {
	
	declare @type : a.b.c..* : @MyAnnotation("ady 1");
	
	declare @method : * *(String) : @MyAnnotation("ady 2");
	
	declare @field : java.io.Serializable+ * : @MyClassRetentionAnnotation("ady 3");
	
	declare @constructor : new(String,..) : @MyAnnotation;
	
	public static void main(String[] args) throws ClassNotFoundException {
		AjType<DeclareAnnotationTest> myType = AjTypeSystem.getAjType(DeclareAnnotationTest.class);
		DeclareAnnotation[] decAs = myType.getDeclareAnnotations();
		if (decAs.length != 4) throw new RuntimeException("Expecting 4 members, got " + decAs.length);
		
		int atTypeIndex = -1;
		int atMethodIndex = -1;
		int atFieldIndex = -1;
		int atConstructorIndex = -1;
		
		int index = 0;
		for (DeclareAnnotation da : decAs) {
			
			switch (da.getKind()) {
				case Field:
					atFieldIndex = index++;
					break;
				case Method:
					atMethodIndex = index++;
					break;
				case Constructor:
					atConstructorIndex = index++;
					break;
				case Type:
					atTypeIndex = index++;
					break;
				default: throw new RuntimeException ("unknown type");
			}
		}
		
		
		checkAtType(decAs[atTypeIndex]);
		checkAtMethod(decAs[atMethodIndex]);
		checkAtField(decAs[atFieldIndex]);
		checkAtConstructor(decAs[atConstructorIndex]);
	}
		
	
	private static void checkAtType(DeclareAnnotation da) {
		if (da.getKind() != DeclareAnnotation.Kind.Type) throw new RuntimeException("expecting @type");
		if (da.getSignaturePattern() != null) throw new RuntimeException("not expecting a signature pattern");
		if (!da.getTypePattern().asString().equals("a.b.c..*")) throw new RuntimeException("expecting 'a.b.c..*' but got '" + da.getTypePattern().asString() + "'");
		if (da.getDeclaringType().getJavaClass() != DeclareAnnotationTest.class) throw new RuntimeException("bad declaring type: " + da.getDeclaringType());
		MyAnnotation ma = (MyAnnotation) da.getAnnotation();
		if (!ma.value().equals("ady 1")) throw new RuntimeException("bad value: " + ma.value());
		if (!da.getAnnotationAsText().equals("@MyAnnotation(\"ady 1\")")) throw new RuntimeException("bad annotation text: " + da.getAnnotationAsText());
	}

	private static void checkAtMethod(DeclareAnnotation da) {
		if (da.getKind() != DeclareAnnotation.Kind.Method) throw new RuntimeException("expecting @method");
		if (da.getTypePattern() != null) throw new RuntimeException("not expecting a type pattern");
		if (!da.getSignaturePattern().asString().equals("* *(java.lang.String)")) throw new RuntimeException("expecting '* *(java.lang.String)' but got '" + da.getSignaturePattern().asString() + "'");
		if (da.getDeclaringType().getJavaClass() != DeclareAnnotationTest.class) throw new RuntimeException("bad declaring type: " + da.getDeclaringType());
		MyAnnotation ma = (MyAnnotation) da.getAnnotation();
		if (!ma.value().equals("ady 2")) throw new RuntimeException("bad value: " + ma.value());
		if (!da.getAnnotationAsText().equals("@MyAnnotation(\"ady 2\")")) throw new RuntimeException("bad annotation text: " + da.getAnnotationAsText());
	}

	private static void checkAtField(DeclareAnnotation da) {
		if (da.getKind() != DeclareAnnotation.Kind.Field) throw new RuntimeException("expecting @field");
		if (da.getTypePattern() != null) throw new RuntimeException("not expecting a type pattern");
		if (!da.getSignaturePattern().asString().equals("java.io.Serializable+ *")) throw new RuntimeException("expecting 'java.io.Serializable+ *' but got '" + da.getSignaturePattern().asString() + "'");
		if (da.getDeclaringType().getJavaClass() != DeclareAnnotationTest.class) throw new RuntimeException("bad declaring type: " + da.getDeclaringType());
		if (da.getAnnotation() != null) throw new RuntimeException("expecting null annotation, but got " + da.getAnnotation());
		if (!da.getAnnotationAsText().equals("@MyClassRetentionAnnotation(\"ady 3\")")) throw new RuntimeException("bad annotation text: " + da.getAnnotationAsText());		
	}

	private static void checkAtConstructor(DeclareAnnotation da) {
		if (da.getKind() != DeclareAnnotation.Kind.Constructor) throw new RuntimeException("expecting @constructor");
		if (da.getTypePattern() != null) throw new RuntimeException("not expecting a type pattern");
		if (!da.getSignaturePattern().asString().equals("new(java.lang.String, ..)")) throw new RuntimeException("expecting 'new(java.lang.String,..)' but got '" + da.getSignaturePattern().asString() + "'");
		if (da.getDeclaringType().getJavaClass() != DeclareAnnotationTest.class) throw new RuntimeException("bad declaring type: " + da.getDeclaringType());
		MyAnnotation ma = (MyAnnotation) da.getAnnotation();
		if (!ma.value().equals("some value")) throw new RuntimeException("bad value: " + ma.value());
		if (!da.getAnnotationAsText().equals("@MyAnnotation")) throw new RuntimeException("bad annotation text: " + da.getAnnotationAsText());		
	}

}

@Retention(RetentionPolicy.RUNTIME)
@interface MyAnnotation {
	
	String value() default "some value";
	
}

@interface MyClassRetentionAnnotation {
	String value();
}
