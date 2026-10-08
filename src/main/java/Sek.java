public class Sek extends Currency{
   public static final Sek SEK = new Sek();

   private Sek(){
      super("SEK", "kr", 100000, 50000, 10000, 5000, 2000, 1000, 500, 100, 1);
   }
}
