package com.kronos.aspects;

import com.kronos.code.MyProcessor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import com.kronos.code.OkToIgnore; 
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public aspect ProcessAspect perthis(initMyProcessor(MyProcessor)) {

	HashMap<String, Field> fieldList = new HashMap<String, Field>();
	
	pointcut initMyProcessor(MyProcessor myProcessor) : initialization(MyProcessor+.new(..)) && this(myProcessor);
	
	pointcut executesProcess(MyProcessor myProcessor) : execution(* MyProcessor+.process()) && this(myProcessor);
	
	pointcut fieldAccessor(MyProcessor myProcessor) : get(* MyProcessor+.*) && cflow(executesProcess(myProcessor)); 
	
	// find all of the public fields or fields with public accessors
	after(MyProcessor myProcessor): initMyProcessor(myProcessor) { 
		// this advice is executed for each type in the hierarchy, including the interface
		// so you will get all of the fields and methods for each type in the hierarchy
		System.out.println(thisJoinPoint.getArgs()); Class currentType = thisJoinPointStaticPart.getSourceLocation().getWithinType();
System.out.println(thisEnclosingJoinPointStaticPart);		Field[] fields = currentType.getDeclaredFields();
		for (Field field : fields) {
			// make sure it should not be ignored
			if(field.getAnnotation(OkToIgnore.class) == null){
				boolean addField = false;
				// if public
				if(field.getModifiers() == Modifier.PUBLIC){
					addField = true;
				}else {
					// find a public accessor if it exists - assumes convention of getFieldName
					Method[] methods = currentType.getMethods();
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
	before(MyProcessor myProcessor): executesProcess(myProcessor) && !cflowbelow(executesProcess(MyProcessor)){
		Class declaringType = thisJoinPointStaticPart.getSignature().getDeclaringType();
		System.out.println(myProcessor.getClass().toString() + " execution OK: " + myProcessor.getClass().equals(declaringType));
	}
	
	// check access of the fields
	before(MyProcessor myProcessor): fieldAccessor(myProcessor){
		String fieldName = thisJoinPointStaticPart.getSignature().getName().toString();
		// remove the fields from the field list as they are accessed
		if(fieldList.containsKey(fieldName)){
			fieldList.remove(fieldName);
		}
	}
	
	after(MyProcessor myProcessor): executesProcess(myProcessor) && !cflowbelow(executesProcess(MyProcessor)){
		// report the missing field accessors
		for(String fieldName : fieldList.keySet()){
			System.out.println("Failed to process " + fieldName + " in " + myProcessor.getClass().toString());
		}
	}

}
