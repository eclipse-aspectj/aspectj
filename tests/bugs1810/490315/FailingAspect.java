package test;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class FailingAspect {
	
    SomeContext authenticationContext;

    @SuppressWarnings("unchecked")
    @Around("execution(* test..SomeServiceImpl.someMethod(test.SomeCriteria))" +
            "&& @annotation(test.SomeAnno)")
    public SomePiece<Collection<SomeDTO>> interceptSomeMethod(ProceedingJoinPoint pjp) throws Throwable {
        SomePiece<Collection<SomeDTO>> piece = (SomePiece<Collection<SomeDTO>>) pjp.proceed();
        List<SomeDTO> filteredResult = piece.getData().stream().filter(item ->
                authenticationContext.getPermissionDetails().checkAccess(
                	item.getTag(), SomeEnum.R)).collect(Collectors.toList());
        return new SomePiece<>(filteredResult, piece.isLast());
    }

}
