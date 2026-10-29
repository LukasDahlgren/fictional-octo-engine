package kassasystem.basket;

import kassasystem.money.Money;
import kassasystem.product.Product;

public class BasketItem{
   private final Product product;
   private int quantity;
   private boolean frozen;


   public BasketItem(Product product, int quantity){
      if (product == null) {
         throw new IllegalArgumentException("Product must not be null");
      }
      if (quantity < 1) {
         throw new IllegalArgumentException("Quantity must be at least 1");
      }
      this.product = product;
      this.quantity = quantity;
   }

   public Product getProduct(){
      return product;
   }

   public int getQuantity(){
      return quantity;
   }

   public void increaseQuantity(){
      increaseQuantity(1);
   }

   public void increaseQuantity(int amount){
      ensureMutable();
      if (amount < 1) {
         throw new IllegalArgumentException("Amount must be at least 1");
      }
      quantity = Math.addExact(quantity, amount);
   }

   public void decreaseQuantity(){
      ensureMutable();
      if (quantity == 1) {
         return;
      }
      quantity--;
   }

   void freeze(){
      frozen = true;
   }

   private void ensureMutable(){
      if (frozen) {
         throw new IllegalStateException("Closed purchase rows cannot be changed");
      }
   }

   public Money getTotalPrice(){
      return product.getPrice().multiply(quantity);
   }

}
