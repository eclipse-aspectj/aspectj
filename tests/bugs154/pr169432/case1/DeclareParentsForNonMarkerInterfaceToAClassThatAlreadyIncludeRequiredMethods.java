package test;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareParents;

@Aspect
public class
DeclareParentsForNonMarkerInterfaceToAClassThatAlreadyIncludeRequiredMethods {
        @DeclareParents("test.ClassThatAlreadyIncludesRequiredMethods")
        public NonMarkerInterface nmi;
}