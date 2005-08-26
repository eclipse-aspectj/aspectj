class HttpServlet {
    protected void doPost() {
    }    
}

abstract class MockServlet extends HttpServlet {
    protected void doPost() {
    }
    
    private static aspect FindMatches {
    	  declare warning: execution(* HttpServlet.do*(..)): "servlet request";
    }
}

class MockDelayingServlet extends MockServlet {
    private static final long serialVersionUID = 1; 
}

class MockServlet4 extends MockDelayingServlet 
{
    protected void doPost()
    {
    } 
}






