package kassasystem;

import java.time.Clock;
import java.time.LocalDate;
import kassasystem.payment.Payer;
import kassasystem.payment.Payment;
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
    private final Payer payer;
    private Purchase currentPurchase;

    private final TransactionLogger transactionLogger;

    public CashRegister(Clock clock, Payer payer) {
        if (clock == null) {
            throw new IllegalArgumentException("Clock must not be null");
        }
        if (payer == null) {
            throw new IllegalArgumentException("Payer must not be null");
        }
        this.clock = clock;
        this.transactionLogger = null;
        this.payer = payer;
    }

    public CashRegister(
            TransactionLogger transactionLogger,
            Clock clock,
            Payer payer
    ) {
        if (transactionLogger == null) {
            throw new IllegalArgumentException("Transaction logger must not be null");
        }
        if (clock == null) {
            throw new IllegalArgumentException("Clock must not be null");
        }
        if (payer == null) {
            throw new IllegalArgumentException("Payer must not be null");
        }
        this.transactionLogger = transactionLogger;
        this.clock = clock;
        this.payer = payer;
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
        if (scanner == null) {
            throw new IllegalArgumentException("Scanner must not be null");
        }
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

        return finishPurchase(totalPrice, null);
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

        return finishPurchase(discountedPrice, customer);
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

        if (currentPurchase.getBasket().isEmpty()) {
            throw new IllegalStateException("Cannot complete a purchase with no products in the basket");
        }
    }

    private Receipt finishPurchase(Money totalPrice, Customer customer) {
        Receipt receipt = new Receipt(
                currentPurchase.getBasket(),
                totalPrice,
                clock
        );

        Payment payment = new Payment(totalPrice, payer);
        if (!payment.pay()) {
            throw new IllegalStateException("Payment failed");
        }

        // A log failure cannot undo successful payment.
        Purchase completedPurchase = currentPurchase;
        completedPurchase.complete();
        currentPurchase = null;
        if (customer != null) {
            customer.addPurchase(completedPurchase);
        }

        if (transactionLogger != null) {
            try {
                transactionLogger.log(receipt);
            } catch (IOException exception) {
                throw new UncheckedIOException(
                        "Purchase completed and paid, but transaction could not be logged",
                        exception
                );
            }
        }

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

