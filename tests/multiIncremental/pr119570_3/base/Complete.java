
import java.util.ArrayList;

interface INode<Parent, Child> {
    INode<Parent, Child> getParent();
}

abstract aspect NodeImpl<Parent, Child> {
    declare parents : Child implements INode<Parent, Child>;
    declare parents : Parent implements INode<Parent, Child>;

    private INode<Parent, Child> INode.fParent;
    public final INode<Parent, Child> INode.getParent() {
        return fParent;
    }
}

aspect PC extends NodeImpl<Tag, Tag> {
//  declare parents : Tag implements INode<Tag,Tag>;
}

class Text {}
class Tag {}

public class Complete {
    public static void main(String[] args) {
        ((Tag) null).getParent();       
    }
}
