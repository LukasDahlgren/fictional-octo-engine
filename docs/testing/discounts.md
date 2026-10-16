# Beslutstabell för rabatter

Systemet har följande rabattregler:

- En kund med ett aktivt medlemskap får 10 % medlemsrabatt.
- En vara kan omfattas av en tidsbegränsad erbjudanderabatt.
- Högst en rabatt får användas på samma vara.
- Om både medlemsrabatten och erbjudanderabatten gäller används den rabatt
  som ger kunden den största besparingen.
- En kund utan giltig rabatt betalar ordinarie pris.

T = sant  
F = falskt
- = villkoret saknar betydelse för regeln

| Villkor/handling | Regel 1 | Regel 2 | Regel 3 | Regel 4 | Regel 5 |
|---|---:|---:|---:|---:|---:|
| Har kunden ett aktivt medlemskap? | F | T | F | T | T |
| Finns en aktiv erbjudanderabatt? | F | F | T | T | T |
| Är erbjudanderabatten större än 10 %? | - | - | - | F | T |
| Ingen rabatt används | T | F | F | F | F |
| Medlemsrabatt på 10 % används | F | T | F | T | F |
| Erbjudanderabatten används | F | F | T | F | T |

## Testfall som ska tas fram

1. Kund utan aktivt medlemskap och utan erbjudande får ingen rabatt.
2. Kund med aktivt medlemskap men utan erbjudande får 10 % rabatt.
3. Kund utan aktivt medlemskap får en aktiv erbjudanderabatt.
4. Kund med aktivt medlemskap får medlemsrabatten när erbjudandet är högst 10 %.
5. Kund med aktivt medlemskap får erbjudanderabatten när den är större än 10 %.

Utöver beslutstabellen ska gränserna för erbjudandets giltighetsperiod testas:

- erbjudandet är aktivt på startdatumet,
- erbjudandet är aktivt på slutdatumet,
- erbjudandet är inaktivt före startdatumet,
- erbjudandet är inaktivt efter slutdatumet.