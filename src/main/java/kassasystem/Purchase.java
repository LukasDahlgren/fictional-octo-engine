package kassasystem;

import kassasystem.basket.Basket;

public class Purchase {

    public enum Status {
        ACTIVE,
        COMPLETED,
        CANCELLED
    }

    private final Basket basket;
    private Status status;

    public Purchase() {
        this(new Basket());
    }

    public Purchase(Basket basket) {
        if (basket == null) {
            throw new IllegalArgumentException("Basket must not be null");
        }

        this.basket = basket;
        this.status = Status.ACTIVE;
    }

    public Basket getBasket() {
        return basket;
    }

    public Status getStatus() {
        return status;
    }

    public void complete() {
        if (status != Status.ACTIVE) {
            throw new IllegalStateException("Only active purchases can be completed");
        }
        status = Status.COMPLETED;
    }

    public void cancel() {
        if (status != Status.ACTIVE) {
            throw new IllegalStateException("Only active purchases can be cancelled");
        }

        status = Status.CANCELLED;
    }
}
