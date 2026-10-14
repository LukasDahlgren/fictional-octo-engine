package kassasystem;

import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.product.Product;
import kassasystem.product.ProductCategory;
import kassasystem.product.VATCategory;

public class main{
   public static void main(String[] args){

      ProductCategory drinks = new ProductCategory("drinks", VATCategory.TWELVE_PERCENT);
      Product product = new Product("beer", new Money(Sek.SEK, 10), drinks);
      System.out.println("" + product.getPrice().getAmount() * (1 + product.getCategory().getVat()));
   }
}
