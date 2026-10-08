# Kassasystem

Denna uppgift är lämplig för grupper på tre till fem personer, den är betydligt renare än den föregå-
ende, men kan upplevas som lite tråkigare eftersom den är mer styrd av hur denna typ av system
fungerar i verkligheten.
Uppgiften går ut på att implementera klasser som skulle kunna användas i ett kassasystem med va-
ror, kvitton, rabatter, kunder, etc.
Samtliga testdesigntekniker går att applicera i denna typ av projekt. Vilka som är lämpligast beror på
exakt vad man gör.
En profiler kan vara intressant om man vill studera hur någon del av systemet klarar stora mängder
data, eller vid generering av rapporter som ställer samman större datamängder.
Till skillnad från rougelike-förslaget kan det här eventuellt vara intressant att utveckla en liten del av
ett användarinterface, eller åtminstone något som kopplar ihop de olika delarna, dock absolut inte
hela kassasystemet.

## Arbetsfördelning för fyra personer

Fördelningen nedan täcker alla funktioner i förslagslistan. Den anger vem som
ansvarar för respektive område, inte att gruppen måste implementera allt.
Arbetsmängden beror på hur långt varje funktion utvecklas, så börja med den
föreslagna första delen och välj sedan utökningar tillsammans.

Utveckla först en enkel version av **Pengar** och **Varor** tillsammans, som
uppgiften föreslår. Bestäm hur belopp, avrundning, antal och varuidentitet ska
representeras. Därefter ansvarar person 3 för Pengar och person 1 för Varor.

### Simon – Varor och lager

**Ansvar:** Varor, Varugrupper, Leverantörer, Momskategorier, Pant,
EAN/GTIN, Våg och Lagersaldo.

**Varför kombinationen passar:** Alla dessa funktioner beskriver en vara,
hur den identifieras, hur dess prisunderlag bestäms och hur mycket som finns
i lager. Varugrupper och leverantörer delar behovet av att hantera samlingar
av varor. Viktvaror, moms och pant kräver tydliga regler som resten av systemet
kan använda utan att själva känna till alla varutyper.

**Börja med:** Varor, Varugrupper, Momskategorier och Pant. Lägg därefter till
Lagersaldo och välj mellan EAN/GTIN, Våg och Leverantörer som utökningar.

**Samarbete och tester:** Person 2 använder varugrupper för rabatter och
person 4 använder varorna i kvitton och scanning. Person 3 behöver uppdatera
lagret vid återköp. Testa exempelvis viktbaserade priser, gruppmedlemskap,
lagergränser och att pant hålls separat från moms och rabatter enligt uppgiften.

### Mohamad – Rabatter, kunder och medlemsförmåner

**Ansvar:** Enkla rabatter, Mer komplicerade rabatter, Bara en rabatt,
Kunder, Medlemskap, Återbäring, Poäng och Riktad reklam.

**Varför kombinationen passar:** Kundens medlemskap och köphistorik påverkar
vilka erbjudanden och förmåner kunden får. Rabattregler och valet av bästa
rabatt hör ihop eftersom de måste bedömas tillsammans. Återbäring, poäng och
riktad reklam använder samma köphistorik, men bör ha egna klasser med egna
ansvar. Rabattberäkningen ska ligga här, inte i kvittoklassen.

**Börja med:** Kunder, Medlemskap, Enkla rabatter och Bara en rabatt med
ett begränsat antal rabattregler. Välj sedan en mer komplicerad rabatt och
antingen Poäng eller Återbäring innan fler förmåner läggs till.

**Samarbete och tester:** Person 1 tillhandahåller varor och varugrupper,
person 4 behöver rabattresultatet för kvittot och person 3 behöver regler
för att korrigera förmåner vid återköp. Testa giltighetstider, beloppsgränser,
konkurrerande rabatter och att samma köp inte ger återbäring flera gånger.

### Rasmus – Betalningar och återköp

**Ansvar:** Pengar, Valutor, Växel, Bonuscheckar, Presentkort, Uppdelat köp
och Återköp.

**Varför kombinationen passar:** Funktionerna hanterar hur ett köp betalas
och hur pengar eller betalningsvärde återförs. Bonuscheckar och presentkort
är betalningsmedel och behöver därför fungera ihop med uppdelade betalningar
och växelregler. Återköp hör hit eftersom återbetalningen måste ta hänsyn
till det ursprungliga köpet och dess betalningar.

**Börja med:** Pengar, Växel och Uppdelat köp med två betalningsmedel.
Lägg därefter till Presentkort eller Bonuscheckar. Avgränsa först Återköp
till ett enkelt köp innan rabatter och medlemsförmåner kopplas in.

**Samarbete och tester:** Person 4 skickar det slutliga köpbeloppet till
betalningen. Person 2 ansvarar för rabatt- och förmånsregler vid återköp,
och person 1 för lagerjusteringen. Testa otillräcklig betalning, saknade
växelvalörer, delbetalning, förbrukade presentkort och att en bonuscheck
inte ger kontant växel. Bestäm gemensamt vilka återbetalningsregler som gäller.

### Lukas – Kassaflöde, kvitton och uppföljning

**Ansvar:** Kvitton, Kassan, Scanning, Åldersgränser, Personal,
Parkera köp, Loggning och Rapporter.

**Varför kombinationen passar:** Kassan samordnar köpet från registrering
av varor till färdigt kvitto. Scanning, ålderskontroll och parkerade köp
är delar av detta flöde. Personal kopplas till köp och kassahändelser,
medan loggning och rapporter gör samma händelser möjliga att följa upp.
Kassan ska anropa de andra personernas funktioner, och kvittot ska redovisa
resultatet utan att ta över deras beräkningsregler.

**Börja med:** Ett litet kassaflöde och Kvitton som använder de andra
områdena. Lägg sedan till Scanning med ett interface och en test double,
samt Åldersgränser. Välj därefter exempelvis Parkera köp eller en enkel
försäljningsrapport; ett komplett användargränssnitt behövs inte.

**Samarbete och tester:** Detta område kopplar ihop alla fyra personers kod.
Alla hjälper därför till med integrationen. Testa misslyckad scanning,
åldersgränsens gränsvärden, upprepade tillägg och borttagningar av samma
vara samt kvittoformat och datum med en kontrollerbar klocka. Om Personal
väljs bör det ingå beteende, till exempel försäljningsstatistik per person.

### Gemensamma överenskommelser

- Bestäm tidigt vilka uppgifter som skickas mellan områdena: varor och
  kvantiteter, rabattresultat, slutbelopp, betalningsresultat och slutförda köp.
- Bestäm beräkningsordningen för rabatter, moms, pant och medlemsförmåner.
  Spara tillräcklig information om köpet för att kunna hantera återköp korrekt.
- Varje person skriver tester för sitt område. Gör också ett gemensamt test
  som går från registrerade varor via rabatt och betalning till färdigt kvitto.
- Balansera arbetet efter komplexitet, inte antal funktioner. Person 2 kan få
  mycket arbete med konkurrerande rabatter, person 3 med återköp och person 4
  med integration. Begränsa dessa delar innan fler utökningar väljs.

## Ursprungliga funktionsförslag

Nedanstående lista innehåller förslag på funktioner som kan implementeras. Några av dessa, som
pengar och varor är absolut nödvändiga, och är lämpliga att utveckla tillsammans för att komma
igång i projektet. Därefter kan varje person välja ett antal funktioner och gruppen jobba mer distribu-
nerat. Det går inte att säga exakt hur många funktioner man ska ta per person eftersom detta beror på
deras komplexitet. Tänkt dock på att varje person måste ha tillräckligt mycket intressant funktion-
alitet, och också möjlighet att interagera med andra deltagares kod. Det är också viktigt att arbeta
objektorienterat. Rabatter till exempel ska inte räknas ut av kvittoklassen, även om den säkert ansva-
rar för att samla ihop dem.

| | |
| --- | --- |
| **Pengar**<br><br>I princip det som gjordes när vi gick igenom<br>TDD. En klass som representerar pengar. | **Valutor**<br><br>En möjlig utökning av ovanstående så att sy-<br>stemet stödjer flera valutor. Kan ge intres-<br>santa effekter om man tänker på växelkurser,<br>och vad som ska hända om kunden inte beta-<br>lar med ett exakt belopp. I vilken valuta ska<br>kunden då få växel? |
| **Varor**<br><br>Som ett minimum ett namn och ett pris. An-<br>tagligen också förmågan att presentera sig<br>själv på något lämpligt sätt.<br>7<br>https://docs.oracle.com/en/java/javase/16/docs/a<br>pi/java.base/java/lang/Iterable.html | **Varugrupper**<br><br>Samlingar av varor med något gemensamt,<br>ofta använt i samband med rabatter, eller för<br>att generera statistik. Man bör kunna lägga till<br>och ta bort varor ur gruppen, samt iterera<br>över den7<br>. |
| **Momskategorier**<br><br>Olika varutyper har olika momssats som behö-<br>ver räknas ut och redovisas på kvitton och<br>eventuella relevanta rapporter. | **Pant**<br><br>Vissa varor, till exempel många dryckesför-<br>packningar, har en separat pant som läggs på<br>utöver priset, och som man (rimligen) inte be-<br>talar moms eller får rabatt på.<br>1 |
| **Åldersgränser**<br><br>Vissa varor, som alkohol och tobak, får bara<br>köpas av personer över en viss ålder. Detta är<br>bara intressant om man implementerar själva<br>kassan, annars blir det bara ett fält i varan el-<br>ler varukategorin. | **Leverantörer**<br><br>Egentligen samma sak som varugrupper, men<br>kopplade till en viss leverantör. |
| **Kvitton**<br><br>En samling med varor som automatiskt kan<br>räkna ut pris. Funktioner man kan tänka sig är<br>lägga till och ta bort varor, gruppera varor,<br>sortera dem i bokstavsordning, samt naturligt-<br>vis skriva ut kvittot. Tänk på att samma vara<br>kan läggas till och tas bort vid olika tillfällen.<br>Java har ett ganska enkelt interface för att<br>skriva ut på en skrivare8 som kan implemente-<br>ras, och med fördel kan testas med hjälp av,<br>till exempel, mock-objekt. Kvitton har normalt<br>också datum och tid som kan testas med hjälp<br>av test doubles.<br>Oavsett om man genererar kvittot i textform,<br>som en bild, eller skriver ut det på riktigt, så<br>måste man tänka på vad som händer om nam-<br>net på varan är för långt, och på att talen bör<br>komma i prydliga kolumner. Formaterings-<br>strängar och fonter med fast bredd är använd-<br>bara här. Fonter kan också ställa till problem<br>eftersom dessa kan skilja sig åt mellan datorer<br>och operativsystem.<br>Många av de andra funktionerna kan påverka<br>kvittot, till exempel personen som sitter i kas-<br>san, kundens eventuella medlemskap, moms-<br>satser, etc. | **Kassan**<br><br>En driver för många av de andra funktionerna.<br>Exakt vad denna ska innehålla beror på vad<br>man väljer att implementera, och det finns<br>8 https://docs.oracle.com/javase/tutorial/2d/prin-<br>ting/index.html<br>inga krav på att man överhuvudtaget gör<br>denna. |
| **Växel**<br><br>Om kunden betalar kontakt ska systemet<br>lämna växel med minsta antal sedlar och<br>mynt. Idag hanteras detta ofta av automtiska<br>system, vad händer med ett sådant om det<br>inte finns tillräckligt mycket av en valör? I vissa<br>fall kan man ge fler av en lägre valör, i andra<br>inte. | **EAN/GTIN9**<br><br>En naturlig utökning av varuklassen skulle vara<br>en klass som representerar streckkodens GTIN<br>(Global Trade Item Number). Validering av<br>kontrollsiffran är en funktion här.<br>Generering av den associerade streckkoden<br>skulle kunna vara en intressant utökning, och<br>kan göras antingen till en bild eller till en skri-<br>vare. |
| **Scanning**<br><br>Scanning av varorna är det vanliga sättet att<br>registrera köp. Ni har inte tillgång till en scan-<br>ner, så att försöka lägga till en sådan funktion-<br>alitet skulle kunna vara ett intressant exempel<br>på hur man testar interaktion med något som<br>ännu inte är implementerat med hjälp av In-<br>terface och test doubles.<br>Tänk på att scanning inte alltid lyckas. | **Våg**<br><br>En variant av ovanstående är att kunna väga<br>varor. Detta skulle också få in en ny typ av va-<br>ror där priset är per viktenhet istället för per<br>styck.<br>Tänk på att olika varor använder olika vikten-<br>heter. Saffran mäts i gram, godis i hekto, frukt<br>och grönsaker i kilo och grus i ton.<br>9 https://sv.wikipedia.org/wiki/European_Ar-<br>ticle_Number<br>2 |
| **Enkla rabatter**<br><br>Enkla rabatter är sådana som ger ett visst be-<br>lopp eller en viss procentsats i rabatt. De kan<br>vara kopplade till hela köpet, en viss vara eller<br>en varugrupp. Nästan alltid har de en giltig-<br>hetstid som kan testas med hjälp av depen-<br>dency injection.<br>Ett designmönster som kan vara intressant här<br>är Decorator. | **Mer komplicerade rabatter**<br><br>Mer komplicerade rabatter är till exempel tag<br>3 betala för 2, rabatter som bara gäller max 2<br>köp per kund, rabatter som kräver att kunden<br>handlat över ett visst belopp med eller utan<br>den rabatterade varan, eller att det är en spe-<br>ciell tid på dagen och kanske bara gäller vissa<br>kunder så som pensionärsrabatter dagtid vissa<br>dagar, eller happy hour. |
| **Bara en rabatt**<br><br>Oavsett vilka rabatter man har brukar det i de<br>flesta fall vara så att max en rabatt kan tilläm-<br>pas på en viss vara, och då är det viktigt att<br>det är den som ger högst rabatt som används.<br>I vissa fall kan detta bli ganska komplicerat,<br>speciellt om det finns vissa varor som ingår i<br>flera rabatterbjudanden, eller om det finns ra-<br>batter som gäller hela köpet samtidigt som<br>det finns rabatter på vissa varor. I det senare<br>fallet kan det i vissa fall vara bättre att räkna<br>bort den rabatterade varan om rabatten på<br>hela köpet är högre. | **Lagersaldo**<br><br>En naturlig utökning av ett kassasystem är att<br>också hålla reda på lagersaldot för varorna i<br>butiken, och automatiskt uppdatera detta när<br>någonting köps. |
| **Kunder**<br><br>Namn, adress, personnummer, telefonnum-<br>mer, e-postadress. Ju mer information vi har<br>om kunderna desto bättre. Självklart vill vi<br>också hålla reda på vad varje kund handlar så<br>att vi kan generera statistik och riktad reklam.<br>Ett tips är dock att inte försöka få in all inform-<br>ation ni kan komma på i klassen. Det är lätt att<br>det blir för stort fokus på kvantitet istället för<br>kvalitet. Välj ut delar som blir intressanta ur<br>testsynvinkel, eller som är nödvändiga för<br>andra funktioner. | **Medlemskap**<br><br>Många affärskedjor erbjuder medlemskap till<br>sina kunder. Dessa ger ofta någon typ av för-<br>del för kunden, samtidigt som de tillåter affä-<br>ren att samla in betydligt mer information om<br>kunden och dennes köp.<br>Medlemskap kan också vara tidsbegränsade,<br>kosta pengar, ha en minimiålder, etc. |
| **Återbäring**<br><br>En fördel som kan finnas med ett medlemskap<br>är direkt återbäring, där medlemmen får till-<br>baka en del av köpesumman. Detta kan ske<br>när kunden handlat för tillräckligt mycket<br>inom en viss tid, eller med viss periodicitet.<br>Självklart är det viktigt att köp man redan fått<br>återbäring på inte räknas med nästa gång<br>återbäringen ska betalas ut.<br>Ibland finns det också olika nivåer på återbä-<br>ring beroende på hur mycket man har handlat<br>för eller vilken typ av medlemskap man har.<br>Vissa kategorier av varor kan vara uteslutna ur<br>återbäringsprogram, till exempel lotter och<br>presentkort. | **Poäng**<br><br>En variant på återbäring är att man istället<br>samlar poäng som sen kan bytas in mot varor<br>eller presentkort.<br>En typ av erbjudande som ibland förekommer<br>är att medlemmar får dubbla poäng under en<br>viss period, eller på vissa varor eller katego-<br>rier. |
| **Bonuscheckar**<br><br>En sak som ofta kan köpas för poäng är bonus-<br>checkar som sen kan användas för att helt el-<br>ler delvis betala ett köp. Dessa räknas normalt<br>inte som rabatter, så man kan betala ett köp<br>med flera bonuscheckar, och eventuella<br>3<br>rabatter ska appliceras. En möjlig begränsning<br>är att man inte får ut något av värdet för en<br>bonuscheck i kontanter, så att de alltså inte<br>ger någon växel om man betalar för mycket. | **Presentkort**<br><br>En annan form av alternativt betalningsmedel<br>är presentkort. Dessa är varor, men räknas<br>aldrig med i rabatter eller bonussystem, och<br>måste ibland aktiveras för att vara giltiga. De<br>har normalt en tidsgräns för hur länge de får<br>användas. |
| **Riktad reklam**<br><br>En viktig anledning till medlemskap är möjlig-<br>heten att generera riktad reklam som är an-<br>passad efter vilka varor eller varukategorier<br>som kunden brukar köpa, ålder, kön, adress,<br>etc. | **Personal**<br><br>Kassasystem har normalt information om vem<br>som satt i kassan vid en viss tidpunkt. Detta är<br>absolut relevant, men behöver kompletteras<br>med funktionalitet för att bli intressant i detta<br>sammanhang. Annars blir det bara en klass till<br>och ett fält i kvitto-klassen.<br>En möjlighet skulle vara att hålla reda på hur<br>många varor varje medlem av kassapersona-<br>len kan hantera per tidsenhet. En ofta kritise-<br>rad funktion i denna typ av system.<br>En annan skulle vara att införa någon typ av<br>bonussystem som är beroende av hur mycket<br>personen har sålt under månaden eller året. |
| **Återköp**<br><br>Ibland är inte kunder nöjda med ett köp utan<br>vill göra ett återköp av hela eller delar av det.<br>Detta påverkar många av de tidigare uppräk-<br>nade funktionerna, till exempel lagerhållning,<br>återbäring och poäng. Om varan ingått i en<br>grupp av varor som kunden fått rabatt för<br>måste detta också tas hänsyn till, samma sak o<br>man har återbäring eller poängsystem. | **Uppdelat köp**<br><br>Det finns många sätt att betala på: kontanter,<br>kort, Swish, poäng, bonuscheckar, present-<br>kort, etc. Speciellt med de sistnämnda är det<br>vanligt att dessa bara täcker delar av ett köp,<br>och att kunden vill använda andra betalnings-<br>medel för resten. |
| **Parkera köp**<br><br>Ett köp kan parkeras om kunden glömt något<br>och vill gå tillbaka in i butiken eller hem och<br>hämta plånboken. Detta är bara relevant om<br>man faktiskt bygger själva kassan. | **Loggning**<br><br>Väldigt mycket av vad som sker i ett system av<br>denna typ loggas, antingen för att det finns<br>lagkrav på det, eller för att kunna få ut inform-<br>ation om hur systemet används. Detta kan gö-<br>ras i databaser eller filer, som kan testas med<br>hjälp av test doubles. |
| **Rapporter**<br><br>All den information som finns i systemet ger<br>möjlighet att generera rapporter till olika delar<br>av verksamheten. Dessa kan till exempel vara i<br>html eller skrivas ut på skrivare. Alternativt<br>kan man testa att generera diagram i html el-<br>ler bildformat. |  |
