import java.rmi.RemoteException;


class UseOperators {
	public void f3() throws NullPointerException, RemoteException
	{
		Operators.Operator<String> f = new Operators.Operator<String>() {

			public String execute(String aArg) throws NullPointerException,
					RemoteException {
				System.out.println("Doh!");
				return aArg;
			}
			
		}; 
		f.execute("");
	}
}