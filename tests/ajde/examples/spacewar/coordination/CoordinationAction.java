
package coordination;


/**
 * Interface for coordination actions that are passed to guardedEntry methods of
 * Coordinator.
 * Coordination actions should be passed as anonymous classes that simply 
 * implement the doit method. 
 *
 */
public interface CoordinationAction {
    /**
     * This method is called  by Coordinator.guardedEntry(...) and
     * Coordinator.guardedExit(...). Use it for changing coordination state
     * upon entering and exiting methods.
     */

    public void doit();
}
