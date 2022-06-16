
# PCPFiller

PCPFiller (PC Part Picker Filler) è una utility CLI destinata al preprocessing di dataset.

Trovare datasets di componenti hardware per PC per un qualsiasi progetto di ML/DL, che siano decenti e reperibili gratuitamente non è per niente facile; la maggior parte dei datasets disponibili online sono incompleti, molto obsoleti, troppo piccoli per il training di una AI e spesso anche incorretti.

PCPFiller is offre di costruire e fornire datasets completi e di dimensioni sufficienti per allenare una AI.

## Quick start

Per una list di comandi e opzioni, consultare la help page con il seguente comando

```bash
PCPFiller.py --help
```

## Come funziona

### Fase di download list component

Per prima cosa, PCPFiller scaricherà una lista di componenti hardware dal noto sito PCPartPicker.com.
In tale lista sono inclusi componenti principali, quali CPU, GPU, RAM, mouse, monitor, HDD, e altre periferiche secondarie.

Per scaricare i componenti, eseguire il seguente comando

```bash
PCPFiller.py --fetch <tipo componente>
```

Per sapere la lista dei componenti attualmente supportati, eseguire il seguente comando

```bash
PCPFiller.py --supported-parts
```

Il download dei componenti è possibile grazie alle ottime API per python fornite da PCPartPicker.com stesso (<https://pypi.org/project/pcpartpicker>), ottenibili con `pip`.

**NOTA**: quando si scaricano i componenti, assicurarsi di settare la `region` correttamente.

```bash
PCPFiller.py --fetch <tipo componente> --region <regione>
```

Per sapere la lista delle regioni attualmente supportate, eseguire il seguente comando

```bash
PCPFiller.py --supported-regions
```

### Fase di parsing e conversione

PCPFiller si occpuerà poi di fare il parsing di tali componenti, manipolando alcuni dati per facilitare l'elaborazione, rimuovendo i componenti con dati mancanti, per poi convertirli in formato CSV, in modo da renderli utilizzabili da altri strumenti come Weka per eventuale analisi dati, e con lo scopo di poter procedere al "filling" di eventuali dati mancanti.

Per convertire i componenti (precedentemente scaricati), eseguire il seguente comando

```bash
PCPFiller.py --to-csv <tipo componente>
```

PCPFiller fornirà delle statistiche sulla qualità del datasat base appena convertito.

**NOTA**: le directory di input/output di PCPFiller sono di default `./parts` e `./parts/formatted`. Tali percorsi possono essere cambiati; consultare la help page.

### Fase di filling

blah blah blah

## Descrizione dell'agente intelligente

L'obiettivo dell'agente è quello di valutare la relazione tra le varie statistiche di un componente, per poi essere in grado di completare eventuali dati mancanti (continui e nominali).

### Specifica PEAS

| PEAS        |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
|-------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Performance | La performance dell'agente è dettata dalla precisione e accuratezza dei dati mancanti predetti                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| Enviroment  | - Completamente osservabile, dato che ha accesso a tutte le informazioni di un componente </br> - Deterministico, in quanto i dati predetti dipendono solamente dallo stato iniziale dei componenti e dalle modifiche dell'agente </br> - Sequenziale, in quanto i dati in output possono variare in baso alle iterazioni precedenti </br> - Statico, i dati non variano mentre l'agente sta operando </br> - Singolo agente, in quanto PCPFiller è l'unico a manipolare i dati </br> - Continuo, la decisione del dato predetto si evolve in modo continuo |
| Actuators   | Gli attuatori consistono nei dati predetti inseriti nel dataset                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| Sensors     | I sensori dell'agente consistono nei dati già presenti di un dato componente nel dataset                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |

PCPFiller, con qualche modifica, potrebbe essere per poter lavorare in un ambiente dinamico e multi agente, in modo da poter predirre i risultati in modo dinamico dato che il mondo economico è sempre in evoluzione.
Si potrebbe tener conto del trend di popolarità del manufacturer del componente hardware, eventuali festività che potrebbero portare a cambiamenti (ad esempio al prezzo) e tante altre variabili dinamiche.

Però come menzionato prima, lo scopo di PCPFiller è solo quello di completare e fornire datasets per altri eventuali progetti di ML/DL.

## Raccolta, analisi e preprocessing dei dati

### Scelta di dataset di partenza

Per poter fornire un datasets utilizzabile, PCPFiller necessità di alcuni dati di partenza per il training.

L'idea della creazione di un dataset da zero è stata scartata immediatamente per evitare di introdurre un ulteriore possibilità di errore.
Quindi, inizialmente si aveva pensato di fare scraping su siti di eShopping (es: Amazon, NewEgg etc.), ma anche ciò è risultato impossibile da realizzare,
dato che un singolo sito non forniva abbastanza dati, e utilizzare piu siti significava dover rendere PCPFiller compatibile con una moltitudine di formati dati,
il che sarebbe diventato presto impossibile da mantere con un qualisasi cambiamento dei suddetti siti. Oltretutto, lo scraping potrebbe aver implicazioni legali,
dato che la maggior parte dei siti non lo permettte.

Si è quindi ricorso a PCPartPicker.com, un sito che offre un'interfaccia per creare delle PC build selezionando i vari componenti; il database di PCPartPicker contiente una buona mole di dati,
e come menzionato in precendenza, offre anche delle API per poter accedere a tali dati.

Le GPU e RAM erano i dataset di partenza con piu dati disponibili, si è quindi deciso di operare principalmente su di essi.

- Nel caso delle GPU, le entries sono ~4400, rimuovendo le entries incomplete si arriva a ~350, quindi con una notevole perdita del ~90%.
- Nel caso delle RAM, le entries sono ~7000, rimuovendo le entries incomplete si arriva a ~1800, quindi con una perdita del ~70%.

### Preprocessing dei dati

PCPFiller scarterà automaticamente tutte le entries che presentano campi vuoti / mancanti, transformando alcuni dati per facilitarne l'analisi.

Inoltre alcuni dati non rilevanti potrebbero essere rimossi; riferisi all'analisi dati per più dettagli

### Analisi dati disponibili

Segue una descrizione generale dei dati disponibili per le RAM e l'eventuale utilità per lo scopo predisposto da PCPFiller

| Campo          |  Tipo   | Descrizione                                                                                         | Utilità                                                                                                                                         |
|----------------|---------|-----------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|
| Brand          | nominal | Nome del brand della casa produttrice                                                               | Potrebbe essere molto utile soprattutto nella predizione di prezzo. Molti brand sono noti per sovrapprezzare i propri prodotti.                 |
| Model          | nominal | Nome del modello                                                                                    | Non molto utile, in quando è insolito che due prodotti condividano lo stesso nome; porterebbe solamente all'inquinamento della predizione       |
| Module Type    | nominal | Tipo del modulo (DDR2/3/4), rappresenta la "generazione" del modulo                                 | Molto utile, dato che tipo di moduli diversi fanno variare molto prezzo ed altre statistiche come frequenza e dimensione modulo                 |
| Speed (cycles) | numeric | Frequenza del module, rappresentata in hertz (convertita in MHz in fase di preprocessing)           | Decisamente utile, in quanto strettamente correlata con altri parametri                                                                         |
| Modules number | numeric | Quantità di moduli                                                                                  | Strettamente correlata all dimensione e prezzo di un singolo modulo (1x8GB, 2x4GB, 2x16GB etc.)                                                 |
| Price / GB     | numeric | Prezzo in Euro per un GB                                                                            | Dato derivato da prezzo e dimensione/quantità dei moduli. Potrebbe essere utile ma con il rischio che potrebbe portare a un caso di overfitting |
| Color          | nominal | Colore                                                                                              | Alcuni colori (es: Gold, Silver) potrebbero essere correlati a prezzo e brand.                                                                  |
| FW Latency     | numeric | First World Latency, latenza in condizioni ottimali (performance del modulo)                        | Strettamente correlato ad altri parametri                                                                                                       |
| CAS timing     | numeric | Latenza di "Column Access Strobe" (performance del modulo)                                          | Strettamente correlato ad altri parametri                                                                                                       |
| ECC            | nominal | Error Correction, capacità di correzioni errori (convertita in true/false in fase di preprocessing) | Solitamente la funzionalità di ECC è presente in moduli più pregiati. Quindi correlata ad altri parametri                                       |
| Price          | numeric | Prezzo in Euro                                                                                      | Decisamente utile                                                                                                                               |

Analizzando i dati con Weka, possiamo risalire a queste informazioni (il campo `Model` è stato rimosso)
![AllAttributes](https://i.imgur.com/IhiIzCs.png)

Dei dati piu rilevanti, possiamo dire:

- La generazione piu offerta è DDR4, seguita da DDR3 e DDR2
- La frequenza media e di circa 2500 MHz con una standard deviation di ~840
- La maggior parte dei prodotti vengono venduti in batch da 2 con una dimensione media di 10GB
- Moduli con ECC non sono molto comuni
- Il prezzo può arrivare anche a 2800 Euro, con una media di ~200 Euro e standard deviation di 220

Dato che ci interessa principalmente la previsione di campi correlati al prezzo, commentiamo la loro relazione</br>
**NOTA**: è stato applicato un po' di jitter sui dati per migliorarne la visualizzazione

- Brand</br>
  ![Brand-Price](https://imgur.com/wYf7UOE.png)</br>
  Come previsto, alcuni brand più blasonati sono soliti scegliere prezzi piu alti per i propri prodotti

- Generazione Modulo</br>
  ![-Price](https://imgur.com/bwnYbOS.png)</br>
  Nessuna sorpresa, le ultime generazioni hanno un prezzo piu elevato

Lo stesso possiamo dire per gli altri parametri di performance del modulo. Maggiori performance risultano in un prezzo piu alto.

- Frequenza</br>
  ![Speed-Price](https://imgur.com/thitmXA.png)

- Dimensione modulo</br>
  ![Size-Price](https://imgur.com/WMf46fx.png)

- FWLatency</br>
  ![FWL-Price](https://imgur.com/JRfz2Zr.png)

Di seguito una overview delle relazioni tra i vari campi</br>
  ![Overview](https://imgur.com/0CwtbBq.png)

### Processing dei dati

Come menzionato prima, con PCPFiller siamo principalmente interessati al prezzo (e alla predizione di esso), quindi si è deciso di ignorare l'attributo `Model` e anche il `Color`;
nonostante il colore potrebbe essere parzialmente utile per la previsione del prezzo, in seguito ad una analisi dei dati (e futuri test di regressione), si è deciso di rimuoverlo perchè
comportava solamente un incremento di complessità per il nostro modello, senza apportare netti benefici.

Il prezzo è un valore numerico continuo, possiamo quindi scegliere vari algoritmi specializziati per regressione con valori numerici:

- LinearRegression
- Alberi:
  - RandomTree
  - RandomForest
  - M5P

Per quanto riguarda il testing, data la ridotta dimensione del dataset, il testing con un precentage-split non produceva buoni risultati (anche con seed diversi), a meno che non si alzava la percentuale di split, con il rischio però di overfitting.

Creare un dataset specificatamente per il testing era fuori discussione.

Si è quindi optato per una k-fold cross-validation.
I test sono stati effettuati con 5, 10 e 15 partizioni (fold), data la ridotta dimensione del dataset, i risultati migliori si sono ottenuti con 10 fold.

**NOTA**: dato che stiamo cercando di creare un modello per la predizione del prezzo, il nostro obiettivo è quello di ridurre quanto più possibile lo scarto quadratico.

#### Linear regression

- Modello risultante:
  
  ```text
  price =

    -40.3402 * brand=HP,OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte +
    -65.533  * brand=OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte +
     95.5787 * brand=Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte +
     20.2446 * brand=PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte +
    -28.679  * brand=IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte +
    -13.7937 * brand=Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte +
    -47.5016 * brand=Samsung,G.Skill,Corsair,Gigabyte +
     73.7973 * brand=G.Skill,Corsair,Gigabyte +
     16.5777 * brand=Corsair,Gigabyte +
     91.7859 * module_type=DDR3,DDR4 +
    -57.8488 * module_type=DDR4 +
      0.2269 * speed +
    102.812  * number_of_modules +
     14.61   * module_size +
     12.9136 * price_per_gb +
     49.6886 * first_word_latency +
    -31.4733 * cas_timing +
     24.9922 * error_correction=False +
  -1079.3951
  ```

- Risultati del testing:

  | Coefficiente di correlazione | Errore medio assoluto | Errore quadratico medio | Errore assoluto relativo | Errore quadratico relativo |
  |------------------------------|-----------------------|-------------------------|--------------------------|----------------------------|
  | 0.8835                       | 59.5598               | 105.8831                | 43.8108%                 | 46.8338%                   |

- Visualizzazione dei risultati:</br>
  ![LinearRegressionResult](https://imgur.com/cfpjAxt.png)

L'algoritmo di linear regression è quindi risultato discreto. Come si può vedere tende a sovrastimare il prezzo. Esploriamo altri algoritmi.

<details>
<summary>Output completo del risultato migliore</summary>

```text
=== Run information ===

Scheme:       weka.classifiers.functions.LinearRegression -S 0 -R 1.0E-8 -num-decimal-places 4
Relation:     memory-weka.filters.unsupervised.attribute.Remove-R2-weka.filters.unsupervised.attribute.Remove-R7
Instances:    1791
Attributes:   10
              brand
              module_type
              speed
              number_of_modules
              module_size
              price_per_gb
              first_word_latency
              cas_timing
              error_correction
              price
Test mode:    10-fold cross-validation

=== Classifier model (full training set) ===


Linear Regression Model

price =

    -40.3402 * brand=HP,OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte +
    -65.533  * brand=OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte +
     95.5787 * brand=Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte +
     20.2446 * brand=PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte +
    -28.679  * brand=IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte +
    -13.7937 * brand=Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte +
    -47.5016 * brand=Samsung,G.Skill,Corsair,Gigabyte +
     73.7973 * brand=G.Skill,Corsair,Gigabyte +
     16.5777 * brand=Corsair,Gigabyte +
     91.7859 * module_type=DDR3,DDR4 +
    -57.8488 * module_type=DDR4 +
      0.2269 * speed +
    102.812  * number_of_modules +
     14.61   * module_size +
     12.9136 * price_per_gb +
     49.6886 * first_word_latency +
    -31.4733 * cas_timing +
     24.9922 * error_correction=False +
  -1079.3951

Time taken to build model: 0.01 seconds

=== Cross-validation ===
=== Summary ===

Correlation coefficient                  0.8835
Mean absolute error                     59.5598
Root mean squared error                105.8831
Relative absolute error                 43.8108 %
Root relative squared error             46.8338 %
Total Number of Instances             1791     
```

</details>

#### RandomTree

- Albero risultante:</br>
  ![RandomTreeResult](https://imgur.com/7Fhygy0.png)

- Risultati del testing:</br>
  | Coefficiente di correlazione | Errore medio assoluto | Errore quadratico medio | Errore assoluto relativo | Errore quadratico relativo |
  |------------------------------|-----------------------|-------------------------|--------------------------|----------------------------|
  | 0.9267                       | 31.9919               | 86.2286                 | 23.5325%                 | 38.1403%                   |

- Visualizzazione dei risultati:</br>
  ![RandomTreeResult](https://imgur.com/vaS6Wt4.png)

Come si può notare si ottengono risultati migliori, anche se l'albero risultante è molto grande, probabilmente a causa dell'attributo Brand; anche senza il Brand la performance non cambiava di molto.

<details>
<summary>Output completo del risultato migliore</summary>

```text
=== Run information ===

Scheme:       weka.classifiers.trees.RandomTree -K 0 -M 1.0 -V 0.001 -S 1
Relation:     memory-weka.filters.unsupervised.attribute.Remove-R2-weka.filters.unsupervised.attribute.Remove-R7
Instances:    1791
Attributes:   10
              brand
              module_type
              speed
              number_of_modules
              module_size
              price_per_gb
              first_word_latency
              cas_timing
              error_correction
              price
Test mode:    10-fold cross-validation

=== Classifier model (full training set) ===


RandomTree
==========

module_size < 12
|   first_word_latency < 9.57
|   |   price_per_gb < 14.2
|   |   |   number_of_modules < 3
|   |   |   |   price_per_gb < 11.15
|   |   |   |   |   brand = ADATA
|   |   |   |   |   |   first_word_latency < 9.32 : 161 (1/0)
|   |   |   |   |   |   first_word_latency >= 9.32
|   |   |   |   |   |   |   price_per_gb < 9.2 : 137.44 (2/20.66)
|   |   |   |   |   |   |   price_per_gb >= 9.2 : 152.32 (1/0)
|   |   |   |   |   brand = Corsair
|   |   |   |   |   |   cas_timing < 15.5 : 79.99 (1/0)
|   |   |   |   |   |   cas_timing >= 15.5
|   |   |   |   |   |   |   speed < 3733 : 142.5 (2/6.23)
|   |   |   |   |   |   |   speed >= 3733
|   |   |   |   |   |   |   |   first_word_latency < 9.25 : 169.99 (2/25)
|   |   |   |   |   |   |   |   first_word_latency >= 9.25 : 155.59 (2/0.36)
|   |   |   |   |   brand = Crucial
|   |   |   |   |   |   speed < 3800
|   |   |   |   |   |   |   price_per_gb < 5.66 : 76.22 (1/0)
|   |   |   |   |   |   |   price_per_gb >= 5.66
|   |   |   |   |   |   |   |   price_per_gb < 7.43 : 109.67 (2/21.81)
|   |   |   |   |   |   |   |   price_per_gb >= 7.43 : 128.7 (2/29.11)
|   |   |   |   |   |   speed >= 3800 : 157.99 (1/0)
|   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   price_per_gb < 10.15
|   |   |   |   |   |   |   module_size < 6 : 75.85 (6/44.72)
|   |   |   |   |   |   |   module_size >= 6
|   |   |   |   |   |   |   |   price_per_gb < 8.45
|   |   |   |   |   |   |   |   |   first_word_latency < 9.39
|   |   |   |   |   |   |   |   |   |   price_per_gb < 7.3 : 109.57 (4/19.68)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 7.3 : 125.34 (8/15.58)
|   |   |   |   |   |   |   |   |   first_word_latency >= 9.39 : 102.34 (2/44.29)
|   |   |   |   |   |   |   |   price_per_gb >= 8.45
|   |   |   |   |   |   |   |   |   cas_timing < 17.5 : 150.1 (8/47.97)
|   |   |   |   |   |   |   |   |   cas_timing >= 17.5 : 139.33 (1/0)
|   |   |   |   |   |   price_per_gb >= 10.15
|   |   |   |   |   |   |   module_size < 6 : 85.68 (1/0)
|   |   |   |   |   |   |   module_size >= 6 : 165.31 (6/8.47)
|   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   brand = Kingston
|   |   |   |   |   |   number_of_modules < 1.5 : 45.95 (2/1.09)
|   |   |   |   |   |   number_of_modules >= 1.5
|   |   |   |   |   |   |   price_per_gb < 6.58 : 89 (1/0)
|   |   |   |   |   |   |   price_per_gb >= 6.58
|   |   |   |   |   |   |   |   first_word_latency < 9.37 : 121.56 (1/0)
|   |   |   |   |   |   |   |   first_word_latency >= 9.37 : 151.05 (1/0)
|   |   |   |   |   brand = Klevv : 28.19 (1/0)
|   |   |   |   |   brand = Mushkin
|   |   |   |   |   |   cas_timing < 10.5 : 84.99 (1/0)
|   |   |   |   |   |   cas_timing >= 10.5
|   |   |   |   |   |   |   module_size < 6 : 88.99 (1/0)
|   |   |   |   |   |   |   module_size >= 6 : 139.99 (1/0)
|   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   brand = Patriot
|   |   |   |   |   |   speed < 2900 : 173.27 (1/0)
|   |   |   |   |   |   speed >= 2900
|   |   |   |   |   |   |   cas_timing < 18.5
|   |   |   |   |   |   |   |   cas_timing < 16.5
|   |   |   |   |   |   |   |   |   price_per_gb < 6.4 : 76.9 (1/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 6.4 : 127.79 (1/0)
|   |   |   |   |   |   |   |   cas_timing >= 16.5
|   |   |   |   |   |   |   |   |   first_word_latency < 9.38 : 91.38 (3/48.17)
|   |   |   |   |   |   |   |   |   first_word_latency >= 9.38
|   |   |   |   |   |   |   |   |   |   price_per_gb < 5.65 : 81.4 (2/0.25)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 5.65 : 98.9 (1/0)
|   |   |   |   |   |   |   cas_timing >= 18.5 : 133.88 (1/0)
|   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   brand = Team
|   |   |   |   |   |   price_per_gb < 9.57 : 137.19 (1/0)
|   |   |   |   |   |   price_per_gb >= 9.57 : 168.98 (1/0)
|   |   |   |   |   brand = Thermaltake
|   |   |   |   |   |   speed < 4200 : 146.39 (4/32.25)
|   |   |   |   |   |   speed >= 4200 : 169.89 (1/0)
|   |   |   |   |   brand = Transcend : 76.06 (1/0)
|   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   price_per_gb >= 11.15
|   |   |   |   |   brand = ADATA : 205.79 (1/0)
|   |   |   |   |   brand = Corsair : 215.65 (1/0)
|   |   |   |   |   brand = Crucial : 206.9 (1/0)
|   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   cas_timing < 17.5 : 208.78 (10/47.83)
|   |   |   |   |   |   cas_timing >= 17.5
|   |   |   |   |   |   |   speed < 4199.5
|   |   |   |   |   |   |   |   price_per_gb < 11.81 : 181.05 (2/3.22)
|   |   |   |   |   |   |   |   price_per_gb >= 11.81 : 203 (3/31.67)
|   |   |   |   |   |   |   speed >= 4199.5
|   |   |   |   |   |   |   |   first_word_latency < 8.77 : 197 (1/0)
|   |   |   |   |   |   |   |   first_word_latency >= 8.77
|   |   |   |   |   |   |   |   |   price_per_gb < 12.42 : 187.89 (1/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 12.42 : 213.9 (2/18.49)
|   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   brand = Kingston : 100.63 (1/0)
|   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   brand = Patriot : 111.95 (1/0)
|   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   brand = Team
|   |   |   |   |   |   cas_timing < 16 : 179.86 (1/0)
|   |   |   |   |   |   cas_timing >= 16 : 201.41 (1/0)
|   |   |   |   |   brand = Thermaltake : 180.72 (4/1.91)
|   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   number_of_modules >= 3
|   |   |   |   module_size < 6
|   |   |   |   |   price_per_gb < 7.69 : 238.38 (1/0)
|   |   |   |   |   price_per_gb >= 7.69
|   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   brand = Corsair : 193.57 (1/0)
|   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   price_per_gb < 8.83 : 126.87 (1/0)
|   |   |   |   |   |   |   price_per_gb >= 8.83 : 158.75 (2/9.58)
|   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   module_size >= 6
|   |   |   |   |   number_of_modules < 6
|   |   |   |   |   |   module_type = DDR2 : 0 (0/0)
|   |   |   |   |   |   module_type = DDR4
|   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   speed < 3533 : 403.56 (2/5.27)
|   |   |   |   |   |   |   |   speed >= 3533
|   |   |   |   |   |   |   |   |   speed < 3700 : 364.99 (1/0)
|   |   |   |   |   |   |   |   |   speed >= 3700
|   |   |   |   |   |   |   |   |   |   price_per_gb < 12.44
|   |   |   |   |   |   |   |   |   |   |   speed < 3900 : 352.24 (1/0)
|   |   |   |   |   |   |   |   |   |   |   speed >= 3900 : 279.99 (1/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 12.44 : 443.66 (1/0)
|   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   price_per_gb < 10.65
|   |   |   |   |   |   |   |   |   cas_timing < 16.5
|   |   |   |   |   |   |   |   |   |   price_per_gb < 8.45
|   |   |   |   |   |   |   |   |   |   |   speed < 3300 : 245.7 (1/0)
|   |   |   |   |   |   |   |   |   |   |   speed >= 3300 : 260.75 (1/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 8.45
|   |   |   |   |   |   |   |   |   |   |   first_word_latency < 9.06 : 285.91 (4/36.17)
|   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 9.06
|   |   |   |   |   |   |   |   |   |   |   |   price_per_gb < 9.87 : 298.35 (2/11.19)
|   |   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 9.87 : 329.89 (1/0)
|   |   |   |   |   |   |   |   |   cas_timing >= 16.5 : 331.03 (1/0)
|   |   |   |   |   |   |   |   price_per_gb >= 10.65
|   |   |   |   |   |   |   |   |   cas_timing < 15
|   |   |   |   |   |   |   |   |   |   price_per_gb < 12.77
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 12.2 : 377.3 (2/1.96)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 12.2 : 402.16 (1/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 12.77
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 13.36 : 414.86 (1/0)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 13.36 : 440.16 (1/0)
|   |   |   |   |   |   |   |   |   cas_timing >= 15 : 354.84 (3/17.05)
|   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   brand = Kingston : 230.77 (1/0)
|   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   module_type = DDR3
|   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   brand = Corsair : 0 (0/0)
|   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   speed < 2266.5 : 253.38 (1/0)
|   |   |   |   |   |   |   |   speed >= 2266.5 : 231 (1/0)
|   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   brand = Kingston : 383.45 (1/0)
|   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   number_of_modules >= 6
|   |   |   |   |   |   speed < 3333 : 444.1 (1/0)
|   |   |   |   |   |   speed >= 3333
|   |   |   |   |   |   |   speed < 3733 : 680.99 (1/0)
|   |   |   |   |   |   |   speed >= 3733 : 635 (1/0)
|   |   price_per_gb >= 14.2
|   |   |   number_of_modules < 6
|   |   |   |   price_per_gb < 31.45
|   |   |   |   |   number_of_modules < 3.5
|   |   |   |   |   |   module_type = DDR2 : 0 (0/0)
|   |   |   |   |   |   module_type = DDR4
|   |   |   |   |   |   |   module_size < 6 : 131.5 (2/32.43)
|   |   |   |   |   |   |   module_size >= 6
|   |   |   |   |   |   |   |   speed < 4500
|   |   |   |   |   |   |   |   |   price_per_gb < 19.6
|   |   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   |   |   |   first_word_latency < 8.13 : 289.99 (1/0)
|   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 8.13
|   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 8.76 : 234.99 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 8.76
|   |   |   |   |   |   |   |   |   |   |   |   |   price_per_gb < 17.66 : 269.99 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 17.66 : 294.99 (1/0)
|   |   |   |   |   |   |   |   |   |   brand = Crucial
|   |   |   |   |   |   |   |   |   |   |   number_of_modules < 1.5 : 140.19 (1/0)
|   |   |   |   |   |   |   |   |   |   |   number_of_modules >= 1.5 : 231.87 (1/0)
|   |   |   |   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 17.82
|   |   |   |   |   |   |   |   |   |   |   |   price_per_gb < 15.83
|   |   |   |   |   |   |   |   |   |   |   |   |   price_per_gb < 15.01 : 233.47 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 15.01 : 248.59 (2/2.54)
|   |   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 15.83 : 262.15 (3/42.08)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 17.82 : 302.82 (2/14.59)
|   |   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Kingston
|   |   |   |   |   |   |   |   |   |   |   speed < 3800 : 238.46 (1/0)
|   |   |   |   |   |   |   |   |   |   |   speed >= 3800 : 273.27 (1/0)
|   |   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 19.6
|   |   |   |   |   |   |   |   |   |   price_per_gb < 27.8
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 22.21
|   |   |   |   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Corsair : 336.39 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Crucial : 350.9 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = G.Skill : 323.14 (2/7.16)
|   |   |   |   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 22.21
|   |   |   |   |   |   |   |   |   |   |   |   price_per_gb < 23.67
|   |   |   |   |   |   |   |   |   |   |   |   |   speed < 4133 : 375.72 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 4133 : 359.99 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 23.67 : 388.73 (3/28.67)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 27.8 : 494.99 (1/0)
|   |   |   |   |   |   |   |   speed >= 4500 : 483.28 (1/0)
|   |   |   |   |   |   module_type = DDR3
|   |   |   |   |   |   |   price_per_gb < 23.07
|   |   |   |   |   |   |   |   price_per_gb < 17.57 : 131.11 (3/46.54)
|   |   |   |   |   |   |   |   price_per_gb >= 17.57
|   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Corsair : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = G.Skill : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Kingston : 89.03 (1/0)
|   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Patriot : 71.04 (1/0)
|   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   price_per_gb >= 23.07
|   |   |   |   |   |   |   |   first_word_latency < 8.88
|   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Corsair : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = G.Skill : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Klevv : 227.73 (1/0)
|   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = OCZ : 184.7 (1/0)
|   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   first_word_latency >= 8.88
|   |   |   |   |   |   |   |   |   module_size < 1.5 : 86.24 (1/0)
|   |   |   |   |   |   |   |   |   module_size >= 1.5
|   |   |   |   |   |   |   |   |   |   price_per_gb < 25.17 : 143.3 (1/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 25.17 : 158.75 (1/0)
|   |   |   |   |   number_of_modules >= 3.5
|   |   |   |   |   |   module_size < 6
|   |   |   |   |   |   |   speed < 3333 : 290 (1/0)
|   |   |   |   |   |   |   speed >= 3333 : 240.08 (1/0)
|   |   |   |   |   |   module_size >= 6
|   |   |   |   |   |   |   first_word_latency < 9.46
|   |   |   |   |   |   |   |   price_per_gb < 17.38
|   |   |   |   |   |   |   |   |   price_per_gb < 15.97
|   |   |   |   |   |   |   |   |   |   first_word_latency < 9.08
|   |   |   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Corsair : 495 (1/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = G.Skill : 480.5 (2/21.16)
|   |   |   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   first_word_latency >= 9.08 : 468.6 (1/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 15.97 : 527.34 (1/0)
|   |   |   |   |   |   |   |   price_per_gb >= 17.38
|   |   |   |   |   |   |   |   |   first_word_latency < 9.05 : 606.99 (1/0)
|   |   |   |   |   |   |   |   |   first_word_latency >= 9.05 : 586.76 (2/3.15)
|   |   |   |   |   |   |   first_word_latency >= 9.46
|   |   |   |   |   |   |   |   price_per_gb < 19.34 : 552.99 (1/0)
|   |   |   |   |   |   |   |   price_per_gb >= 19.34
|   |   |   |   |   |   |   |   |   price_per_gb < 22.12 : 684.99 (1/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 22.12 : 730.51 (1/0)
|   |   |   |   price_per_gb >= 31.45
|   |   |   |   |   price_per_gb < 51.48
|   |   |   |   |   |   price_per_gb < 32.26 : 1022.64 (1/0)
|   |   |   |   |   |   price_per_gb >= 32.26
|   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   first_word_latency < 8.35
|   |   |   |   |   |   |   |   |   price_per_gb < 39.21 : 564.29 (1/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 39.21
|   |   |   |   |   |   |   |   |   |   price_per_gb < 44.07 : 690.4 (1/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 44.07 : 724.99 (2/25)
|   |   |   |   |   |   |   |   first_word_latency >= 8.35 : 527.57 (1/0)
|   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   speed < 4700
|   |   |   |   |   |   |   |   |   price_per_gb < 34.18 : 520.92 (1/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 34.18 : 572.97 (1/0)
|   |   |   |   |   |   |   |   speed >= 4700 : 740.55 (1/0)
|   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   brand = Kingston : 287.74 (1/0)
|   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   price_per_gb >= 51.48
|   |   |   |   |   |   price_per_gb < 60.33
|   |   |   |   |   |   |   first_word_latency < 7.48 : 925.64 (1/0)
|   |   |   |   |   |   |   first_word_latency >= 7.48 : 879.99 (1/0)
|   |   |   |   |   |   price_per_gb >= 60.33 : 1004.99 (1/0)
|   |   |   number_of_modules >= 6
|   |   |   |   price_per_gb < 19.92
|   |   |   |   |   first_word_latency < 8.26 : 1012.88 (1/0)
|   |   |   |   |   first_word_latency >= 8.26 : 927.28 (2/22.18)
|   |   |   |   price_per_gb >= 19.92 : 1537.22 (1/0)
|   first_word_latency >= 9.57
|   |   price_per_gb < 9.1
|   |   |   number_of_modules < 3.5
|   |   |   |   module_size < 6
|   |   |   |   |   price_per_gb < 6.52
|   |   |   |   |   |   module_size < 3
|   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   brand = Corsair : 0 (0/0)
|   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   brand = G.Skill : 0 (0/0)
|   |   |   |   |   |   |   brand = GeIL : 21.26 (1/0)
|   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   brand = Kingston : 2 (1/0)
|   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   brand = Patriot : 12.9 (1/0)
|   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   module_size >= 3
|   |   |   |   |   |   |   brand = ADATA : 23.99 (1/0)
|   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   speed < 2533
|   |   |   |   |   |   |   |   |   speed < 1466.5
|   |   |   |   |   |   |   |   |   |   speed < 1199.5
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 5 : 19.99 (1/0)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 5 : 39.99 (1/0)
|   |   |   |   |   |   |   |   |   |   speed >= 1199.5
|   |   |   |   |   |   |   |   |   |   |   number_of_modules < 1.5 : 23.18 (5/3.32)
|   |   |   |   |   |   |   |   |   |   |   number_of_modules >= 1.5 : 43.48 (4/4.21)
|   |   |   |   |   |   |   |   |   speed >= 1466.5
|   |   |   |   |   |   |   |   |   |   number_of_modules < 1.5 : 24.37 (12/1.46)
|   |   |   |   |   |   |   |   |   |   number_of_modules >= 1.5 : 46.85 (11/34.46)
|   |   |   |   |   |   |   |   speed >= 2533 : 46.19 (3/7.56)
|   |   |   |   |   |   |   brand = Crucial : 17.98 (1/0)
|   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   number_of_modules < 1.5 : 25.47 (5/0.27)
|   |   |   |   |   |   |   |   number_of_modules >= 1.5 : 44.75 (11/17.36)
|   |   |   |   |   |   |   brand = GeIL : 39.09 (1/0)
|   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   brand = Kingston
|   |   |   |   |   |   |   |   number_of_modules < 1.5 : 23.07 (14/5.67)
|   |   |   |   |   |   |   |   number_of_modules >= 1.5 : 48.77 (9/15.43)
|   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   brand = Mushkin
|   |   |   |   |   |   |   |   first_word_latency < 13.63
|   |   |   |   |   |   |   |   |   speed < 1199.5 : 48.15 (1/0)
|   |   |   |   |   |   |   |   |   speed >= 1199.5
|   |   |   |   |   |   |   |   |   |   number_of_modules < 1.5 : 25.38 (1/0)
|   |   |   |   |   |   |   |   |   |   number_of_modules >= 1.5 : 49.44 (2/1.66)
|   |   |   |   |   |   |   |   first_word_latency >= 13.63 : 48.13 (2/0)
|   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   brand = Patriot
|   |   |   |   |   |   |   |   first_word_latency < 12.67 : 47.23 (3/9.56)
|   |   |   |   |   |   |   |   first_word_latency >= 12.67
|   |   |   |   |   |   |   |   |   number_of_modules < 1.5 : 20.88 (8/2.24)
|   |   |   |   |   |   |   |   |   number_of_modules >= 1.5 : 41.07 (6/11.47)
|   |   |   |   |   |   |   brand = Samsung : 18.98 (1/0)
|   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   price_per_gb >= 6.52
|   |   |   |   |   |   number_of_modules < 1.5 : 28.59 (50/32.94)
|   |   |   |   |   |   number_of_modules >= 1.5
|   |   |   |   |   |   |   error_correction = False
|   |   |   |   |   |   |   |   price_per_gb < 8.74
|   |   |   |   |   |   |   |   |   module_size < 3
|   |   |   |   |   |   |   |   |   |   number_of_modules < 2.5 : 30.83 (4/3.46)
|   |   |   |   |   |   |   |   |   |   number_of_modules >= 2.5 : 48.52 (2/0.31)
|   |   |   |   |   |   |   |   |   module_size >= 3 : 59.21 (51/29.04)
|   |   |   |   |   |   |   |   price_per_gb >= 8.74 : 38.9 (6/49.28)
|   |   |   |   |   |   |   error_correction = True : 99.95 (1/0)
|   |   |   |   module_size >= 6
|   |   |   |   |   number_of_modules < 1.5
|   |   |   |   |   |   price_per_gb < 6.17 : 41.34 (98/21.94)
|   |   |   |   |   |   price_per_gb >= 6.17 : 57.47 (66/30.56)
|   |   |   |   |   number_of_modules >= 1.5
|   |   |   |   |   |   price_per_gb < 6.42
|   |   |   |   |   |   |   price_per_gb < 5.22
|   |   |   |   |   |   |   |   price_per_gb < 2.31 : 15.48 (1/0)
|   |   |   |   |   |   |   |   price_per_gb >= 2.31 : 74.92 (82/29.19)
|   |   |   |   |   |   |   price_per_gb >= 5.22 : 92.94 (110/40.01)
|   |   |   |   |   |   price_per_gb >= 6.42
|   |   |   |   |   |   |   price_per_gb < 7.75 : 112.96 (73/38.2)
|   |   |   |   |   |   |   price_per_gb >= 7.75 : 135.08 (35/30.86)
|   |   |   number_of_modules >= 3.5
|   |   |   |   speed < 2733
|   |   |   |   |   module_size < 6
|   |   |   |   |   |   price_per_gb < 7.24
|   |   |   |   |   |   |   price_per_gb < 5.91 : 84.61 (7/24.43)
|   |   |   |   |   |   |   price_per_gb >= 5.91 : 104.2 (6/14.04)
|   |   |   |   |   |   price_per_gb >= 7.24
|   |   |   |   |   |   |   cas_timing < 15.5 : 128.49 (3/31.17)
|   |   |   |   |   |   |   cas_timing >= 15.5 : 142.91 (1/0)
|   |   |   |   |   module_size >= 6
|   |   |   |   |   |   price_per_gb < 5.25
|   |   |   |   |   |   |   price_per_gb < 4.82 : 143.99 (5/47.67)
|   |   |   |   |   |   |   price_per_gb >= 4.82 : 162.04 (11/12.81)
|   |   |   |   |   |   price_per_gb >= 5.25
|   |   |   |   |   |   |   price_per_gb < 5.3 : 337.7 (1/0)
|   |   |   |   |   |   |   price_per_gb >= 5.3
|   |   |   |   |   |   |   |   first_word_latency < 14.21
|   |   |   |   |   |   |   |   |   price_per_gb < 6.67
|   |   |   |   |   |   |   |   |   |   first_word_latency < 10.63 : 170.22 (1/0)
|   |   |   |   |   |   |   |   |   |   first_word_latency >= 10.63
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 5.88 : 179.66 (7/21.75)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 5.88 : 196.66 (6/50.35)
|   |   |   |   |   |   |   |   |   price_per_gb >= 6.67
|   |   |   |   |   |   |   |   |   |   cas_timing < 15.5 : 255.68 (2/36.18)
|   |   |   |   |   |   |   |   |   |   cas_timing >= 15.5
|   |   |   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Corsair : 217.96 (1/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = G.Skill : 235.47 (2/6.38)
|   |   |   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   first_word_latency >= 14.21 : 271.18 (1/0)
|   |   |   |   speed >= 2733
|   |   |   |   |   module_size < 6 : 97.85 (1/0)
|   |   |   |   |   module_size >= 6
|   |   |   |   |   |   cas_timing < 17
|   |   |   |   |   |   |   price_per_gb < 5.89
|   |   |   |   |   |   |   |   speed < 3100
|   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   |   |   first_word_latency < 10.33 : 187.1 (1/0)
|   |   |   |   |   |   |   |   |   |   first_word_latency >= 10.33
|   |   |   |   |   |   |   |   |   |   |   speed < 2966.5 : 164.99 (1/0)
|   |   |   |   |   |   |   |   |   |   |   speed >= 2966.5 : 149.06 (1/0)
|   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = G.Skill : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Kingston : 186.86 (1/0)
|   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   speed >= 3100
|   |   |   |   |   |   |   |   |   price_per_gb < 5.12
|   |   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 4.69 : 139.99 (1/0)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 4.69 : 159.99 (2/0)
|   |   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = G.Skill : 158 (1/0)
|   |   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 5.12 : 174.02 (6/29.77)
|   |   |   |   |   |   |   price_per_gb >= 5.89
|   |   |   |   |   |   |   |   cas_timing < 15.5
|   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Corsair : 228.45 (2/44.16)
|   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = G.Skill : 201.83 (4/24.61)
|   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Kingston : 215.22 (3/13.47)
|   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   cas_timing >= 15.5
|   |   |   |   |   |   |   |   |   price_per_gb < 5.98 : 379.99 (1/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 5.98
|   |   |   |   |   |   |   |   |   |   price_per_gb < 7.3
|   |   |   |   |   |   |   |   |   |   |   first_word_latency < 10.33
|   |   |   |   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   |   |   |   |   |   price_per_gb < 6.33 : 194.99 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 6.33 : 214 (2/16.04)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   |   |   |   |   |   price_per_gb < 6.56 : 198.34 (2/31.81)
|   |   |   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 6.56 : 216 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 10.33 : 223.7 (1/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 7.3
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 7.91 : 244.78 (4/1.72)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 7.91
|   |   |   |   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   |   |   |   |   |   price_per_gb < 8.69 : 265.38 (2/31.64)
|   |   |   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 8.69 : 284.89 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = G.Skill : 264.99 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   cas_timing >= 17
|   |   |   |   |   |   |   price_per_gb < 7.29
|   |   |   |   |   |   |   |   price_per_gb < 6.3 : 189.99 (1/0)
|   |   |   |   |   |   |   |   price_per_gb >= 6.3 : 215.96 (2/5.93)
|   |   |   |   |   |   |   price_per_gb >= 7.29
|   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   |   price_per_gb < 8.22 : 248.37 (1/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 8.22 : 283.02 (2/27.04)
|   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   |   cas_timing < 18.5 : 262.7 (1/0)
|   |   |   |   |   |   |   |   |   cas_timing >= 18.5 : 287.33 (1/0)
|   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   price_per_gb >= 9.1
|   |   |   module_size < 6
|   |   |   |   number_of_modules < 3.5
|   |   |   |   |   price_per_gb < 81.41
|   |   |   |   |   |   speed < 2733
|   |   |   |   |   |   |   price_per_gb < 23.55
|   |   |   |   |   |   |   |   number_of_modules < 1.5
|   |   |   |   |   |   |   |   |   cas_timing < 12
|   |   |   |   |   |   |   |   |   |   module_size < 3
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 22.06 : 25.8 (22/42.86)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 22.06
|   |   |   |   |   |   |   |   |   |   |   |   brand = ADATA : 44.45 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Corsair : 44.8 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = G.Skill : 23.44 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Kingston : 44.26 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Patriot : 45.26 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   module_size >= 3
|   |   |   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Corsair : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Crucial : 44.65 (2/28.52)
|   |   |   |   |   |   |   |   |   |   |   brand = G.Skill : 38.97 (5/3.8)
|   |   |   |   |   |   |   |   |   |   |   brand = GeIL : 37.2 (1/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Kingston : 46.11 (8/28.42)
|   |   |   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Mushkin : 57.99 (1/0)
|   |   |   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Patriot : 36.57 (1/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Team : 81.12 (1/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Transcend : 46.33 (1/0)
|   |   |   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   |   cas_timing >= 12
|   |   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Corsair : 40.99 (1/0)
|   |   |   |   |   |   |   |   |   |   brand = Crucial : 67.34 (4/35.21)
|   |   |   |   |   |   |   |   |   |   brand = G.Skill : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = GeIL : 43.57 (1/0)
|   |   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Kingston
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 11.05 : 38.34 (1/0)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 11.05 : 54.65 (3/17.18)
|   |   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Mushkin
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 15.36 : 38.52 (2/2.25)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 15.36 : 82.84 (1/0)
|   |   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Patriot
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 13.29 : 42.61 (2/2.98)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 13.29 : 61.99 (1/0)
|   |   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Team : 43.36 (1/0)
|   |   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   number_of_modules >= 1.5
|   |   |   |   |   |   |   |   |   cas_timing < 10.5
|   |   |   |   |   |   |   |   |   |   price_per_gb < 16.07
|   |   |   |   |   |   |   |   |   |   |   module_size < 3
|   |   |   |   |   |   |   |   |   |   |   |   module_size < 1.5 : 28.67 (4/8.25)
|   |   |   |   |   |   |   |   |   |   |   |   module_size >= 1.5
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Corsair : 42.81 (2/7.92)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   |   |   |   |   |   |   module_type = DDR2 : 54.11 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   module_type = DDR4 : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   module_type = DDR3 : 37.55 (2/0.95)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Kingston
|   |   |   |   |   |   |   |   |   |   |   |   |   |   price_per_gb < 10.69 : 63.13 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 10.69
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed < 1200 : 58 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 1200 : 43.44 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Mushkin : 57.05 (2/15.48)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Patriot : 54.69 (4/27.24)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   module_size >= 3
|   |   |   |   |   |   |   |   |   |   |   |   speed < 1466.5
|   |   |   |   |   |   |   |   |   |   |   |   |   number_of_modules < 2.5 : 95.87 (3/38.62)
|   |   |   |   |   |   |   |   |   |   |   |   |   number_of_modules >= 2.5 : 141.54 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   speed >= 1466.5
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = ADATA : 85.03 (2/22.47)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Corsair : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Crucial : 86.98 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = G.Skill : 73.34 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Kingston : 74.94 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Klevv : 82.54 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Team : 97.31 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 16.07
|   |   |   |   |   |   |   |   |   |   |   module_size < 1.5 : 37.93 (2/16.24)
|   |   |   |   |   |   |   |   |   |   |   module_size >= 1.5
|   |   |   |   |   |   |   |   |   |   |   |   price_per_gb < 18.28
|   |   |   |   |   |   |   |   |   |   |   |   |   number_of_modules < 2.5 : 129.99 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   number_of_modules >= 2.5 : 109 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 18.28 : 80.66 (6/44.33)
|   |   |   |   |   |   |   |   |   cas_timing >= 10.5
|   |   |   |   |   |   |   |   |   |   price_per_gb < 11.9 : 89.32 (3/1.56)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 11.9 : 101.27 (6/35.98)
|   |   |   |   |   |   |   price_per_gb >= 23.55
|   |   |   |   |   |   |   |   module_size < 1.5
|   |   |   |   |   |   |   |   |   number_of_modules < 1.5 : 25.9 (1/0)
|   |   |   |   |   |   |   |   |   number_of_modules >= 1.5 : 75.77 (2/4.37)
|   |   |   |   |   |   |   |   module_size >= 1.5
|   |   |   |   |   |   |   |   |   error_correction = False
|   |   |   |   |   |   |   |   |   |   number_of_modules < 1.5
|   |   |   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Corsair : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = G.Skill : 121.06 (1/0)
|   |   |   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Kingston : 154 (1/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   number_of_modules >= 1.5
|   |   |   |   |   |   |   |   |   |   |   module_size < 3 : 142.01 (1/0)
|   |   |   |   |   |   |   |   |   |   |   module_size >= 3
|   |   |   |   |   |   |   |   |   |   |   |   module_type = DDR2
|   |   |   |   |   |   |   |   |   |   |   |   |   speed < 733.5 : 195.8 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 733.5 : 244.99 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   module_type = DDR4 : 191.63 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   module_type = DDR3 : 210.39 (1/0)
|   |   |   |   |   |   |   |   |   error_correction = True
|   |   |   |   |   |   |   |   |   |   price_per_gb < 31.35 : 103.62 (2/2.58)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 31.35
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 40.42 : 145.6 (1/0)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 40.42 : 177.79 (1/0)
|   |   |   |   |   |   speed >= 2733
|   |   |   |   |   |   |   brand = ADATA : 133.86 (1/0)
|   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   speed < 3300 : 168.63 (1/0)
|   |   |   |   |   |   |   |   speed >= 3300 : 229 (1/0)
|   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   price_per_gb < 16.99
|   |   |   |   |   |   |   |   |   cas_timing < 15.5
|   |   |   |   |   |   |   |   |   |   price_per_gb < 13.87 : 101.28 (1/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 13.87 : 124.24 (2/13)
|   |   |   |   |   |   |   |   |   cas_timing >= 15.5
|   |   |   |   |   |   |   |   |   |   price_per_gb < 14.64 : 107.99 (2/4)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 14.64 : 124.26 (1/0)
|   |   |   |   |   |   |   |   price_per_gb >= 16.99 : 150.06 (2/36.84)
|   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   brand = Patriot : 122.56 (1/0)
|   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   brand = Team
|   |   |   |   |   |   |   |   price_per_gb < 17.29 : 128.86 (2/22.66)
|   |   |   |   |   |   |   |   price_per_gb >= 17.29 : 147.31 (2/18.02)
|   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   price_per_gb >= 81.41 : 947 (1/0)
|   |   |   |   number_of_modules >= 3.5
|   |   |   |   |   module_size < 3 : 97.15 (1/0)
|   |   |   |   |   module_size >= 3
|   |   |   |   |   |   price_per_gb < 17.16
|   |   |   |   |   |   |   price_per_gb < 13.08
|   |   |   |   |   |   |   |   number_of_modules < 5
|   |   |   |   |   |   |   |   |   first_word_latency < 10.63 : 202.88 (2/10.21)
|   |   |   |   |   |   |   |   |   first_word_latency >= 10.63
|   |   |   |   |   |   |   |   |   |   price_per_gb < 10.95 : 164.35 (3/27.41)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 10.95 : 181.33 (1/0)
|   |   |   |   |   |   |   |   number_of_modules >= 5
|   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Corsair : 225.69 (1/0)
|   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = G.Skill : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Kingston : 250.91 (1/0)
|   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   price_per_gb >= 13.08
|   |   |   |   |   |   |   |   first_word_latency < 10.33
|   |   |   |   |   |   |   |   |   cas_timing < 15.5
|   |   |   |   |   |   |   |   |   |   price_per_gb < 13.86 : 217 (1/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 13.86 : 230.95 (2/19.36)
|   |   |   |   |   |   |   |   |   cas_timing >= 15.5 : 237.44 (2/19.76)
|   |   |   |   |   |   |   |   first_word_latency >= 10.33
|   |   |   |   |   |   |   |   |   first_word_latency < 12.66 : 214.2 (2/3.2)
|   |   |   |   |   |   |   |   |   first_word_latency >= 12.66
|   |   |   |   |   |   |   |   |   |   price_per_gb < 14.46 : 216.47 (1/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 14.46 : 246.09 (1/0)
|   |   |   |   |   |   price_per_gb >= 17.16
|   |   |   |   |   |   |   speed < 2399.5 : 340.32 (1/0)
|   |   |   |   |   |   |   speed >= 2399.5 : 303.1 (1/0)
|   |   |   module_size >= 6
|   |   |   |   number_of_modules < 3.5
|   |   |   |   |   price_per_gb < 15.42
|   |   |   |   |   |   number_of_modules < 1.5
|   |   |   |   |   |   |   price_per_gb < 12.53
|   |   |   |   |   |   |   |   brand = ADATA : 82.58 (1/0)
|   |   |   |   |   |   |   |   brand = Corsair : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Crucial : 79.41 (3/14.53)
|   |   |   |   |   |   |   |   brand = G.Skill : 0 (0/0)
|   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Kingston
|   |   |   |   |   |   |   |   |   price_per_gb < 11.06 : 79.18 (2/7.45)
|   |   |   |   |   |   |   |   |   price_per_gb >= 11.06 : 96.7 (2/2.91)
|   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   price_per_gb >= 12.53
|   |   |   |   |   |   |   |   price_per_gb < 14.01 : 105.84 (4/11.41)
|   |   |   |   |   |   |   |   price_per_gb >= 14.01 : 119.75 (7/7.72)
|   |   |   |   |   |   number_of_modules >= 1.5
|   |   |   |   |   |   |   price_per_gb < 11.72
|   |   |   |   |   |   |   |   number_of_modules < 2.5
|   |   |   |   |   |   |   |   |   price_per_gb < 10.69 : 156.39 (26/45.63)
|   |   |   |   |   |   |   |   |   price_per_gb >= 10.69 : 181.37 (7/15.22)
|   |   |   |   |   |   |   |   number_of_modules >= 2.5 : 233.5 (1/0)
|   |   |   |   |   |   |   price_per_gb >= 11.72
|   |   |   |   |   |   |   |   first_word_latency < 10.53
|   |   |   |   |   |   |   |   |   speed < 3533
|   |   |   |   |   |   |   |   |   |   first_word_latency < 10.19
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 13.05 : 194.52 (1/0)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 13.05
|   |   |   |   |   |   |   |   |   |   |   |   speed < 3100 : 244.99 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   speed >= 3100 : 223.2 (1/0)
|   |   |   |   |   |   |   |   |   |   first_word_latency >= 10.19 : 237.03 (1/0)
|   |   |   |   |   |   |   |   |   speed >= 3533 : 205.59 (1/0)
|   |   |   |   |   |   |   |   first_word_latency >= 10.53
|   |   |   |   |   |   |   |   |   brand = ADATA : 205.6 (3/17.36)
|   |   |   |   |   |   |   |   |   brand = Corsair : 189.99 (1/0)
|   |   |   |   |   |   |   |   |   brand = Crucial : 202.34 (1/0)
|   |   |   |   |   |   |   |   |   brand = G.Skill : 209.5 (4/41.74)
|   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Kingston
|   |   |   |   |   |   |   |   |   |   speed < 3066 : 200 (1/0)
|   |   |   |   |   |   |   |   |   |   speed >= 3066 : 219.41 (1/0)
|   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Patriot : 227.67 (1/0)
|   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Team : 210.42 (1/0)
|   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   price_per_gb >= 15.42
|   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   speed < 3400 : 285.58 (1/0)
|   |   |   |   |   |   |   speed >= 3400 : 266.81 (1/0)
|   |   |   |   |   |   brand = Crucial
|   |   |   |   |   |   |   price_per_gb < 15.98 : 376.94 (1/0)
|   |   |   |   |   |   |   price_per_gb >= 15.98
|   |   |   |   |   |   |   |   price_per_gb < 19.81 : 132.52 (3/3.27)
|   |   |   |   |   |   |   |   price_per_gb >= 19.81 : 186.38 (2/12.18)
|   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   speed < 2733 : 313 (1/0)
|   |   |   |   |   |   |   speed >= 2733 : 294.8 (1/0)
|   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   brand = HP : 159.02 (1/0)
|   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   brand = Kingston
|   |   |   |   |   |   |   first_word_latency < 12.57 : 148.28 (1/0)
|   |   |   |   |   |   |   first_word_latency >= 12.57
|   |   |   |   |   |   |   |   number_of_modules < 1.5
|   |   |   |   |   |   |   |   |   speed < 1466.5 : 154.88 (1/0)
|   |   |   |   |   |   |   |   |   speed >= 1466.5 : 215.29 (1/0)
|   |   |   |   |   |   |   |   number_of_modules >= 1.5 : 330 (1/0)
|   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   brand = PNY : 254.81 (2/44.56)
|   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   brand = Team : 255.47 (2/17.14)
|   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   number_of_modules >= 3.5
|   |   |   |   |   number_of_modules < 6
|   |   |   |   |   |   price_per_gb < 15.03
|   |   |   |   |   |   |   cas_timing < 17
|   |   |   |   |   |   |   |   brand = ADATA : 380.37 (2/12.32)
|   |   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   |   price_per_gb < 10.6 : 302.04 (2/9.24)
|   |   |   |   |   |   |   |   |   price_per_gb >= 10.6
|   |   |   |   |   |   |   |   |   |   first_word_latency < 10.63 : 414.99 (1/0)
|   |   |   |   |   |   |   |   |   |   first_word_latency >= 10.63 : 373.4 (1/0)
|   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   |   speed < 2566.5 : 397.84 (1/0)
|   |   |   |   |   |   |   |   |   speed >= 2566.5
|   |   |   |   |   |   |   |   |   |   price_per_gb < 9.53 : 291.08 (1/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 9.53 : 318.77 (1/0)
|   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Kingston : 360.75 (1/0)
|   |   |   |   |   |   |   |   brand = Klevv : 420.29 (1/0)
|   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   cas_timing >= 17
|   |   |   |   |   |   |   |   speed < 3400 : 307.38 (1/0)
|   |   |   |   |   |   |   |   speed >= 3400
|   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   |   |   price_per_gb < 11.24 : 316.12 (1/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 11.24
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 13.22 : 403.2 (1/0)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 13.22 : 443.12 (1/0)
|   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = G.Skill : 428.38 (1/0)
|   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   price_per_gb >= 15.03
|   |   |   |   |   |   |   speed < 2800
|   |   |   |   |   |   |   |   cas_timing < 15.5 : 560.47 (1/0)
|   |   |   |   |   |   |   |   cas_timing >= 15.5 : 541.11 (1/0)
|   |   |   |   |   |   |   speed >= 2800 : 518.54 (1/0)
|   |   |   |   |   number_of_modules >= 6
|   |   |   |   |   |   module_type = DDR2 : 0 (0/0)
|   |   |   |   |   |   module_type = DDR4
|   |   |   |   |   |   |   price_per_gb < 11.5
|   |   |   |   |   |   |   |   speed < 3400 : 629.9 (1/0)
|   |   |   |   |   |   |   |   speed >= 3400
|   |   |   |   |   |   |   |   |   price_per_gb < 9.82 : 584.99 (1/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 9.82 : 672.04 (1/0)
|   |   |   |   |   |   |   price_per_gb >= 11.5
|   |   |   |   |   |   |   |   first_word_latency < 11 : 909.99 (1/0)
|   |   |   |   |   |   |   |   first_word_latency >= 11
|   |   |   |   |   |   |   |   |   price_per_gb < 12.65 : 799.9 (1/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 12.65 : 820 (1/0)
|   |   |   |   |   |   module_type = DDR3 : 618.64 (1/0)
module_size >= 12
|   speed < 3533
|   |   number_of_modules < 3
|   |   |   price_per_gb < 7.65
|   |   |   |   brand = ADATA
|   |   |   |   |   first_word_latency < 11.34
|   |   |   |   |   |   first_word_latency < 10.33
|   |   |   |   |   |   |   price_per_gb < 5.99 : 169.58 (1/0)
|   |   |   |   |   |   |   price_per_gb >= 5.99 : 213.77 (1/0)
|   |   |   |   |   |   first_word_latency >= 10.33
|   |   |   |   |   |   |   price_per_gb < 6.3 : 176.5 (1/0)
|   |   |   |   |   |   |   price_per_gb >= 6.3 : 113.3 (1/0)
|   |   |   |   |   first_word_latency >= 11.34 : 98.04 (2/4.56)
|   |   |   |   brand = Corsair
|   |   |   |   |   number_of_modules < 1.5
|   |   |   |   |   |   speed < 2266.5 : 70.22 (2/0.34)
|   |   |   |   |   |   speed >= 2266.5
|   |   |   |   |   |   |   cas_timing < 15.5 : 78.61 (2/13.1)
|   |   |   |   |   |   |   cas_timing >= 15.5
|   |   |   |   |   |   |   |   price_per_gb < 4.6
|   |   |   |   |   |   |   |   |   price_per_gb < 4.11
|   |   |   |   |   |   |   |   |   |   price_per_gb < 3.81 : 56.99 (1/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 3.81 : 129.99 (1/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 4.11 : 68.44 (4/1.58)
|   |   |   |   |   |   |   |   price_per_gb >= 4.6
|   |   |   |   |   |   |   |   |   first_word_latency < 10.33 : 88.99 (1/0)
|   |   |   |   |   |   |   |   |   first_word_latency >= 10.33
|   |   |   |   |   |   |   |   |   |   module_size < 24 : 79.99 (1/0)
|   |   |   |   |   |   |   |   |   |   module_size >= 24
|   |   |   |   |   |   |   |   |   |   |   cas_timing < 17 : 172.25 (2/7.54)
|   |   |   |   |   |   |   |   |   |   |   cas_timing >= 17 : 154.72 (1/0)
|   |   |   |   |   number_of_modules >= 1.5
|   |   |   |   |   |   module_size < 24
|   |   |   |   |   |   |   price_per_gb < 5.57
|   |   |   |   |   |   |   |   speed < 2833
|   |   |   |   |   |   |   |   |   speed < 2533
|   |   |   |   |   |   |   |   |   |   cas_timing < 13.5 : 111.23 (1/0)
|   |   |   |   |   |   |   |   |   |   cas_timing >= 13.5
|   |   |   |   |   |   |   |   |   |   |   first_word_latency < 13.7 : 131.93 (2/3.74)
|   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 13.7 : 162.13 (1/0)
|   |   |   |   |   |   |   |   |   speed >= 2533
|   |   |   |   |   |   |   |   |   |   cas_timing < 17
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 4.19 : 119.05 (2/25.6)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 4.19 : 148.11 (3/7.09)
|   |   |   |   |   |   |   |   |   |   cas_timing >= 17 : 139.99 (1/0)
|   |   |   |   |   |   |   |   speed >= 2833
|   |   |   |   |   |   |   |   |   price_per_gb < 4.6 : 135 (7/47.68)
|   |   |   |   |   |   |   |   |   price_per_gb >= 4.6
|   |   |   |   |   |   |   |   |   |   cas_timing < 15.5 : 162.01 (5/28.42)
|   |   |   |   |   |   |   |   |   |   cas_timing >= 15.5
|   |   |   |   |   |   |   |   |   |   |   speed < 3266.5
|   |   |   |   |   |   |   |   |   |   |   |   price_per_gb < 5.1 : 153.09 (3/7.25)
|   |   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 5.1 : 171.66 (1/0)
|   |   |   |   |   |   |   |   |   |   |   speed >= 3266.5 : 170.4 (1/0)
|   |   |   |   |   |   |   price_per_gb >= 5.57
|   |   |   |   |   |   |   |   speed < 3333
|   |   |   |   |   |   |   |   |   speed < 3100 : 191.07 (3/28.18)
|   |   |   |   |   |   |   |   |   speed >= 3100
|   |   |   |   |   |   |   |   |   |   price_per_gb < 6.4 : 194.99 (1/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 6.4 : 214.9 (1/0)
|   |   |   |   |   |   |   |   speed >= 3333 : 235.39 (2/29.11)
|   |   |   |   |   |   module_size >= 24
|   |   |   |   |   |   |   first_word_latency < 10.33
|   |   |   |   |   |   |   |   price_per_gb < 5.21 : 316.52 (1/0)
|   |   |   |   |   |   |   |   price_per_gb >= 5.21 : 349.99 (1/0)
|   |   |   |   |   |   |   first_word_latency >= 10.33
|   |   |   |   |   |   |   |   speed < 2833
|   |   |   |   |   |   |   |   |   price_per_gb < 4.34 : 257.49 (2/6.25)
|   |   |   |   |   |   |   |   |   price_per_gb >= 4.34 : 294.97 (1/0)
|   |   |   |   |   |   |   |   speed >= 2833 : 297 (2/4.02)
|   |   |   |   brand = Crucial
|   |   |   |   |   module_size < 24
|   |   |   |   |   |   speed < 1866.5 : 70.46 (2/41.73)
|   |   |   |   |   |   speed >= 1866.5
|   |   |   |   |   |   |   first_word_latency < 11.34
|   |   |   |   |   |   |   |   cas_timing < 15.5
|   |   |   |   |   |   |   |   |   price_per_gb < 5.27 : 133.4 (3/-0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 5.27 : 203.75 (1/0)
|   |   |   |   |   |   |   |   cas_timing >= 15.5
|   |   |   |   |   |   |   |   |   speed < 3100 : 186.76 (2/48.16)
|   |   |   |   |   |   |   |   |   speed >= 3100
|   |   |   |   |   |   |   |   |   |   price_per_gb < 5.66
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 4.8 : 143.98 (3/0)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 4.8 : 164.99 (2/3.94)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 5.66 : 195.13 (1/0)
|   |   |   |   |   |   |   first_word_latency >= 11.34
|   |   |   |   |   |   |   |   number_of_modules < 1.5 : 119.03 (2/10.4)
|   |   |   |   |   |   |   |   number_of_modules >= 1.5
|   |   |   |   |   |   |   |   |   price_per_gb < 4.56 : 128.69 (4/21.05)
|   |   |   |   |   |   |   |   |   price_per_gb >= 4.56 : 159.97 (1/0)
|   |   |   |   |   module_size >= 24
|   |   |   |   |   |   speed < 2933
|   |   |   |   |   |   |   error_correction = False
|   |   |   |   |   |   |   |   number_of_modules < 1.5
|   |   |   |   |   |   |   |   |   price_per_gb < 3.46 : 102.65 (1/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 3.46 : 118.57 (1/0)
|   |   |   |   |   |   |   |   number_of_modules >= 1.5 : 209.57 (1/0)
|   |   |   |   |   |   |   error_correction = True : 213 (1/0)
|   |   |   |   |   |   speed >= 2933
|   |   |   |   |   |   |   price_per_gb < 3.95
|   |   |   |   |   |   |   |   price_per_gb < 3.37 : 214.99 (1/0)
|   |   |   |   |   |   |   |   price_per_gb >= 3.37 : 107.99 (1/0)
|   |   |   |   |   |   |   price_per_gb >= 3.95
|   |   |   |   |   |   |   |   price_per_gb < 4.96 : 294.08 (3/28.36)
|   |   |   |   |   |   |   |   price_per_gb >= 4.96
|   |   |   |   |   |   |   |   |   price_per_gb < 5.37 : 333.95 (1/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 5.37 : 354.43 (2/0.55)
|   |   |   |   brand = G.Skill
|   |   |   |   |   module_size < 24
|   |   |   |   |   |   number_of_modules < 1.5
|   |   |   |   |   |   |   price_per_gb < 5.42 : 78.3 (7/17.05)
|   |   |   |   |   |   |   price_per_gb >= 5.42
|   |   |   |   |   |   |   |   cas_timing < 15.5 : 104.27 (2/9.15)
|   |   |   |   |   |   |   |   cas_timing >= 15.5 : 90.36 (2/0.66)
|   |   |   |   |   |   number_of_modules >= 1.5
|   |   |   |   |   |   |   cas_timing < 14.5
|   |   |   |   |   |   |   |   price_per_gb < 6.57 : 186.08 (2/48.72)
|   |   |   |   |   |   |   |   price_per_gb >= 6.57 : 238.04 (4/44.56)
|   |   |   |   |   |   |   cas_timing >= 14.5
|   |   |   |   |   |   |   |   price_per_gb < 5.37
|   |   |   |   |   |   |   |   |   speed < 2533
|   |   |   |   |   |   |   |   |   |   cas_timing < 16.5 : 130.37 (7/28.68)
|   |   |   |   |   |   |   |   |   |   cas_timing >= 16.5
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 4.15 : 116.3 (1/0)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 4.15 : 149.05 (1/0)
|   |   |   |   |   |   |   |   |   speed >= 2533
|   |   |   |   |   |   |   |   |   |   price_per_gb < 4.22 : 121.09 (3/30.72)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 4.22
|   |   |   |   |   |   |   |   |   |   |   first_word_latency < 12.09
|   |   |   |   |   |   |   |   |   |   |   |   price_per_gb < 4.78 : 145.7 (3/4.95)
|   |   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 4.78 : 162.57 (7/17.34)
|   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 12.09 : 143.01 (2/2.82)
|   |   |   |   |   |   |   |   price_per_gb >= 5.37
|   |   |   |   |   |   |   |   |   cas_timing < 15.5
|   |   |   |   |   |   |   |   |   |   speed < 2266.5 : 178.49 (1/0)
|   |   |   |   |   |   |   |   |   |   speed >= 2266.5
|   |   |   |   |   |   |   |   |   |   |   first_word_latency < 11.25 : 179.2 (1/0)
|   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 11.25
|   |   |   |   |   |   |   |   |   |   |   |   price_per_gb < 6.7 : 185.9 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 6.7 : 242.85 (1/0)
|   |   |   |   |   |   |   |   |   cas_timing >= 15.5
|   |   |   |   |   |   |   |   |   |   price_per_gb < 6.41
|   |   |   |   |   |   |   |   |   |   |   speed < 3100
|   |   |   |   |   |   |   |   |   |   |   |   speed < 2700 : 182.4 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   speed >= 2700 : 198.8 (1/0)
|   |   |   |   |   |   |   |   |   |   |   speed >= 3100 : 178.52 (3/28.18)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 6.41
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 6.97 : 213.52 (2/5.2)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 6.97 : 233.67 (3/9.94)
|   |   |   |   |   module_size >= 24
|   |   |   |   |   |   number_of_modules < 1.5 : 146.95 (2/32.83)
|   |   |   |   |   |   number_of_modules >= 1.5
|   |   |   |   |   |   |   first_word_latency < 11.75
|   |   |   |   |   |   |   |   price_per_gb < 5.17 : 310.08 (1/0)
|   |   |   |   |   |   |   |   price_per_gb >= 5.17
|   |   |   |   |   |   |   |   |   price_per_gb < 5.72 : 351.97 (1/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 5.72 : 379.86 (1/0)
|   |   |   |   |   |   |   first_word_latency >= 11.75 : 286.52 (1/0)
|   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   brand = Gigabyte : 357.27 (1/0)
|   |   |   |   brand = HP : 44.41 (1/0)
|   |   |   |   brand = IBM : 111.99 (1/0)
|   |   |   |   brand = Kingston
|   |   |   |   |   speed < 2533
|   |   |   |   |   |   cas_timing < 16
|   |   |   |   |   |   |   module_size < 24
|   |   |   |   |   |   |   |   error_correction = False
|   |   |   |   |   |   |   |   |   cas_timing < 14.5
|   |   |   |   |   |   |   |   |   |   price_per_gb < 4.26 : 66.98 (1/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 4.26 : 138.99 (1/0)
|   |   |   |   |   |   |   |   |   cas_timing >= 14.5
|   |   |   |   |   |   |   |   |   |   number_of_modules < 1.5 : 89 (1/0)
|   |   |   |   |   |   |   |   |   |   number_of_modules >= 1.5 : 144.44 (1/0)
|   |   |   |   |   |   |   |   error_correction = True : 107.62 (1/0)
|   |   |   |   |   |   |   module_size >= 24 : 186.2 (1/0)
|   |   |   |   |   |   cas_timing >= 16
|   |   |   |   |   |   |   error_correction = False : 74.56 (5/37.02)
|   |   |   |   |   |   |   error_correction = True
|   |   |   |   |   |   |   |   price_per_gb < 3.61 : 39.29 (1/0)
|   |   |   |   |   |   |   |   price_per_gb >= 3.61 : 76.18 (1/0)
|   |   |   |   |   speed >= 2533
|   |   |   |   |   |   module_size < 24
|   |   |   |   |   |   |   number_of_modules < 1.5
|   |   |   |   |   |   |   |   price_per_gb < 5.4 : 76.14 (11/39.25)
|   |   |   |   |   |   |   |   price_per_gb >= 5.4 : 97.13 (6/37.13)
|   |   |   |   |   |   |   number_of_modules >= 1.5
|   |   |   |   |   |   |   |   price_per_gb < 5.54
|   |   |   |   |   |   |   |   |   first_word_latency < 10.63
|   |   |   |   |   |   |   |   |   |   speed < 3100
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 4.72 : 139.88 (1/0)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 4.72 : 161.93 (1/0)
|   |   |   |   |   |   |   |   |   |   speed >= 3100 : 147.4 (3/11.61)
|   |   |   |   |   |   |   |   |   first_word_latency >= 10.63
|   |   |   |   |   |   |   |   |   |   price_per_gb < 4.25 : 127.45 (1/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 4.25 : 144.62 (1/0)
|   |   |   |   |   |   |   |   price_per_gb >= 5.54 : 195.66 (2/8.94)
|   |   |   |   |   |   module_size >= 24
|   |   |   |   |   |   |   cas_timing < 17.5
|   |   |   |   |   |   |   |   price_per_gb < 4.31 : 263.25 (2/39.25)
|   |   |   |   |   |   |   |   price_per_gb >= 4.31 : 282.42 (1/0)
|   |   |   |   |   |   |   cas_timing >= 17.5 : 175.79 (4/33.8)
|   |   |   |   brand = Klevv : 143.74 (1/0)
|   |   |   |   brand = Mushkin
|   |   |   |   |   speed < 1866.5 : 62.07 (2/0.55)
|   |   |   |   |   speed >= 1866.5
|   |   |   |   |   |   price_per_gb < 7.19
|   |   |   |   |   |   |   first_word_latency < 13.1 : 149.75 (2/47.82)
|   |   |   |   |   |   |   first_word_latency >= 13.1
|   |   |   |   |   |   |   |   speed < 2399.5
|   |   |   |   |   |   |   |   |   price_per_gb < 6.38 : 191 (1/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 6.38 : 216.99 (1/0)
|   |   |   |   |   |   |   |   speed >= 2399.5 : 217.89 (1/0)
|   |   |   |   |   |   price_per_gb >= 7.19 : 120.99 (1/0)
|   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   brand = PNY
|   |   |   |   |   speed < 2933 : 71.59 (1/0)
|   |   |   |   |   speed >= 2933 : 129.05 (1/0)
|   |   |   |   brand = Patriot
|   |   |   |   |   number_of_modules < 1.5 : 65.77 (8/8.36)
|   |   |   |   |   number_of_modules >= 1.5
|   |   |   |   |   |   module_size < 24 : 131.32 (4/39.48)
|   |   |   |   |   |   module_size >= 24 : 224.9 (2/24.95)
|   |   |   |   brand = Samsung
|   |   |   |   |   speed < 2799.5
|   |   |   |   |   |   cas_timing < 17
|   |   |   |   |   |   |   price_per_gb < 4.88
|   |   |   |   |   |   |   |   first_word_latency < 14 : 120 (1/0)
|   |   |   |   |   |   |   |   first_word_latency >= 14 : 140 (1/0)
|   |   |   |   |   |   |   price_per_gb >= 4.88
|   |   |   |   |   |   |   |   speed < 1866.5 : 109.85 (1/0)
|   |   |   |   |   |   |   |   speed >= 1866.5 : 86 (1/0)
|   |   |   |   |   |   cas_timing >= 17
|   |   |   |   |   |   |   price_per_gb < 5.33
|   |   |   |   |   |   |   |   module_size < 24 : 80.2 (1/0)
|   |   |   |   |   |   |   |   module_size >= 24 : 154.77 (1/0)
|   |   |   |   |   |   |   price_per_gb >= 5.33 : 180.99 (1/0)
|   |   |   |   |   speed >= 2799.5 : 340 (1/0)
|   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   brand = Team
|   |   |   |   |   price_per_gb < 6.07
|   |   |   |   |   |   number_of_modules < 1.5 : 85.9 (3/49.86)
|   |   |   |   |   |   number_of_modules >= 1.5
|   |   |   |   |   |   |   cas_timing < 15.5 : 170.41 (2/0)
|   |   |   |   |   |   |   cas_timing >= 15.5
|   |   |   |   |   |   |   |   speed < 3100
|   |   |   |   |   |   |   |   |   price_per_gb < 4.94 : 150 (1/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 4.94 : 169.31 (3/23.94)
|   |   |   |   |   |   |   |   speed >= 3100
|   |   |   |   |   |   |   |   |   price_per_gb < 4.61 : 128.19 (1/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 4.61 : 166.88 (1/0)
|   |   |   |   |   price_per_gb >= 6.07
|   |   |   |   |   |   speed < 2800 : 227.96 (1/0)
|   |   |   |   |   |   speed >= 2800 : 205.46 (2/44.76)
|   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   brand = V7 : 0 (0/0)
|   |   |   price_per_gb >= 7.65
|   |   |   |   number_of_modules < 1.5
|   |   |   |   |   cas_timing < 16.5
|   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   brand = Corsair : 0 (0/0)
|   |   |   |   |   |   brand = Crucial
|   |   |   |   |   |   |   module_size < 24
|   |   |   |   |   |   |   |   speed < 1199.5 : 187.81 (1/0)
|   |   |   |   |   |   |   |   speed >= 1199.5
|   |   |   |   |   |   |   |   |   price_per_gb < 15.53 : 236.94 (1/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 15.53 : 259.99 (1/0)
|   |   |   |   |   |   |   module_size >= 24 : 432.31 (1/0)
|   |   |   |   |   |   brand = G.Skill : 0 (0/0)
|   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   brand = IBM : 151.33 (1/0)
|   |   |   |   |   |   brand = Kingston
|   |   |   |   |   |   |   cas_timing < 10
|   |   |   |   |   |   |   |   price_per_gb < 10.44 : 142.93 (2/20.12)
|   |   |   |   |   |   |   |   price_per_gb >= 10.44 : 186.62 (1/0)
|   |   |   |   |   |   |   cas_timing >= 10
|   |   |   |   |   |   |   |   module_type = DDR2 : 0 (0/0)
|   |   |   |   |   |   |   |   module_type = DDR4
|   |   |   |   |   |   |   |   |   price_per_gb < 20.09
|   |   |   |   |   |   |   |   |   |   speed < 2399.5
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 15.09 : 226 (1/0)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 15.09 : 256.76 (1/0)
|   |   |   |   |   |   |   |   |   |   speed >= 2399.5 : 202.2 (1/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 20.09 : 386.27 (1/0)
|   |   |   |   |   |   |   |   module_type = DDR3
|   |   |   |   |   |   |   |   |   cas_timing < 12
|   |   |   |   |   |   |   |   |   |   module_size < 24
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 15.13
|   |   |   |   |   |   |   |   |   |   |   |   price_per_gb < 9.49 : 135.61 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 9.49 : 168 (1/0)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 15.13
|   |   |   |   |   |   |   |   |   |   |   |   price_per_gb < 21.95 : 316 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 21.95 : 386.25 (1/0)
|   |   |   |   |   |   |   |   |   |   module_size >= 24 : 276 (1/0)
|   |   |   |   |   |   |   |   |   cas_timing >= 12 : 126.9 (1/0)
|   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   brand = Mushkin : 197.1 (1/0)
|   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   brand = Samsung
|   |   |   |   |   |   |   module_size < 24 : 154 (1/0)
|   |   |   |   |   |   |   module_size >= 24 : 409.93 (1/0)
|   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   brand = Team : 160.75 (1/0)
|   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   cas_timing >= 16.5
|   |   |   |   |   |   price_per_gb < 8.07 : 499 (1/0)
|   |   |   |   |   |   price_per_gb >= 8.07
|   |   |   |   |   |   |   cas_timing < 17.5
|   |   |   |   |   |   |   |   module_size < 24
|   |   |   |   |   |   |   |   |   price_per_gb < 13.74 : 180.53 (1/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 13.74 : 259 (1/0)
|   |   |   |   |   |   |   |   module_size >= 24
|   |   |   |   |   |   |   |   |   price_per_gb < 11.29
|   |   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Corsair : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Crucial : 285 (1/0)
|   |   |   |   |   |   |   |   |   |   brand = G.Skill : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Samsung
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 9.84 : 306 (1/0)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 9.84 : 323.7 (1/0)
|   |   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 11.29 : 399 (1/0)
|   |   |   |   |   |   |   cas_timing >= 17.5
|   |   |   |   |   |   |   |   error_correction = False
|   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Corsair : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Crucial : 137.81 (2/17.51)
|   |   |   |   |   |   |   |   |   brand = G.Skill : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Kingston : 267.16 (1/0)
|   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Samsung : 272 (1/0)
|   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   error_correction = True : 555.66 (1/0)
|   |   |   |   number_of_modules >= 1.5
|   |   |   |   |   first_word_latency < 9.71
|   |   |   |   |   |   price_per_gb < 10.02
|   |   |   |   |   |   |   speed < 3100 : 248.8 (1/0)
|   |   |   |   |   |   |   speed >= 3100
|   |   |   |   |   |   |   |   cas_timing < 14.5
|   |   |   |   |   |   |   |   |   price_per_gb < 8.62 : 257.15 (2/49.7)
|   |   |   |   |   |   |   |   |   price_per_gb >= 8.62 : 290.83 (2/12.57)
|   |   |   |   |   |   |   |   cas_timing >= 14.5
|   |   |   |   |   |   |   |   |   speed < 3300
|   |   |   |   |   |   |   |   |   |   price_per_gb < 9.12 : 279.06 (1/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 9.12 : 304.5 (1/0)
|   |   |   |   |   |   |   |   |   speed >= 3300 : 280.87 (1/0)
|   |   |   |   |   |   price_per_gb >= 10.02
|   |   |   |   |   |   |   price_per_gb < 11.06 : 336.99 (1/0)
|   |   |   |   |   |   |   price_per_gb >= 11.06 : 370.88 (1/0)
|   |   |   |   |   first_word_latency >= 9.71
|   |   |   |   |   |   price_per_gb < 11.36
|   |   |   |   |   |   |   price_per_gb < 8.42 : 250.66 (3/20.18)
|   |   |   |   |   |   |   price_per_gb >= 8.42
|   |   |   |   |   |   |   |   module_type = DDR2 : 0 (0/0)
|   |   |   |   |   |   |   |   module_type = DDR4
|   |   |   |   |   |   |   |   |   price_per_gb < 10.09
|   |   |   |   |   |   |   |   |   |   speed < 2933 : 301.6 (2/3.13)
|   |   |   |   |   |   |   |   |   |   speed >= 2933
|   |   |   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Corsair : 305.98 (1/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = G.Skill : 282.99 (1/0)
|   |   |   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 10.09 : 339.86 (1/0)
|   |   |   |   |   |   |   |   module_type = DDR3
|   |   |   |   |   |   |   |   |   error_correction = False : 325.2 (1/0)
|   |   |   |   |   |   |   |   |   error_correction = True : 298.64 (1/0)
|   |   |   |   |   |   price_per_gb >= 11.36
|   |   |   |   |   |   |   price_per_gb < 14.14
|   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   |   price_per_gb < 13.22 : 405.78 (2/5.09)
|   |   |   |   |   |   |   |   |   price_per_gb >= 13.22 : 438.04 (1/0)
|   |   |   |   |   |   |   |   brand = Crucial : 414.85 (1/0)
|   |   |   |   |   |   |   |   brand = G.Skill : 387.29 (1/0)
|   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Team : 412.71 (1/0)
|   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   price_per_gb >= 14.14
|   |   |   |   |   |   |   |   brand = ADATA : 504.3 (1/0)
|   |   |   |   |   |   |   |   brand = Corsair : 466.81 (1/0)
|   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   brand = G.Skill : 0 (0/0)
|   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   number_of_modules >= 3
|   |   |   module_size < 24
|   |   |   |   price_per_gb < 7
|   |   |   |   |   first_word_latency < 11.46
|   |   |   |   |   |   price_per_gb < 5.06
|   |   |   |   |   |   |   price_per_gb < 4.5
|   |   |   |   |   |   |   |   cas_timing < 15.5 : 287.12 (2/0.27)
|   |   |   |   |   |   |   |   cas_timing >= 15.5
|   |   |   |   |   |   |   |   |   price_per_gb < 4.17 : 257.1 (2/27.2)
|   |   |   |   |   |   |   |   |   price_per_gb >= 4.17 : 274.93 (5/12.33)
|   |   |   |   |   |   |   price_per_gb >= 4.5
|   |   |   |   |   |   |   |   speed < 2799.5
|   |   |   |   |   |   |   |   |   number_of_modules < 6
|   |   |   |   |   |   |   |   |   |   price_per_gb < 4.82 : 298.37 (1/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 4.82 : 319.01 (1/0)
|   |   |   |   |   |   |   |   |   number_of_modules >= 6 : 577.83 (1/0)
|   |   |   |   |   |   |   |   speed >= 2799.5
|   |   |   |   |   |   |   |   |   first_word_latency < 10.33
|   |   |   |   |   |   |   |   |   |   price_per_gb < 4.79 : 297.78 (5/34.07)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 4.79 : 315.89 (3/29.39)
|   |   |   |   |   |   |   |   |   first_word_latency >= 10.33 : 315.54 (2/0.31)
|   |   |   |   |   |   price_per_gb >= 5.06
|   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   price_per_gb < 5.55
|   |   |   |   |   |   |   |   |   price_per_gb < 5.23 : 653.54 (1/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 5.23 : 684.99 (1/0)
|   |   |   |   |   |   |   |   price_per_gb >= 5.55
|   |   |   |   |   |   |   |   |   number_of_modules < 6
|   |   |   |   |   |   |   |   |   |   price_per_gb < 6.34
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 5.9 : 367.5 (1/0)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 5.9 : 389 (2/0.99)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 6.34 : 423.44 (2/1.89)
|   |   |   |   |   |   |   |   |   number_of_modules >= 6
|   |   |   |   |   |   |   |   |   |   price_per_gb < 6.05 : 744.99 (1/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 6.05 : 803.89 (1/0)
|   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   first_word_latency < 11.08
|   |   |   |   |   |   |   |   |   price_per_gb < 5.94
|   |   |   |   |   |   |   |   |   |   first_word_latency < 10.33
|   |   |   |   |   |   |   |   |   |   |   cas_timing < 15.5 : 354.9 (1/0)
|   |   |   |   |   |   |   |   |   |   |   cas_timing >= 15.5 : 373.4 (1/0)
|   |   |   |   |   |   |   |   |   |   first_word_latency >= 10.33 : 359 (1/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 5.94
|   |   |   |   |   |   |   |   |   |   speed < 3233 : 389.7 (2/9)
|   |   |   |   |   |   |   |   |   |   speed >= 3233 : 407.53 (1/0)
|   |   |   |   |   |   |   |   first_word_latency >= 11.08 : 329.7 (1/0)
|   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   brand = Kingston
|   |   |   |   |   |   |   |   number_of_modules < 6
|   |   |   |   |   |   |   |   |   price_per_gb < 6.33
|   |   |   |   |   |   |   |   |   |   speed < 3166.5 : 379.95 (1/0)
|   |   |   |   |   |   |   |   |   |   speed >= 3166.5 : 336.16 (1/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 6.33 : 429.99 (1/0)
|   |   |   |   |   |   |   |   number_of_modules >= 6 : 749 (1/0)
|   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   first_word_latency >= 11.46
|   |   |   |   |   |   cas_timing < 14.5
|   |   |   |   |   |   |   price_per_gb < 3.55 : 214.13 (1/0)
|   |   |   |   |   |   |   price_per_gb >= 3.55
|   |   |   |   |   |   |   |   first_word_latency < 11.93 : 265.47 (1/0)
|   |   |   |   |   |   |   |   first_word_latency >= 11.93 : 239.99 (1/0)
|   |   |   |   |   |   cas_timing >= 14.5
|   |   |   |   |   |   |   number_of_modules < 6
|   |   |   |   |   |   |   |   price_per_gb < 5.05
|   |   |   |   |   |   |   |   |   price_per_gb < 4.24
|   |   |   |   |   |   |   |   |   |   price_per_gb < 3.58 : 213.32 (2/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 3.58 : 254.44 (4/38.08)
|   |   |   |   |   |   |   |   |   price_per_gb >= 4.24
|   |   |   |   |   |   |   |   |   |   first_word_latency < 13.42
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 4.56 : 284.8 (3/11.73)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 4.56 : 299.28 (2/24.8)
|   |   |   |   |   |   |   |   |   |   first_word_latency >= 13.42 : 300.95 (1/0)
|   |   |   |   |   |   |   |   price_per_gb >= 5.05
|   |   |   |   |   |   |   |   |   first_word_latency < 13.09
|   |   |   |   |   |   |   |   |   |   price_per_gb < 5.9 : 342.47 (1/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 5.9 : 412.98 (1/0)
|   |   |   |   |   |   |   |   |   first_word_latency >= 13.09 : 422.82 (1/0)
|   |   |   |   |   |   |   number_of_modules >= 6 : 611.09 (1/0)
|   |   |   |   price_per_gb >= 7
|   |   |   |   |   price_per_gb < 20.69
|   |   |   |   |   |   price_per_gb < 12.08
|   |   |   |   |   |   |   first_word_latency < 12.57
|   |   |   |   |   |   |   |   price_per_gb < 9.23
|   |   |   |   |   |   |   |   |   speed < 3100 : 1174.99 (1/0)
|   |   |   |   |   |   |   |   |   speed >= 3100
|   |   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   |   |   |   number_of_modules < 6
|   |   |   |   |   |   |   |   |   |   |   |   price_per_gb < 8.33 : 483.99 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 8.33 : 581.97 (1/0)
|   |   |   |   |   |   |   |   |   |   |   number_of_modules >= 6
|   |   |   |   |   |   |   |   |   |   |   |   price_per_gb < 7.89 : 933.02 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 7.89 : 1086.09 (1/0)
|   |   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = G.Skill : 473.49 (1/0)
|   |   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   price_per_gb >= 9.23
|   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   |   |   price_per_gb < 11.28
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 9.7 : 593.98 (1/0)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 9.7
|   |   |   |   |   |   |   |   |   |   |   |   price_per_gb < 10.41 : 647.38 (2/0.1)
|   |   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 10.41 : 685 (1/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 11.28 : 758.25 (1/0)
|   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   |   |   price_per_gb < 10.41 : 623.19 (1/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 10.41 : 709.25 (1/0)
|   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   first_word_latency >= 12.57 : 556.42 (1/0)
|   |   |   |   |   |   price_per_gb >= 12.08
|   |   |   |   |   |   |   price_per_gb < 13.53
|   |   |   |   |   |   |   |   price_per_gb < 12.46 : 787.48 (1/0)
|   |   |   |   |   |   |   |   price_per_gb >= 12.46 : 808.08 (2/0.53)
|   |   |   |   |   |   |   price_per_gb >= 13.53 : 922.99 (1/0)
|   |   |   |   |   price_per_gb >= 20.69 : 1725.25 (1/0)
|   |   |   module_size >= 24
|   |   |   |   price_per_gb < 4.64
|   |   |   |   |   price_per_gb < 4.25 : 524.99 (1/0)
|   |   |   |   |   price_per_gb >= 4.25
|   |   |   |   |   |   speed < 2833 : 585.04 (1/0)
|   |   |   |   |   |   speed >= 2833 : 567.49 (2/11.09)
|   |   |   |   price_per_gb >= 4.64
|   |   |   |   |   price_per_gb < 4.79 : 1215.34 (4/38.93)
|   |   |   |   |   price_per_gb >= 4.79
|   |   |   |   |   |   price_per_gb < 4.97
|   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   brand = Corsair : 629.99 (1/0)
|   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   brand = G.Skill : 0 (0/0)
|   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   brand = Kingston : 615 (1/0)
|   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   price_per_gb >= 4.97
|   |   |   |   |   |   |   first_word_latency < 11.34
|   |   |   |   |   |   |   |   number_of_modules < 6
|   |   |   |   |   |   |   |   |   price_per_gb < 6.48
|   |   |   |   |   |   |   |   |   |   price_per_gb < 5.89
|   |   |   |   |   |   |   |   |   |   |   first_word_latency < 10.33 : 740.6 (1/0)
|   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 10.33 : 721.05 (1/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 5.89
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 6.11 : 766.57 (1/0)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 6.11 : 797.78 (2/1.93)
|   |   |   |   |   |   |   |   |   price_per_gb >= 6.48
|   |   |   |   |   |   |   |   |   |   price_per_gb < 7.1 : 859.99 (1/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 7.1 : 956.52 (1/0)
|   |   |   |   |   |   |   |   number_of_modules >= 6
|   |   |   |   |   |   |   |   |   first_word_latency < 10.33 : 1500.47 (1/0)
|   |   |   |   |   |   |   |   |   first_word_latency >= 10.33 : 1284.24 (1/0)
|   |   |   |   |   |   |   first_word_latency >= 11.34
|   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Corsair : 702.93 (1/0)
|   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   brand = G.Skill : 804.83 (1/0)
|   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   speed >= 3533
|   |   number_of_modules < 6
|   |   |   price_per_gb < 11.74
|   |   |   |   module_size < 24
|   |   |   |   |   first_word_latency < 9.21
|   |   |   |   |   |   first_word_latency < 8.94
|   |   |   |   |   |   |   number_of_modules < 3
|   |   |   |   |   |   |   |   price_per_gb < 7.37
|   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Corsair : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Crucial : 183.91 (2/0)
|   |   |   |   |   |   |   |   |   brand = G.Skill : 211.05 (1/0)
|   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   price_per_gb >= 7.37
|   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Corsair : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Crucial : 293.92 (1/0)
|   |   |   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   |   |   speed < 3800
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 10.44
|   |   |   |   |   |   |   |   |   |   |   |   price_per_gb < 8.57 : 260.82 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 8.57
|   |   |   |   |   |   |   |   |   |   |   |   |   price_per_gb < 9.49 : 292.33 (2/21.72)
|   |   |   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 9.49 : 310.2 (1/0)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 10.44 : 358.16 (1/0)
|   |   |   |   |   |   |   |   |   |   speed >= 3800 : 368.16 (1/0)
|   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   number_of_modules >= 3
|   |   |   |   |   |   |   |   price_per_gb < 8.31
|   |   |   |   |   |   |   |   |   price_per_gb < 6.68
|   |   |   |   |   |   |   |   |   |   price_per_gb < 6.18 : 384.42 (1/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 6.18 : 406.18 (1/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 6.68 : 448.2 (1/0)
|   |   |   |   |   |   |   |   price_per_gb >= 8.31
|   |   |   |   |   |   |   |   |   price_per_gb < 9.93 : 615.92 (2/0.42)
|   |   |   |   |   |   |   |   |   price_per_gb >= 9.93 : 654 (1/0)
|   |   |   |   |   |   first_word_latency >= 8.94 : 355.8 (3/43.05)
|   |   |   |   |   first_word_latency >= 9.21
|   |   |   |   |   |   number_of_modules < 3
|   |   |   |   |   |   |   price_per_gb < 7.58
|   |   |   |   |   |   |   |   brand = ADATA : 209.9 (1/0)
|   |   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   |   price_per_gb < 5.97
|   |   |   |   |   |   |   |   |   |   number_of_modules < 1.5 : 92.99 (1/0)
|   |   |   |   |   |   |   |   |   |   number_of_modules >= 1.5
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 5.17 : 149.99 (1/0)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 5.17 : 181 (1/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 5.97
|   |   |   |   |   |   |   |   |   |   price_per_gb < 6.57 : 195.9 (1/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 6.57 : 224.73 (1/0)
|   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   |   first_word_latency < 9.72
|   |   |   |   |   |   |   |   |   |   price_per_gb < 5.68 : 171.22 (1/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 5.68 : 192.27 (1/0)
|   |   |   |   |   |   |   |   |   first_word_latency >= 9.72
|   |   |   |   |   |   |   |   |   |   price_per_gb < 5.43 : 150.7 (2/0.05)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 5.43
|   |   |   |   |   |   |   |   |   |   |   price_per_gb < 6.61 : 202.29 (2/30.47)
|   |   |   |   |   |   |   |   |   |   |   price_per_gb >= 6.61 : 218.84 (2/14.9)
|   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Kingston : 202.84 (1/0)
|   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Thermaltake : 199.89 (1/0)
|   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   price_per_gb >= 7.58
|   |   |   |   |   |   |   |   first_word_latency < 9.41 : 260.13 (1/0)
|   |   |   |   |   |   |   |   first_word_latency >= 9.41
|   |   |   |   |   |   |   |   |   speed < 3800 : 289.74 (2/22.56)
|   |   |   |   |   |   |   |   |   speed >= 3800
|   |   |   |   |   |   |   |   |   |   price_per_gb < 10.43 : 317.5 (1/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 10.43 : 349.99 (1/0)
|   |   |   |   |   |   number_of_modules >= 3
|   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   price_per_gb < 7.84
|   |   |   |   |   |   |   |   |   price_per_gb < 5.83 : 319.99 (1/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 5.83 : 426.57 (1/0)
|   |   |   |   |   |   |   |   price_per_gb >= 7.84
|   |   |   |   |   |   |   |   |   price_per_gb < 9.58 : 576.88 (1/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 9.58 : 654.94 (2/25.45)
|   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   price_per_gb < 6.6
|   |   |   |   |   |   |   |   |   first_word_latency < 9.72 : 395.03 (2/1.45)
|   |   |   |   |   |   |   |   |   first_word_latency >= 9.72
|   |   |   |   |   |   |   |   |   |   price_per_gb < 5.3 : 323.89 (1/0)
|   |   |   |   |   |   |   |   |   |   price_per_gb >= 5.3 : 354.03 (1/0)
|   |   |   |   |   |   |   |   price_per_gb >= 6.6
|   |   |   |   |   |   |   |   |   price_per_gb < 7.22 : 449.06 (1/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 7.22 : 475.17 (1/0)
|   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   module_size >= 24
|   |   |   |   |   number_of_modules < 3
|   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   price_per_gb < 5.2 : 315.98 (1/0)
|   |   |   |   |   |   |   price_per_gb >= 5.2 : 350.22 (1/0)
|   |   |   |   |   |   brand = Crucial : 367.83 (1/0)
|   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   price_per_gb < 6.8
|   |   |   |   |   |   |   |   price_per_gb < 5.46 : 297.14 (1/0)
|   |   |   |   |   |   |   |   price_per_gb >= 5.46 : 402.38 (1/0)
|   |   |   |   |   |   |   price_per_gb >= 6.8
|   |   |   |   |   |   |   |   price_per_gb < 7.91 : 467.92 (2/0)
|   |   |   |   |   |   |   |   price_per_gb >= 7.91 : 544.99 (1/0)
|   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   brand = Patriot : 250.89 (2/0)
|   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   brand = Thermaltake : 362.99 (1/0)
|   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   number_of_modules >= 3
|   |   |   |   |   |   price_per_gb < 5.75
|   |   |   |   |   |   |   price_per_gb < 5.17 : 619.87 (1/0)
|   |   |   |   |   |   |   price_per_gb >= 5.17 : 708.69 (2/37.88)
|   |   |   |   |   |   price_per_gb >= 5.75
|   |   |   |   |   |   |   speed < 3800 : 757.31 (1/0)
|   |   |   |   |   |   |   speed >= 3800
|   |   |   |   |   |   |   |   price_per_gb < 6.48 : 790.33 (1/0)
|   |   |   |   |   |   |   |   price_per_gb >= 6.48 : 867.64 (1/0)
|   |   |   price_per_gb >= 11.74
|   |   |   |   number_of_modules < 3
|   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   brand = Corsair
|   |   |   |   |   |   module_size < 24
|   |   |   |   |   |   |   price_per_gb < 19.66 : 574.99 (1/0)
|   |   |   |   |   |   |   price_per_gb >= 19.66 : 683.3 (1/0)
|   |   |   |   |   |   module_size >= 24 : 765.9 (1/0)
|   |   |   |   |   brand = Crucial
|   |   |   |   |   |   price_per_gb < 14.25 : 416.01 (1/0)
|   |   |   |   |   |   price_per_gb >= 14.25 : 496 (1/0)
|   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   speed < 3800
|   |   |   |   |   |   |   price_per_gb < 15.07 : 465.27 (1/0)
|   |   |   |   |   |   |   price_per_gb >= 15.07 : 498.99 (1/0)
|   |   |   |   |   |   speed >= 3800
|   |   |   |   |   |   |   first_word_latency < 8.75 : 406.24 (1/0)
|   |   |   |   |   |   |   first_word_latency >= 8.75
|   |   |   |   |   |   |   |   price_per_gb < 15.03 : 417.4 (1/0)
|   |   |   |   |   |   |   |   price_per_gb >= 15.03 : 544.19 (1/0)
|   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   number_of_modules >= 3
|   |   |   |   |   speed < 3866.5
|   |   |   |   |   |   price_per_gb < 16.48
|   |   |   |   |   |   |   first_word_latency < 9
|   |   |   |   |   |   |   |   price_per_gb < 14.48 : 904.43 (1/0)
|   |   |   |   |   |   |   |   price_per_gb >= 14.48 : 948.63 (1/0)
|   |   |   |   |   |   |   first_word_latency >= 9
|   |   |   |   |   |   |   |   cas_timing < 17.5 : 824.99 (1/0)
|   |   |   |   |   |   |   |   cas_timing >= 17.5
|   |   |   |   |   |   |   |   |   price_per_gb < 12.26 : 767.59 (1/0)
|   |   |   |   |   |   |   |   |   price_per_gb >= 12.26 : 802.1 (1/0)
|   |   |   |   |   |   price_per_gb >= 16.48 : 1161 (1/0)
|   |   |   |   |   speed >= 3866.5
|   |   |   |   |   |   price_per_gb < 13.79 : 1691.57 (1/0)
|   |   |   |   |   |   price_per_gb >= 13.79 : 1837.39 (1/0)
|   |   number_of_modules >= 6
|   |   |   price_per_gb < 18.42
|   |   |   |   speed < 3700
|   |   |   |   |   module_size < 24
|   |   |   |   |   |   price_per_gb < 12.5 : 1549.22 (1/0)
|   |   |   |   |   |   price_per_gb >= 12.5 : 1651.99 (1/0)
|   |   |   |   |   module_size >= 24
|   |   |   |   |   |   price_per_gb < 5.85 : 1422.6 (2/0.82)
|   |   |   |   |   |   price_per_gb >= 5.85
|   |   |   |   |   |   |   price_per_gb < 6.66
|   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Corsair : 1614.99 (1/0)
|   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   brand = G.Skill : 1570.3 (1/0)
|   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   price_per_gb >= 6.66 : 1793.7 (1/0)
|   |   |   |   speed >= 3700 : 1890.76 (1/0)
|   |   |   price_per_gb >= 18.42 : 2823.99 (1/0)

Size of the tree : 2142

Time taken to build model: 0.01 seconds

=== Cross-validation ===
=== Summary ===

Correlation coefficient                  0.9267
Mean absolute error                     31.9919
Root mean squared error                 86.2286
Relative absolute error                 23.5325 %
Root relative squared error             38.1403 %
Total Number of Instances             1791     
```

</details>

Vediamo se si può migliorare ulteriormente.

#### RandomForest

- Risultati del testing:</br>
  | Coefficiente di correlazione | Errore medio assoluto | Errore quadratico medio | Errore assoluto relativo | Errore quadratico relativo |
  |------------------------------|-----------------------|-------------------------|--------------------------|----------------------------|
  | 0.9708                       | 21.6589               | 58.0235                 | 15.9318%                 | 25.6647%                   |

- Visualizzazione dei risultati:</br>
  ![RandomForestResult](https://imgur.com/u0Xu3RU.png)

<details>
<summary>Output completo del risultato migliore</summary>

```text
=== Run information ===

Scheme:       weka.classifiers.trees.RandomForest -P 100 -I 100 -num-slots 1 -K 0 -M 1.0 -V 0.001 -S 1
Relation:     memory-weka.filters.unsupervised.attribute.Remove-R2-weka.filters.unsupervised.attribute.Remove-R7
Instances:    1791
Attributes:   10
              brand
              module_type
              speed
              number_of_modules
              module_size
              price_per_gb
              first_word_latency
              cas_timing
              error_correction
              price
Test mode:    10-fold cross-validation

=== Classifier model (full training set) ===

RandomForest

Bagging with 100 iterations and base learner

weka.classifiers.trees.RandomTree -K 0 -M 1.0 -V 0.001 -S 1 -do-not-check-capabilities

Time taken to build model: 0.31 seconds

=== Cross-validation ===
=== Summary ===

Correlation coefficient                  0.9708
Mean absolute error                     21.6589
Root mean squared error                 58.0235
Relative absolute error                 15.9318 %
Root relative squared error             25.6647 %
Total Number of Instances             1791     

```

</details>

Il RandomForest ha migliorato ulteriormente la performance, riducendo di molto l'errore quadratico medio.

Possiamo fare ancora di meglio?

#### M5P

L'algoritmo ha presentato risultati migliori se si effettuava il pruning. I risultati seguenti sono con pruning.

- Albero risultante:</br>
  ![M5PResult](https://imgur.com/8MQoerL.png)

- Risultati del testing:</br>
  | Coefficiente di correlazione | Errore medio assoluto | Errore quadratico medio | Errore assoluto relativo | Errore quadratico relativo |
  |------------------------------|-----------------------|-------------------------|--------------------------|----------------------------|
  | 0.9881                       | 11.5444               | 36.0696                 | 8.4918%                  | 15.9541%                   |

- Visualizzazione dei risultati:</br>
  ![M5PResult](https://imgur.com/cWWzMSG.png)

<details>
<summary>Output completo del risultato migliore</summary>

```text
=== Run information ===

Scheme:       weka.classifiers.trees.M5P -M 4.0 -num-decimal-places 4
Relation:     memory-weka.filters.unsupervised.attribute.Remove-R2-weka.filters.unsupervised.attribute.Remove-R7
Instances:    1791
Attributes:   10
              brand
              module_type
              speed
              number_of_modules
              module_size
              price_per_gb
              first_word_latency
              cas_timing
              error_correction
              price
Test mode:    10-fold cross-validation

=== Classifier model (full training set) ===

M5 pruned model tree:
(using smoothed linear models)

number_of_modules <= 2.5 : 
|   price_per_gb <= 10.883 : 
|   |   module_size <= 12 : 
|   |   |   number_of_modules <= 1.5 : LM1 (292/4.096%)
|   |   |   number_of_modules >  1.5 : 
|   |   |   |   module_size <= 6 : LM2 (133/0.719%)
|   |   |   |   module_size >  6 : LM3 (392/0.002%)
|   |   module_size >  12 : 
|   |   |   price_per_gb <= 5.469 : 
|   |   |   |   module_size <= 24 : LM4 (148/1.865%)
|   |   |   |   module_size >  24 : LM5 (38/11.524%)
|   |   |   price_per_gb >  5.469 : 
|   |   |   |   number_of_modules <= 1.5 : 
|   |   |   |   |   module_size <= 24 : LM6 (33/0.002%)
|   |   |   |   |   module_size >  24 : 
|   |   |   |   |   |   price_per_gb <= 7.303 : LM7 (6/0.004%)
|   |   |   |   |   |   price_per_gb >  7.303 : 
|   |   |   |   |   |   |   speed <= 2533 : LM8 (4/3.106%)
|   |   |   |   |   |   |   speed >  2533 : LM9 (2/12.536%)
|   |   |   |   number_of_modules >  1.5 : 
|   |   |   |   |   price_per_gb <= 8.618 : 
|   |   |   |   |   |   speed <= 3100 : LM10 (26/0.003%)
|   |   |   |   |   |   speed >  3100 : 
|   |   |   |   |   |   |   module_size <= 24 : LM11 (36/0.004%)
|   |   |   |   |   |   |   module_size >  24 : LM12 (13/0.009%)
|   |   |   |   |   price_per_gb >  8.618 : LM13 (20/0.004%)
|   price_per_gb >  10.883 : 
|   |   module_size <= 6 : 
|   |   |   price_per_gb <= 21.623 : 
|   |   |   |   module_size <= 3 : LM14 (39/1.638%)
|   |   |   |   module_size >  3 : 
|   |   |   |   |   number_of_modules <= 1.5 : LM15 (20/0.001%)
|   |   |   |   |   number_of_modules >  1.5 : LM16 (36/0.001%)
|   |   |   price_per_gb >  21.623 : 
|   |   |   |   module_size <= 3 : LM17 (11/4.145%)
|   |   |   |   module_size >  3 : LM18 (13/8.05%)
|   |   module_size >  6 : 
|   |   |   price_per_gb <= 18.926 : 
|   |   |   |   module_size <= 12 : 
|   |   |   |   |   number_of_modules <= 1.5 : LM19 (19/0.001%)
|   |   |   |   |   number_of_modules >  1.5 : LM20 (75/0.002%)
|   |   |   |   module_size >  12 : LM21 (39/11.206%)
|   |   |   price_per_gb >  18.926 : LM22 (34/10.306%)
number_of_modules >  2.5 : 
|   module_size <= 12 : 
|   |   price_per_gb <= 9.118 : LM23 (121/4.78%)
|   |   price_per_gb >  9.118 : 
|   |   |   module_size <= 6 : 
|   |   |   |   module_size <= 3 : LM24 (11/14.765%)
|   |   |   |   module_size >  3 : LM25 (25/10.247%)
|   |   |   module_size >  6 : 
|   |   |   |   number_of_modules <= 6 : LM26 (51/8.081%)
|   |   |   |   number_of_modules >  6 : LM27 (13/0.008%)
|   module_size >  12 : 
|   |   price_per_gb <= 4.708 : LM28 (32/21.98%)
|   |   price_per_gb >  4.708 : 
|   |   |   number_of_modules <= 6 : 
|   |   |   |   price_per_gb <= 10.187 : 
|   |   |   |   |   module_size <= 24 : LM29 (50/0.009%)
|   |   |   |   |   module_size >  24 : LM30 (17/0.017%)
|   |   |   |   price_per_gb >  10.187 : 
|   |   |   |   |   price_per_gb <= 13.053 : LM31 (11/0.009%)
|   |   |   |   |   price_per_gb >  13.053 : 
|   |   |   |   |   |   speed <= 3800 : LM32 (5/0.007%)
|   |   |   |   |   |   speed >  3800 : LM33 (2/32.262%)
|   |   |   number_of_modules >  6 : LM34 (24/24.034%)

LM num: 1
price = 
	0.1488 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
	+ 0.2694 * brand=Corsair,Gigabyte 
	- 0.6963 * module_type=DDR4 
	+ 0.0041 * speed 
	+ 4.7795 * number_of_modules 
	+ 5.689 * module_size 
	+ 1.0836 * price_per_gb 
	+ 0.7543 * first_word_latency 
	- 0.528 * cas_timing 
	- 19.531

LM num: 2
price = 
	0.1488 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
	+ 0.2694 * brand=Corsair,Gigabyte 
	- 0.6963 * module_type=DDR4 
	+ 0.0041 * speed 
	+ 3.9104 * number_of_modules 
	+ 16.2681 * module_size 
	+ 8.611 * price_per_gb 
	+ 0.7543 * first_word_latency 
	- 0.528 * cas_timing 
	- 87.8072

LM num: 3
price = 
	0.1488 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
	+ 0.2694 * brand=Corsair,Gigabyte 
	- 0.6963 * module_type=DDR4 
	+ 0.0041 * speed 
	+ 3.9104 * number_of_modules 
	+ 1.2156 * module_size 
	+ 15.699 * price_per_gb 
	+ 0.7543 * first_word_latency 
	- 0.528 * cas_timing 
	- 26.7795

LM num: 4
price = 
	0.2378 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
	+ 0.2694 * brand=Corsair,Gigabyte 
	- 0.6963 * module_type=DDR4 
	+ 0.005 * speed 
	+ 74.4974 * number_of_modules 
	+ 1.6869 * module_size 
	+ 24.6292 * price_per_gb 
	+ 0.7543 * first_word_latency 
	- 0.528 * cas_timing 
	- 154.8069

LM num: 5
price = 
	0.2378 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
	+ 0.2694 * brand=Corsair,Gigabyte 
	- 0.6963 * module_type=DDR4 
	+ 0.005 * speed 
	+ 113.6714 * number_of_modules 
	+ 2.7819 * module_size 
	+ 43.6769 * price_per_gb 
	+ 0.7543 * first_word_latency 
	- 0.528 * cas_timing 
	- 247.4403

LM num: 6
price = 
	0.2378 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
	+ 0.2694 * brand=Corsair,Gigabyte 
	- 0.6963 * module_type=DDR4 
	+ 0.0094 * speed 
	+ 38.7671 * number_of_modules 
	+ 4.7815 * module_size 
	+ 20.8216 * price_per_gb 
	+ 0.7543 * first_word_latency 
	- 0.528 * cas_timing 
	- 174.6024

LM num: 7
price = 
	-20.912 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
	+ 0.2694 * brand=Corsair,Gigabyte 
	- 0.6963 * module_type=DDR4 
	+ 0.0415 * speed 
	+ 38.7671 * number_of_modules 
	+ 5.9852 * module_size 
	+ 33.0941 * price_per_gb 
	+ 0.7543 * first_word_latency 
	- 0.528 * cas_timing 
	- 316.996

LM num: 8
price = 
	-20.3311 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
	+ 0.2694 * brand=Corsair,Gigabyte 
	- 0.6963 * module_type=DDR4 
	+ 0.0547 * speed 
	+ 38.7671 * number_of_modules 
	+ 5.9852 * module_size 
	+ 30.5237 * price_per_gb 
	+ 0.7543 * first_word_latency 
	- 0.528 * cas_timing 
	- 319.5833

LM num: 9
price = 
	-20.912 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
	+ 0.2694 * brand=Corsair,Gigabyte 
	- 0.6963 * module_type=DDR4 
	+ 0.0562 * speed 
	+ 38.7671 * number_of_modules 
	+ 5.9852 * module_size 
	+ 30.5237 * price_per_gb 
	+ 0.7543 * first_word_latency 
	- 0.528 * cas_timing 
	- 320.2058

LM num: 10
price = 
	0.2378 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
	+ 0.2694 * brand=Corsair,Gigabyte 
	- 0.6963 * module_type=DDR4 
	+ 0.0066 * speed 
	+ 27.0611 * number_of_modules 
	+ 6.6457 * module_size 
	+ 31.7974 * price_per_gb 
	+ 0.7543 * first_word_latency 
	- 0.528 * cas_timing 
	- 178.8676

LM num: 11
price = 
	0.2378 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
	+ 0.2694 * brand=Corsair,Gigabyte 
	- 0.6963 * module_type=DDR4 
	+ 0.0066 * speed 
	+ 27.0611 * number_of_modules 
	+ 7.385 * module_size 
	+ 32.7385 * price_per_gb 
	+ 0.7543 * first_word_latency 
	- 0.528 * cas_timing 
	- 197.1231

LM num: 12
price = 
	0.2378 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
	+ 0.2694 * brand=Corsair,Gigabyte 
	- 0.6963 * module_type=DDR4 
	+ 0.0066 * speed 
	+ 27.0611 * number_of_modules 
	+ 8.811 * module_size 
	+ 40.888 * price_per_gb 
	+ 0.7543 * first_word_latency 
	- 0.528 * cas_timing 
	- 228.3235

LM num: 13
price = 
	0.2378 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
	+ 0.2694 * brand=Corsair,Gigabyte 
	- 0.6963 * module_type=DDR4 
	+ 0.0066 * speed 
	+ 27.0611 * number_of_modules 
	+ 6.2443 * module_size 
	+ 30.6004 * price_per_gb 
	+ 0.7543 * first_word_latency 
	- 0.528 * cas_timing 
	- 164.8208

LM num: 14
price = 
	0.3344 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
	+ 4.1161 * brand=Corsair,Gigabyte 
	- 0.6963 * module_type=DDR4 
	+ 0.0216 * speed 
	+ 42.4659 * number_of_modules 
	+ 24.9761 * module_size 
	+ 4.96 * price_per_gb 
	+ 3.7962 * first_word_latency 
	- 2.9577 * cas_timing 
	- 197.032

LM num: 15
price = 
	0.3344 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
	+ 4.1161 * brand=Corsair,Gigabyte 
	- 0.6963 * module_type=DDR4 
	+ 0.0216 * speed 
	+ 39.6718 * number_of_modules 
	+ 9.5141 * module_size 
	+ 6.0183 * price_per_gb 
	+ 3.7962 * first_word_latency 
	- 2.9577 * cas_timing 
	- 162.0718

LM num: 16
price = 
	0.3344 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
	+ 4.1161 * brand=Corsair,Gigabyte 
	- 0.6963 * module_type=DDR4 
	+ 0.0216 * speed 
	+ 35.2397 * number_of_modules 
	+ 9.5141 * module_size 
	+ 7.4409 * price_per_gb 
	+ 3.7962 * first_word_latency 
	- 2.9577 * cas_timing 
	- 155.0012

LM num: 17
price = 
	0.3344 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
	+ 4.1161 * brand=Corsair,Gigabyte 
	- 0.6963 * module_type=DDR4 
	+ 0.0216 * speed 
	+ 66.1785 * number_of_modules 
	+ 20.2228 * module_size 
	+ 6.1677 * price_per_gb 
	+ 3.7962 * first_word_latency 
	- 2.9577 * cas_timing 
	- 252.2435

LM num: 18
price = 
	0.3344 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
	+ 4.1161 * brand=Corsair,Gigabyte 
	- 0.6963 * module_type=DDR4 
	+ 0.0216 * speed 
	+ 85.7293 * number_of_modules 
	+ 19.6053 * module_size 
	+ 7.8766 * price_per_gb 
	+ 3.7962 * first_word_latency 
	- 2.9577 * cas_timing 
	- 305.8893

LM num: 19
price = 
	0.3344 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
	+ 5.6568 * brand=Corsair,Gigabyte 
	- 0.6963 * module_type=DDR4 
	+ 0.0168 * speed 
	+ 74.4671 * number_of_modules 
	+ 5.9021 * module_size 
	+ 11.952 * price_per_gb 
	+ 2.6849 * first_word_latency 
	- 2.1759 * cas_timing 
	- 226.8823

LM num: 20
price = 
	0.3344 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
	+ 5.6568 * brand=Corsair,Gigabyte 
	- 0.6963 * module_type=DDR4 
	+ 0.0168 * speed 
	+ 53.0781 * number_of_modules 
	+ 5.9021 * module_size 
	+ 15.1838 * price_per_gb 
	+ 2.6849 * first_word_latency 
	- 2.1759 * cas_timing 
	- 187.4139

LM num: 21
price = 
	0.3344 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
	+ 7.0442 * brand=Corsair,Gigabyte 
	- 0.6963 * module_type=DDR4 
	+ 0.0168 * speed 
	+ 189.0597 * number_of_modules 
	+ 17.2787 * module_size 
	+ 21.1545 * price_per_gb 
	+ 2.6849 * first_word_latency 
	- 2.1759 * cas_timing 
	- 580.9767

LM num: 22
price = 
	0.3344 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
	+ 6.6107 * brand=Corsair,Gigabyte 
	- 0.6963 * module_type=DDR4 
	+ 0.0138 * speed 
	+ 174.4256 * number_of_modules 
	+ 24.8485 * module_size 
	+ 14.8361 * price_per_gb 
	+ 1.5947 * first_word_latency 
	- 1.4981 * cas_timing 
	- 560.1475

LM num: 23
price = 
	9.0102 * brand=Corsair,Gigabyte 
	- 2.6671 * module_type=DDR4 
	+ 0.082 * speed 
	+ 55.3806 * number_of_modules 
	+ 27.6585 * module_size 
	+ 28.0194 * price_per_gb 
	+ 19.6662 * first_word_latency 
	- 12.6186 * cas_timing 
	- 669.1195

LM num: 24
price = 
	10.7688 * brand=Corsair,Gigabyte 
	- 2.6671 * module_type=DDR4 
	+ 0.1416 * speed 
	+ 46.5465 * number_of_modules 
	+ 39.51 * module_size 
	+ 13.1701 * price_per_gb 
	+ 35.953 * first_word_latency 
	- 21.8778 * cas_timing 
	- 793.8814

LM num: 25
price = 
	8.5443 * brand=Corsair,Gigabyte 
	- 2.6671 * module_type=DDR4 
	+ 0.1441 * speed 
	+ 46.5465 * number_of_modules 
	+ 32.1377 * module_size 
	+ 15.8718 * price_per_gb 
	+ 35.953 * first_word_latency 
	- 22.4966 * cas_timing 
	- 763.0183

LM num: 26
price = 
	4.413 * brand=Corsair,Gigabyte 
	- 2.6671 * module_type=DDR4 
	+ 0.1265 * speed 
	+ 53.1828 * number_of_modules 
	+ 14.324 * module_size 
	+ 28.9225 * price_per_gb 
	+ 27.633 * first_word_latency 
	- 20.127 * cas_timing 
	- 657.6971

LM num: 27
price = 
	4.413 * brand=Corsair,Gigabyte 
	- 2.6671 * module_type=DDR4 
	+ 0.1265 * speed 
	+ 73.5548 * number_of_modules 
	+ 14.324 * module_size 
	+ 39.5852 * price_per_gb 
	+ 30.3304 * first_word_latency 
	- 20.127 * cas_timing 
	- 781.341

LM num: 28
price = 
	3.4586 * brand=Corsair,Gigabyte 
	- 2.6671 * module_type=DDR4 
	+ 0.0757 * speed 
	+ 56.3497 * number_of_modules 
	+ 20.557 * module_size 
	+ 75.3036 * price_per_gb 
	+ 14.1946 * first_word_latency 
	- 9.4533 * cas_timing 
	- 826.4525

LM num: 29
price = 
	3.4586 * brand=Corsair,Gigabyte 
	- 2.6671 * module_type=DDR4 
	+ 0.0823 * speed 
	+ 48.506 * number_of_modules 
	+ 15.4749 * module_size 
	+ 63.3642 * price_per_gb 
	+ 14.1946 * first_word_latency 
	- 9.4533 * cas_timing 
	- 696.6788

LM num: 30
price = 
	3.4586 * brand=Corsair,Gigabyte 
	- 2.6671 * module_type=DDR4 
	+ 0.0823 * speed 
	+ 48.506 * number_of_modules 
	+ 18.4727 * module_size 
	+ 81.84 * price_per_gb 
	+ 14.1946 * first_word_latency 
	- 9.4533 * cas_timing 
	- 747.6965

LM num: 31
price = 
	3.4586 * brand=Corsair,Gigabyte 
	- 2.6671 * module_type=DDR4 
	+ 0.1738 * speed 
	+ 48.506 * number_of_modules 
	+ 17.3483 * module_size 
	+ 64.1732 * price_per_gb 
	+ 14.1946 * first_word_latency 
	- 9.4533 * cas_timing 
	- 1025.9906

LM num: 32
price = 
	3.4586 * brand=Corsair,Gigabyte 
	- 2.6671 * module_type=DDR4 
	+ 0.2918 * speed 
	+ 48.506 * number_of_modules 
	+ 17.3483 * module_size 
	+ 65.3092 * price_per_gb 
	+ 14.1946 * first_word_latency 
	- 9.4533 * cas_timing 
	- 1441.4778

LM num: 33
price = 
	3.4586 * brand=Corsair,Gigabyte 
	- 2.6671 * module_type=DDR4 
	+ 0.3099 * speed 
	+ 48.506 * number_of_modules 
	+ 17.3483 * module_size 
	+ 64.6559 * price_per_gb 
	+ 14.1946 * first_word_latency 
	- 9.4533 * cas_timing 
	- 1482.7493

LM num: 34
price = 
	3.4586 * brand=Corsair,Gigabyte 
	- 2.6671 * module_type=DDR4 
	+ 0.1572 * speed 
	+ 75.6196 * number_of_modules 
	+ 34.1828 * module_size 
	+ 92.0395 * price_per_gb 
	+ 14.1946 * first_word_latency 
	- 9.4533 * cas_timing 
	- 1333.6165

Number of Rules : 34

Time taken to build model: 0.11 seconds

=== Cross-validation ===
=== Summary ===

Correlation coefficient                  0.9881
Mean absolute error                     11.5444
Root mean squared error                 36.0696
Relative absolute error                  8.4918 %
Root relative squared error             15.9541 %
Total Number of Instances             1791     

```

</details>

Possiamo dire che l'algoritmo M5P ha costruito un ottimo albero e con ottime prestazioni.

### Scelta dell'algoritmo

Mettiamo a confronto le performance dei vari algoritmi usati

| Algoritmo         | Coefficiente di correlazione | Errore medio assoluto | Errore quadratico medio | Errore assoluto relativo | Errore quadratico relativo |
|-------------------|------------------------------|-----------------------|-------------------------|--------------------------|----------------------------|
| Linear Regression | 0.8835                       | 59.5598               | 105.8831                | 43.8108%                 | 46.8338%                   |
| Random Tree       | 0.9267                       | 31.9919               | 86.2286                 | 23.5325%                 | 38.1403%                   |
| Random Forest     | 0.9708                       | 21.6589               | 58.0235                 | 15.9318%                 | 25.6647%                   |
| M5P               | 0.9881                       | 11.5444               | 36.0696                 | 8.4918%                  | 15.9541%                   |

Dato che si è scelto di usare python come linguaggio, purtroppo non possiamo fare affidamento all' `M5P` dato che la sua implementazione in scikit-learn è ancora in fase di developement. Non resta che scegliere il Random Forest.

## Implementazione
