package kassasystem.customer;

import kassasystem.Purchase;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Customer {

    private final String customerNumber;
    private final String name;
    private final Membership membership;
    private final List<Purchase> purchaseHistory;

    public Customer(String customerNumber, String name) {
        this(customerNumber, name, null);
    }

    public Customer(String customerNumber, String name, Membership membership) {
        this.customerNumber = customerNumber;
        this.name = name;
        this.membership = membership;
        this.purchaseHistory = new ArrayList<>();
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public String getName() {
        return name;
    }

    public void addPurchase(Purchase purchase) {
        purchaseHistory.add(purchase);
    }

    public List<Purchase> getPurchaseHistory() {
        return new ArrayList<>(purchaseHistory);
    }

    public boolean hasActiveMembershipOn(LocalDate date) {
        if (membership == null) {
            return false;
        }

        return membership.isActiveOn(date);
    }
}