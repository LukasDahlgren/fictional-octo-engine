import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

public class Receipt {
   Clock clock;
   Money totalPrice;
   LocalDateTime timestamp;

   public Receipt(Basket basket, Clock clock) {
      this.clock = clock;
      this.totalPrice = basket.getTotalPrice();
      this.timestamp = LocalDateTime.now(clock);
   }

   public List<BasketItem> getItems() {
      // TODO: Return the receipt's item snapshot.
      throw new UnsupportedOperationException("Not implemented yet");
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
