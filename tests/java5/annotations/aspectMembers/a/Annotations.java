package a;

import java.lang.annotation.*;

@Target(ElementType.ANNOTATION_TYPE)
@interface AnnotationAnnotation {}

@Target(ElementType.CONSTRUCTOR)
@interface ConstructorAnnotation {}

@Target(ElementType.FIELD)
@interface FieldAnnotation {}

@Target(ElementType.LOCAL_VARIABLE)
@interface LocalVarAnnotation {}

@Target(ElementType.METHOD) 
@interface MethodAnnotation {}

@Target(ElementType.PACKAGE)
@interface PackageAnnotation {}

@Target(ElementType.PARAMETER) 
@interface ParameterAnnotation {}

@Target(ElementType.TYPE)
@interface TypeAnnotation {}

@Retention(RetentionPolicy.RUNTIME)
@interface AnyAnnotation {}