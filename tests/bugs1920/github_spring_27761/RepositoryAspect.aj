import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public aspect RepositoryAspect {
  Object around(): execution(* JpaRepository.save*(..)) {
    System.out.println(thisJoinPoint);
    return proceed();
  }

  public static void main(String[] args) {
    new RepositoryImpl<String>().saveAll(Arrays.asList("One", "Two", "Three"));
  }
}

interface CrudRepository<T> {
  <S extends T> Iterable<S> saveAll(Iterable<S> entities);
}

/*
interface JpaRepository<T> extends CrudRepository<T> {
  @Override
  <S extends T> List<S> saveAll(Iterable<S> entities);
}
*/

class RepositoryImpl<S> implements JpaRepository<String> {
  @Override
  public <S extends String> List<S> saveAll(Iterable<S> entities) {
    List<S> entityList = new ArrayList<>();
    entities.iterator().forEachRemaining(entityList::add);
    System.out.println("Saving " + entityList);
    return entityList;
  }
}
