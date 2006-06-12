package pkg;

public aspect Deow {

	declare warning : (get(* System.out) || get(* System.err)) : "There should be no printlns";  

}
