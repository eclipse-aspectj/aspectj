
/**
 * This aspect represents upacking apples after an airplane trip.
 * 
 * @author	Mik Kersten
 * @version	$Version$
 */

public class BigRigAspect extends TransportAspect
{
	/**
	 * Bruise apples with a bruising factor of 15 after unpacking.
	 */ 
	static advice unpackCrosscut() {
	    after {  
		bruise( 40 );
	    }
	    before {
		bruise( 10 );
	    }
    }
}
