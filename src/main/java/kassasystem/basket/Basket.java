package kassasystem.basket;

import kassasystem.money.Currency;
import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.product.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Basket{

   private final List<BasketItem> items;
   private final Currency currency;
   private boolean frozen;

   public Basket(){
      this(Sek.SEK);
   }

   public Basket(Currency currency){
      if (currency == null) {
         throw new IllegalArgumentException("Currency must not be null");
      }
      this.currency = currency;
      items = new ArrayList<>();
   }

   public void addItem(BasketItem item){
      ensureMutable();
      if (item == null) {
         throw new IllegalArgumentException("Item must not be null");
      }
      Currency itemCurrency = item.getProduct().getPrice().getCurrency();
      if (itemCurrency == null || currency.compareTo(itemCurrency) != 0) {
         throw new IllegalArgumentException("Item must use the basket currency");
      }
      for (BasketItem existingItem : items) {
         if (existingItem.getProduct().getEan().equals(item.getProduct().getEan())) {
            existingItem.increaseQuantity(item.getQuantity());
            return;
         }
      }
      items.add(item);
   }

   public void addProduct(Product product){
      addItem(new BasketItem(product, 1));
   }
   public List<BasketItem> getItems(){
      return Collections.unmodifiableList(items);
   }

   public Money getTotalPrice(){
      double total = 0;
      for(BasketItem item : items){
         total += item.getTotalPrice().getAmount();
      }
      return new Money(currency, total);
   }

   public int getTotalQuantity(){
      int total = 0;
      for(BasketItem item : items){
         total = Math.addExact(total, item.getQuantity());
      }
      return total;
   }

   public boolean removeItem(BasketItem item){
      ensureMutable();
      return items.remove(item);
   }

   public boolean removeProduct(Product product){
      ensureMutable();

      if(product == null){
         return false;
      }

      for (int i = 0; i < items.size(); i++) {
         BasketItem item = items.get(i);
         if (item.getProduct().getEan().equals(product.getEan())) {
            if (item.getQuantity() == 1) {
               items.remove(i);
            } else {
               item.decreaseQuantity();
            }
            return true;
         }
      }
      return false;
   }

   public boolean removeAll(List<BasketItem> items){
      ensureMutable();
      if (items == null) {
         throw new IllegalArgumentException("Items must not be null");
      }
      return this.items.removeAll(items);
   }

   // Closing a purchase also locks rows held by callers before checkout.
   public void freeze(){
      for (BasketItem item : items) {
         item.freeze();
      }
      frozen = true;
   }

   private void ensureMutable(){
      if (frozen) {
         throw new IllegalStateException("Closed purchase baskets cannot be changed");
      }
   }

   public boolean isEmpty(){
      return items.isEmpty();
   }
}
