import java.io.IOException;
import java.util.Locale;

import junit.framework.TestCase;

import org.aspectj.apache.bcel.generic.Instruction;
import org.aspectj.apache.bcel.util.ByteSequence;

public class LocaleTest extends TestCase {

	public LocaleTest(String name) {
		super(name);
	}

	public void testNormalLocale() {
		doBipush();		
	}

	public void testTurkishLocale() {
		Locale def = Locale.getDefault();
		Locale.setDefault(new Locale("tr", ""));
		try {
			doBipush();
		} finally {
			Locale.setDefault(def);
		}
	}
	
	private static void doBipush() {
		try {
			Instruction.readInstruction(
						new ByteSequence(new byte[] { 
							(byte)16, // bipush 
							(byte) 3  // data for bipush
							}));
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
}

