import java.io.IOException;

/*
 * Created on 30-Jul-03
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

/**
 * @author websterm
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Main {

	public static void main(String[] args) throws IOException {
		String propsName = (args.length > 0)? args[0] : "test.props";
		new test.TestProperties().load(propsName);
	}
}
