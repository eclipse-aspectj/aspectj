package bugsOtherPackage;

public interface INode<Parent, Child> {
    boolean setParent(INode<Parent, Child> p);
    INode<Parent, Child> getParent();
}