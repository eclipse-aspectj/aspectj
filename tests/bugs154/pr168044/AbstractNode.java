public abstract class AbstractNode < SelfNode extends AbstractNode<SelfNode, DualNode>, DualNode extends AbstractNode<DualNode, SelfNode> > {
}

class SubClass extends AbstractNode<A,B> {
}

class A extends AbstractNode<A,B> { }
class B extends AbstractNode<B,A> { }

abstract class Mad
<
   Id       extends Comparable<Id>,
   Np       extends Mad<Id, Np, Nt, Np, Nt>,
   Nt       extends Mad<Id, Np, Nt, Nt, Np>,
   SelfNode extends Mad<Id, Np, Nt, SelfNode, DualNode>,
   DualNode extends Mad<Id, Np, Nt, DualNode, SelfNode>
>
{
}
