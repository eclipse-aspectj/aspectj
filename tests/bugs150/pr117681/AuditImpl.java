public class AuditImpl implements Audit {
   private String lastUpdatedBy;
   public String getLastUpdatedBy() {
       return lastUpdatedBy;
   }
   public void setLastUpdatedBy(String un) {
       lastUpdatedBy = un;
   }
}

