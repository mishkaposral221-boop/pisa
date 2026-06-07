package rich.util.modules.autobuy;

public class BuyRequest {
   public int price;
   public String itemId;
   public String displayName;
   public int count;
   public String loreHash;
   public int maxPrice;
   public int minQuantity;

   public BuyRequest(int var1, String var2, String var3, int var4, String var5, int var6, int var7) {
      this.price = var1;
      this.itemId = var2;
      this.displayName = var3;
      this.count = var4;
      this.loreHash = var5;
      this.maxPrice = var6;
      this.minQuantity = var7;
   }
}
