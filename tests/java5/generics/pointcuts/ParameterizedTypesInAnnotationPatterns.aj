import java.util.List;

public aspect ParameterizedTypesInAnnotationPatterns {
	// CE - not an annotation type
	pointcut simple() : staticinitialization(@List<String> String);

	// CE - no static initialization join points for parameterized types, use raw type instead
	pointcut combined() : staticinitialization(@(Foo || List<String>) String);
}

@interface Foo {}
