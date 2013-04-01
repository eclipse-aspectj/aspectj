public aspect UserTrait {
	public interface I extends IUser { } 

	declare parents : Youser implements I;

	public void I.setUsername(String username) {
		testSetUsername(username);
	}

	private void I.testSetUsername(String username) { }

}
