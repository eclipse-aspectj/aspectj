package com.kronos.aspects;

import com.kronos.code.MyProcessor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import com.kronos.code.OkToIgnore; 
import java.lang.reflect.Modifier;
 
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;

@Aspect("perthis(initMyProcessor(com.kronos.code.MyProcessor))")
class AnnotatedProcessAspect  {

	HashMap<String, Field> fieldList = new HashMap<String, Field>();
	
	@Pointcut("initialization(com.kronos.code.MyProcessor+.new(..)) && this(myProcessor)")
	public void initMyProcessor(MyProcessor myProcessor){}
	


	@Pointcut("execution(* com.kronos.code.MyProcessor+.process()) && this(myProcessor)")
	public void executesProcess(MyProcessor myProcessor){}
	
	@Pointcut("get(* com.kronos.code.MyProcessor+.*) && cflow(executesProcess(myProcessor))") 
	public void fieldAccessor(MyProcessor myProcessor){}
	
	// find all of the public fields or fields with public accessors
	@After("initMyProcessor(myProcessor)") 
	public void adterInitMyProcessor(MyProcessor myProcessor){ 
		Field[] fields = myProcessor.getClass().getDeclaredFields();
		for (Field field : fields) {
			// make sure it should not be ignored
			if(field.getAnnotation(OkToIgnore.class) == null){
				boolean addField = false;
				// if public
				if(field.getModifiers() == Modifier.PUBLIC){
					addField = true;
				}else {
					// find a public accessor if it exists - assumes convention of getFieldName
					Method[] methods = myProcessor.getClass().getMethods();
					for (Method method : methods) {
						String methodName = method.getName();
						if(methodName.equalsIgnoreCase("get" + field.getName()) && method.getModifiers() == Modifier.PUBLIC){
							addField = true;
						}
					}
				}
				
				if(addField){
					fieldList.put(field.getName().toString(), field);
				}
			}
		}
	}
	
	// check where the process method is defined
	@Before("executesProcess(myProcessor)")
	public void beforeExecutesProcess(MyProcessor myProcessor, JoinPoint thisJoinPoint){
		Class declaringType = thisJoinPoint.getSignature().getDeclaringType();
		System.out.println(myProcessor.getClass().toString() + " execution OK: " + myProcessor.getClass().equals(declaringType));
	}
	
	// check access of the fields
	@Before("fieldAccessor(myProcessor)")
	public void beforeFieldAccessor(MyProcessor myProcessor, JoinPoint thisJoinPoint){
		String fieldName = thisJoinPoint.getSignature().getName().toString();
		// remove the fields from the field list as they are accessed
		if(fieldList.containsKey(fieldName)){
			fieldList.remove(fieldName);
		}
	}
	
	@After("executesProcess(myProcessor)")
	public void afterExecutesProcess(MyProcessor myProcessor){
		// report the missing field accessors
		for(String fieldName : fieldList.keySet()){
			System.out.println("Failed to process " + fieldName + " in " + myProcessor.getClass().toString());
		}
	}

}
