import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;


public interface BadInterface {
    static final Comparator MY_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            return 0;
        }
    };
    
    static final String aString = "Constant String"; // StringLiteral
    static final String bString = aString; 			//SingleNameReference
    static final String cString = aString + bString; // BinaryExpression
    static final String dString = aString + " and " + bString;//CombinedBinaryExpression
    static final String eString = "Hello" + " World"; //ExtendedStringLiteral
    
    static final int aInteger = 1; //IntLiteral
    static final int bInteger = aInteger; //SingleNameReference
    static final int cInteger = aInteger + bInteger; //BinaryExpression
    static final int dInteger = aInteger + 3 + bInteger; //CombinedBinaryExpression
    
    public List<String> aList = new LinkedList<String>() {{ add("Busted"); }};
    
    public List<String> bList = new LinkedList<String>() {
       public int size() {
          for(int i = 0; i < 100; i++) {
             return 0;
          }
          return modCount;
       }
    };
    
	public List<String> cList = 
		Collections.unmodifiableList(new ArrayList<String>(){{add("VMID"); }});

    
}
