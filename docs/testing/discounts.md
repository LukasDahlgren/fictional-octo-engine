# Discount Decision Table

The system follows these discount rules:

- A customer with an active membership receives a 10% membership discount.
- An item can be eligible for a time-limited offer discount.
- At most one discount can be applied to the same item.
- If both the membership discount and the offer discount apply, use the discount
  that gives the customer the greatest savings.
- A customer without a valid discount pays the regular price.

T = true<br>
F = false<br>
- = the condition does not affect the rule

| Condition/action | Rule 1 | Rule 2 | Rule 3 | Rule 4 | Rule 5 |
|---|---:|---:|---:|---:|---:|
| Does the customer have an active membership? | F | T | F | T | T |
| Is an offer discount active? | F | F | T | T | T |
| Is the offer discount greater than 10%? | - | - | - | F | T |
| No discount is applied | T | F | F | F | F |
| The 10% membership discount is applied | F | T | F | T | F |
| The offer discount is applied | F | F | T | F | T |

## Test Cases

1. A customer without an active membership or an offer receives no discount.
2. A customer with an active membership and no offer receives a 10% discount.
3. A customer without an active membership receives an active offer discount.
4. A customer with an active membership receives the membership discount when the offer is 10% or less.
5. A customer with an active membership receives the offer discount when it is greater than 10%.

In addition to the decision table, test the boundaries of the offer's validity period:

- the offer is active on its start date,
- the offer is active on its end date,
- the offer is inactive before its start date,
- the offer is inactive after its end date.
