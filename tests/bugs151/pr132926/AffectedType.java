public class AffectedType {

	public static void main(String[] args) {
		
	}
}

aspect X {
	declare @type: AffectedType: @InputAnnotation;
}