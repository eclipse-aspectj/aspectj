public aspect Mark {

	public static interface IMarker {
		
	}
	
	public String IMarker.markMethod() {
		return "something done";
	}
	
	declare parents : ((@Anno *)) implements IMarker;
	
}
