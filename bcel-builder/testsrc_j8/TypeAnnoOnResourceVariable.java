import java.io.*;

public class TypeAnnoOnResourceVariable {
	public void m() throws Exception {
		try (@Anno BufferedReader br1 = new BufferedReader(new FileReader("a"));
		     @Anno(99) BufferedReader br2 = new BufferedReader(new FileReader("b"))) {
 			System.out.println(br1.readLine()+br2.readLine());
		}
	}
}
