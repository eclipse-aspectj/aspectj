/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
 
package figures;

public aspect Enforcement {

	before(int newValue): set(int Point.*) && args(newValue) {
		if (newValue < 0) {
				throw new IllegalArgumentException("> val: " + newValue + " is too small");
		}	
	}

	declare warning: call(void Canvas.updateHistory(..)) && !within(Enforcement): "";

	after() returning: call(void FigureElement+.set*(..)) {
		//Canvas.updateHistory();	
	}

	declare error: 
		set(private * FigureElement+.*) &&
		!(withincode(* FigureElement+.set*(..)) || withincode(FigureElement+.new(..))):
			"should only assign to fileds from set methods";

}





















//  	before(int newValue): set(int Point.*) && args(newValue) {
//  		if (newValue < 0) {
//  			throw new IllegalArgumentException("> value: " + newValue + " too small");
//  		}
//  	}
//
//	declare warning: call(void Canvas.updateHistory(..)) && !within(Enforcement): 
//		"found call";
//
//	after() returning: call(void FigureElement+.set*(..)) {
//			Canvas.updateHistory();
//	}
//	
//	declare error: 
//		set(private * FigureElement+.*) &&
//		!(withincode(* FigureElement+.set*(..)) || withincode(FigureElement+.new(..))):
//		"should only assign to fields from set methods";
