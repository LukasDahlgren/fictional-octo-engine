public final class ReceiptLine {
   private final String productName;
   private final int quantity;
   private final Money unitPrice;

   public ReceiptLine(String productName, int quantity, Money unitPrice) {
      if (productName == null || productName.isBlank()) {
         throw new IllegalArgumentException("Product name must not be blank");
      }
      if (quantity < 1) {
         throw new IllegalArgumentException("Quantity must be at least 1");
      }
      if (unitPrice == null) {
         throw new IllegalArgumentException("Unit price must not be null");
      }
      this.productName = productName;
      this.quantity = quantity;
      this.unitPrice = new Money(unitPrice.getCurrency(), unitPrice.getAmount());
   }

   public String getProductName() {
      return productName;
   }

   public int getQuantity() {
      return quantity;
   }

   public Money getUnitPrice() {
      return unitPrice;
   }

   public Money getTotalPrice() {
      double total = unitPrice.getAmount() * quantity;
      return new Money(unitPrice.getCurrency(), total);
   }
}
