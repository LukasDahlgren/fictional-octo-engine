# Scanning – equivalence classes

`BarcodeScanner` reads a code, looks up a product and returns a `ScanResult`.
It has no basket dependency. `CashRegister` is responsible for adding a found
product to the basket. Basket updates, repeated additions and quantity overflow
belong in the basket or cash-register test design.

These cases describe the intended behavior. `scan()` is currently a placeholder.
Tests for the result object itself are described in [scan-result.md](scan-result.md).

## Equivalence classes

EC-01: code matches a product: valid<br>
EC-02: code does not match a product: invalid<br>
EC-03: no code read (null): invalid<br>
EC-04: code is empty: invalid<br>
EC-05: code contains only whitespace: invalid<br>
EC-06: code reader is provided: valid<br>
EC-07: code reader is missing (null): invalid<br>
EC-08: product lookup is provided: valid<br>
EC-09: product lookup is missing (null): invalid<br>

## Dependency failures

DF-01: code reader fails and throws an exception. No code is returned. This is different from EC-03, where the reader works but cannot read a code.<br>
DF-02: code reader returns a code, but product lookup fails and throws an exception. This is different from EC-02, where lookup works but finds no product.<br>

## Mock pre-state

```text
Product lookup:
  "000123" -> the milk Product object, unit price 12.50 SEK
  "unknown" -> null
  Records the codes received and the number of calls.

Code reader:
  Returns the input specified in the test case.
  Records the number of calls.

BarcodeScanner:
  Uses this code reader and product lookup.
  No scan performed yet.
```

Each test starts with fresh objects. SC-06 and SC-07 replace the specified
dependency with `null` before constructing `BarcodeScanner`.
For every call to `scan()`, the code reader is called exactly once.

## BarcodeScanner test cases

| Test ID | Covered class | Test input / setup | Expected result |
|---|---|---|---|
| SC-01 | EC-01, EC-06, EC-08 | Code reader returns `"000123"`, which maps to milk. | Return `ScanResult` with status `FOUND` and the exact milk Product object from lookup. Lookup is called once with `"000123"`, preserving leading zeros. |
| SC-02 | EC-02 | Code reader returns `"unknown"`. Lookup returns `null`. | Return status `UNKNOWN_PRODUCT` and product `null`. Lookup is called once with `"unknown"`. |
| SC-03 | EC-03 | Code reader returns `null`. | Return status `FAILED_READ` and product `null`. Product lookup is not called. |
| SC-04 | EC-04 | Code reader returns `""`. | Return status `INVALID_CODE` and product `null`. Product lookup is not called. |
| SC-05 | EC-05 | Code reader returns `" "`, `"\t"`, or `"\n"`. | Return status `INVALID_CODE` and product `null` for each input. Product lookup is not called. |
| SC-06 | EC-07 | Code reader is `null`. Lookup is valid. | `BarcodeScanner` constructor throws `IllegalArgumentException`. |
| SC-07 | EC-09 | Lookup is `null`. Code reader is valid. | `BarcodeScanner` constructor throws `IllegalArgumentException`. |
| SC-08 | DF-01 | Code reader double throws `IllegalStateException`. | The same exception propagates to the caller. Product lookup is not called. No `ScanResult` is returned. |
| SC-09 | DF-02 | Code reader returns `"000123"`. Lookup double throws `IllegalStateException`. | Lookup is called once with `"000123"`. The same exception propagates to the caller. No `ScanResult` is returned. |
