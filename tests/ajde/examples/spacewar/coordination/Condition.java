
package coordination;


/**
 * Interface for pre-conditions that are passed to guardedEntry methods of
 * Coordinator.
 * Conditions should be passed as anonymous classes that simply implement
 * the checkit method.
 *
 */
public interface Condition {

    /**
     * This method is called automatically by Coordinator.guardedEntry(...)
     * and it's called everytime the coordination state changes.
     */

    public boolean checkit();
}
