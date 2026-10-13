# Scanning – equivalence classes

## Equivalence classes

EC-01: code matches a product: valid<br>
EC-02: code does not match a product: invalid<br>
EC-03: no code read (null): invalid<br>
EC-04: code is empty: invalid<br>
EC-05: code contains only whitespace: invalid<br>
EC-06: basket is provided: valid<br>
EC-07: basket is missing (null): invalid<br>
EC-08: scanner is provided: valid<br>
EC-09: scanner is missing (null): invalid<br>
EC-10: product lookup is provided: valid<br>
EC-11: product lookup is missing (null): invalid<br>
EC-12: matching product is not in the basket: valid<br>
EC-13: matching product is already in the basket and its quantity can increase: valid<br>
EC-14: matching product is already at maximum quantity: invalid<br>

## Dependency failures

DF-01: scanner fails and throws an exception. No code is returned. This is different from EC-03, where the scanner works but cannot read a code.<br>
DF-02: scanner reads a code, but product lookup fails and throws an exception. This is different from EC-02, where lookup works but finds no product.<br>

## Mock pre-state

```text
Basket:
  Bread: quantity 1, unit price 20.00 SEK
  Total quantity: 1
  Total price: 20.00 SEK

Product lookup:
  "000123" -> Milk, unit price 12.50 SEK
  "unknown" -> no product

Scanner:
  Returns the input specified in the test case.

Scanning object:
  Uses this basket, scanner and product lookup.
  No scan performed yet.
```

Each test starts with fresh objects. SC-06–SC-08 replace the specified dependency
with `null` before constructing the scanning object.

## Test cases

| Test ID | Covered class | Test input / setup | Expected result |
|---|---|---|---|
| SC-01 | EC-01, EC-06, EC-08, EC-10, EC-12 | `"000123"` maps to milk, price 12.50. Basket contains one bread, price 20. | Add one milk. Keep the bread. Total quantity 2, total price 32.50. |
| SC-02 | EC-02 | `"unknown"` has no matching product. Basket contains one bread. | Report unknown product. Basket stays unchanged. |
| SC-03 | EC-03 | Scanner returns `null`. Basket contains one bread. | Report failed read. No product lookup. Basket stays unchanged. |
| SC-04 | EC-04 | Scanner returns `""`. Basket contains one bread. | Report invalid code. No product lookup. Basket stays unchanged. |
| SC-05 | EC-05 | Scanner returns `" "`, `"\t"`, or `"\n"`. Basket contains one bread. | Report invalid code for each input. No product lookup. Basket stays unchanged. |
| SC-06 | EC-07 | Basket is `null`. Scanner and lookup are valid. | Constructor throws `IllegalArgumentException`. |
| SC-07 | EC-09 | Scanner is `null`. Basket and lookup are valid. | Constructor throws `IllegalArgumentException`. |
| SC-08 | EC-11 | Lookup is `null`. Basket and scanner are valid. | Constructor throws `IllegalArgumentException`. |
| SC-09 | EC-01, EC-13 | Basket contains one bread and two milk units. Scan `"000123"`. | One milk row with quantity 3. Bread stays unchanged. Total quantity 4, total price 57.50. |
| SC-10 | EC-01, EC-14 | Basket contains one bread and a milk row with quantity `Integer.MAX_VALUE`. Scan `"000123"`. | `ArithmeticException` propagates. Rows and quantities stay unchanged. No success result. |
| SC-11 | DF-01 | Scanner double throws `IllegalStateException` instead of returning a code. Basket contains one bread. | The caller receives the exception. Product lookup is not called. Basket stays unchanged. |
| SC-12 | DF-02 | Scanner returns `"000123"`. Lookup double throws `IllegalStateException` instead of returning a product. Basket contains one bread. | The caller receives the exception. No product is added. Basket stays unchanged. |
