package bugs;

import bugsOtherPackage.INode;
import bugsOtherPackage.NodeImpl;

// comments in this bug relate to what happened on AspectJ5 M4
public class ParameterizedDP {

    public static void main(String[] args) {
//        // 1) compile-time error here without 
//        //    {unneeded} subaspect declare-parent
//        // Tag as INode<Tag, Tag> from PC extends NodeImpl<Tag, Tag>
//        ((TaggedTexts.Tag) null).getParent();    
        TaggedTexts.Tag tag1 = new TaggedTexts.Tag();
        TaggedTexts.Tag tag2 = new TaggedTexts.Tag();
        tag1.getParent();
        tag1.setParent(tag2);
        if (!tag1.getParent().equals(tag2)) throw new RuntimeException("");
    }

}
class TaggedTexts {

    public static class Text {  }

    public static class Tag {  }
    static aspect PC extends NodeImpl<Tag, Tag> {
//        // unneeded declare-parents duplicates one in NodeImpl
//        // when here, get spurious error message
//        // when commented out, d-p fails and get compiler error at 1) above
//        declare parents : Tag implements INode<Tag,Tag>;
    }
}