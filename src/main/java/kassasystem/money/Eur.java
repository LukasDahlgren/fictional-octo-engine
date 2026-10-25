package kassasystem.money;

public class Eur extends Currency{
    public static final Eur EUR = new Eur();

    private Eur(){
        super("EUR", "€");
    }
}
