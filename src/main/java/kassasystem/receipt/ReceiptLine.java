package kassasystem.receipt;

import kassasystem.money.Currency;
import kassasystem.money.Money;
import kassasystem.money.Sek;

import java.math.BigDecimal;

public final class ReceiptLine {
   private final String productName;
   private final int quantity;
   private final Money unitPrice;
   private final BigDecimal vatRate;

   public ReceiptLine(String productName, int quantity, Money unitPrice) {
      this(productName, quantity, unitPrice, BigDecimal.ZERO);
   }

   public ReceiptLine(String productName, int quantity, Money unitPrice, BigDecimal vatRate) {
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
      BigDecimal amount = unitPrice.getAmount();
      if (amount.compareTo(BigDecimal.ZERO) < 0) {
         throw new IllegalArgumentException("Unit price must not be negative");
      }
      if (vatRate == null || vatRate.compareTo(BigDecimal.ZERO) < 0
              || vatRate.compareTo(BigDecimal.ONE) > 0) {
         throw new IllegalArgumentException("VAT rate must be between zero and one");
      }
      this.productName = productName;
      this.quantity = quantity;
      this.unitPrice = unitPrice;
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

   public BigDecimal getVatRate() {
      return vatRate;
   }

   public Money getTotalPrice() {
      Money total = unitPrice.multiply(quantity);
      return total;
   }
}
