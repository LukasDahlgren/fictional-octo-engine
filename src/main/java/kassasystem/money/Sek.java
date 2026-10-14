package kassasystem.money;

public class Sek extends Currency{
   public static final Sek SEK = new Sek();

   private Sek(){
      super("SEK", "kr");
   }
}
