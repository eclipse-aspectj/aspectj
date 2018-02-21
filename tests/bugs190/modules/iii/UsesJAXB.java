import javax.xml.transform.TransformerFactory;
import javax.xml.bind.JAXBContext;
import javax.xml.parsers.DocumentBuilderFactory;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class UsesJAXB {

 //   private static final Logger LOG = LoggerFactory.getLogger(UsesJAXB.class);
    
    TransformerFactory tf = TransformerFactory.newInstance();
    
    public UsesJAXB() {
  //      LOG.error("UMS001");
        Document document;
        JAXBContext context;
    }

    public void m(JAXBContext jc) {
    }

    public static void main(String[] argv) {
	    System.out.println("UsesJAXB.running...");
	    new UsesJAXB().m(null);
    }
}
