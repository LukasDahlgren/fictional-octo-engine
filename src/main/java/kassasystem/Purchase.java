package kassasystem;

import kassasystem.basket.Basket;

public class Purchase {

    public enum Status {
        ACTIVE,
        PAUSED,
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

    public void pause() {
        // TODO: Allow an active purchase to become paused.
        throw new UnsupportedOperationException("Pausing a purchase is not implemented yet");
    }

    public void resume() {
        // TODO: Allow a paused purchase to become active again.
        throw new UnsupportedOperationException("Resuming a purchase is not implemented yet");
    }

    public void complete() {
        // TODO: Validate that the purchase can be completed before changing its status.
        throw new UnsupportedOperationException("Completing a purchase is not implemented yet");
    }

    public void cancel() {
        // TODO: Allow an active or paused purchase to become cancelled.
        throw new UnsupportedOperationException("Cancelling a purchase is not implemented yet");
    }
}
