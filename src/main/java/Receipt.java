import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Receipt {
   Clock clock;
   Money totalPrice;
   LocalDateTime timestamp;
   private final List<ReceiptLine> items;

   public Receipt(Basket basket, Clock clock) {
      if (basket == null) {
         throw new IllegalArgumentException("Basket cannot be null");
      }
      if (clock == null) {
         throw new IllegalArgumentException("Clock cannot be null");
      }

      this.clock = clock;
      this.totalPrice = basket.getTotalPrice();
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
      // TODO: Return the purchase total captured when the receipt was created.
      throw new UnsupportedOperationException("Not implemented yet");
   }

   public LocalDateTime getTimestamp() {
      return timestamp;
   }

   public String toText() {
      // TODO: Format the receipt as readable text.
      throw new UnsupportedOperationException("Not implemented yet");
   }
}
