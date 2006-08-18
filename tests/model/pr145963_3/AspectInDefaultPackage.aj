
public aspect AspectInDefaultPackage {

	declare warning : (get(* System.out) || get(* System.err)) : "There should be no printlns";

}
