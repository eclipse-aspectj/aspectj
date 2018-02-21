import javax.xml.transform.TransformerFactory;
import javax.xml.bind.JAXBContext;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

aspect Azpect {
	before(JAXBContext x): execution(* m(JAXBContext)) && args(x) {
		System.out.println(x);
	}
}
