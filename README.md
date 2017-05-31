# BI-PSI-2015
## Zadání
### Situace

Na planetě Mars bylo vypuštěno několik průzkumných robotů. Roboti posílají informace ze senzorů a fotografie okolí na stacionární družici na oběžné dráze. Nastřádané informace jsou později družicí odeslány k Zemi.

Bohužel však nejsou roboti na povrchu planety sami. Na komunikační server na družici se neustále kdosi pokouší připojovat, prolámat ochrany a server kompromitovat. Nevíme, zda se jedná o konkurenci nebo o cizí formu života, v každém případě je třeba server chránit a striktně dodržovat komunikační protokol.

Implementujte server běžící na družici, který bude od robotů přijímat a ukládat informace.


### Obecné schema komunikace

 Komunikační protokol je postaven nad transportním protokolem TCP. Server naslouchá na portu, který je zvolen v intervalu 3000 až 3999 (včetně). Protokol je textově orientovaný a lze jej vyzkoušet např. pomocí příkazu telnet adresa_serveru 3999 (v případě portu 3999).

Server očekává, že se k němu budou připojovat jednotliví klienti (roboti). Server si po navázání spojení klientem vyžádá autorizaci a autentizaci robota pomocí hesla. Poté může server přijmout informace ze senzorů a načíst fotografie okolí (pokud ji robot pošle). Po odpojení klienta nebo při porušení pravidel protokolu (detekováno na straně serveru) je spojení ukončeno. Na server se může připojovat více robotů najednou a je třeba je obsloužit tak, aby komunikace s jedním robotem neblokovala komunikaci s jiným robotem. 

```
       Klient                                             Server

                    ---------- connect ----------->
                                                    server si vyžádá zadání
                                                    uživatelského jména
                    <--------- zpráva 200 ---------
robot pošle svoje
uživatelské jméno
                    ---------- zpráva U ---------->
                                                    server si zapamatuje
                                                    zaslané uživatel. jméno
                                                    (v této fázi neprovádí
                                                    žádnou kontrolu
                                                    povolených znaků a délky
                                                    uživatelského jména)
                    <--------- zpráva 201 ---------
robot pošle svoje
heslo
                    ---------- zpráva P ---------->
                                                    server si zkontroluje
                                                    poslané uživatelské
                                                    jméno a heslo.

                                                    Pokud kontrola selhala,
                                                    bude odeslána zpráva
                                                    500 a spojení bude ze
                                                    strany serveru ukončeno.
                    <--------- zpráva 500 ---------
                                                    close()

                                 NEBO

                                                    Pokud prošla kontrola
                                                    bez chyby, bude odeslána
                                                    zpráva 202.
                    <--------- zpráva 202 ---------
Robot může nyní
poslat informace
ze senzorů a odeslat
fotografie okolí.
Lze podeslat libovolný
počet zpráv I a F.
                    ---------- zpráva I ---------->
                                                    Pokud nesedí syntaxe
                                                    zprávy I, bude odeslána
                                                    zpráva 501 a spojení
                                                    bude ukončeno.

                                                    Zpráva je uložena do
                                                    logu, zpátky bude
                                                    odeslána zpráva 202.
                    <--------- zpráva 202 ---------
další zpráva
                    ---------- zpráva I ---------->
                                                    Pokud nesedí syntaxe
                                                    zprávy I, bude odeslána
                                                    zpráva 501 a spojení
                                                    bude ukončeno.

                                                    Zpráva je uložena do
                                                    logu, zpátky bude
                                                    odeslána zpráva 202.
                    <--------- zpráva 202 ---------
a také fotografie
                    ---------- zpráva F ---------->
                                                    Pokud nesedí syntaxe
                                                    zprávy F, bude odeslána
                                                    zpráva 501 a spojení
                                                    bude ukončeno.

                                                    Zpráva F obsahuje
                                                    kontrolní součet,
                                                    ten je třeba
                                                    zkontrolovat. Pokud
                                                    součet nesedí, bude
                                                    odeslána zpráva 300,
                                                    spojení nebude
                                                    ukončeno, zpráva však
                                                    nebude uložena na disk.

                                                    Jinak bude fotografie
                                                    uložena na disk a
                                                    server odešle zprávu
                                                    202.
                    <--------- zpráva 202 ---------
Robot pokračuje
v odesílání zpráv
I a F, nakonec
spojení uzavře.

close()
                                                    Server reaguje
                                                    uzavřením spojení

                                                    close()
```

 Spojení musí být ukončeno do 45 sekund, jinak bude ze strany serveru násilně ukončeno (zpráva 502).

Pozor! Sondy jsou poměrně hloupé a nedodržují zcela přesně protokol. Například přihlašovací údaje vysílají bez vyzvání, nebo nečekají na potvrzení předchozí zprávy a rovnou posílají další… 
