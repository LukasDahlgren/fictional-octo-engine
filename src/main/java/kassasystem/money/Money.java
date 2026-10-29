package kassasystem.money;

import java.math.BigDecimal;
import java.util.Objects;

public class Money{

   private final Currency currency;
   private final BigDecimal amount;
   private final double nonFiniteAmount;

   public Money(Currency currency, double amount){
      this.currency = currency;
      if (Double.isFinite(amount)) {
         this.amount = BigDecimal.valueOf(amount);
         this.nonFiniteAmount = 0;
      } else {
         this.amount = null;
         this.nonFiniteAmount = amount;
      }
   }

   private Money(Currency currency, BigDecimal amount) {
      this.currency = currency;
      double doubleAmount = amount.doubleValue();
      if (Double.isFinite(doubleAmount)) {
         this.amount = amount;
         this.nonFiniteAmount = 0;
      } else {
         this.amount = null;
         this.nonFiniteAmount = doubleAmount;
      }
   }

   public double getAmount(){
      if (amount == null) {
         return nonFiniteAmount;
      }
      return amount.doubleValue();
   }

   public Currency getCurrency(){
      return currency;
   }

   // Adds two monetary amounts and returns a new Money object.
   public Money add(Money other) {
      requireSameCurrency(other);
      if (amount == null || other.amount == null) {
         return new Money(currency, getAmount() + other.getAmount());
      }
      return new Money(currency, amount.add(other.amount));
   }

   public Money subtract(Money other) {
      requireSameCurrency(other);
      if (amount == null || other.amount == null) {
         return new Money(currency, getAmount() - other.getAmount());
      }
      return new Money(currency, amount.subtract(other.amount));
   }

   public Money multiply(int factor) {
      if (amount == null) {
         return new Money(currency, nonFiniteAmount * factor);
      }
      return new Money(currency, amount.multiply(BigDecimal.valueOf(factor)));
   }

   public boolean isGreaterThan(Money other) {
      requireSameCurrency(other);
      if (amount == null || other.amount == null) {
         return getAmount() > other.getAmount();
      }
      return amount.compareTo(other.amount) > 0;
   }

   public boolean isLessThan(Money other) {
      requireSameCurrency(other);
      if (amount == null || other.amount == null) {
         return getAmount() < other.getAmount();
      }
      return amount.compareTo(other.amount) < 0;
   }

   private void requireSameCurrency(Money other) {
      Objects.requireNonNull(other, "Money must not be null.");

      if (!currency.equals(other.currency)) {
         throw new IllegalArgumentException(
                 "Currencies must match."
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

      if (!Objects.equals(currency, other.currency)) {
         return false;
      }
      if (amount == null || other.amount == null) {
         return amount == null && other.amount == null
                 && Double.compare(nonFiniteAmount, other.nonFiniteAmount) == 0;
      }
      return amount.compareTo(other.amount) == 0;
   }

   @Override
   public int hashCode() {
      if (amount == null) {
         return Objects.hash(currency, Double.hashCode(nonFiniteAmount));
      }
      return Objects.hash(currency, amount.stripTrailingZeros());
   }


}
