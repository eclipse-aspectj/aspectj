import org.xml.sax.SAXException;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.xml.sax.InputSource;
import org.apache.xerces.parsers.DOMParser;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import org.w3c.dom.Document;

//public class UsesDOMParser {
//
//	public static void main(String[] args) throws Exception {
//		PipedInputStream in = new PipedInputStream();
//		DOMParser parser = new DOMParser();
//		parser.parse(new InputSource(in));
//		System.out.println("All done!");
//	}
//} 

public class UsesDOMParser {

	public static void main(String[] args) throws Exception {
		PipedInputStream in = new PipedInputStream();
		final PipedOutputStream out = new PipedOutputStream(in);

		Thread t = new Thread() {
			public void run() {
				try {
					String str =
						"<?xml version=\"1.0\"?>\n"
							+ "<test>\n"
							+ "   <goes>\n"
							+ "      <here>yeah</here>\n"
							+ "   </goes>\n"
							+ "</test>\n";

					PrintWriter o = new PrintWriter(out);
					o.println(str);
					o.flush();
					o.close();
					out.flush();
					out.close();
				} catch (Exception e) {
					String error =
						e.getClass().getName() + ": " + e.getMessage();
					throw new RuntimeException(error);
				}
			}
		};

		t.start();

		DOMParser parser = new DOMParser();
		parser.parse(new InputSource(in));
		Document doc = parser.getDocument();
		org.w3c.dom.Element root = doc.getDocumentElement();
  
		t.join();

		System.out.println("All done!");
	}
} // end class