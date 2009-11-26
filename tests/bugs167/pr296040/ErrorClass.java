import java.util.Set;
import java.util.Map.Entry;

import com.google.common.collect.TreeMultimap;

public class ErrorClass {

    public void useGoogleCollections() {
        TreeMultimap<String, String> countResult = TreeMultimap.create();
        Set<Entry<String, String>> entries = countResult.entries();
        System.out.println(entries.size());
    }

    public static void main(String[] args) {
		new ErrorClass().useGoogleCollections();
	}
}