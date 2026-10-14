# EAN-13 validation – equivalence classes

## Equivalence classes

EC-01: input contains exactly 13 ASCII digits and has a correct check digit from 1 to 9: valid<br>
EC-02: input contains exactly 13 ASCII digits and has a correct check digit of 0: valid<br>
EC-03: input is null: invalid<br>
EC-04: input is empty: invalid<br>
EC-05: input contains fewer than 13 digits: invalid<br>
EC-06: input contains more than 13 digits: invalid<br>
EC-07: input contains a letter: invalid<br>
EC-08: input contains punctuation: invalid<br>
EC-09: input contains whitespace: invalid<br>
EC-10: input contains Unicode numerals instead of ASCII digits: invalid<br>
EC-11: input contains 13 ASCII digits but an incorrect check digit: invalid<br>

These classes cover EAN-13 syntax and check-digit validation only. A valid check digit does not establish that a number was assigned by GS1 or belongs to a particular product.

## Test cases

| Test ID | Covered class | Test input | Expected result |
|---|---|---|---|
| EAN-01 | EC-01, EC-02 | `4006381333931`; `0000000000000` | Accept both as valid EAN-13 codes: one has a nonzero check digit and one has a zero check digit. The all-zero code's acceptance does not imply retail assignment. |
| EAN-02 | EC-03 | `null` | Rejected as invalid input. |
| EAN-03 | EC-04 | `""` | Rejected as invalid input. |
| EAN-04 | EC-05 | `400638133393` | Rejected because it has fewer than 13 digits. |
| EAN-05 | EC-06 | `40063813339310` | Rejected because it has more than 13 digits. |
| EAN-06 | EC-07 | `40063813339A1` | Rejected because it contains a letter. |
| EAN-07 | EC-08 | `40063813339-1` | Rejected because it contains punctuation. |
| EAN-08 | EC-09 | `4006381333 931` | Rejected because it contains whitespace. |
| EAN-09 | EC-10 | `４００６３８１３３３９３１` | Rejected because the characters are full-width Unicode numerals, not ASCII digits. |
| EAN-10 | EC-11 | `4006381333932` | Rejected because the check digit is incorrect. |
