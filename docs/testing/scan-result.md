# ScanResult – equivalence classes

`ScanResult` contains a status and a product. `FOUND` must include a product;
failure statuses must have product `null`.

These cases test the result object directly, without a code reader or lookup.
Tests for the scanning flow are described in [scanning.md](scanning.md).

## Equivalence classes

EC-01: result status is `FOUND` and a product is provided: valid<br>
EC-02: result status is `FOUND` and the product is missing (null): invalid<br>
EC-03: result status is `FAILED_READ`, `INVALID_CODE` or `UNKNOWN_PRODUCT` and the product is missing (null): valid<br>
EC-04: result status is `FAILED_READ`, `INVALID_CODE` or `UNKNOWN_PRODUCT` and a product is provided: invalid<br>
EC-05: result status is missing (null): invalid<br>

## Pre-state

```text
Product:
  Milk, unit price 12.50 SEK

ScanResult:
  Not constructed yet.
```

Each test starts with fresh objects.

## Test cases

| Test ID | Covered class | Test input / setup | Expected result |
|---|---|---|---|
| SR-01 | EC-01 | Construct `ScanResult` with status `FOUND` and the milk Product object. | `getStatus()` returns `FOUND`. `getProduct()` returns the exact milk Product object. |
| SR-02 | EC-02 | Construct `ScanResult` with status `FOUND` and product `null`. | Constructor throws `IllegalArgumentException`. |
| SR-03 | EC-03 | Construct `ScanResult` with each of `FAILED_READ`, `INVALID_CODE` and `UNKNOWN_PRODUCT`, and product `null`. | `getStatus()` returns the supplied status. `getProduct()` returns `null` for each status. |
| SR-04 | EC-04 | Construct `ScanResult` with each of `FAILED_READ`, `INVALID_CODE` and `UNKNOWN_PRODUCT`, and the milk Product object. | Constructor throws `IllegalArgumentException` for each status. |
| SR-05 | EC-05 | Construct `ScanResult` with status `null`, once with product `null` and once with the milk Product object. | Constructor throws `IllegalArgumentException` for both inputs. |
