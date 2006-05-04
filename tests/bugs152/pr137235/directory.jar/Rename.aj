public aspect Rename {
    String around() : call(* getName()) { 
    	return "AspectJ not just "+proceed(); 
	}
}
