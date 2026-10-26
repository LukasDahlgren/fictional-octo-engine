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

   private final Money subtotalPrice;
   private final Money totalPrice;
   private final LocalDateTime timestamp;
   private final List<ReceiptLine> items;

   public Receipt(Basket basket, Clock clock) {
      this(basket, getBasketTotalPrice(basket), clock);
   }

   public Receipt(Basket basket, Money totalPrice, Clock clock) {
      if (basket == null) {
         throw new IllegalArgumentException("Basket cannot be null");
      }

      if (totalPrice == null) {
         throw new IllegalArgumentException("Total price cannot be null");
      }

      if (clock == null) {
         throw new IllegalArgumentException("Clock cannot be null");
      }

      if (totalPrice.getCurrency().compareTo(Sek.SEK) != 0) {
         throw new IllegalArgumentException("Receipt must use SEK");
      }

      this.subtotalPrice = basket.getTotalPrice();
      this.totalPrice = totalPrice;
      this.timestamp = LocalDateTime.now(clock);
      this.items = new ArrayList<>();

      for (BasketItem item : basket.getItems()) {
         Product product = item.getProduct();

         ReceiptLine line = new ReceiptLine(
                 product.getName(),
                 item.getQuantity(),
                 product.getPrice()
         );

         items.add(line);
      }
   }

   private static Money getBasketTotalPrice(Basket basket) {
      if (basket == null) {
         throw new IllegalArgumentException("Basket cannot be null");
      }

      return basket.getTotalPrice();
   }

   public List<ReceiptLine> getItems() {
      return Collections.unmodifiableList(items);
   }

   public Money getTotalPrice() {
      return totalPrice;
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
      DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(
              "yyyy-MM-dd HH:mm",
              Locale.ROOT
      );

      text.append("Kvitto\n");
      text.append("Datum: ")
              .append(timestamp.format(dateFormat))
              .append("\n");

      String headingFormat =
              "%-" + nameWidth + "s %5s %10s %10s\n";

      text.append(String.format(
              Locale.ROOT,
              headingFormat,
              "Vara",
              "Antal",
              "Styckpris",
              "Summa"
      ));

      String itemFormat =
              "%-" + nameWidth + "s %5d %10.2f %10.2f\n";

      for (ReceiptLine item : items) {
         String row = String.format(
                 Locale.ROOT,
                 itemFormat,
                 item.getProductName(),
                 item.getQuantity(),
                 item.getUnitPrice().getAmount(),
                 item.getTotalPrice().getAmount()
         );

         text.append(row);
      }

      double discountAmount =
              subtotalPrice.getAmount() - totalPrice.getAmount();

      if (discountAmount > 0.0) {
         String subtotal = String.format(
                 Locale.ROOT,
                 "Delsumma: %.2f SEK\n",
                 subtotalPrice.getAmount()
         );

         String discount = String.format(
                 Locale.ROOT,
                 "Rabatt: -%.2f SEK\n",
                 discountAmount
         );

         text.append(subtotal);
         text.append(discount);
      }

      String total = String.format(
              Locale.ROOT,
              "Totalt: %.2f SEK\n",
              totalPrice.getAmount()
      );

      text.append(total);

      return text.toString();
   }
}