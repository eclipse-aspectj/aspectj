import java.util.List;

public aspect ParameterizedTypesInAnnotationPatterns {
	// CE - not an annotation type
	pointcut simple() : staticinitialization(@List<String> String);
	
	// no CE, good enough for now? may improve error reporting for this later
	pointcut combined() : staticinitialization(@(Foo || List<String>) String);
	
}

@interface Foo {}