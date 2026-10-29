package kassasystem;

public class Purchase{

   public enum Status{
      ACTIVE, COMPLETED, CANCELLED
   }

   private final Basket basket;
   private Status status;

   public Purchase(){
      this(new Basket());
   }

   public Purchase(Basket basket){
      if(basket == null){
         throw new IllegalArgumentException("Basket must not be null");
      }

      this.basket = basket;
      this.status = Status.ACTIVE;
   }

   public Basket getBasket(){
      return basket;
   }

   public Status getStatus(){
      return status;
   }

   // Only checkout code in this package may change purchase status.
   void complete(){
      if(status != Status.ACTIVE){
         throw new IllegalStateException("Only active purchases can be completed");
      }
      basket.freeze();
      status = Status.COMPLETED;
   }

   void cancel(){
      if(status != Status.ACTIVE){
         throw new IllegalStateException("Only active purchases can be cancelled");
      }

      basket.freeze();
      status = Status.CANCELLED;
   }
}
