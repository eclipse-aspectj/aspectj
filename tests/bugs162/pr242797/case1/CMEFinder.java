import java.util.List;

public class CMEFinder extends OnetFinder<CMEStub, PartitionedCME, LocalizedCME, ContentModelElement>{
} 

class CMEStub {}
class PartitionedCME implements Partitioned{ }
class LocalizedCME implements Localized {}
class ContentModelElement extends OnetElement {} 