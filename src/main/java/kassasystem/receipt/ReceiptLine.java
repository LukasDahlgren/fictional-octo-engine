package kassasystem.receipt;

import kassasystem.money.Currency;
import kassasystem.money.Money;
import kassasystem.money.Sek;

public final class ReceiptLine {
   private final String productName;
   private final int quantity;
   private final Money unitPrice;
   private final double vatRate;

   public ReceiptLine(String productName, int quantity, Money unitPrice) {
      this(productName, quantity, unitPrice, 0);
   }

   public ReceiptLine(String productName, int quantity, Money unitPrice, double vatRate) {
      if (productName == null || productName.isBlank()) {
         throw new IllegalArgumentException("Product name must not be blank");
      }
      if (quantity < 1) {
         throw new IllegalArgumentException("Quantity must be at least 1");
      }
      if (unitPrice == null) {
         throw new IllegalArgumentException("Unit price must not be null");
      }
      Currency currency = unitPrice.getCurrency();
      if (currency == null || currency.compareTo(Sek.SEK) != 0) {
         throw new IllegalArgumentException("Unit price must use SEK");
      }
      double amount = unitPrice.getAmount();
      if (!Double.isFinite(amount)) {
         throw new IllegalArgumentException("Unit price must be finite");
      }
      if (amount < 0) {
         throw new IllegalArgumentException("Unit price must not be negative");
      }
      if (!Double.isFinite(vatRate) || vatRate < 0 || vatRate > 1) {
         throw new IllegalArgumentException("VAT rate must be between zero and one");
      }
      this.productName = productName;
      this.quantity = quantity;
      this.unitPrice = new Money(currency, amount);
      this.vatRate = vatRate;
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

   public double getVatRate() {
      return vatRate;
   }

   public Money getTotalPrice() {
      double total = unitPrice.getAmount() * quantity;
      if (!Double.isFinite(total)) {
         throw new ArithmeticException("Total price overflow");
      }
      return new Money(unitPrice.getCurrency(), total);
   }
}
