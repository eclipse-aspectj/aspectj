public aspect Detour {
	
	void around() : execution(* Main.main(..)) {
		System.out.println("Main class successfully woven");
	}
	
}