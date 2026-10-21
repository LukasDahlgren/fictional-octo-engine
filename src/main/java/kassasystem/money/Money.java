package kassasystem.money;

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
      return new Money(currency, amount + other.amount);
   }


}
