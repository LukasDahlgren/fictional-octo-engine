package kassasystem.money;

import java.util.Objects;

public class Money{

   private final Currency currency;
   private final double amount;

   public Money(Currency currency, double amount){
      this.currency = currency;
      this.amount = amount;
   }
   public double getAmount(){
      return amount;
   }
   public Currency getCurrency(){
      return currency;
   }

   // Adderar två pengabelopp och returnerar ett nytt Money-objekt.
   public Money add(Money other) {
      requireSameCurrency(other);
      return new Money(currency, amount + other.amount);
   }

   public Money subtract(Money other) {
      requireSameCurrency(other);
      return new Money(currency, amount - other.amount);
   }

   public Money multiply(int factor) {
      return new Money(currency, amount * factor);
   }

   public boolean isGreaterThan(Money other) {
      requireSameCurrency(other);
      return amount > other.amount;
   }

   public boolean isLessThan(Money other) {
      requireSameCurrency(other);
      return amount < other.amount;
   }

   private void requireSameCurrency(Money other) {
      Objects.requireNonNull(other, "Money får inte vara null.");

      if (!currency.equals(other.currency)) {
         throw new IllegalArgumentException(
                 "Valutorna måste vara samma."
         );
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (!(obj instanceof Money other)) {
         return false;
      }

      return Double.compare(amount, other.amount) == 0
              && Objects.equals(currency, other.currency);
   }

   @Override
   public int hashCode() {
      return Objects.hash(currency, amount);
   }


}
