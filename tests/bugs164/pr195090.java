/**
 * Helper interface.
 */
interface Interface{
        boolean methodToBeImplemented();
}

/**
 * Exposes the ajc$this_ is never read warning.
 */
public aspect pr195090 {
        public boolean Interface.methodToBeImplemented() {
                return returnTrue();
        }

        public static boolean returnTrue() {
                return true;
        }
}
