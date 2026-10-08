public class Currency implements Comparable<Currency>{

   private String name;
   private String symbol;
   public int[] denominations;

   Currency(String name, String symbol, int... denominations){
      this.name = name;
      this.symbol = symbol;
      this.denominations = denominations;
   }

   public Money getZeroMoney(){
      return new Money(this, 0);
   }

   @Override
   public int compareTo(Currency other){
      return this.name.compareTo(other.name);
   }

}
