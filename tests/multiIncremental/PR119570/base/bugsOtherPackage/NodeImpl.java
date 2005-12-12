package bugsOtherPackage;

import java.util.ArrayList;

public abstract aspect NodeImpl<Parent, Child> {

    declare parents : Child implements INode<Parent, Child>;
    declare parents : Parent implements INode<Parent, Child>;

    private final ArrayList<INode> INode.fChildren = new ArrayList<INode>();

    // are you not supposed to use type parameters here?
    private INode<Parent, Child> INode.fParent;
    public final INode<Parent, Child> INode.getParent() {
        return fParent;
    }
    public final boolean INode.setParent(INode<Parent, Child> newParent) {
        fParent = newParent;
        return true; 
    }
}