public class TestMarkers {
        public class SuperGenericsType {}

        public class SubGenericsType 
                //Following line would produce correct code
                //extends SuperGenericsType
        {}

        public class Super<T extends SuperGenericsType> {}

        public class Sub extends Super<SubGenericsType> {}
}