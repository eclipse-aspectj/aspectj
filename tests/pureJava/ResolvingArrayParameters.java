/*
 * Not finding methods in super- and sub-classes with
 * primitive parameter types.
 *
 * For example:
 *
 * pureJava\ResolvingArrayParameters.java:991:9: two applicable and accessible meth
 * ods are equally specific: public void C_float_short_long.f(float[]) and public v
 * oid C_long.f(long[])
 *       new C_float_short_long().f(longs);
 *
 */
public class ResolvingArrayParameters {
    public static void main(String[] args) {
        new ResolvingArrayParameters().realMain(args);
    }
    public void realMain(String[] args) {

        wants();
        
        boolean[] booleans = new boolean[]{};
        byte[] bytes = new byte[]{};
        char[] chars = new char[]{};
        short[] shorts = new short[]{};
        int[] ints = new int[]{};
        long[] longs = new long[]{};
        float[] floats = new float[]{};
        double[] doubles = new double[]{};

        // changed from C_boolean... to avoid having to expect one for each subclass
        new CC_boolean().f(booleans);
        new CC_byte().f(bytes);
        new CC_char().f(chars);
        new CC_short().f(shorts);
        new CC_int().f(ints);
        new CC_long().f(longs);
        new CC_float().f(floats);
        new CC_double().f(doubles);

        new C_boolean_double().f(doubles); new C_boolean_double().f(booleans);
        new C_byte_double().f(doubles); new C_byte_double().f(bytes);
        new C_char_double().f(doubles); new C_char_double().f(chars);
        new C_short_double().f(doubles); new C_short_double().f(shorts);
        new C_int_double().f(doubles); new C_int_double().f(ints);
        new C_long_double().f(doubles); new C_long_double().f(longs);
        new C_float_double().f(doubles); new C_float_double().f(floats);


        new C_boolean_float().f(floats); new C_boolean_float().f(booleans);
        new C_byte_float().f(floats); new C_byte_float().f(bytes);
        new C_char_float().f(floats); new C_char_float().f(chars);
        new C_short_float().f(floats); new C_short_float().f(shorts);
        new C_int_float().f(floats); new C_int_float().f(ints);
        new C_long_float().f(floats); new C_long_float().f(longs);
        new C_double_float().f(floats); new C_double_float().f(doubles);


        new C_boolean_long().f(longs); new C_boolean_long().f(booleans);
        new C_byte_long().f(longs); new C_byte_long().f(bytes);
        new C_char_long().f(longs); new C_char_long().f(chars);
        new C_short_long().f(longs); new C_short_long().f(shorts);
        new C_int_long().f(longs); new C_int_long().f(ints);
        new C_float_long().f(longs); new C_float_long().f(floats);
        new C_double_long().f(longs); new C_double_long().f(doubles);


        new C_boolean_int().f(ints); new C_boolean_int().f(booleans);
        new C_byte_int().f(ints); new C_byte_int().f(bytes);
        new C_char_int().f(ints); new C_char_int().f(chars);
        new C_short_int().f(ints); new C_short_int().f(shorts);
        new C_long_int().f(ints); new C_long_int().f(longs);
        new C_float_int().f(ints); new C_float_int().f(floats);
        new C_double_int().f(ints); new C_double_int().f(doubles);


        new C_boolean_short().f(shorts); new C_boolean_short().f(booleans);
        new C_byte_short().f(shorts); new C_byte_short().f(bytes);
        new C_char_short().f(shorts); new C_char_short().f(chars);
        new C_int_short().f(shorts); new C_int_short().f(ints);
        new C_long_short().f(shorts); new C_long_short().f(longs);
        new C_float_short().f(shorts); new C_float_short().f(floats);
        new C_double_short().f(shorts); new C_double_short().f(doubles);

        new C_boolean_char().f(chars); new C_boolean_char().f(booleans);
        new C_byte_char().f(chars); new C_byte_char().f(bytes);
        new C_short_char().f(chars); new C_short_char().f(shorts);
        new C_int_char().f(chars); new C_int_char().f(ints);
        new C_long_char().f(chars); new C_long_char().f(longs);
        new C_float_char().f(chars); new C_float_char().f(floats);
        new C_double_char().f(chars); new C_double_char().f(doubles);

        new C_boolean_byte().f(bytes); new C_boolean_byte().f(booleans);
        new C_char_byte().f(bytes); new C_char_byte().f(chars);
        new C_short_byte().f(bytes); new C_short_byte().f(shorts);
        new C_int_byte().f(bytes); new C_int_byte().f(ints);
        new C_long_byte().f(bytes); new C_long_byte().f(longs);
        new C_float_byte().f(bytes); new C_float_byte().f(floats);
        new C_double_byte().f(bytes); new C_double_byte().f(doubles);

        new C_byte_boolean().f(booleans); new C_byte_boolean().f(bytes);
        new C_char_boolean().f(booleans); new C_char_boolean().f(chars);
        new C_short_boolean().f(booleans); new C_short_boolean().f(shorts);
        new C_int_boolean().f(booleans); new C_int_boolean().f(ints);
        new C_long_boolean().f(booleans); new C_long_boolean().f(longs);
        new C_float_boolean().f(booleans); new C_float_boolean().f(floats);
        new C_double_boolean().f(booleans); new C_double_boolean().f(doubles);

        // ------------------------------------------------------------
        new C_char_boolean_byte().f(chars);
        new C_char_boolean_byte().f(booleans);
        new C_char_boolean_byte().f(bytes);
        new C_short_boolean_byte().f(shorts);
        new C_short_boolean_byte().f(booleans);
        new C_short_boolean_byte().f(bytes);
        new C_int_boolean_byte().f(ints);
        new C_int_boolean_byte().f(booleans);
        new C_int_boolean_byte().f(bytes);
        new C_long_boolean_byte().f(longs);
        new C_long_boolean_byte().f(booleans);
        new C_long_boolean_byte().f(bytes);
        new C_float_boolean_byte().f(floats);
        new C_float_boolean_byte().f(booleans);
        new C_float_boolean_byte().f(bytes);
        new C_double_boolean_byte().f(doubles);
        new C_double_boolean_byte().f(booleans);
        new C_double_boolean_byte().f(bytes);

        new C_byte_boolean_double().f(bytes);
        new C_byte_boolean_double().f(booleans);
        new C_byte_boolean_double().f(doubles);
        new C_char_boolean_double().f(chars);
        new C_char_boolean_double().f(booleans);
        new C_char_boolean_double().f(doubles);
        new C_short_boolean_double().f(shorts);
        new C_short_boolean_double().f(booleans);
        new C_short_boolean_double().f(doubles);
        new C_int_boolean_double().f(ints);
        new C_int_boolean_double().f(booleans);
        new C_int_boolean_double().f(doubles);
        new C_long_boolean_double().f(longs);
        new C_long_boolean_double().f(booleans);
        new C_long_boolean_double().f(doubles);
        new C_float_boolean_double().f(floats);
        new C_float_boolean_double().f(booleans);
        new C_float_boolean_double().f(doubles);

        new C_byte_boolean_float().f(bytes);
        new C_byte_boolean_float().f(booleans);
        new C_byte_boolean_float().f(floats);
        new C_char_boolean_float().f(chars);
        new C_char_boolean_float().f(booleans);
        new C_char_boolean_float().f(floats);
        new C_short_boolean_float().f(shorts);
        new C_short_boolean_float().f(booleans);
        new C_short_boolean_float().f(floats);
        new C_int_boolean_float().f(ints);
        new C_int_boolean_float().f(booleans);
        new C_int_boolean_float().f(floats);
        new C_long_boolean_float().f(longs);
        new C_long_boolean_float().f(booleans);
        new C_long_boolean_float().f(floats);
        new C_double_boolean_float().f(doubles);
        new C_double_boolean_float().f(booleans);
        new C_double_boolean_float().f(floats);

        new C_byte_boolean_long().f(bytes);
        new C_byte_boolean_long().f(booleans);
        new C_byte_boolean_long().f(longs);
        new C_char_boolean_long().f(chars);
        new C_char_boolean_long().f(booleans);
        new C_char_boolean_long().f(longs);
        new C_short_boolean_long().f(shorts);
        new C_short_boolean_long().f(booleans);
        new C_short_boolean_long().f(longs);
        new C_int_boolean_long().f(ints);
        new C_int_boolean_long().f(booleans);
        new C_int_boolean_long().f(longs);
        new C_float_boolean_long().f(floats);
        new C_float_boolean_long().f(booleans);
        new C_float_boolean_long().f(longs);
        new C_double_boolean_long().f(doubles);
        new C_double_boolean_long().f(booleans);
        new C_double_boolean_long().f(longs);

        new C_byte_boolean_int().f(bytes);
        new C_byte_boolean_int().f(booleans);
        new C_byte_boolean_int().f(ints);
        new C_char_boolean_int().f(chars);
        new C_char_boolean_int().f(booleans);
        new C_char_boolean_int().f(ints);
        new C_short_boolean_int().f(shorts);
        new C_short_boolean_int().f(booleans);
        new C_short_boolean_int().f(ints);
        new C_long_boolean_int().f(longs);
        new C_long_boolean_int().f(booleans);
        new C_long_boolean_int().f(ints);
        new C_float_boolean_int().f(floats);
        new C_float_boolean_int().f(booleans);
        new C_float_boolean_int().f(ints);
        new C_double_boolean_int().f(doubles);
        new C_double_boolean_int().f(booleans);
        new C_double_boolean_int().f(ints);

        new C_byte_boolean_short().f(bytes);
        new C_byte_boolean_short().f(booleans);
        new C_byte_boolean_short().f(shorts);
        new C_char_boolean_short().f(chars);
        new C_char_boolean_short().f(booleans);
        new C_char_boolean_short().f(shorts);
        new C_int_boolean_short().f(ints);
        new C_int_boolean_short().f(booleans);
        new C_int_boolean_short().f(shorts);
        new C_long_boolean_short().f(longs);
        new C_long_boolean_short().f(booleans);
        new C_long_boolean_short().f(shorts);
        new C_float_boolean_short().f(floats);
        new C_float_boolean_short().f(booleans);
        new C_float_boolean_short().f(shorts);
        new C_double_boolean_short().f(doubles);
        new C_double_boolean_short().f(booleans);
        new C_double_boolean_short().f(shorts);

        new C_byte_boolean_char().f(bytes);
        new C_byte_boolean_char().f(booleans);
        new C_byte_boolean_char().f(chars);
        new C_short_boolean_char().f(shorts);
        new C_short_boolean_char().f(booleans);
        new C_short_boolean_char().f(chars);
        new C_int_boolean_char().f(ints);
        new C_int_boolean_char().f(booleans);
        new C_int_boolean_char().f(chars);
        new C_long_boolean_char().f(longs);
        new C_long_boolean_char().f(booleans);
        new C_long_boolean_char().f(chars);
        new C_float_boolean_char().f(floats);
        new C_float_boolean_char().f(booleans);
        new C_float_boolean_char().f(chars);
        new C_double_boolean_char().f(doubles);
        new C_double_boolean_char().f(booleans);
        new C_double_boolean_char().f(chars);

        // ------------------------------------------------------------
        new C_char_byte_boolean().f(chars);
        new C_char_byte_boolean().f(bytes);
        new C_char_byte_boolean().f(booleans);
        new C_short_byte_boolean().f(shorts);
        new C_short_byte_boolean().f(bytes);
        new C_short_byte_boolean().f(booleans);
        new C_int_byte_boolean().f(ints);
        new C_int_byte_boolean().f(bytes);
        new C_int_byte_boolean().f(booleans);
        new C_long_byte_boolean().f(longs);
        new C_long_byte_boolean().f(bytes);
        new C_long_byte_boolean().f(booleans);
        new C_float_byte_boolean().f(floats);
        new C_float_byte_boolean().f(bytes);
        new C_float_byte_boolean().f(booleans);
        new C_double_byte_boolean().f(doubles);
        new C_double_byte_boolean().f(bytes);
        new C_double_byte_boolean().f(booleans);

        new C_boolean_byte_char().f(booleans);
        new C_boolean_byte_char().f(bytes);
        new C_boolean_byte_char().f(chars);
        new C_short_byte_char().f(shorts);
        new C_short_byte_char().f(bytes);
        new C_short_byte_char().f(chars);
        new C_int_byte_char().f(ints);
        new C_int_byte_char().f(bytes);
        new C_int_byte_char().f(chars);
        new C_long_byte_char().f(longs);
        new C_long_byte_char().f(bytes);
        new C_long_byte_char().f(chars);
        new C_float_byte_char().f(floats);
        new C_float_byte_char().f(bytes);
        new C_float_byte_char().f(chars);
        new C_double_byte_char().f(doubles);
        new C_double_byte_char().f(bytes);
        new C_double_byte_char().f(chars);

        new C_boolean_byte_short().f(booleans);
        new C_boolean_byte_short().f(bytes);
        new C_boolean_byte_short().f(shorts);
        new C_char_byte_short().f(chars);
        new C_char_byte_short().f(bytes);
        new C_char_byte_short().f(shorts);
        new C_int_byte_short().f(ints);
        new C_int_byte_short().f(bytes);
        new C_int_byte_short().f(shorts);
        new C_long_byte_short().f(longs);
        new C_long_byte_short().f(bytes);
        new C_long_byte_short().f(shorts);
        new C_float_byte_short().f(floats);
        new C_float_byte_short().f(bytes);
        new C_float_byte_short().f(shorts);
        new C_double_byte_short().f(doubles);
        new C_double_byte_short().f(bytes);
        new C_double_byte_short().f(shorts);

        new C_boolean_byte_int().f(booleans);
        new C_boolean_byte_int().f(bytes);
        new C_boolean_byte_int().f(ints);
        new C_char_byte_int().f(chars);
        new C_char_byte_int().f(bytes);
        new C_char_byte_int().f(ints);
        new C_short_byte_int().f(shorts);
        new C_short_byte_int().f(bytes);
        new C_short_byte_int().f(ints);
        new C_long_byte_int().f(longs);
        new C_long_byte_int().f(bytes);
        new C_long_byte_int().f(ints);
        new C_float_byte_int().f(floats);
        new C_float_byte_int().f(bytes);
        new C_float_byte_int().f(ints);
        new C_double_byte_int().f(doubles);
        new C_double_byte_int().f(bytes);
        new C_double_byte_int().f(ints);

        new C_boolean_byte_long().f(booleans);
        new C_boolean_byte_long().f(bytes);
        new C_boolean_byte_long().f(longs);
        new C_char_byte_long().f(chars);
        new C_char_byte_long().f(bytes);
        new C_char_byte_long().f(longs);
        new C_short_byte_long().f(shorts);
        new C_short_byte_long().f(bytes);
        new C_short_byte_long().f(longs);
        new C_int_byte_long().f(ints);
        new C_int_byte_long().f(bytes);
        new C_int_byte_long().f(longs);
        new C_float_byte_long().f(floats);
        new C_float_byte_long().f(bytes);
        new C_float_byte_long().f(longs);
        new C_double_byte_long().f(doubles);
        new C_double_byte_long().f(bytes);
        new C_double_byte_long().f(longs);

        new C_boolean_byte_float().f(booleans);
        new C_boolean_byte_float().f(bytes);
        new C_boolean_byte_float().f(floats);
        new C_char_byte_float().f(chars);
        new C_char_byte_float().f(bytes);
        new C_char_byte_float().f(floats);
        new C_short_byte_float().f(shorts);
        new C_short_byte_float().f(bytes);
        new C_short_byte_float().f(floats);
        new C_int_byte_float().f(ints);
        new C_int_byte_float().f(bytes);
        new C_int_byte_float().f(floats);
        new C_long_byte_float().f(longs);
        new C_long_byte_float().f(bytes);
        new C_long_byte_float().f(floats);
        new C_double_byte_float().f(doubles);
        new C_double_byte_float().f(bytes);
        new C_double_byte_float().f(floats);

        new C_boolean_byte_double().f(booleans);
        new C_boolean_byte_double().f(bytes);
        new C_boolean_byte_double().f(doubles);
        new C_char_byte_double().f(chars);
        new C_char_byte_double().f(bytes);
        new C_char_byte_double().f(doubles);
        new C_short_byte_double().f(shorts);
        new C_short_byte_double().f(bytes);
        new C_short_byte_double().f(doubles);
        new C_int_byte_double().f(ints);
        new C_int_byte_double().f(bytes);
        new C_int_byte_double().f(doubles);
        new C_long_byte_double().f(longs);
        new C_long_byte_double().f(bytes);
        new C_long_byte_double().f(doubles);
        new C_float_byte_double().f(floats);
        new C_float_byte_double().f(bytes);
        new C_float_byte_double().f(doubles);

        // ------------------------------------------------------------

        new C_byte_char_boolean().f(bytes);
        new C_byte_char_boolean().f(chars);
        new C_byte_char_boolean().f(booleans);
        new C_short_char_boolean().f(shorts);
        new C_short_char_boolean().f(chars);
        new C_short_char_boolean().f(booleans);
        new C_int_char_boolean().f(ints);
        new C_int_char_boolean().f(chars);
        new C_int_char_boolean().f(booleans);
        new C_long_char_boolean().f(longs);
        new C_long_char_boolean().f(chars);
        new C_long_char_boolean().f(booleans);
        new C_float_char_boolean().f(floats);
        new C_float_char_boolean().f(chars);
        new C_float_char_boolean().f(booleans);
        new C_double_char_boolean().f(doubles);
        new C_double_char_boolean().f(chars);
        new C_double_char_boolean().f(booleans);

        new C_boolean_char_byte().f(booleans);
        new C_boolean_char_byte().f(chars);
        new C_boolean_char_byte().f(bytes);
        new C_short_char_byte().f(shorts);
        new C_short_char_byte().f(chars);
        new C_short_char_byte().f(bytes);
        new C_int_char_byte().f(ints);
        new C_int_char_byte().f(chars);
        new C_int_char_byte().f(bytes);
        new C_long_char_byte().f(longs);
        new C_long_char_byte().f(chars);
        new C_long_char_byte().f(bytes);
        new C_float_char_byte().f(floats);
        new C_float_char_byte().f(chars);
        new C_float_char_byte().f(bytes);
        new C_double_char_byte().f(doubles);
        new C_double_char_byte().f(chars);
        new C_double_char_byte().f(bytes);

        new C_boolean_char_short().f(booleans);
        new C_boolean_char_short().f(chars);
        new C_boolean_char_short().f(shorts);
        new C_byte_char_short().f(bytes);
        new C_byte_char_short().f(chars);
        new C_byte_char_short().f(shorts);
        new C_int_char_short().f(ints);
        new C_int_char_short().f(chars);
        new C_int_char_short().f(shorts);
        new C_long_char_short().f(longs);
        new C_long_char_short().f(chars);
        new C_long_char_short().f(shorts);
        new C_float_char_short().f(floats);
        new C_float_char_short().f(chars);
        new C_float_char_short().f(shorts);
        new C_double_char_short().f(doubles);
        new C_double_char_short().f(chars);
        new C_double_char_short().f(shorts);

        new C_boolean_char_int().f(booleans);
        new C_boolean_char_int().f(chars);
        new C_boolean_char_int().f(ints);
        new C_byte_char_int().f(bytes);
        new C_byte_char_int().f(chars);
        new C_byte_char_int().f(ints);
        new C_short_char_int().f(shorts);
        new C_short_char_int().f(chars);
        new C_short_char_int().f(ints);
        new C_long_char_int().f(longs);
        new C_long_char_int().f(chars);
        new C_long_char_int().f(ints);
        new C_float_char_int().f(floats);
        new C_float_char_int().f(chars);
        new C_float_char_int().f(ints);
        new C_double_char_int().f(doubles);
        new C_double_char_int().f(chars);
        new C_double_char_int().f(ints);

        new C_boolean_char_long().f(booleans);
        new C_boolean_char_long().f(chars);
        new C_boolean_char_long().f(longs);
        new C_byte_char_long().f(bytes);
        new C_byte_char_long().f(chars);
        new C_byte_char_long().f(longs);
        new C_short_char_long().f(shorts);
        new C_short_char_long().f(chars);
        new C_short_char_long().f(longs);
        new C_int_char_long().f(ints);
        new C_int_char_long().f(chars);
        new C_int_char_long().f(longs);
        new C_float_char_long().f(floats);
        new C_float_char_long().f(chars);
        new C_float_char_long().f(longs);
        new C_double_char_long().f(doubles);
        new C_double_char_long().f(chars);
        new C_double_char_long().f(longs);

        new C_boolean_char_float().f(booleans);
        new C_boolean_char_float().f(chars);
        new C_boolean_char_float().f(floats);
        new C_byte_char_float().f(bytes);
        new C_byte_char_float().f(chars);
        new C_byte_char_float().f(floats);
        new C_short_char_float().f(shorts);
        new C_short_char_float().f(chars);
        new C_short_char_float().f(floats);
        new C_int_char_float().f(ints);
        new C_int_char_float().f(chars);
        new C_int_char_float().f(floats);
        new C_long_char_float().f(longs);
        new C_long_char_float().f(chars);
        new C_long_char_float().f(floats);
        new C_double_char_float().f(doubles);
        new C_double_char_float().f(chars);
        new C_double_char_float().f(floats);

        new C_boolean_char_double().f(booleans);
        new C_boolean_char_double().f(chars);
        new C_boolean_char_double().f(doubles);
        new C_byte_char_double().f(bytes);
        new C_byte_char_double().f(chars);
        new C_byte_char_double().f(doubles);
        new C_short_char_double().f(shorts);
        new C_short_char_double().f(chars);
        new C_short_char_double().f(doubles);
        new C_int_char_double().f(ints);
        new C_int_char_double().f(chars);
        new C_int_char_double().f(doubles);
        new C_long_char_double().f(longs);
        new C_long_char_double().f(chars);
        new C_long_char_double().f(doubles);
        new C_float_char_double().f(floats);
        new C_float_char_double().f(chars);
        new C_float_char_double().f(doubles);

        //XX
        new C_byte_double_boolean().f(bytes);
        new C_byte_double_boolean().f(doubles);
        new C_byte_double_boolean().f(booleans);
        new C_char_double_boolean().f(chars);
        new C_char_double_boolean().f(doubles);
        new C_char_double_boolean().f(booleans);
        new C_short_double_boolean().f(shorts);
        new C_short_double_boolean().f(doubles);
        new C_short_double_boolean().f(booleans);
        new C_int_double_boolean().f(ints);
        new C_int_double_boolean().f(doubles);
        new C_int_double_boolean().f(booleans);
        new C_long_double_boolean().f(longs);
        new C_long_double_boolean().f(doubles);
        new C_long_double_boolean().f(booleans);
        new C_float_double_boolean().f(floats);
        new C_float_double_boolean().f(doubles);
        new C_float_double_boolean().f(booleans);

        new C_boolean_double_byte().f(booleans);
        new C_boolean_double_byte().f(doubles);
        new C_boolean_double_byte().f(bytes);
        new C_char_double_byte().f(chars);
        new C_char_double_byte().f(doubles);
        new C_char_double_byte().f(bytes);
        new C_short_double_byte().f(shorts);
        new C_short_double_byte().f(doubles);
        new C_short_double_byte().f(bytes);
        new C_int_double_byte().f(ints);
        new C_int_double_byte().f(doubles);
        new C_int_double_byte().f(bytes);
        new C_long_double_byte().f(longs);
        new C_long_double_byte().f(doubles);
        new C_long_double_byte().f(bytes);
        new C_float_double_byte().f(floats);
        new C_float_double_byte().f(doubles);
        new C_float_double_byte().f(bytes);

        new C_boolean_double_char().f(booleans);
        new C_boolean_double_char().f(doubles);
        new C_boolean_double_char().f(chars);
        new C_byte_double_char().f(bytes);
        new C_byte_double_char().f(doubles);
        new C_byte_double_char().f(chars);
        new C_short_double_char().f(shorts);
        new C_short_double_char().f(doubles);
        new C_short_double_char().f(chars);
        new C_int_double_char().f(ints);
        new C_int_double_char().f(doubles);
        new C_int_double_char().f(chars);
        new C_long_double_char().f(longs);
        new C_long_double_char().f(doubles);
        new C_long_double_char().f(chars);
        new C_float_double_char().f(floats);
        new C_float_double_char().f(doubles);
        new C_float_double_char().f(chars);

        new C_boolean_double_short().f(booleans);
        new C_boolean_double_short().f(doubles);
        new C_boolean_double_short().f(shorts);
        new C_byte_double_short().f(bytes);
        new C_byte_double_short().f(doubles);
        new C_byte_double_short().f(shorts);
        new C_char_double_short().f(chars);
        new C_char_double_short().f(doubles);
        new C_char_double_short().f(shorts);
        new C_int_double_short().f(ints);
        new C_int_double_short().f(doubles);
        new C_int_double_short().f(shorts);
        new C_long_double_short().f(longs);
        new C_long_double_short().f(doubles);
        new C_long_double_short().f(shorts);
        new C_float_double_short().f(floats);
        new C_float_double_short().f(doubles);
        new C_float_double_short().f(shorts);

        new C_boolean_double_int().f(booleans);
        new C_boolean_double_int().f(doubles);
        new C_boolean_double_int().f(ints);
        new C_byte_double_int().f(bytes);
        new C_byte_double_int().f(doubles);
        new C_byte_double_int().f(ints);
        new C_char_double_int().f(chars);
        new C_char_double_int().f(doubles);
        new C_char_double_int().f(ints);
        new C_short_double_int().f(shorts);
        new C_short_double_int().f(doubles);
        new C_short_double_int().f(ints);
        new C_long_double_int().f(longs);
        new C_long_double_int().f(doubles);
        new C_long_double_int().f(ints);
        new C_float_double_int().f(floats);
        new C_float_double_int().f(doubles);
        new C_float_double_int().f(ints);

        new C_boolean_double_long().f(booleans);
        new C_boolean_double_long().f(doubles);
        new C_boolean_double_long().f(longs);
        new C_byte_double_long().f(bytes);
        new C_byte_double_long().f(doubles);
        new C_byte_double_long().f(longs);
        new C_char_double_long().f(chars);
        new C_char_double_long().f(doubles);
        new C_char_double_long().f(longs);
        new C_short_double_long().f(shorts);
        new C_short_double_long().f(doubles);
        new C_short_double_long().f(longs);
        new C_int_double_long().f(ints);
        new C_int_double_long().f(doubles);
        new C_int_double_long().f(longs);
        new C_float_double_long().f(floats);
        new C_float_double_long().f(doubles);
        new C_float_double_long().f(longs);

        new C_boolean_double_float().f(booleans);
        new C_boolean_double_float().f(doubles);
        new C_boolean_double_float().f(floats);
        new C_byte_double_float().f(bytes);
        new C_byte_double_float().f(doubles);
        new C_byte_double_float().f(floats);
        new C_char_double_float().f(chars);
        new C_char_double_float().f(doubles);
        new C_char_double_float().f(floats);
        new C_short_double_float().f(shorts);
        new C_short_double_float().f(doubles);
        new C_short_double_float().f(floats);
        new C_int_double_float().f(ints);
        new C_int_double_float().f(doubles);
        new C_int_double_float().f(floats);
        new C_long_double_float().f(longs);
        new C_long_double_float().f(doubles);
        new C_long_double_float().f(floats);

        // ------------------------------------------------------------
        new C_byte_float_boolean().f(bytes);
        new C_byte_float_boolean().f(floats);
        new C_byte_float_boolean().f(booleans);
        new C_char_float_boolean().f(chars);
        new C_char_float_boolean().f(floats);
        new C_char_float_boolean().f(booleans);
        new C_short_float_boolean().f(shorts);
        new C_short_float_boolean().f(floats);
        new C_short_float_boolean().f(booleans);
        new C_int_float_boolean().f(ints);
        new C_int_float_boolean().f(floats);
        new C_int_float_boolean().f(booleans);
        new C_long_float_boolean().f(longs);
        new C_long_float_boolean().f(floats);
        new C_long_float_boolean().f(booleans);
        new C_double_float_boolean().f(doubles);
        new C_double_float_boolean().f(floats);
        new C_double_float_boolean().f(booleans);

        new C_boolean_float_byte().f(booleans);
        new C_boolean_float_byte().f(floats);
        new C_boolean_float_byte().f(bytes);
        new C_char_float_byte().f(chars);
        new C_char_float_byte().f(floats);
        new C_char_float_byte().f(bytes);
        new C_short_float_byte().f(shorts);
        new C_short_float_byte().f(floats);
        new C_short_float_byte().f(bytes);
        new C_int_float_byte().f(ints);
        new C_int_float_byte().f(floats);
        new C_int_float_byte().f(bytes);
        new C_long_float_byte().f(longs);
        new C_long_float_byte().f(floats);
        new C_long_float_byte().f(bytes);
        new C_double_float_byte().f(doubles);
        new C_double_float_byte().f(floats);
        new C_double_float_byte().f(bytes);

        new C_boolean_float_char().f(booleans);
        new C_boolean_float_char().f(floats);
        new C_boolean_float_char().f(chars);
        new C_byte_float_char().f(bytes);
        new C_byte_float_char().f(floats);
        new C_byte_float_char().f(chars);
        new C_short_float_char().f(shorts);
        new C_short_float_char().f(floats);
        new C_short_float_char().f(chars);
        new C_int_float_char().f(ints);
        new C_int_float_char().f(floats);
        new C_int_float_char().f(chars);
        new C_long_float_char().f(longs);
        new C_long_float_char().f(floats);
        new C_long_float_char().f(chars);
        new C_double_float_char().f(doubles);
        new C_double_float_char().f(floats);
        new C_double_float_char().f(chars);

        new C_boolean_float_short().f(booleans);
        new C_boolean_float_short().f(floats);
        new C_boolean_float_short().f(shorts);
        new C_byte_float_short().f(bytes);
        new C_byte_float_short().f(floats);
        new C_byte_float_short().f(shorts);
        new C_char_float_short().f(chars);
        new C_char_float_short().f(floats);
        new C_char_float_short().f(shorts);
        new C_int_float_short().f(ints);
        new C_int_float_short().f(floats);
        new C_int_float_short().f(shorts);
        new C_long_float_short().f(longs);
        new C_long_float_short().f(floats);
        new C_long_float_short().f(shorts);
        new C_double_float_short().f(doubles);
        new C_double_float_short().f(floats);
        new C_double_float_short().f(shorts);

        new C_boolean_float_int().f(booleans);
        new C_boolean_float_int().f(floats);
        new C_boolean_float_int().f(ints);
        new C_byte_float_int().f(bytes);
        new C_byte_float_int().f(floats);
        new C_byte_float_int().f(ints);
        new C_char_float_int().f(chars);
        new C_char_float_int().f(floats);
        new C_char_float_int().f(ints);
        new C_short_float_int().f(shorts);
        new C_short_float_int().f(floats);
        new C_short_float_int().f(ints);
        new C_long_float_int().f(longs);
        new C_long_float_int().f(floats);
        new C_long_float_int().f(ints);
        new C_double_float_int().f(doubles);
        new C_double_float_int().f(floats);
        new C_double_float_int().f(ints);

        new C_boolean_float_long().f(booleans);
        new C_boolean_float_long().f(floats);
        new C_boolean_float_long().f(longs);
        new C_byte_float_long().f(bytes);
        new C_byte_float_long().f(floats);
        new C_byte_float_long().f(longs);
        new C_char_float_long().f(chars);
        new C_char_float_long().f(floats);
        new C_char_float_long().f(longs);
        new C_short_float_long().f(shorts);
        new C_short_float_long().f(floats);
        new C_short_float_long().f(longs);
        new C_long_float_long().f(longs);
        new C_long_float_long().f(floats);
        new C_long_float_long().f(longs);
        new C_double_float_long().f(doubles);
        new C_double_float_long().f(floats);
        new C_double_float_long().f(longs);

        new C_boolean_float_double().f(booleans);
        new C_boolean_float_double().f(floats);
        new C_boolean_float_double().f(doubles);
        new C_byte_float_double().f(bytes);
        new C_byte_float_double().f(floats);
        new C_byte_float_double().f(doubles);
        new C_char_float_double().f(chars);
        new C_char_float_double().f(floats);
        new C_char_float_double().f(doubles);
        new C_short_float_double().f(shorts);
        new C_short_float_double().f(floats);
        new C_short_float_double().f(doubles);
        new C_int_float_double().f(ints);
        new C_int_float_double().f(floats);
        new C_int_float_double().f(doubles);
        new C_long_float_double().f(longs);
        new C_long_float_double().f(floats);
        new C_long_float_double().f(doubles);

        // ------------------------------------------------------------
        new C_byte_int_boolean().f(bytes);
        new C_byte_int_boolean().f(ints);
        new C_byte_int_boolean().f(booleans);
        new C_char_int_boolean().f(chars);
        new C_char_int_boolean().f(ints);
        new C_char_int_boolean().f(booleans);
        new C_short_int_boolean().f(shorts);
        new C_short_int_boolean().f(ints);
        new C_short_int_boolean().f(booleans);
        new C_long_int_boolean().f(longs);
        new C_long_int_boolean().f(ints);
        new C_long_int_boolean().f(booleans);
        new C_float_int_boolean().f(floats);
        new C_float_int_boolean().f(ints);
        new C_float_int_boolean().f(booleans);
        new C_double_int_boolean().f(doubles);
        new C_double_int_boolean().f(ints);
        new C_double_int_boolean().f(booleans);

        new C_boolean_int_byte().f(booleans);
        new C_boolean_int_byte().f(ints);
        new C_boolean_int_byte().f(bytes);
        new C_char_int_byte().f(chars);
        new C_char_int_byte().f(ints);
        new C_char_int_byte().f(bytes);
        new C_short_int_byte().f(shorts);
        new C_short_int_byte().f(ints);
        new C_short_int_byte().f(bytes);
        new C_long_int_byte().f(longs);
        new C_long_int_byte().f(ints);
        new C_long_int_byte().f(bytes);
        new C_float_int_byte().f(floats);
        new C_float_int_byte().f(ints);
        new C_float_int_byte().f(bytes);
        new C_double_int_byte().f(doubles);
        new C_double_int_byte().f(ints);
        new C_double_int_byte().f(bytes);

        new C_boolean_int_char().f(booleans);
        new C_boolean_int_char().f(ints);
        new C_boolean_int_char().f(chars);
        new C_byte_int_char().f(bytes);
        new C_byte_int_char().f(ints);
        new C_byte_int_char().f(chars);
        new C_short_int_char().f(shorts);
        new C_short_int_char().f(ints);
        new C_short_int_char().f(chars);
        new C_long_int_char().f(longs);
        new C_long_int_char().f(ints);
        new C_long_int_char().f(chars);
        new C_float_int_char().f(floats);
        new C_float_int_char().f(ints);
        new C_float_int_char().f(chars);
        new C_double_int_char().f(doubles);
        new C_double_int_char().f(ints);
        new C_double_int_char().f(chars);

        new C_boolean_int_short().f(booleans);
        new C_boolean_int_short().f(ints);
        new C_boolean_int_short().f(shorts);
        new C_byte_int_short().f(bytes);
        new C_byte_int_short().f(ints);
        new C_byte_int_short().f(shorts);
        new C_char_int_short().f(chars);
        new C_char_int_short().f(ints);
        new C_char_int_short().f(shorts);
        new C_long_int_short().f(longs);
        new C_long_int_short().f(ints);
        new C_long_int_short().f(shorts);
        new C_float_int_short().f(floats);
        new C_float_int_short().f(ints);
        new C_float_int_short().f(shorts);
        new C_double_int_short().f(doubles);
        new C_double_int_short().f(ints);
        new C_double_int_short().f(shorts);

        new C_boolean_int_long().f(booleans);
        new C_boolean_int_long().f(ints);
        new C_boolean_int_long().f(longs);
        new C_byte_int_long().f(bytes);
        new C_byte_int_long().f(ints);
        new C_byte_int_long().f(longs);
        new C_char_int_long().f(chars);
        new C_char_int_long().f(ints);
        new C_char_int_long().f(longs);
        new C_short_int_long().f(shorts);
        new C_short_int_long().f(ints);
        new C_short_int_long().f(longs);
        new C_float_int_long().f(floats);
        new C_float_int_long().f(ints);
        new C_float_int_long().f(longs);
        new C_double_int_long().f(doubles);
        new C_double_int_long().f(ints);
        new C_double_int_long().f(longs);

        new C_boolean_int_float().f(booleans);
        new C_boolean_int_float().f(ints);
        new C_boolean_int_float().f(floats);
        new C_byte_int_float().f(bytes);
        new C_byte_int_float().f(ints);
        new C_byte_int_float().f(floats);
        new C_char_int_float().f(chars);
        new C_char_int_float().f(ints);
        new C_char_int_float().f(floats);
        new C_short_int_float().f(shorts);
        new C_short_int_float().f(ints);
        new C_short_int_float().f(floats);
        new C_long_int_float().f(longs);
        new C_long_int_float().f(ints);
        new C_long_int_float().f(floats);
        new C_double_int_float().f(doubles);
        new C_double_int_float().f(ints);
        new C_double_int_float().f(floats);

        new C_boolean_int_double().f(booleans);
        new C_boolean_int_double().f(ints);
        new C_boolean_int_double().f(doubles);
        new C_byte_int_double().f(bytes);
        new C_byte_int_double().f(ints);
        new C_byte_int_double().f(doubles);
        new C_char_int_double().f(chars);
        new C_char_int_double().f(ints);
        new C_char_int_double().f(doubles);
        new C_short_int_double().f(shorts);
        new C_short_int_double().f(ints);
        new C_short_int_double().f(doubles);
        new C_long_int_double().f(longs);
        new C_long_int_double().f(ints);
        new C_long_int_double().f(doubles);
        new C_float_int_double().f(floats);
        new C_float_int_double().f(ints);
        new C_float_int_double().f(doubles);

        // ------------------------------------------------------------
        new C_byte_short_boolean().f(bytes);
        new C_byte_short_boolean().f(shorts);
        new C_byte_short_boolean().f(booleans);
        new C_char_short_boolean().f(chars);
        new C_char_short_boolean().f(shorts);
        new C_char_short_boolean().f(booleans);
        new C_int_short_boolean().f(ints);
        new C_int_short_boolean().f(shorts);
        new C_int_short_boolean().f(booleans);
        new C_long_short_boolean().f(longs);
        new C_long_short_boolean().f(shorts);
        new C_long_short_boolean().f(booleans);
        new C_float_short_boolean().f(floats);
        new C_float_short_boolean().f(shorts);
        new C_float_short_boolean().f(booleans);
        new C_double_short_boolean().f(doubles);
        new C_double_short_boolean().f(shorts);
        new C_double_short_boolean().f(booleans);

        new C_boolean_short_byte().f(booleans);
        new C_boolean_short_byte().f(shorts);
        new C_boolean_short_byte().f(bytes);
        new C_char_short_byte().f(chars);
        new C_char_short_byte().f(shorts);
        new C_char_short_byte().f(bytes);
        new C_int_short_byte().f(ints);
        new C_int_short_byte().f(shorts);
        new C_int_short_byte().f(bytes);
        new C_long_short_byte().f(longs);
        new C_long_short_byte().f(shorts);
        new C_long_short_byte().f(bytes);
        new C_float_short_byte().f(floats);
        new C_float_short_byte().f(shorts);
        new C_float_short_byte().f(bytes);
        new C_double_short_byte().f(doubles);
        new C_double_short_byte().f(shorts);
        new C_double_short_byte().f(bytes);

        new C_boolean_short_char().f(booleans);
        new C_boolean_short_char().f(shorts);
        new C_boolean_short_char().f(chars);
        new C_byte_short_char().f(bytes);
        new C_byte_short_char().f(shorts);
        new C_byte_short_char().f(chars);
        new C_int_short_char().f(ints);
        new C_int_short_char().f(shorts);
        new C_int_short_char().f(chars);
        new C_long_short_char().f(longs);
        new C_long_short_char().f(shorts);
        new C_long_short_char().f(chars);
        new C_float_short_char().f(floats);
        new C_float_short_char().f(shorts);
        new C_float_short_char().f(chars);
        new C_double_short_char().f(doubles);
        new C_double_short_char().f(shorts);
        new C_double_short_char().f(chars);

        new C_boolean_short_int().f(booleans);
        new C_boolean_short_int().f(shorts);
        new C_boolean_short_int().f(ints);
        new C_byte_short_int().f(bytes);
        new C_byte_short_int().f(shorts);
        new C_byte_short_int().f(ints);
        new C_char_short_int().f(chars);
        new C_char_short_int().f(shorts);
        new C_char_short_int().f(ints);
        new C_long_short_int().f(longs);
        new C_long_short_int().f(shorts);
        new C_long_short_int().f(ints);
        new C_float_short_int().f(floats);
        new C_float_short_int().f(shorts);
        new C_float_short_int().f(ints);
        new C_double_short_int().f(doubles);
        new C_double_short_int().f(shorts);
        new C_double_short_int().f(ints);
        
        new C_boolean_short_long().f(booleans);
        new C_boolean_short_long().f(shorts);
        new C_boolean_short_long().f(longs);
        new C_byte_short_long().f(bytes);
        new C_byte_short_long().f(shorts);
        new C_byte_short_long().f(longs);
        new C_char_short_long().f(chars);
        new C_char_short_long().f(shorts);
        new C_char_short_long().f(longs);
        new C_int_short_long().f(ints);
        new C_int_short_long().f(shorts);
        new C_int_short_long().f(longs);
        new C_float_short_long().f(floats);
        new C_float_short_long().f(shorts);
        new C_float_short_long().f(longs);
        new C_double_short_long().f(doubles);
        new C_double_short_long().f(shorts);
        new C_double_short_long().f(longs);
        
        new C_boolean_short_float().f(booleans);
        new C_boolean_short_float().f(shorts);
        new C_boolean_short_float().f(floats);
        new C_byte_short_float().f(bytes);
        new C_byte_short_float().f(shorts);
        new C_byte_short_float().f(floats);
        new C_char_short_float().f(chars);
        new C_char_short_float().f(shorts);
        new C_char_short_float().f(floats);
        new C_int_short_float().f(ints);
        new C_int_short_float().f(shorts);
        new C_int_short_float().f(floats);
        new C_long_short_float().f(longs);
        new C_long_short_float().f(shorts);
        new C_long_short_float().f(floats);
        new C_double_short_float().f(doubles);
        new C_double_short_float().f(shorts);
        new C_double_short_float().f(floats);
        
        new C_boolean_short_double().f(booleans);
        new C_boolean_short_double().f(shorts);
        new C_boolean_short_double().f(doubles);
        new C_byte_short_double().f(bytes);
        new C_byte_short_double().f(shorts);
        new C_byte_short_double().f(doubles);
        new C_char_short_double().f(chars);
        new C_char_short_double().f(shorts);
        new C_char_short_double().f(doubles);
        new C_int_short_double().f(ints);
        new C_int_short_double().f(shorts);
        new C_int_short_double().f(doubles);
        new C_long_short_double().f(longs);
        new C_long_short_double().f(shorts);
        new C_long_short_double().f(doubles);
        new C_float_short_double().f(floats);
        new C_float_short_double().f(shorts);
        new C_float_short_double().f(doubles);

        org.aspectj.testing.Tester.checkAllEventsIgnoreDups();
        
    }
    
    void m(String msg) { org.aspectj.testing.Tester.expectEvent(msg); }
    
    void wants() {
        m("C_boolean-boolean");
        m("C_byte-byte");
        m("C_char-char");
        m("C_short-short");
        m("C_int-int");
        m("C_long-long");
        m("C_float-float");
        m("C_double-double");

        m("C_byte_double_boolean-byte");
        m("C_char_double_boolean-char");
        m("C_short_double_boolean-short");
        m("C_int_double_boolean-int");
        m("C_long_double_boolean-long");
        m("C_float_double_boolean-float");

        m("C_boolean_double_byte-boolean");
        m("C_char_double_byte-char");
        m("C_short_double_byte-short");
        m("C_int_double_byte-int");
        m("C_long_double_byte-long");
        m("C_float_double_byte-float");

        m("C_boolean_double_char-boolean");
        m("C_byte_double_char-byte");
        m("C_short_double_char-short");
        m("C_int_double_char-int");
        m("C_long_double_char-long");
        m("C_float_double_char-float");

        m("C_boolean_double_short-boolean");
        m("C_byte_double_short-byte");
        m("C_char_double_short-char");
        m("C_int_double_short-int");
        m("C_long_double_short-long");
        m("C_float_double_short-float");

        m("C_boolean_double_int-boolean");
        m("C_byte_double_int-byte");
        m("C_char_double_int-char");
        m("C_short_double_int-short");
        m("C_long_double_int-long");
        m("C_float_double_int-float");

        m("C_boolean_double_long-boolean");
        m("C_byte_double_long-byte");
        m("C_char_double_long-char");
        m("C_short_double_long-short");
        m("C_int_double_long-int");
        m("C_float_double_long-float");

        m("C_boolean_double_float-boolean");
        m("C_byte_double_float-byte");
        m("C_char_double_float-char");
        m("C_short_double_float-short");
        m("C_int_double_float-int");
        m("C_long_double_float-long");

// ------------------------------------------------------------
        m("C_byte_float_boolean-byte");
        m("C_char_float_boolean-char");
        m("C_short_float_boolean-short");
        m("C_int_float_boolean-int");
        m("C_long_float_boolean-long");
        m("C_double_float_boolean-double");

        m("C_boolean_float_byte-boolean");
        m("C_char_float_byte-char");
        m("C_short_float_byte-short");
        m("C_int_float_byte-int");
        m("C_long_float_byte-long");
        m("C_double_float_byte-double");

        m("C_boolean_float_char-boolean");
        m("C_byte_float_char-byte");
        m("C_short_float_char-short");
        m("C_int_float_char-int");
        m("C_long_float_char-long");
        m("C_double_float_char-double");

        m("C_boolean_float_short-boolean");
        m("C_byte_float_short-byte");
        m("C_char_float_short-char");
        m("C_int_float_short-int");
        m("C_long_float_short-long");
        m("C_double_float_short-double");

        m("C_boolean_float_int-boolean");
        m("C_byte_float_int-byte");
        m("C_char_float_int-char");
        m("C_short_float_int-short");
        m("C_long_float_int-long");
        m("C_double_float_int-double");

        m("C_boolean_float_long-boolean");
        m("C_byte_float_long-byte");
        m("C_char_float_long-char");
        m("C_short_float_long-short");
        m("C_long_float_long-long");
        m("C_double_float_long-double");

        m("C_boolean_float_double-boolean");
        m("C_byte_float_double-byte");
        m("C_char_float_double-char");
        m("C_short_float_double-short");
        m("C_int_float_double-int");
        m("C_long_float_double-long");

// ------------------------------------------------------------
        m("C_byte_int_boolean-byte");
        m("C_char_int_boolean-char");
        m("C_short_int_boolean-short");
        m("C_long_int_boolean-long");
        m("C_float_int_boolean-float");
        m("C_double_int_boolean-double");

        m("C_boolean_int_byte-boolean");
        m("C_char_int_byte-char");
        m("C_short_int_byte-short");
        m("C_long_int_byte-long");
        m("C_float_int_byte-float");
        m("C_double_int_byte-double");

        m("C_boolean_int_char-boolean");
        m("C_byte_int_char-byte");
        m("C_short_int_char-short");
        m("C_long_int_char-long");
        m("C_float_int_char-float");
        m("C_double_int_char-double");

        m("C_boolean_int_short-boolean");
        m("C_byte_int_short-byte");
        m("C_char_int_short-char");
        m("C_long_int_short-long");
        m("C_float_int_short-float");
        m("C_double_int_short-double");

        m("C_boolean_int_long-boolean");
        m("C_byte_int_long-byte");
        m("C_char_int_long-char");
        m("C_short_int_long-short");
        m("C_float_int_long-float");
        m("C_double_int_long-double");

        m("C_boolean_int_float-boolean");
        m("C_byte_int_float-byte");
        m("C_char_int_float-char");
        m("C_short_int_float-short");
        m("C_long_int_float-long");
        m("C_double_int_float-double");

        m("C_boolean_int_double-boolean");
        m("C_byte_int_double-byte");
        m("C_char_int_double-char");
        m("C_short_int_double-short");
        m("C_long_int_double-long");
        m("C_float_int_double-float");

// ------------------------------------------------------------
        m("C_byte_short_boolean-byte");
        m("C_char_short_boolean-char");
        m("C_int_short_boolean-int");
        m("C_long_short_boolean-long");
        m("C_float_short_boolean-float");
        m("C_double_short_boolean-double");

        m("C_boolean_short_byte-boolean");
        m("C_char_short_byte-char");
        m("C_int_short_byte-int");
        m("C_long_short_byte-long");
        m("C_float_short_byte-float");
        m("C_double_short_byte-double");

        m("C_boolean_short_char-boolean");
        m("C_byte_short_char-byte");
        m("C_int_short_char-int");
        m("C_long_short_char-long");
        m("C_float_short_char-float");
        m("C_double_short_char-double");

        m("C_boolean_short_int-boolean");
        m("C_byte_short_int-byte");
        m("C_char_short_int-char");
        m("C_long_short_int-long");
        m("C_float_short_int-float");
        m("C_double_short_int-double");

        m("C_boolean_short_long-boolean");
        m("C_byte_short_long-byte");
        m("C_char_short_long-char");
        m("C_int_short_long-int");
        m("C_float_short_long-float");
        m("C_double_short_long-double");

        m("C_boolean_short_float-boolean");
        m("C_byte_short_float-byte");
        m("C_char_short_float-char");
        m("C_int_short_float-int");
        m("C_long_short_float-long");
        m("C_double_short_float-double");

        m("C_boolean_short_double-boolean");
        m("C_byte_short_double-byte");
        m("C_char_short_double-char");
        m("C_int_short_double-int");
        m("C_long_short_double-long");
        m("C_float_short_double-float");

// ------------------------------------------------------------
        m("C_byte_char_boolean-byte");
        m("C_short_char_boolean-short");
        m("C_int_char_boolean-int");
        m("C_long_char_boolean-long");
        m("C_float_char_boolean-float");
        m("C_double_char_boolean-double");

        m("C_boolean_char_byte-boolean");
        m("C_short_char_byte-short");
        m("C_int_char_byte-int");
        m("C_long_char_byte-long");
        m("C_float_char_byte-float");
        m("C_double_char_byte-double");

        m("C_boolean_char_short-boolean");
        m("C_byte_char_short-byte");
        m("C_int_char_short-int");
        m("C_long_char_short-long");
        m("C_float_char_short-float");
        m("C_double_char_short-double");

        m("C_boolean_char_int-boolean");
        m("C_byte_char_int-byte");
        m("C_short_char_int-short");
        m("C_long_char_int-long");
        m("C_float_char_int-float");
        m("C_double_char_int-double");

        m("C_boolean_char_long-boolean");
        m("C_byte_char_long-byte");
        m("C_short_char_long-short");
        m("C_int_char_long-int");
        m("C_float_char_long-float");
        m("C_double_char_long-double");

        m("C_boolean_char_float-boolean");
        m("C_byte_char_float-byte");
        m("C_short_char_float-short");
        m("C_int_char_float-int");
        m("C_long_char_float-long");
        m("C_double_char_float-double");

        m("C_boolean_char_double-boolean");
        m("C_byte_char_double-byte");
        m("C_short_char_double-short");
        m("C_int_char_double-int");
        m("C_long_char_double-long");
        m("C_float_char_double-float");


// ------------------------------------------------------------
        m("C_char_byte_boolean-char");
        m("C_short_byte_boolean-short");
        m("C_int_byte_boolean-int");
        m("C_long_byte_boolean-long");
        m("C_float_byte_boolean-float");
        m("C_double_byte_boolean-double");

        m("C_boolean_byte_char-boolean");
        m("C_short_byte_char-short");
        m("C_int_byte_char-int");
        m("C_long_byte_char-long");
        m("C_float_byte_char-float");
        m("C_double_byte_char-double");

        m("C_boolean_byte_short-boolean");
        m("C_char_byte_short-char");
        m("C_int_byte_short-int");
        m("C_long_byte_short-long");
        m("C_float_byte_short-float");
        m("C_double_byte_short-double");

        m("C_boolean_byte_int-boolean");
        m("C_char_byte_int-char");
        m("C_short_byte_int-short");
        m("C_long_byte_int-long");
        m("C_float_byte_int-float");
        m("C_double_byte_int-double");

        m("C_boolean_byte_long-boolean");
        m("C_char_byte_long-char");
        m("C_short_byte_long-short");
        m("C_int_byte_long-int");
        m("C_float_byte_long-float");
        m("C_double_byte_long-double");

        m("C_boolean_byte_float-boolean");
        m("C_char_byte_float-char");
        m("C_short_byte_float-short");
        m("C_int_byte_float-int");
        m("C_long_byte_float-long");
        m("C_double_byte_float-double");

        m("C_boolean_byte_double-boolean");
        m("C_char_byte_double-char");
        m("C_short_byte_double-short");
        m("C_int_byte_double-int");
        m("C_long_byte_double-long");
        m("C_float_byte_double-float");

// ------------------------------------------------------------
        m("C_byte_boolean_double-byte");
        m("C_char_boolean_double-char");
        m("C_short_boolean_double-short");
        m("C_int_boolean_double-int");
        m("C_long_boolean_double-long");
        m("C_float_boolean_double-float");

        m("C_byte_boolean_float-byte");
        m("C_char_boolean_float-char");
        m("C_short_boolean_float-short");
        m("C_int_boolean_float-int");
        m("C_long_boolean_float-long");
        m("C_double_boolean_float-double");

        m("C_byte_boolean_long-byte");
        m("C_char_boolean_long-char");
        m("C_short_boolean_long-short");
        m("C_int_boolean_long-int");
        m("C_float_boolean_long-float");
        m("C_double_boolean_long-double");

        m("C_byte_boolean_int-byte");
        m("C_char_boolean_int-char");
        m("C_short_boolean_int-short");
        m("C_long_boolean_int-long");
        m("C_float_boolean_int-float");
        m("C_double_boolean_int-double");

        m("C_byte_boolean_short-byte");
        m("C_char_boolean_short-char");
        m("C_int_boolean_short-int");
        m("C_long_boolean_short-long");
        m("C_float_boolean_short-float");
        m("C_double_boolean_short-double");

        m("C_byte_boolean_char-byte");
        m("C_short_boolean_char-short");
        m("C_int_boolean_char-int");
        m("C_long_boolean_char-long");
        m("C_float_boolean_char-float");
        m("C_double_boolean_char-double");

        m("C_char_boolean_byte-char");
        m("C_short_boolean_byte-short");
        m("C_int_boolean_byte-int");
        m("C_long_boolean_byte-long");
        m("C_float_boolean_byte-float");
        m("C_double_boolean_byte-double");


// --------------------------------------------------
        m("C_boolean_double-boolean");
        m("C_byte_double-byte");
        m("C_char_double-char");
        m("C_short_double-short");
        m("C_int_double-int");
        m("C_long_double-long");
        m("C_float_double-float");


        m("C_boolean_float-boolean");
        m("C_byte_float-byte");
        m("C_char_float-char");
        m("C_short_float-short");
        m("C_int_float-int");
        m("C_long_float-long");
        m("C_double_float-double");


        m("C_boolean_long-boolean");
        m("C_byte_long-byte");
        m("C_char_long-char");
        m("C_short_long-short");
        m("C_int_long-int");
        m("C_float_long-float");
        m("C_double_long-double");


        m("C_boolean_int-boolean");
        m("C_byte_int-byte");
        m("C_char_int-char");
        m("C_short_int-short");
        m("C_long_int-long");
        m("C_float_int-float");
        m("C_double_int-double");


        m("C_boolean_short-boolean");
        m("C_byte_short-byte");
        m("C_char_short-char");
        m("C_int_short-int");
        m("C_long_short-long");
        m("C_float_short-float");
        m("C_double_short-double");


        m("C_boolean_char-boolean");
        m("C_byte_char-byte");
        m("C_short_char-short");
        m("C_int_char-int");
        m("C_long_char-long");
        m("C_float_char-float");
        m("C_double_char-double");


        m("C_boolean_byte-boolean");
        m("C_char_byte-char");
        m("C_short_byte-short");
        m("C_int_byte-int");
        m("C_long_byte-long");
        m("C_float_byte-float");
        m("C_double_byte-double");


        m("C_byte_boolean-byte");
        m("C_char_boolean-char");
        m("C_short_boolean-short");
        m("C_int_boolean-int");
        m("C_long_boolean-long");
        m("C_float_boolean-float");
        m("C_double_boolean-double");
    }
}

// start-changed
class C_boolean { public void f(boolean[] xs) { } }
class C_byte    { public void f(byte[] xs) { } }
class C_char    { public void f(char[] xs) { } }
class C_short   { public void f(short[] xs) { } }
class C_int     { public void f(int[] xs) { } }
class C_long    { public void f(long[] xs) { } }
class C_float   { public void f(float[] xs) { } }
class C_double  { public void f(double[] xs) { } }
// end-changed

// start-new-block
class CC_boolean { public void f(boolean[] xs) { A.a("C_boolean-boolean"); } }
class CC_byte    { public void f(byte[] xs) { A.a("C_byte-byte"); } }
class CC_char    { public void f(char[] xs) { A.a("C_char-char"); } }
class CC_short   { public void f(short[] xs) { A.a("C_short-short"); } }
class CC_int     { public void f(int[] xs) { A.a("C_int-int"); } }
class CC_long    { public void f(long[] xs) { A.a("C_long-long"); } }
class CC_float   { public void f(float[] xs) { A.a("C_float-float"); } }
class CC_double  { public void f(double[] xs) { A.a("C_double-double"); } }
// end-new-block

class A { public static void a(String msg) { org.aspectj.testing.Tester.event(msg); } }

class C_byte_double_boolean extends C_double_boolean    { public void f(byte[] xs) { A.a("C_byte_double_boolean-byte"); } }
class C_char_double_boolean extends C_double_boolean    { public void f(char[] xs) { A.a("C_char_double_boolean-char"); } }
class C_short_double_boolean extends C_double_boolean   { public void f(short[] xs) { A.a("C_short_double_boolean-short"); } }
class C_int_double_boolean extends C_double_boolean     { public void f(int[] xs) { A.a("C_int_double_boolean-int"); } }
class C_long_double_boolean extends C_double_boolean    { public void f(long[] xs) { A.a("C_long_double_boolean-long"); } }
class C_float_double_boolean extends C_double_boolean   { public void f(float[] xs) { A.a("C_float_double_boolean-float"); } }

class C_boolean_double_byte extends C_double_byte { public void f(boolean[] xs) { A.a("C_boolean_double_byte-boolean"); } }
class C_char_double_byte extends C_double_byte    { public void f(char[] xs) { A.a("C_char_double_byte-char"); } }
class C_short_double_byte extends C_double_byte   { public void f(short[] xs) { A.a("C_short_double_byte-short"); } }
class C_int_double_byte extends C_double_byte     { public void f(int[] xs) { A.a("C_int_double_byte-int"); } }
class C_long_double_byte extends C_double_byte    { public void f(long[] xs) { A.a("C_long_double_byte-long"); } }
class C_float_double_byte extends C_double_byte   { public void f(float[] xs) { A.a("C_float_double_byte-float"); } }

class C_boolean_double_char extends C_double_char { public void f(boolean[] xs) { A.a("C_boolean_double_char-boolean"); } }
class C_byte_double_char extends C_double_char    { public void f(byte[] xs) { A.a("C_byte_double_char-byte"); } }
class C_short_double_char extends C_double_char   { public void f(short[] xs) { A.a("C_short_double_char-short"); } }
class C_int_double_char extends C_double_char     { public void f(int[] xs) { A.a("C_int_double_char-int"); } }
class C_long_double_char extends C_double_char    { public void f(long[] xs) { A.a("C_long_double_char-long"); } }
class C_float_double_char extends C_double_char   { public void f(float[] xs) { A.a("C_float_double_char-float"); } }

class C_boolean_double_short extends C_double_short { public void f(boolean[] xs) { A.a("C_boolean_double_short-boolean"); } }
class C_byte_double_short extends C_double_short    { public void f(byte[] xs) { A.a("C_byte_double_short-byte"); } }
class C_char_double_short extends C_double_short    { public void f(char[] xs) { A.a("C_char_double_short-char"); } }
class C_int_double_short extends C_double_short     { public void f(int[] xs) { A.a("C_int_double_short-int"); } }
class C_long_double_short extends C_double_short    { public void f(long[] xs) { A.a("C_long_double_short-long"); } }
class C_float_double_short extends C_double_short   { public void f(float[] xs) { A.a("C_float_double_short-float"); } }

class C_boolean_double_int extends C_double_int { public void f(boolean[] xs) { A.a("C_boolean_double_int-boolean"); } }
class C_byte_double_int extends C_double_int    { public void f(byte[] xs) { A.a("C_byte_double_int-byte"); } }
class C_char_double_int extends C_double_int    { public void f(char[] xs) { A.a("C_char_double_int-char"); } }
class C_short_double_int extends C_double_int   { public void f(short[] xs) { A.a("C_short_double_int-short"); } }
class C_long_double_int extends C_double_int    { public void f(long[] xs) { A.a("C_long_double_int-long"); } }
class C_float_double_int extends C_double_int   { public void f(float[] xs) { A.a("C_float_double_int-float"); } }

class C_boolean_double_long extends C_double_long { public void f(boolean[] xs) { A.a("C_boolean_double_long-boolean"); } }
class C_byte_double_long extends C_double_long    { public void f(byte[] xs) { A.a("C_byte_double_long-byte"); } }
class C_char_double_long extends C_double_long    { public void f(char[] xs) { A.a("C_char_double_long-char"); } }
class C_short_double_long extends C_double_long   { public void f(short[] xs) { A.a("C_short_double_long-short"); } }
class C_int_double_long extends C_double_long     { public void f(int[] xs) { A.a("C_int_double_long-int"); } }
class C_float_double_long extends C_double_long   { public void f(float[] xs) { A.a("C_float_double_long-float"); } }

class C_boolean_double_float extends C_double_float { public void f(boolean[] xs) { A.a("C_boolean_double_float-boolean"); } }
class C_byte_double_float extends C_double_float    { public void f(byte[] xs) { A.a("C_byte_double_float-byte"); } }
class C_char_double_float extends C_double_float    { public void f(char[] xs) { A.a("C_char_double_float-char"); } }
class C_short_double_float extends C_double_float   { public void f(short[] xs) { A.a("C_short_double_float-short"); } }
class C_int_double_float extends C_double_float     { public void f(int[] xs) { A.a("C_int_double_float-int"); } }
class C_long_double_float extends C_double_float    { public void f(long[] xs) { A.a("C_long_double_float-long"); } }

// ------------------------------------------------------------
class C_byte_float_boolean extends C_float_boolean    { public void f(byte[] xs) { A.a("C_byte_float_boolean-byte"); } }
class C_char_float_boolean extends C_float_boolean    { public void f(char[] xs) { A.a("C_char_float_boolean-char"); } }
class C_short_float_boolean extends C_float_boolean   { public void f(short[] xs) { A.a("C_short_float_boolean-short"); } }
class C_int_float_boolean extends C_float_boolean     { public void f(int[] xs) { A.a("C_int_float_boolean-int"); } }
class C_long_float_boolean extends C_float_boolean    { public void f(long[] xs) { A.a("C_long_float_boolean-long"); } }
class C_double_float_boolean extends C_float_boolean  { public void f(double[] xs) { A.a("C_double_float_boolean-double"); } }

class C_boolean_float_byte extends C_float_byte { public void f(boolean[] xs) { A.a("C_boolean_float_byte-boolean"); } }
class C_char_float_byte extends C_float_byte    { public void f(char[] xs) { A.a("C_char_float_byte-char"); } }
class C_short_float_byte extends C_float_byte   { public void f(short[] xs) { A.a("C_short_float_byte-short"); } }
class C_int_float_byte extends C_float_byte     { public void f(int[] xs) { A.a("C_int_float_byte-int"); } }
class C_long_float_byte extends C_float_byte    { public void f(long[] xs) { A.a("C_long_float_byte-long"); } }
class C_double_float_byte extends C_float_byte  { public void f(double[] xs) { A.a("C_double_float_byte-double"); } }

class C_boolean_float_char extends C_float_char { public void f(boolean[] xs) { A.a("C_boolean_float_char-boolean"); } }
class C_byte_float_char extends C_float_char    { public void f(byte[] xs) { A.a("C_byte_float_char-byte"); } }
class C_short_float_char extends C_float_char   { public void f(short[] xs) { A.a("C_short_float_char-short"); } }
class C_int_float_char extends C_float_char     { public void f(int[] xs) { A.a("C_int_float_char-int"); } }
class C_long_float_char extends C_float_char    { public void f(long[] xs) { A.a("C_long_float_char-long"); } }
class C_double_float_char extends C_float_char  { public void f(double[] xs) { A.a("C_double_float_char-double"); } }

class C_boolean_float_short extends C_float_short { public void f(boolean[] xs) { A.a("C_boolean_float_short-boolean"); } }
class C_byte_float_short extends C_float_short    { public void f(byte[] xs) { A.a("C_byte_float_short-byte"); } }
class C_char_float_short extends C_float_short    { public void f(char[] xs) { A.a("C_char_float_short-char"); } }
class C_int_float_short extends C_float_short     { public void f(int[] xs) { A.a("C_int_float_short-int"); } }
class C_long_float_short extends C_float_short    { public void f(long[] xs) { A.a("C_long_float_short-long"); } }
class C_double_float_short extends C_float_short  { public void f(double[] xs) { A.a("C_double_float_short-double"); } }

class C_boolean_float_int extends C_float_int { public void f(boolean[] xs) { A.a("C_boolean_float_int-boolean"); } }
class C_byte_float_int extends C_float_int    { public void f(byte[] xs) { A.a("C_byte_float_int-byte"); } }
class C_char_float_int extends C_float_int    { public void f(char[] xs) { A.a("C_char_float_int-char"); } }
class C_short_float_int extends C_float_int   { public void f(short[] xs) { A.a("C_short_float_int-short"); } }
class C_long_float_int extends C_float_int    { public void f(long[] xs) { A.a("C_long_float_int-long"); } }
class C_double_float_int extends C_float_int  { public void f(double[] xs) { A.a("C_double_float_int-double"); } }

class C_boolean_float_long extends C_float_long { public void f(boolean[] xs) { A.a("C_boolean_float_long-boolean"); } }
class C_byte_float_long extends C_float_long    { public void f(byte[] xs) { A.a("C_byte_float_long-byte"); } }
class C_char_float_long extends C_float_long    { public void f(char[] xs) { A.a("C_char_float_long-char"); } }
class C_short_float_long extends C_float_long   { public void f(short[] xs) { A.a("C_short_float_long-short"); } }
class C_long_float_long extends C_float_long    { public void f(long[] xs) { A.a("C_long_float_long-long"); } }
class C_double_float_long extends C_float_long  { public void f(double[] xs) { A.a("C_double_float_long-double"); } }

class C_boolean_float_double extends C_float_double { public void f(boolean[] xs) { A.a("C_boolean_float_double-boolean"); } }
class C_byte_float_double extends C_float_double    { public void f(byte[] xs) { A.a("C_byte_float_double-byte"); } }
class C_char_float_double extends C_float_double    { public void f(char[] xs) { A.a("C_char_float_double-char"); } }
class C_short_float_double extends C_float_double   { public void f(short[] xs) { A.a("C_short_float_double-short"); } }
class C_int_float_double extends C_float_double     { public void f(int[] xs) { A.a("C_int_float_double-int"); } }
class C_long_float_double extends C_float_double    { public void f(long[] xs) { A.a("C_long_float_double-long"); } }

// ------------------------------------------------------------
class C_byte_int_boolean extends C_int_boolean    { public void f(byte[] xs) { A.a("C_byte_int_boolean-byte"); } }
class C_char_int_boolean extends C_int_boolean    { public void f(char[] xs) { A.a("C_char_int_boolean-char"); } }
class C_short_int_boolean extends C_int_boolean   { public void f(short[] xs) { A.a("C_short_int_boolean-short"); } }
class C_long_int_boolean extends C_int_boolean    { public void f(long[] xs) { A.a("C_long_int_boolean-long"); } }
class C_float_int_boolean extends C_int_boolean   { public void f(float[] xs) { A.a("C_float_int_boolean-float"); } }
class C_double_int_boolean extends C_int_boolean  { public void f(double[] xs) { A.a("C_double_int_boolean-double"); } }

class C_boolean_int_byte extends C_int_byte { public void f(boolean[] xs) { A.a("C_boolean_int_byte-boolean"); } }
class C_char_int_byte extends C_int_byte    { public void f(char[] xs) { A.a("C_char_int_byte-char"); } }
class C_short_int_byte extends C_int_byte   { public void f(short[] xs) { A.a("C_short_int_byte-short"); } }
class C_long_int_byte extends C_int_byte    { public void f(long[] xs) { A.a("C_long_int_byte-long"); } }
class C_float_int_byte extends C_int_byte   { public void f(float[] xs) { A.a("C_float_int_byte-float"); } }
class C_double_int_byte extends C_int_byte  { public void f(double[] xs) { A.a("C_double_int_byte-double"); } }

class C_boolean_int_char extends C_int_char { public void f(boolean[] xs) { A.a("C_boolean_int_char-boolean"); } }
class C_byte_int_char extends C_int_char    { public void f(byte[] xs) { A.a("C_byte_int_char-byte"); } }
class C_short_int_char extends C_int_char   { public void f(short[] xs) { A.a("C_short_int_char-short"); } }
class C_long_int_char extends C_int_char    { public void f(long[] xs) { A.a("C_long_int_char-long"); } }
class C_float_int_char extends C_int_char   { public void f(float[] xs) { A.a("C_float_int_char-float"); } }
class C_double_int_char extends C_int_char  { public void f(double[] xs) { A.a("C_double_int_char-double"); } }

class C_boolean_int_short extends C_int_short { public void f(boolean[] xs) { A.a("C_boolean_int_short-boolean"); } }
class C_byte_int_short extends C_int_short    { public void f(byte[] xs) { A.a("C_byte_int_short-byte"); } }
class C_char_int_short extends C_int_short    { public void f(char[] xs) { A.a("C_char_int_short-char"); } }
class C_long_int_short extends C_int_short    { public void f(long[] xs) { A.a("C_long_int_short-long"); } }
class C_float_int_short extends C_int_short   { public void f(float[] xs) { A.a("C_float_int_short-float"); } }
class C_double_int_short extends C_int_short  { public void f(double[] xs) { A.a("C_double_int_short-double"); } }

class C_boolean_int_long extends C_int_long { public void f(boolean[] xs) { A.a("C_boolean_int_long-boolean"); } }
class C_byte_int_long extends C_int_long    { public void f(byte[] xs) { A.a("C_byte_int_long-byte"); } }
class C_char_int_long extends C_int_long    { public void f(char[] xs) { A.a("C_char_int_long-char"); } }
class C_short_int_long extends C_int_long   { public void f(short[] xs) { A.a("C_short_int_long-short"); } }
class C_float_int_long extends C_int_long   { public void f(float[] xs) { A.a("C_float_int_long-float"); } }
class C_double_int_long extends C_int_long  { public void f(double[] xs) { A.a("C_double_int_long-double"); } }

class C_boolean_int_float extends C_int_float { public void f(boolean[] xs) { A.a("C_boolean_int_float-boolean"); } }
class C_byte_int_float extends C_int_float    { public void f(byte[] xs) { A.a("C_byte_int_float-byte"); } }
class C_char_int_float extends C_int_float    { public void f(char[] xs) { A.a("C_char_int_float-char"); } }
class C_short_int_float extends C_int_float   { public void f(short[] xs) { A.a("C_short_int_float-short"); } }
class C_long_int_float extends C_int_float    { public void f(long[] xs) { A.a("C_long_int_float-long"); } }
class C_double_int_float extends C_int_float  { public void f(double[] xs) { A.a("C_double_int_float-double"); } }

class C_boolean_int_double extends C_int_double { public void f(boolean[] xs) { A.a("C_boolean_int_double-boolean"); } }
class C_byte_int_double extends C_int_double    { public void f(byte[] xs) { A.a("C_byte_int_double-byte"); } }
class C_char_int_double extends C_int_double    { public void f(char[] xs) { A.a("C_char_int_double-char"); } }
class C_short_int_double extends C_int_double   { public void f(short[] xs) { A.a("C_short_int_double-short"); } }
class C_long_int_double extends C_int_double    { public void f(long[] xs) { A.a("C_long_int_double-long"); } }
class C_float_int_double extends C_int_double   { public void f(float[] xs) { A.a("C_float_int_double-float"); } }

// ------------------------------------------------------------
class C_byte_short_boolean extends C_short_boolean    { public void f(byte[] xs) { A.a("C_byte_short_boolean-byte"); } }
class C_char_short_boolean extends C_short_boolean    { public void f(char[] xs) { A.a("C_char_short_boolean-char"); } }
class C_int_short_boolean extends C_short_boolean     { public void f(int[] xs) { A.a("C_int_short_boolean-int"); } }
class C_long_short_boolean extends C_short_boolean    { public void f(long[] xs) { A.a("C_long_short_boolean-long"); } }
class C_float_short_boolean extends C_short_boolean   { public void f(float[] xs) { A.a("C_float_short_boolean-float"); } }
class C_double_short_boolean extends C_short_boolean  { public void f(double[] xs) { A.a("C_double_short_boolean-double"); } }

class C_boolean_short_byte extends C_short_byte { public void f(boolean[] xs) { A.a("C_boolean_short_byte-boolean"); } }
class C_char_short_byte extends C_short_byte    { public void f(char[] xs) { A.a("C_char_short_byte-char"); } }
class C_int_short_byte extends C_short_byte     { public void f(int[] xs) { A.a("C_int_short_byte-int"); } }
class C_long_short_byte extends C_short_byte    { public void f(long[] xs) { A.a("C_long_short_byte-long"); } }
class C_float_short_byte extends C_short_byte   { public void f(float[] xs) { A.a("C_float_short_byte-float"); } }
class C_double_short_byte extends C_short_byte  { public void f(double[] xs) { A.a("C_double_short_byte-double"); } }

class C_boolean_short_char extends C_short_char { public void f(boolean[] xs) { A.a("C_boolean_short_char-boolean"); } }
class C_byte_short_char extends C_short_char    { public void f(byte[] xs) { A.a("C_byte_short_char-byte"); } }
class C_int_short_char extends C_short_char     { public void f(int[] xs) { A.a("C_int_short_char-int"); } }
class C_long_short_char extends C_short_char    { public void f(long[] xs) { A.a("C_long_short_char-long"); } }
class C_float_short_char extends C_short_char   { public void f(float[] xs) { A.a("C_float_short_char-float"); } }
class C_double_short_char extends C_short_char  { public void f(double[] xs) { A.a("C_double_short_char-double"); } }

class C_boolean_short_int extends C_short_int { public void f(boolean[] xs) { A.a("C_boolean_short_int-boolean"); } }
class C_byte_short_int extends C_short_int    { public void f(byte[] xs) { A.a("C_byte_short_int-byte"); } }
class C_char_short_int extends C_short_int    { public void f(char[] xs) { A.a("C_char_short_int-char"); } }
class C_long_short_int extends C_short_int    { public void f(long[] xs) { A.a("C_long_short_int-long"); } }
class C_float_short_int extends C_short_int   { public void f(float[] xs) { A.a("C_float_short_int-float"); } }
class C_double_short_int extends C_short_int  { public void f(double[] xs) { A.a("C_double_short_int-double"); } }

class C_boolean_short_long extends C_short_long { public void f(boolean[] xs) { A.a("C_boolean_short_long-boolean"); } }
class C_byte_short_long extends C_short_long    { public void f(byte[] xs) { A.a("C_byte_short_long-byte"); } }
class C_char_short_long extends C_short_long    { public void f(char[] xs) { A.a("C_char_short_long-char"); } }
class C_int_short_long extends C_short_long     { public void f(int[] xs) { A.a("C_int_short_long-int"); } }
class C_float_short_long extends C_short_long   { public void f(float[] xs) { A.a("C_float_short_long-float"); } }
class C_double_short_long extends C_short_long  { public void f(double[] xs) { A.a("C_double_short_long-double"); } }

class C_boolean_short_float extends C_short_float { public void f(boolean[] xs) { A.a("C_boolean_short_float-boolean"); } }
class C_byte_short_float extends C_short_float    { public void f(byte[] xs) { A.a("C_byte_short_float-byte"); } }
class C_char_short_float extends C_short_float    { public void f(char[] xs) { A.a("C_char_short_float-char"); } }
class C_int_short_float extends C_short_float     { public void f(int[] xs) { A.a("C_int_short_float-int"); } }
class C_long_short_float extends C_short_float    { public void f(long[] xs) { A.a("C_long_short_float-long"); } }
class C_double_short_float extends C_short_float  { public void f(double[] xs) { A.a("C_double_short_float-double"); } }

class C_boolean_short_double extends C_short_double { public void f(boolean[] xs) { A.a("C_boolean_short_double-boolean"); } }
class C_byte_short_double extends C_short_double    { public void f(byte[] xs) { A.a("C_byte_short_double-byte"); } }
class C_char_short_double extends C_short_double    { public void f(char[] xs) { A.a("C_char_short_double-char"); } }
class C_int_short_double extends C_short_double     { public void f(int[] xs) { A.a("C_int_short_double-int"); } }
class C_long_short_double extends C_short_double    { public void f(long[] xs) { A.a("C_long_short_double-long"); } }
class C_float_short_double extends C_short_double   { public void f(float[] xs) { A.a("C_float_short_double-float"); } }

// ------------------------------------------------------------
class C_byte_char_boolean extends C_char_boolean    { public void f(byte[] xs) { A.a("C_byte_char_boolean-byte"); } }
class C_short_char_boolean extends C_char_boolean   { public void f(short[] xs) { A.a("C_short_char_boolean-short"); } }
class C_int_char_boolean extends C_char_boolean     { public void f(int[] xs) { A.a("C_int_char_boolean-int"); } }
class C_long_char_boolean extends C_char_boolean    { public void f(long[] xs) { A.a("C_long_char_boolean-long"); } }
class C_float_char_boolean extends C_char_boolean   { public void f(float[] xs) { A.a("C_float_char_boolean-float"); } }
class C_double_char_boolean extends C_char_boolean  { public void f(double[] xs) { A.a("C_double_char_boolean-double"); } }

class C_boolean_char_byte extends C_char_byte { public void f(boolean[] xs) { A.a("C_boolean_char_byte-boolean"); } }
class C_short_char_byte extends C_char_byte   { public void f(short[] xs) { A.a("C_short_char_byte-short"); } }
class C_int_char_byte extends C_char_byte     { public void f(int[] xs) { A.a("C_int_char_byte-int"); } }
class C_long_char_byte extends C_char_byte    { public void f(long[] xs) { A.a("C_long_char_byte-long"); } }
class C_float_char_byte extends C_char_byte   { public void f(float[] xs) { A.a("C_float_char_byte-float"); } }
class C_double_char_byte extends C_char_byte  { public void f(double[] xs) { A.a("C_double_char_byte-double"); } }

class C_boolean_char_short extends C_char_short { public void f(boolean[] xs) { A.a("C_boolean_char_short-boolean"); } }
class C_byte_char_short extends C_char_short    { public void f(byte[] xs) { A.a("C_byte_char_short-byte"); } }
class C_int_char_short extends C_char_short     { public void f(int[] xs) { A.a("C_int_char_short-int"); } }
class C_long_char_short extends C_char_short    { public void f(long[] xs) { A.a("C_long_char_short-long"); } }
class C_float_char_short extends C_char_short   { public void f(float[] xs) { A.a("C_float_char_short-float"); } }
class C_double_char_short extends C_char_short  { public void f(double[] xs) { A.a("C_double_char_short-double"); } }

class C_boolean_char_int extends C_char_int { public void f(boolean[] xs) { A.a("C_boolean_char_int-boolean"); } }
class C_byte_char_int extends C_char_int    { public void f(byte[] xs) { A.a("C_byte_char_int-byte"); } }
class C_short_char_int extends C_char_int   { public void f(short[] xs) { A.a("C_short_char_int-short"); } }
class C_long_char_int extends C_char_int    { public void f(long[] xs) { A.a("C_long_char_int-long"); } }
class C_float_char_int extends C_char_int   { public void f(float[] xs) { A.a("C_float_char_int-float"); } }
class C_double_char_int extends C_char_int  { public void f(double[] xs) { A.a("C_double_char_int-double"); } }

class C_boolean_char_long extends C_char_long { public void f(boolean[] xs) { A.a("C_boolean_char_long-boolean"); } }
class C_byte_char_long extends C_char_long    { public void f(byte[] xs) { A.a("C_byte_char_long-byte"); } }
class C_short_char_long extends C_char_long   { public void f(short[] xs) { A.a("C_short_char_long-short"); } }
class C_int_char_long extends C_char_long     { public void f(int[] xs) { A.a("C_int_char_long-int"); } }
class C_float_char_long extends C_char_long   { public void f(float[] xs) { A.a("C_float_char_long-float"); } }
class C_double_char_long extends C_char_long  { public void f(double[] xs) { A.a("C_double_char_long-double"); } }

class C_boolean_char_float extends C_char_float { public void f(boolean[] xs) { A.a("C_boolean_char_float-boolean"); } }
class C_byte_char_float extends C_char_float    { public void f(byte[] xs) { A.a("C_byte_char_float-byte"); } }
class C_short_char_float extends C_char_float   { public void f(short[] xs) { A.a("C_short_char_float-short"); } }
class C_int_char_float extends C_char_float     { public void f(int[] xs) { A.a("C_int_char_float-int"); } }
class C_long_char_float extends C_char_float    { public void f(long[] xs) { A.a("C_long_char_float-long"); } }
class C_double_char_float extends C_char_float  { public void f(double[] xs) { A.a("C_double_char_float-double"); } }

class C_boolean_char_double extends C_char_double { public void f(boolean[] xs) { A.a("C_boolean_char_double-boolean"); } }
class C_byte_char_double extends C_char_double    { public void f(byte[] xs) { A.a("C_byte_char_double-byte"); } }
class C_short_char_double extends C_char_double   { public void f(short[] xs) { A.a("C_short_char_double-short"); } }
class C_int_char_double extends C_char_double     { public void f(int[] xs) { A.a("C_int_char_double-int"); } }
class C_long_char_double extends C_char_double    { public void f(long[] xs) { A.a("C_long_char_double-long"); } }
class C_float_char_double extends C_char_double   { public void f(float[] xs) { A.a("C_float_char_double-float"); } }


// ------------------------------------------------------------
class C_char_byte_boolean extends C_byte_boolean    { public void f(char[] xs) { A.a("C_char_byte_boolean-char"); } }
class C_short_byte_boolean extends C_byte_boolean   { public void f(short[] xs) { A.a("C_short_byte_boolean-short"); } }
class C_int_byte_boolean extends C_byte_boolean     { public void f(int[] xs) { A.a("C_int_byte_boolean-int"); } }
class C_long_byte_boolean extends C_byte_boolean    { public void f(long[] xs) { A.a("C_long_byte_boolean-long"); } }
class C_float_byte_boolean extends C_byte_boolean   { public void f(float[] xs) { A.a("C_float_byte_boolean-float"); } }
class C_double_byte_boolean extends C_byte_boolean  { public void f(double[] xs) { A.a("C_double_byte_boolean-double"); } }

class C_boolean_byte_char extends C_byte_char { public void f(boolean[] xs) { A.a("C_boolean_byte_char-boolean"); } }
class C_short_byte_char extends C_byte_char   { public void f(short[] xs) { A.a("C_short_byte_char-short"); } }
class C_int_byte_char extends C_byte_char     { public void f(int[] xs) { A.a("C_int_byte_char-int"); } }
class C_long_byte_char extends C_byte_char    { public void f(long[] xs) { A.a("C_long_byte_char-long"); } }
class C_float_byte_char extends C_byte_char   { public void f(float[] xs) { A.a("C_float_byte_char-float"); } }
class C_double_byte_char extends C_byte_char  { public void f(double[] xs) { A.a("C_double_byte_char-double"); } }

class C_boolean_byte_short extends C_byte_short { public void f(boolean[] xs) { A.a("C_boolean_byte_short-boolean"); } }
class C_char_byte_short extends C_byte_short    { public void f(char[] xs) { A.a("C_char_byte_short-char"); } }
class C_int_byte_short extends C_byte_short     { public void f(int[] xs) { A.a("C_int_byte_short-int"); } }
class C_long_byte_short extends C_byte_short    { public void f(long[] xs) { A.a("C_long_byte_short-long"); } }
class C_float_byte_short extends C_byte_short   { public void f(float[] xs) { A.a("C_float_byte_short-float"); } }
class C_double_byte_short extends C_byte_short  { public void f(double[] xs) { A.a("C_double_byte_short-double"); } }

class C_boolean_byte_int extends C_byte_int { public void f(boolean[] xs) { A.a("C_boolean_byte_int-boolean"); } }
class C_char_byte_int extends C_byte_int    { public void f(char[] xs) { A.a("C_char_byte_int-char"); } }
class C_short_byte_int extends C_byte_int   { public void f(short[] xs) { A.a("C_short_byte_int-short"); } }
class C_long_byte_int extends C_byte_int    { public void f(long[] xs) { A.a("C_long_byte_int-long"); } }
class C_float_byte_int extends C_byte_int   { public void f(float[] xs) { A.a("C_float_byte_int-float"); } }
class C_double_byte_int extends C_byte_int  { public void f(double[] xs) { A.a("C_double_byte_int-double"); } }

class C_boolean_byte_long extends C_byte_long { public void f(boolean[] xs) { A.a("C_boolean_byte_long-boolean"); } }
class C_char_byte_long extends C_byte_long    { public void f(char[] xs) { A.a("C_char_byte_long-char"); } }
class C_short_byte_long extends C_byte_long   { public void f(short[] xs) { A.a("C_short_byte_long-short"); } }
class C_int_byte_long extends C_byte_long     { public void f(int[] xs) { A.a("C_int_byte_long-int"); } }
class C_float_byte_long extends C_byte_long   { public void f(float[] xs) { A.a("C_float_byte_long-float"); } }
class C_double_byte_long extends C_byte_long  { public void f(double[] xs) { A.a("C_double_byte_long-double"); } }

class C_boolean_byte_float extends C_byte_float { public void f(boolean[] xs) { A.a("C_boolean_byte_float-boolean"); } }
class C_char_byte_float extends C_byte_float    { public void f(char[] xs) { A.a("C_char_byte_float-char"); } }
class C_short_byte_float extends C_byte_float   { public void f(short[] xs) { A.a("C_short_byte_float-short"); } }
class C_int_byte_float extends C_byte_float     { public void f(int[] xs) { A.a("C_int_byte_float-int"); } }
class C_long_byte_float extends C_byte_float    { public void f(long[] xs) { A.a("C_long_byte_float-long"); } }
class C_double_byte_float extends C_byte_float  { public void f(double[] xs) { A.a("C_double_byte_float-double"); } }

class C_boolean_byte_double extends C_byte_double { public void f(boolean[] xs) { A.a("C_boolean_byte_double-boolean"); } }
class C_char_byte_double extends C_byte_double    { public void f(char[] xs) { A.a("C_char_byte_double-char"); } }
class C_short_byte_double extends C_byte_double   { public void f(short[] xs) { A.a("C_short_byte_double-short"); } }
class C_int_byte_double extends C_byte_double     { public void f(int[] xs) { A.a("C_int_byte_double-int"); } }
class C_long_byte_double extends C_byte_double    { public void f(long[] xs) { A.a("C_long_byte_double-long"); } }
class C_float_byte_double extends C_byte_double   { public void f(float[] xs) { A.a("C_float_byte_double-float"); } }

// ------------------------------------------------------------
class C_byte_boolean_double extends C_boolean_double    { public void f(byte[] xs) { A.a("C_byte_boolean_double-byte"); } }
class C_char_boolean_double extends C_boolean_double    { public void f(char[] xs) { A.a("C_char_boolean_double-char"); } }
class C_short_boolean_double extends C_boolean_double   { public void f(short[] xs) { A.a("C_short_boolean_double-short"); } }
class C_int_boolean_double extends C_boolean_double     { public void f(int[] xs) { A.a("C_int_boolean_double-int"); } }
class C_long_boolean_double extends C_boolean_double    { public void f(long[] xs) { A.a("C_long_boolean_double-long"); } }
class C_float_boolean_double extends C_boolean_double   { public void f(float[] xs) { A.a("C_float_boolean_double-float"); } }

class C_byte_boolean_float extends C_boolean_float    { public void f(byte[] xs) { A.a("C_byte_boolean_float-byte"); } }
class C_char_boolean_float extends C_boolean_float    { public void f(char[] xs) { A.a("C_char_boolean_float-char"); } }
class C_short_boolean_float extends C_boolean_float   { public void f(short[] xs) { A.a("C_short_boolean_float-short"); } }
class C_int_boolean_float extends C_boolean_float     { public void f(int[] xs) { A.a("C_int_boolean_float-int"); } }
class C_long_boolean_float extends C_boolean_float    { public void f(long[] xs) { A.a("C_long_boolean_float-long"); } }
class C_double_boolean_float extends C_boolean_float  { public void f(double[] xs) { A.a("C_double_boolean_float-double"); } }

class C_byte_boolean_long extends C_boolean_long    { public void f(byte[] xs) { A.a("C_byte_boolean_long-byte"); } }
class C_char_boolean_long extends C_boolean_long    { public void f(char[] xs) { A.a("C_char_boolean_long-char"); } }
class C_short_boolean_long extends C_boolean_long   { public void f(short[] xs) { A.a("C_short_boolean_long-short"); } }
class C_int_boolean_long extends C_boolean_long     { public void f(int[] xs) { A.a("C_int_boolean_long-int"); } }
class C_float_boolean_long extends C_boolean_long   { public void f(float[] xs) { A.a("C_float_boolean_long-float"); } }
class C_double_boolean_long extends C_boolean_long  { public void f(double[] xs) { A.a("C_double_boolean_long-double"); } }

class C_byte_boolean_int extends C_boolean_int    { public void f(byte[] xs) { A.a("C_byte_boolean_int-byte"); } }
class C_char_boolean_int extends C_boolean_int    { public void f(char[] xs) { A.a("C_char_boolean_int-char"); } }
class C_short_boolean_int extends C_boolean_int   { public void f(short[] xs) { A.a("C_short_boolean_int-short"); } }
class C_long_boolean_int extends C_boolean_int    { public void f(long[] xs) { A.a("C_long_boolean_int-long"); } }
class C_float_boolean_int extends C_boolean_int   { public void f(float[] xs) { A.a("C_float_boolean_int-float"); } }
class C_double_boolean_int extends C_boolean_int  { public void f(double[] xs) { A.a("C_double_boolean_int-double"); } }

class C_byte_boolean_short extends C_boolean_short    { public void f(byte[] xs) { A.a("C_byte_boolean_short-byte"); } }
class C_char_boolean_short extends C_boolean_short    { public void f(char[] xs) { A.a("C_char_boolean_short-char"); } }
class C_int_boolean_short extends C_boolean_short     { public void f(int[] xs) { A.a("C_int_boolean_short-int"); } }
class C_long_boolean_short extends C_boolean_short    { public void f(long[] xs) { A.a("C_long_boolean_short-long"); } }
class C_float_boolean_short extends C_boolean_short   { public void f(float[] xs) { A.a("C_float_boolean_short-float"); } }
class C_double_boolean_short extends C_boolean_short  { public void f(double[] xs) { A.a("C_double_boolean_short-double"); } }

class C_byte_boolean_char extends C_boolean_char    { public void f(byte[] xs) { A.a("C_byte_boolean_char-byte"); } }
class C_short_boolean_char extends C_boolean_char   { public void f(short[] xs) { A.a("C_short_boolean_char-short"); } }
class C_int_boolean_char extends C_boolean_char     { public void f(int[] xs) { A.a("C_int_boolean_char-int"); } }
class C_long_boolean_char extends C_boolean_char    { public void f(long[] xs) { A.a("C_long_boolean_char-long"); } }
class C_float_boolean_char extends C_boolean_char   { public void f(float[] xs) { A.a("C_float_boolean_char-float"); } }
class C_double_boolean_char extends C_boolean_char  { public void f(double[] xs) { A.a("C_double_boolean_char-double"); } }

class C_char_boolean_byte extends C_boolean_byte    { public void f(char[] xs) { A.a("C_char_boolean_byte-char"); } }
class C_short_boolean_byte extends C_boolean_byte   { public void f(short[] xs) { A.a("C_short_boolean_byte-short"); } }
class C_int_boolean_byte extends C_boolean_byte     { public void f(int[] xs) { A.a("C_int_boolean_byte-int"); } }
class C_long_boolean_byte extends C_boolean_byte    { public void f(long[] xs) { A.a("C_long_boolean_byte-long"); } }
class C_float_boolean_byte extends C_boolean_byte   { public void f(float[] xs) { A.a("C_float_boolean_byte-float"); } }
class C_double_boolean_byte extends C_boolean_byte  { public void f(double[] xs) { A.a("C_double_boolean_byte-double"); } }


// --------------------------------------------------
class C_boolean_double extends C_double { public void f(boolean[] xs) { A.a("C_boolean_double-boolean"); }}
class C_byte_double extends C_double    { public void f(byte[] xs) { A.a("C_byte_double-byte"); } }
class C_char_double extends C_double    { public void f(char[] xs) { A.a("C_char_double-char"); } }
class C_short_double extends C_double   { public void f(short[] xs) { A.a("C_short_double-short"); } }
class C_int_double extends C_double     { public void f(int[] xs) { A.a("C_int_double-int"); } }
class C_long_double extends C_double    { public void f(long[] xs) { A.a("C_long_double-long"); } }
class C_float_double extends C_double   { public void f(float[] xs) { A.a("C_float_double-float"); } }


class C_boolean_float extends C_float { public void f(boolean[] xs) { A.a("C_boolean_float-boolean"); } }
class C_byte_float extends C_float    { public void f(byte[] xs) { A.a("C_byte_float-byte"); } }
class C_char_float extends C_float    { public void f(char[] xs) { A.a("C_char_float-char"); } }
class C_short_float extends C_float   { public void f(short[] xs) { A.a("C_short_float-short"); } }
class C_int_float extends C_float     { public void f(int[] xs) { A.a("C_int_float-int"); } }
class C_long_float extends C_float    { public void f(long[] xs) { A.a("C_long_float-long"); } }
class C_double_float extends C_float  { public void f(double[] xs) { A.a("C_double_float-double"); } }


class C_boolean_long extends C_long { public void f(boolean[] xs) { A.a("C_boolean_long-boolean"); } }
class C_byte_long extends C_long    { public void f(byte[] xs) { A.a("C_byte_long-byte"); } }
class C_char_long extends C_long    { public void f(char[] xs) { A.a("C_char_long-char"); } }
class C_short_long extends C_long   { public void f(short[] xs) { A.a("C_short_long-short"); } }
class C_int_long extends C_long     { public void f(int[] xs) { A.a("C_int_long-int"); } }
class C_float_long extends C_long   { public void f(float[] xs) { A.a("C_float_long-float"); } }
class C_double_long extends C_long  { public void f(double[] xs) { A.a("C_double_long-double"); } }


class C_boolean_int extends C_int { public void f(boolean[] xs) { A.a("C_boolean_int-boolean"); } }
class C_byte_int extends C_int    { public void f(byte[] xs) { A.a("C_byte_int-byte"); } }
class C_char_int extends C_int    { public void f(char[] xs) { A.a("C_char_int-char"); } }
class C_short_int extends C_int   { public void f(short[] xs) { A.a("C_short_int-short"); } }
class C_long_int extends C_int    { public void f(long[] xs) { A.a("C_long_int-long"); } }
class C_float_int extends C_int   { public void f(float[] xs) { A.a("C_float_int-float"); } }
class C_double_int extends C_int  { public void f(double[] xs) { A.a("C_double_int-double"); } }


class C_boolean_short extends C_short { public void f(boolean[] xs) { A.a("C_boolean_short-boolean"); } }
class C_byte_short extends C_short    { public void f(byte[] xs) { A.a("C_byte_short-byte"); } }
class C_char_short extends C_short    { public void f(char[] xs) { A.a("C_char_short-char"); } }
class C_int_short extends C_short     { public void f(int[] xs) { A.a("C_int_short-int"); } }
class C_long_short extends C_short    { public void f(long[] xs) { A.a("C_long_short-long"); } }
class C_float_short extends C_short   { public void f(float[] xs) { A.a("C_float_short-float"); } }
class C_double_short extends C_short  { public void f(double[] xs) { A.a("C_double_short-double"); } }


class C_boolean_char extends C_char { public void f(boolean[] xs) { A.a("C_boolean_char-boolean"); } }
class C_byte_char extends C_char    { public void f(byte[] xs) { A.a("C_byte_char-byte"); } }
class C_short_char extends C_char   { public void f(short[] xs) { A.a("C_short_char-short"); } }
class C_int_char extends C_char     { public void f(int[] xs) { A.a("C_int_char-int"); } }
class C_long_char extends C_char    { public void f(long[] xs) { A.a("C_long_char-long"); } }
class C_float_char extends C_char   { public void f(float[] xs) { A.a("C_float_char-float"); } }
class C_double_char extends C_char  { public void f(double[] xs) { A.a("C_double_char-double"); } }


class C_boolean_byte extends C_byte { public void f(boolean[] xs) { A.a("C_boolean_byte-boolean"); } }
class C_char_byte extends C_byte    { public void f(char[] xs) { A.a("C_char_byte-char"); } }
class C_short_byte extends C_byte   { public void f(short[] xs) { A.a("C_short_byte-short"); } }
class C_int_byte extends C_byte     { public void f(int[] xs) { A.a("C_int_byte-int"); } }
class C_long_byte extends C_byte    { public void f(long[] xs) { A.a("C_long_byte-long"); } }
class C_float_byte extends C_byte   { public void f(float[] xs) { A.a("C_float_byte-float"); } }
class C_double_byte extends C_byte  { public void f(double[] xs) { A.a("C_double_byte-double"); } }


class C_byte_boolean extends C_boolean    { public void f(byte[] xs) { A.a("C_byte_boolean-byte"); } }
class C_char_boolean extends C_boolean    { public void f(char[] xs) { A.a("C_char_boolean-char"); } }
class C_short_boolean extends C_boolean   { public void f(short[] xs) { A.a("C_short_boolean-short"); } }
class C_int_boolean extends C_boolean     { public void f(int[] xs) { A.a("C_int_boolean-int"); } }
class C_long_boolean extends C_boolean    { public void f(long[] xs) { A.a("C_long_boolean-long"); } }
class C_float_boolean extends C_boolean   { public void f(float[] xs) { A.a("C_float_boolean-float"); } }
class C_double_boolean extends C_boolean  { public void f(double[] xs) { A.a("C_double_boolean-double"); } }
