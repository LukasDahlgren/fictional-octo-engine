package kassasystem;

import java.time.Clock;
import java.time.LocalDate;

import kassasystem.basket.Basket;
import kassasystem.payment.FakePayer;
import kassasystem.scanning.BarcodeScanner;
import kassasystem.scanning.ScanResult;
import kassasystem.receipt.Receipt;
import kassasystem.customer.Customer;
import kassasystem.discount.DiscountCalculator;
import kassasystem.discount.DiscountOffer;
import kassasystem.money.Money;

import kassasystem.logging.TransactionLogger;

import java.io.IOException;
import java.io.UncheckedIOException;

public class CashRegister {

    private final Clock clock;
    private FakePayer fakePayer;
    private Purchase currentPurchase;

    private final TransactionLogger transactionLogger;

    public CashRegister() {
        this(Clock.systemUTC());
    }

    public CashRegister(Clock clock) {
        if (clock == null) {
            throw new IllegalArgumentException("Clock must not be null");
        }
        this.clock = clock;
        this.transactionLogger = null;
        this.fakePayer = new FakePayer(true);
    }

    public CashRegister(TransactionLogger transactionLogger, Clock clock) {
        if (transactionLogger == null) {
            throw new IllegalArgumentException("Transaction logger must not be null");
        }
        if (clock == null) {
            throw new IllegalArgumentException("Clock must not be null");
        }

        this.transactionLogger = transactionLogger;
        this.clock = clock;
        this.fakePayer = new FakePayer(true);
    }


    // Returns null when the register has no current purchase.
    public Purchase getCurrentPurchase() {
        return currentPurchase;
    }

    public Purchase startPurchase() {
        if (currentPurchase != null) {
            throw new IllegalStateException("Cannot start a purchase while another purchase is current");
        }

        currentPurchase = new Purchase(new Basket());
        return currentPurchase;
    }

    public ScanResult scanProduct(BarcodeScanner scanner) {
        if(currentPurchase == null){
            throw new IllegalStateException("Utan ett pågående köp kan en vara inte scannas");
        }

        if(currentPurchase.getStatus() != Purchase.Status.ACTIVE){
            throw new IllegalStateException("Varor kan endast scannas till ett aktivt köp");
        }

        ScanResult result = scanner.scan();

        if(result.getStatus() != ScanResult.Status.FOUND){
            return result;
        }

        currentPurchase.getBasket().addProduct(result.getProduct());
        return result;
    }

    public Receipt completePurchase() {
        ensureActivePurchase();

        Money totalPrice = currentPurchase.getBasket().getTotalPrice();

        if (!fakePayer.pay()) {
            throw new IllegalStateException("Payment failed");
        }

        return finishPurchase(totalPrice);
    }

    public Receipt completePurchase(Customer customer) {
        return completePurchase(customer, null);
    }

    public Receipt completePurchase(
            Customer customer,
            DiscountOffer offer
    ) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer must not be null");
        }

        ensureActivePurchase();

        Money originalPrice = currentPurchase.getBasket().getTotalPrice();
        LocalDate purchaseDate = LocalDate.now(clock);
        DiscountCalculator calculator = new DiscountCalculator();

        Money discountedPrice = calculator.calculatePrice(
                originalPrice,
                customer,
                purchaseDate,
                offer
        );

        Purchase completedPurchase = currentPurchase;
        Receipt receipt = finishPurchase(discountedPrice);

        customer.addPurchase(completedPurchase);

        return receipt;
    }

    private void ensureActivePurchase() {
    if (currentPurchase == null) {
        throw new IllegalStateException(
                "Cannot complete a purchase when there is no current purchase"
        );
    }

    if (currentPurchase.getStatus() != Purchase.Status.ACTIVE) {
        throw new IllegalStateException(
                "Only active purchases can be completed"
        );
    }
}

    private Receipt finishPurchase(Money totalPrice) {
        Receipt receipt = new Receipt(
                currentPurchase.getBasket(),
                totalPrice,
                clock
        );

        if (transactionLogger != null) {
            try {
                transactionLogger.log(receipt);
            } catch (IOException exception) {
                throw new UncheckedIOException(
                        "Could not log completed purchase",
                        exception
                );
            }
        }

        currentPurchase.complete();
        currentPurchase = null;

        return receipt;
    }

    public void cancelPurchase() {
        if (currentPurchase == null) {
            throw new IllegalStateException("Cannot cancel a purchase when there is no current purchase");
        }

        currentPurchase.cancel();
        currentPurchase = null;
    }
}

