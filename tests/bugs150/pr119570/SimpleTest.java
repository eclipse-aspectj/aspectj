import java.util.ArrayList;

interface INode<P, C> {
    INode<P, C> getParent();
}

 abstract aspect NodeImpl<Parent, Child> {
    declare parents : Child implements INode<Parent, Child>;
    declare parents : Parent implements INode<Parent, Child>;
    public final INode<Parent, Child> INode.getParent() {
        return null;
    }
}

public class SimpleTest {

    public static void main(String[] args) {
        Tag tag1 = new Tag();
        Tag tag2 = new Tag();
        tag1.getParent();
    }

}
class Tag {  }

aspect X extends NodeImpl<Tag,Tag> {}
