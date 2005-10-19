import java.lang.reflect.*;
import java.util.*;

public class Util {
	
	public static void dumpMethods(String clazzname) {//,String[] expectedMethods) {
		List methodsFound = new ArrayList();
		try {
			java.lang.Class clz = Class.forName(clazzname);
			Method m[] = clz.getDeclaredMethods();
			System.err.println("Number of methods defined for "+clazzname+" is "+(m==null?0:m.length));
			if (m!=null) {
				for (int i =0;i<m.length;i++) {
					String methodString = m[i].getReturnType().getName()+" "+m[i].getDeclaringClass().getName()+"."+
							           m[i].getName()+"("+stringify(m[i].getParameterTypes())+")"+
							           (m[i].isBridge()?" [BridgeMethod]":"");
					methodsFound.add(methodString);
				}
			}
		} catch (Throwable e) {e.printStackTrace();}
		
		StringBuffer diagnosticInfo = new StringBuffer();
		Collections.sort(methodsFound);
		for (Iterator iter = methodsFound.iterator(); iter.hasNext();) {
			String element = (String) iter.next();
			System.err.println(element);
		}
//		diagnosticInfo.append("\nExpected:\n").append(dumparray(expectedMethods));
//		diagnosticInfo.append("\nFound:\n").append(dumplist(methodsFound));
//		
//		for (int i = 0; i < expectedMethods.length; i++) {
//			String string = expectedMethods[i];
//			if (!methodsFound.contains(string)) {
//				throw new RuntimeException("Expecting method '"+string+"' but didnt find it\n"+diagnosticInfo.toString());
//			}
//			methodsFound.remove(string);
//		}
//		if (methodsFound.size()>0) {
//			throw new RuntimeException("Found more methods than expected: "+dumplist(methodsFound));
//		}
	}
	
	private static String dumparray(String[] arr) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < arr.length; i++) {
			String string = arr[i];
			sb.append(string).append("\n");
		}
		return sb.toString();
	}
	
	private static String dumplist(List l) {
		StringBuffer sb = new StringBuffer();
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			String element = (String) iter.next();
			sb.append(element).append("\n");
		}
		return sb.toString();
	}
	
	private static String stringify(Class[] clazzes) {
		if (clazzes==null) return "";
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < clazzes.length; i++) {
			Class class1 = clazzes[i];
			if (i>0) sb.append(",");
			sb.append(clazzes[i].getName());
		}
		return sb.toString();
	}
}
