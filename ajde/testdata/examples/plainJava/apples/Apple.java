
package apples;

import java.io.Serializable;
import java.io.IOException;

/**
 * This class represents an apple that has the following two attributes
 * <UL>
 *   <LI>a variety (i.e. "Macintosh" or "Granny Smith")
 *   <LI>a brusing factor represnting how badly bruised the apple is
 * </UL>
 *
 * @author	Mik Kersten
 * @version	$Version$
 */

public class Apple implements Serializable
{
	private String variety  = null;
	private int    bruising = 0;

    /**
	 * Default constructor.
	 *
	 * @param	newVariety	the type of variety for this apple
	 */
	public Apple( String newVariety )
	{
		variety = newVariety;
	}

    /**
     * This inner class represents apple seeds.
     */
    public class AppleSeed {
	private int weight = 0;
	private SeedContents seedContents = null;

	/**
	 * This is how you get poison from the apple.
	 */
	public void getArsenic() {
	    System.out.println( ">> getting arsenic" );
	}

	/**
	 * Reperesents the contents of the seeds.
	 */
	public class SeedContents {
	    public String core = null;
	    public String shell = null;
	}
    }

	/**
	 * Sets the bruising factor of the apple.
	 *
	 * @param	bruiseFactor	the new bruising factor
	 */
	public void bruise( int bruiseFactor )
	{
		bruising = bruising + bruiseFactor;

		if ( bruising > 100 ) bruising = 100;
		if ( bruising < 0 ) bruising = 0;
	}

	/**
	 * Returns the bruising factor.
	 *
	 * @return	bruising	the bruising factor associated with the apple
	 */
	public int getBruising()
	{
		return bruising;
	}


	/**
	 * Serializes the apple object.
	 *
	 * @param	oos		stream that the object is written to
	 */
	private void writeObject( java.io.ObjectOutputStream oos )
		throws IOException
	{
		// TODO: implement
	}


	/**
	 * Reads in the apple object.
	 *
	 * @param	ois		stream that the object is read from
	 */
	private void readObject( java.io.ObjectInputStream ois )
		throws IOException, ClassNotFoundException
	{
		// TODO: implement
	}
}

/**
 * Stub class to represent apple peeling.
 */
class ApplePealer
{
    /**
     * Stub for peeling the apple.
     */
    public void peelApple() {
	System.out.println( ">> peeling the apple..." );
    }
}
