import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Jun Dong (edong@elementum.com)
 *         Created at: 6/18/15 9:50 AM
 * @since 1.0
 * Description:
 */
public class Maps {

    public static <K, V> Map<K, V> toMap(Map.Entry<K, V>... entries) {
        return Collections.unmodifiableMap(
          Stream
            .of(entries)
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));
    }

}
