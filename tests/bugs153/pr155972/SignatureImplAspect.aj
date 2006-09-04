

import java.lang.reflect.Member;

import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.FieldSignature;
import org.aspectj.lang.reflect.InitializerSignature;

public aspect SignatureImplAspect {
	before () : (execution(ConstructorTest.new(..))) {
		//thisJoinPointStaticPart.getSignature().getDeclaringType();
		Member m = ((ConstructorSignature) thisJoinPointStaticPart
				.getSignature()).getConstructor();
		System.out.println(m.getName());
	}
	
	before() : set(int FieldTest.intField) {
		//thisJoinPointStaticPart.getSignature().getDeclaringType();
		Member m = ((FieldSignature) thisJoinPointStaticPart
				.getSignature()).getField();
		System.out.println(m.getName());
	}
	
	before() : staticinitialization(InitializerTest) {
		//thisJoinPointStaticPart.getSignature().getDeclaringType();
		Member m = ((InitializerSignature) thisJoinPointStaticPart
				.getSignature()).getInitializer();
		System.out.println(m.getName());
	}
}
