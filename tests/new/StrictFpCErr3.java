strictfp abstract class StrictClassBadConstructor {
	// Has to be error, may not generate strictfp, but has to set strictfp in bytecode
	strictfp StrictClassBadConstructor() {}
};

