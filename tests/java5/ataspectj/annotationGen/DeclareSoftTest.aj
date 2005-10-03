import org.aspectj.lang.reflect.*;
import java.lang.reflect.*;

public aspect DeclareSoftTest {
	
	declare soft : ClassNotFoundException : call(* Class.forName(..));

	
	public static void main(String[] args) throws ClassNotFoundException {
		AjType<DeclareSoftTest> myType = AjTypeSystem.getAjType(DeclareSoftTest.class);
		DeclareSoft[] decSs = myType.getDeclareSofts();
		if (decSs.length != 1) throw new RuntimeException("Expecting 1 members, got " + decSs.length);
		if (decSs[0].getDeclaringType() != myType) throw new RuntimeException("Bad declaring type: " + decSs[0].getDeclaringType());
		String pc = decSs[0].getPointcutExpression().asString();
		if (!pc.equals("call(* java.lang.Class.forName(..))")) throw new RuntimeException("Bad pcut: " + pc);
		AjType exType = decSs[0].getSoftenedExceptionType();
		if (exType != AjTypeSystem.getAjType(ClassNotFoundException.class)) {
			throw new RuntimeException("Bad ex type: " + exType);
		}
	}
	
}
