
package apples;

/**
 * This aspect crosscuts the process of shipping apples.
 *
 * @author	Mik Kersten
 * @version	$Version$
 */

public class TransportAspect
{
    private String crateName = "temp crate";

    /**
     * Bruises each apple in the crate according to the bruise facor.  The bruise
     * factor is an integer that is passed as a parameter.
     */
    private void bruiser( int bruiseFactor )
    {
        for ( int i = 0; i < 5; i++ )
        {
            System.out.println( "bruising" );
        }
    }
}
