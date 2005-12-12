package bugs;

import bugsOtherPackage.INode;
import bugsOtherPackage.NodeImpl;

public class ParameterizedDP {

    public static void main(String[] args) {
        // 1) compile-time error here without 
        //    {unneeded} subaspect declare-parent
        // Tag as INode<Tag, Tag> from PC extends NodeImpl<Tag, Tag>
        ((TaggedTexts.Tag) null).getParent();       
    }

}
class TaggedTexts {

    public static class Text {  }

    public static class Tag {  }
    static aspect PC extends NodeImpl<Tag, Tag> {
        // unneeded declare-parents duplicates one in NodeImpl
        // when here, get spurious error message
        // when commented out, d-p fails and get compiler error at 1) above
//        declare parents : Tag implements INode<Tag,Tag>;
    }
}
