
/**
 * This aspect crosscuts the process of shipping apples.
 * 
 * @author	Mik Kersten
 * @version	$Version$
 */

public class TransportAspect
{
    private introduction AppleCrate 
        {  
            
            /** 
            * Represents the name of the given crate.  Initialized to be 
            * a placeholder.
            */
            private String crateName = "temp crate";
            
            /**
            * Bruises each apple in the crate according to the bruise facor.  The bruise
            * factor is an integer that is passed as a parameter.
            */
            private void bruiser( int bruiseFactor )
            {
                for ( int i = 0; i < crateContents.length; i++ )
                {
                    crateContents[i].bruise( bruiseFactor );
                }
            }
        }
    
  /**
   * Crosscut <CODE>Apple</CODE> serialization methods.  This can be used for bruising 
   * apples and doing other silly things when the apples are being packed.
   */
  crosscut packCrosscut(): Apple && void writeObject( java.io.ObjectOutputStream out );

  /**
   * Crosscut <CODE>Apple</CODE> de-serialization methods.  This can be used for doing
   * silly things.  It is to be used when the apples are unpacked.
   */
  crosscut unpackCrosscut(): Apple && void readObject( java.io.ObjectInputStream in );
}
