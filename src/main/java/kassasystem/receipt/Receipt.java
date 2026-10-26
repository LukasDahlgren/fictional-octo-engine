package kassasystem.receipt;

import kassasystem.basket.Basket;
import kassasystem.basket.BasketItem;
import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.product.Product;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Receipt {
   private final Money subtotal;
   private final Money discountAmount;
   private final String discountLabel;
   private final Money totalPrice;
   private final LocalDateTime timestamp;
   private final List<ReceiptLine> items;

   public Receipt(Basket basket, Clock clock) {
      this(basket, clock, new Money(Sek.SEK, 0), null);
   }

   // The checkout supplies the calculated discount; the receipt only records it.
   public Receipt(Basket basket, Clock clock, Money discountAmount, String discountLabel) {
      if (basket == null) {
         throw new IllegalArgumentException("Basket cannot be null");
      }
      if (clock == null) {
         throw new IllegalArgumentException("Clock cannot be null");
      }

      this.subtotal = basket.getTotalPrice();
      if (subtotal.getCurrency().compareTo(Sek.SEK) != 0) {
         throw new IllegalArgumentException("Receipt must use SEK");
      }
      if (!Double.isFinite(subtotal.getAmount()) || subtotal.getAmount() < 0) {
         throw new IllegalArgumentException("Subtotal must be finite and non-negative");
      }
      if (discountAmount == null) {
         throw new IllegalArgumentException("Discount amount cannot be null");
      }
      if (discountAmount.getCurrency() == null
              || discountAmount.getCurrency().compareTo(Sek.SEK) != 0) {
         throw new IllegalArgumentException("Discount must use SEK");
      }
      double discount = discountAmount.getAmount();
      if (!Double.isFinite(discount) || discount < 0 || discount > subtotal.getAmount()) {
         throw new IllegalArgumentException("Discount must be between zero and the subtotal");
      }
      if (discount > 0 && (discountLabel == null || discountLabel.isBlank()
              || discountLabel.contains("\n") || discountLabel.contains("\r"))) {
         throw new IllegalArgumentException("Discount label must be a non-blank single line");
      }
      this.discountAmount = new Money(Sek.SEK, discount);
      this.discountLabel = discountLabel;
      this.totalPrice = subtotal.subtract(this.discountAmount);
      this.timestamp = LocalDateTime.now(clock);
      this.items = new ArrayList<>();
      for (BasketItem item : basket.getItems()) {
         Product product = item.getProduct();
         ReceiptLine line = new ReceiptLine(product.getName(), item.getQuantity(), product.getPrice());
         items.add(line);
      }
   }

   public List<ReceiptLine> getItems() {
      return Collections.unmodifiableList(items);
   }

   public Money getTotalPrice() {
      return totalPrice;
   }

   public Money getSubtotal() {
      return subtotal;
   }

   public Money getDiscountAmount() {
      return discountAmount;
   }

   public String getDiscountLabel() {
      return discountLabel;
   }

   public LocalDateTime getTimestamp() {
      return timestamp;
   }

   public String toText() {
      int nameWidth = 20;
      for (ReceiptLine item : items) {
         if (item.getProductName().length() > nameWidth) {
            nameWidth = item.getProductName().length();
         }
      }

      StringBuilder text = new StringBuilder();
      DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ROOT);
      text.append("Kvitto\n");
      text.append("Datum: ").append(timestamp.format(dateFormat)).append("\n");

      String headingFormat = "%-" + nameWidth + "s %5s %10s %10s\n";
      text.append(String.format(Locale.ROOT, headingFormat, "Vara", "Antal", "Styckpris", "Summa"));

      String itemFormat = "%-" + nameWidth + "s %5d %10.2f %10.2f\n";
      for (ReceiptLine item : items) {
         String row = String.format(Locale.ROOT, itemFormat,
                 item.getProductName(), item.getQuantity(),
                 item.getUnitPrice().getAmount(), item.getTotalPrice().getAmount());
         text.append(row);
      }

      if (discountAmount.getAmount() > 0) {
         text.append(String.format(Locale.ROOT, "Delsumma: %.2f SEK\n", subtotal.getAmount()));
         text.append(String.format(Locale.ROOT, "%s: -%.2f SEK\n",
                 discountLabel, discountAmount.getAmount()));
      }
      String total = String.format(Locale.ROOT, "Totalt: %.2f SEK\n", totalPrice.getAmount());
      text.append(total);
      return text.toString();
   }
}
