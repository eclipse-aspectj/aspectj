
public privileged aspect A_ITD {
    declare parents: A extends SuperA<String>;
	public B A.getSomeB(SuperB<String> b){
		return null;
	}
}
