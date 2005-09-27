import java.lang.annotation.*;
import java.lang.ref.*;

class Product{}
class ProductType{}

interface AssociationSource<T> {

  Link<T> getTarget();

  void setTarget(Link<T> _target);

}
aspect ExtendProduct {
       Link<T> AssociationSource._target = null;

       public Link<T> AssociationSource.getTarget() {
           return _target;
       }

       public void AssociationSource.setTarget(Link<T> _target) {
           this._target = _target;
       }

       declare parents : ProductType implements AssociationSource<Product>;
       declare parents : Product     implements AssociationSource<Branch>;
       declare parents : Branch      implements AssociationSource<Revision>;
}

class Link<T> extends SoftReference {

   @SuppressWarnings("unchecked")
   Link(List<T> endPoints) {
       super(endPoints);
   }

   @SuppressWarnings("unchecked")
   public List<T> getEndPoints() {
       return (List<T>)get();
   }

}
