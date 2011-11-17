import org.aspectj.weaver.WeakClassLoaderReference;


public class Code {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			WeakClassLoaderReference wclref = new WeakClassLoaderReference(null);
			System.out.println("OK");
		}catch(Throwable npe){
			System.out.println("KO");
		}
	}

}