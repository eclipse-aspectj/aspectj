import java.io.IOException;
import org.aspectj.lang.SoftException;

public class VerifyError {

    static void foo() throws IOException {
        throw new IOException();
    }

    public static void main(String[] args) throws Exception{
		try {
            foo();
		} catch (SoftException sEx) {
			//
		}
    }
}


aspect Soften {

    declare soft : IOException : call(* foo());
}