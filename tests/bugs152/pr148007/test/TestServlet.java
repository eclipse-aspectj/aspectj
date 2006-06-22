package test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Hello world!
 */
public class TestServlet extends HttpServlet {

    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        booleanTest(response);
    }

    private void booleanTest(HttpServletResponse response) throws ServletException {
        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException ioe) {
            throw new ServletException("Could not get writer.");
        }

        out.println("Test 1a. Should be false. Was: " + invert1a());
        out.println("Test 1b. Should be true. Was: " + invert1b());
        out.println("Test 2. Should be false. Was: " + invert2());
        out.println("Test 3. Should be true. Was: " + invert3());
        out.println("Test 4. Should be true. Was: " + invert4());
        out.println("Test 5. Should be false. Was: " + invert5());
    }

    private boolean invert1a() {
        return ! true;
    }

    private boolean invert1b() {
        return ! false;
    }

    private Boolean invert2() {
        return new Boolean(! isTrue());
    }

    private Boolean invert3() {
        return new Boolean(! isFalse());
    }

    private boolean invert4() {
        boolean temp = isFalse();
        return ! temp;
    }

    private Boolean invert5() {
        boolean temp = isTrue();
        return new Boolean(! temp);
    }

    private boolean isTrue() {
        return true;
    }

    private boolean isFalse() {
        return false;
    }
}
