import java.lang.reflect.Field;

import org.aspectj.lang.reflect.*;

public aspect Instrumentation {

	/**
	 * Instrument field reads.
	 */
	pointcut getField() : get(* *) && !within(Instrumentation);

	after() : getField() {
		final FieldSignature signature = (FieldSignature) thisJoinPointStaticPart
				.getSignature();
		final Field field = signature.getField();
		final SourceLocation sl = thisJoinPointStaticPart.getSourceLocation();
		if (field == null) {
			throw new IllegalStateException(
					"See pr172107: get FieldSignature#getField()==null in "
							+ sl.getFileName() + " at line " + sl.getLine());
		}
	}

	/**
	 * Instrument field reads.
	 */
	pointcut setField() : set(* *) && !within(Instrumentation);

	after() : setField() {
		final FieldSignature signature = (FieldSignature) thisJoinPointStaticPart
				.getSignature();
		final Field field = signature.getField();
		final SourceLocation sl = thisJoinPointStaticPart.getSourceLocation();
		if (field == null) {
			throw new IllegalStateException(
					"See pr172107: set FieldSignature#getField()==null in "
							+ sl.getFileName() + " at line " + sl.getLine());
		}
	}
}
