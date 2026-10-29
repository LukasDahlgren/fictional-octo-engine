package kassasystem.money;

import java.math.BigDecimal;

public class Currency implements Comparable<Currency>{

   private String name;
   private String symbol;

   public Currency(String name, String symbol){
      this.name = name;
      this.symbol = symbol;
   }

   public Money getZeroMoney(){
      return new Money(this, BigDecimal.ZERO);
   }

   @Override
   public int compareTo(Currency other){
      return this.name.compareTo(other.name);
   }

}
