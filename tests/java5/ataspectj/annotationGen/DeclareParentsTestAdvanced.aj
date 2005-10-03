package a.b.c;

import org.aspectj.lang.reflect.*;
import java.lang.reflect.*;

public class DeclareParentsTestAdvanced {
	
	public static void main(String[] args) throws ClassNotFoundException {
		AjType<ConcreteAspect> aType = AjTypeSystem.getAjType(ConcreteAspect.class);
		DeclareParents[] decPs = aType.getDeclareParents();
		if (decPs.length != 1) throw new RuntimeException("should see decp from super");
		DeclareParents dp = decPs[0];
		if (!dp.isImplements()) throw new RuntimeException("Expecting implements");
		if (dp.getDeclaringType().getJavaClass() != AbstractAspect.class) {
			throw new RuntimeException("Expecting declaring class to be AbstractAspect");
		}
		TypePattern tp = dp.getTargetTypesPattern();
		if (!tp.asString().equals("a.b.c.A")) {
			throw new RuntimeException("expecting 'a.b.c.A' but was '" + tp.asString() + "'");
		}
		Type[] parents = dp.getParentTypes();
		if (parents.length != 1) throw new RuntimeException("expecting 1 parent");
		if (((AjType)parents[0]).getJavaClass() != B.class) {
			throw new RuntimeException("expecting 'B' but was '" + parents[0].toString() + "'");
		}
	}
}

abstract aspect AbstractAspect {
	
	declare parents : A implements B;
	
}

aspect ConcreteAspect extends AbstractAspect {}

class A {}

class B {}