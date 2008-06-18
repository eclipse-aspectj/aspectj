public class GenericGenericMemberClass {

        // this line never causes a problem
        GenericClass<Double> [] genericMemberArray;

        // this line causes compilation errors to be introduced
        GenericClass< GenericClass<Double> >[] genericGenericMemberArray;

        // uncommenting the following lines removes the compilation errors (very unexpectedly, for me at least)
//      @SuppressWarnings("unused")
//      private static final GenericClass< GenericClass<Double> > genericGenericMember = null;

        public void test() {
        }
}

