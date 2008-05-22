import java.lang.reflect.Field;

import org.aspectj.lang.reflect.*;

public aspect Instrumentation {
	pointcut nofl() : !within(Instrumentation);
	
	pointcut getField() : get(* *) && nofl() && !get(* System.out) ;

	after() : getField() {
   	final FieldSignature signature = (FieldSignature) thisJoinPointStaticPart.getSignature();
                StringBuffer jpInfo = new StringBuffer();
                jpInfo.append("getField(* ").append(signature.getName()).append(")");
		final Field field = signature.getField();
                jpInfo.append("  getField()='").append(signature.getField()).append("'");
		final Class<?> declaringType = signature.getDeclaringType();
                jpInfo.append("  getDeclaringType()='"+declaringType).append("'");
		final Object receiver = thisJoinPoint.getTarget();
		if (receiver == null) {
                     jpInfo.append(" signature.getTarget() is null (only ok if static)");
                }
		System.out.println(jpInfo.toString());
	}

   

/*
	pointcut setField() : set(* *) && nofl();

	after() : setField() {
		System.out.println("setField()");
		final FieldSignature signature = (FieldSignature) thisJoinPointStaticPart
				.getSignature();
		final Field field = signature.getField();
		if (field == null)
			System.out.println(" - field " + signature.getName()
					+ " is null...bug!");
		final Class<?> declaringType = signature.getDeclaringType();
		if (declaringType == null)
			System.out.println(" - declaringType for the field "
					+ signature.getName() + " is null...bug!");
		final Object receiver = thisJoinPoint.getTarget();
		if (receiver == null)
			System.out.println(" - target (receiver) for the field "
					+ signature.getName() + " is null...bug!");
	}
*/

}

