public aspect Aspect {
        int Target.foo = 9;

        void Target.foo() {

        }
}


class Target {

}


