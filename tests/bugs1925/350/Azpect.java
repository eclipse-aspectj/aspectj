import java.util.HashSet;
import java.util.Set;

interface I {}

public aspect Azpect {
    private Set<String> I.changeListeners = new HashSet<String>();
}
