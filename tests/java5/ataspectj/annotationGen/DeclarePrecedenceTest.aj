import org.aspectj.lang.reflect.*;
import java.lang.reflect.*;

@org.aspectj.lang.annotation.DeclarePrecedence("DeclarePrecedenceTest,*")
public aspect DeclarePrecedenceTest {
	
	declare precedence : org.xyz..*, org.abc..*;

	declare precedence : org.abc..*, org.def..*;

	
	public static void main(String[] args) throws ClassNotFoundException {
		AjType<DeclarePrecedenceTest> myType = AjTypeSystem.getAjType(DeclarePrecedenceTest.class);
		DeclarePrecedence[] decPs = myType.getDeclarePrecedence();
		if (decPs.length != 3) throw new RuntimeException("Expecting 3 members, got " + decPs.length);
		for(int i = 0; i < decPs.length; i++) {
			validateDecP(decPs[i]);
		}
	}
	
	private static void validateDecP(DeclarePrecedence dp) {
		TypePattern[] tps = dp.getPrecedenceOrder();
		if (tps.length != 2) throw new RuntimeException("Expecting 2 type patterns, got " + tps.length);
		if (tps[0].asString().equals("DeclarePrecedenceTest")) {
			if (!tps[1].asString().equals("*")) throw new RuntimeException("Excepting '*', got '" + tps[1].asString() + "'");
		} else if (tps[0].asString().equals("org.xyz..*")) {
			if (!tps[1].asString().equals("org.abc..*")) throw new RuntimeException("Excepting 'org.abc..*', got '" + tps[1].asString() + "'");			
		} else if (tps[0].asString().equals("org.abc..*")) {
			if (!tps[1].asString().equals("org.def..*")) throw new RuntimeException("Excepting 'org.def..*', got '" + tps[1].asString() + "'");						
		} else {
			throw new RuntimeException("Unexpected type pattern: " + tps[0].asString());
		}		
	}
	
}
