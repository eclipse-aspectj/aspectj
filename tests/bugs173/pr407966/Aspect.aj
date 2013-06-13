public aspect Aspect {
        void around(): call(* Def.def(..)) {
                System.out.println("aspect");
        }
}
