Mode: VM run
Title: Accessibility of class and aspect members from inside weaves

   Code inside a weave does the full cross-product of the
   following things:

   read a var         of the class      private       static
   set a var       X                 X  protected  X  non-static
   call a method                        public
                                        default


Both the aspect and the class are in the default (unnamed) package.
