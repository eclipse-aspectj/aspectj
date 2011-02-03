import com.foo.op4j.Country;
import java.lang.Object;
import org.javaruntype.type.Types;
import org.op4j.functions.Function;
import org.op4j.functions.Get;

privileged aspect Country_Roo_Op4j2 {

   public static class Country.Keys {

       public static final Function<Object, Country> COUNTRY = Get.attrOf(Types.forClass(Country.class),"country");
   }
}
