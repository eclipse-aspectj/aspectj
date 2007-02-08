public class Errors3 {

}

aspect X {

  pointcut p1(): execution(trivial * *(..));
  pointcut p2_error(): call(trivial * *(..));
  pointcut p3_error(): get(trivial * *(..));
  pointcut p4(): execution(private !trivial * *(..));
  pointcut p5_error(): call(public trivial * *(..));
  pointcut p6_error(): get(protected !trivial * *(..));
}

