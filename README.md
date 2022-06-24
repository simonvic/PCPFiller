
# PCPFiller

PCPFiller (PC Part Picker Filler) è una utility CLI destinata al preprocessing di dataset.

Trovare datasets di componenti hardware per PC per un qualsiasi progetto di ML/DL, che siano decenti e reperibili gratuitamente non è per niente facile; la maggior parte dei datasets disponibili online sono incompleti, molto obsoleti, troppo piccoli per il training di una AI e spesso anche incorretti.

PCPFiller is offre di costruire e fornire datasets completi e di dimensioni sufficienti per allenare una AI.

## Quick start

PCPFiller consiste in uno script python (`PCPFetcher.py`) il quale scaricherà dei dataset base, i quali saranno poi utilizzati dall'utility Java che si occuperà del "filling" dei dati.

Per una list di comandi e opzioni di `PCPFetcher.py`, consultare la help page con il seguente comando

```bash
PCPFetcher.py --help
```

O per java

```bash
java -jar ./PCPFiller.jar --help
```

> **NOTA**: per semplicità, da ora in poi il modulo Java sarà invocato con il comando `PCPFiller [opzioni]`

## Come funziona

### Fase di download list component

Per prima cosa, PCPFiller scaricherà una lista di componenti hardware dal noto sito PCPartPicker.com.
In tale lista sono inclusi componenti principali, quali CPU, GPU, RAM, mouse, monitor, HDD, e altre periferiche secondarie.

Per scaricare i componenti, eseguire il seguente comando

```bash
PCPFetcher.py --fetch <tipo componente>
```

Per sapere la lista dei componenti attualmente supportati, eseguire il seguente comando

```bash
PCPFetcher.py --supported-parts
```

Il download dei componenti è possibile grazie alle ottime API per python fornite da PCPartPicker.com stesso (<https://pypi.org/project/pcpartpicker>), ottenibili con `pip`.

**NOTA**: quando si scaricano i componenti, assicurarsi di settare la `region` correttamente.

```bash
PCPFetcher.py --fetch <tipo componente> --region <regione>
```

Per sapere la lista delle regioni attualmente supportate, eseguire il seguente comando

```bash
PCPFetcher.py --supported-regions
```

### Fase di parsing e conversione

PCPFiller (modulo Java) si occpuerà poi di fare il parsing di tali componenti, manipolando alcuni dati per facilitare l'elaborazione per poi convertirli in formato CSV, in modo da renderli utilizzabili da altri strumenti come Weka per eventuale analisi dati, e con lo scopo di poter procedere al "filling" di eventuali dati mancanti.

**NOTA**: le directory di input/output di PCPFiller sono di default `./parts` e `./parts/formatted`. Tali percorsi possono essere cambiati; consultare la help page.

### Fase di filling

Il modulo Java poi si occuperà del filling dei dati, attraverso un modello di ML, che può essere allenato e salvato...

```bash
PCPFiller --part "pcpart" --from-json "pcpart.json" --save-model "pcpart.model"
```

... oppure essere caricato

```bash
PCPFiller --part "pcpart" --from-json "pcpart.json" --load-model "pcpart.model"
```

Il dataset di ouput potrà essere salvato nei vari formati supportati (Default: `ARFF`)

```bash
PCPFiller --part "pcpart" --from-json "pcpart.json"  --save-dataset "output.arff" --out-format ARFF
```

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

Le GPU e RAM erano i dataset di partenza con piu dati disponibili.

- Nel caso delle GPU, le entries sono ~4400, rimuovendo le entries incomplete si arriva a ~350, quindi con una notevole perdita del ~90%.
- Nel caso delle RAM, le entries sono ~7000, rimuovendo le entries incomplete si arriva a ~1800, quindi con una perdita del ~70%.

Si è quindi deciso di operare inizialmente al filling del prezzo delle RAM; di seguito sarà tutto relativo ad esso.

### Analisi dati disponibili

Segue una descrizione generale dei dati disponibili per le RAM e l'eventuale utilità per lo scopo predisposto da PCPFiller

| Campo          |  Tipo   | Descrizione                                                                                         | Utilità                                                                                                                                         |
|----------------|---------|-----------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|
| Brand          | nominal | Nome del brand della casa produttrice                                                               | Potrebbe essere molto utile soprattutto nella predizione di prezzo. Molti brand sono noti per sovrapprezzare i propri prodotti.                 |
| Model          | nominal | Nome del modello                                                                                    | Non molto utile, in quando è insolito che due prodotti condividano lo stesso nome; porterebbe solamente all'inquinamento della predizione       |
| Module Type    | nominal | Tipo del modulo (DDR2/3/4), rappresenta la "generazione" del modulo                                 | Molto utile, dato che tipo di moduli diversi fanno variare molto prezzo ed altre statistiche come frequenza e dimensione modulo                 |
| Speed (cycles) | numeric | Frequenza del module, rappresentata in hertz (convertita in MHz in fase di preprocessing)           | Decisamente utile, in quanto strettamente correlata con altri parametri                                                                         |
| Modules number | numeric | Quantità di moduli                                                                                  | Strettamente correlata all dimensione e prezzo di un singolo modulo (1x8GB, 2x4GB, 2x16GB etc.)                                                 |
| Price / GB     | numeric | Prezzo in Euro per un GB                                                                            | Dato derivato da prezzo e dimensione/quantità dei moduli.                                                                                       |
| Color          | nominal | Colore                                                                                              | Alcuni colori (es: Gold, Silver) potrebbero essere correlati a prezzo e brand.                                                                  |
| FW Latency     | numeric | First Word Latency, latenza (ns) tempo di accesso (performance del modulo)                          | Strettamente correlato ad altri parametri                                                                                                       |
| CAS timing     | numeric | Latenza di "Column Access Strobe" (performance del modulo)                                          | Strettamente correlato ad altri parametri                                                                                                       |
| ECC            | nominal | Error Correction, capacità di correzioni errori (convertita in true/false in fase di preprocessing) | Solitamente la funzionalità di ECC è presente in moduli più pregiati. Quindi correlata ad altri parametri                                       |
| Price          | numeric | Prezzo in Euro                                                                                      | Decisamente utile                                                                                                                               |

Anche senza un'analisi dei dati, possiamo confidentemente rimuovere il camp `Model` e `Price per GB`.

Analizzando i dati con Weka, possiamo risalire a queste informazioni
![AllAttributes](https://imgur.com/xOLuDZH.png)

Dei dati piu rilevanti, possiamo dire:

- La generazione piu offerta è DDR4, seguita da DDR3 e DDR2
- La frequenza media e di circa 2500 MHz con una standard deviation di ~840
- La maggior parte dei prodotti vengono venduti in batch da 2 con una dimensione media di 10GB
- Moduli con ECC non sono molto comuni
- Il prezzo può arrivare anche a 2800 Euro, con una media di ~200 Euro e standard deviation di 220

Dato che ci interessa principalmente la previsione di campi correlati al prezzo, commentiamo la loro relazione</br>
> **NOTA**: è stato applicato un po' di jitter sui dati per migliorarne la visualizzazione

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

Per confermare le osservazioni fatte e per avere una visione più accurata della relazione tra gli attributi, utilizziamo ancora una volta Weka.

> **SPOILER** Il campo `color` è stato scartato.
> Nonostante il colore potrebbe essere parzialmente utile per la previsione del prezzo, in seguito alle precedenti osservazioni (e futuri test di regressione), si è deciso di rimuoverlo perchè comportava solamente un incremento di complessità per il nostro modello, senza apportare netti benefici.

#### Correlation Attribute Evaluation

L'algoritmo `CorrelationAttributeEval` valuta il "valore" di un attributo misurando la correlazione di Pearson's tra di esso e l'attributo scelto come classe.
Si sceglie quindi il prezzo come classe, e come metodo di ricerca si usa il `Ranker`.

L'analisi viene effettuata su tutto il dataset (non vengono usate partizioni)

Risultati:
| Valore  | Campo              |
|---------|--------------------|
| 0.6974  | number_of_modules  |
| 0.4739  | module_size        |
| 0.4371  | speed              |
| 0.3142  | cas_timing         |
| 0.2878  | module_type        |
| 0.1201  | brand              |
| 0.0917  | color              |
| 0.0208  | error_correction   |
| -0.3646 | first_word_latency |

<details>
<summary>Output completo</summary>

```text
=== Run information ===

Evaluator:    weka.attributeSelection.CorrelationAttributeEval 
Search:       weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N -1
Relation:     memory-weka.filters.unsupervised.attribute.Remove-R2,7
Instances:    1791
Attributes:   10
              brand
              module_type
              speed
              number_of_modules
              module_size
              color
              first_word_latency
              cas_timing
              error_correction
              price
Evaluation mode:    evaluate on all training data



=== Attribute Selection on all input data ===

Search Method:
 Attribute ranking.

Attribute Evaluator (supervised, Class (numeric): 10 price):
 Correlation Ranking Filter
Ranked attributes:
 0.6974   4 number_of_modules
 0.4739   5 module_size
 0.4371   3 speed
 0.3142   8 cas_timing
 0.2878   2 module_type
 0.1201   1 brand
 0.0917   6 color
 0.0208   9 error_correction
-0.3646   7 first_word_latency

Selected attributes: 4,5,3,8,2,1,6,9,7 : 9

```

</details>

Come previsto il numero dei moduli, la dimensione e la frequenza sono i parametri che piu impattano il prezzo, seguiti poi dai vari parametri di performance.

> NOTA: del campo `first_word_latency`, si dovrebbe prendere il valore assoluto, dato che con l'aumentare della latenza, le performance diminuiscono, e quindi anche il prezzo

Ripetiamo l'analisi anche con una PCA.

#### Principal Components Analysis

Anche in questo caso si sceglie il prezzo come classe, e come metodo di ricerca si usa il `Ranker`.

L'analisi viene effettuata su tutto il dataset (non vengono usate partizioni).

Risultati:

  ```text
Ranked attributes:
 0.8454    1 -0.443speed-0.436module_type=DDR4-0.423cas_timing+0.409module_type=DDR3+0.282first_word_latency...
 0.7829    2 0.526error_correction=True+0.356module_size+0.341brand=Samsung+0.32 brand=Crucial+0.303first_word_latency...
 0.735     3 0.735brand=Corsair-0.54brand=G.Skill+0.264number_of_modules-0.137brand=Patriot+0.132module_size...
 0.6891    4 0.448brand=G.Skill-0.371brand=Patriot-0.328module_type=DDR2+0.263number_of_modules-0.261brand=ADATA...
 0.647     5 0.498module_type=DDR2+0.356brand=OCZ-0.353brand=Kingston+0.282brand=Mushkin+0.257brand=G.Skill...
 0.6078    6 0.554brand=Kingston+0.403brand=OCZ+0.334module_type=DDR2-0.262brand=Patriot-0.237brand=GeIL...
 0.5707    7 -0.644brand=Crucial+0.311brand=Kingston-0.296brand=Klevv-0.231brand=OCZ+0.229brand=Samsung...
 0.5346    8 0.647brand=Patriot-0.54brand=Team-0.365brand=Mushkin+0.231brand=Crucial-0.185brand=ADATA...
 0.4987    9 0.494brand=Team-0.488brand=ADATA+0.34 brand=Samsung+0.32 brand=Patriot-0.316brand=Crucial...
 0.4631   10 0.518brand=ADATA+0.464brand=Thermaltake-0.326brand=Team+0.312brand=Samsung-0.294brand=Crucial...
 0.4278   11 0.595brand=Thermaltake+0.525brand=Mushkin-0.421brand=ADATA-0.244brand=OCZ-0.189brand=IBM...
 0.3928   12 -0.653brand=Gigabyte+0.578brand=IBM-0.361brand=HP-0.124brand=Transcend-0.117module_size...
 0.3579   13 0.698brand=GeIL+0.293brand=PNY-0.292brand=Mushkin+0.287brand=Thermaltake+0.269brand=OCZ...
 0.3232   14 0.544brand=GeIL-0.461brand=Transcend-0.415brand=PNY+0.314brand=Klevv-0.238brand=Silicon Power...
 0.2886   15 -0.714brand=PNY-0.622brand=Klevv+0.222brand=Transcend+0.165brand=Thermaltake+0.122brand=HP...
 0.254    16 -0.59brand=HP+0.528brand=Transcend-0.445brand=IBM+0.23 brand=Samsung-0.182brand=Gigabyte...
 0.2195   17 -0.495brand=Gigabyte+0.431brand=Silicon Power-0.394brand=IBM-0.327brand=Transcend-0.294brand=PNY...
 0.1849   18 0.791brand=Silicon Power-0.508brand=HP+0.294brand=Gigabyte+0.09 brand=IBM-0.088brand=V7...
 0.1504   19 -0.907brand=V7-0.278brand=Gigabyte-0.197brand=IBM-0.154brand=Klevv+0.137brand=PNY...
 0.1193   20 -0.419brand=Transcend-0.329module_type=DDR2-0.312brand=Klevv-0.286brand=Kingston+0.249brand=Samsung...
 0.0902   21 -0.533brand=OCZ-0.486brand=Mushkin+0.411module_type=DDR2+0.206brand=Thermaltake+0.202brand=Corsair...
 0.0657   22 -0.603number_of_modules+0.286brand=Corsair+0.271brand=OCZ-0.258module_type=DDR2+0.248first_word_latency...
 0.0432   23 -0.505first_word_latency+0.402brand=Samsung-0.389number_of_modules-0.24brand=Thermaltake+0.237speed...
  ```

<details>
<summary>Output completo</summary>

```text
=== Run information ===

Evaluator:    weka.attributeSelection.PrincipalComponents -R 0.95 -A 5
Search:       weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N -1
Relation:     memory-weka.filters.unsupervised.attribute.Remove-R2,7-weka.filters.unsupervised.attribute.Remove-R6
Instances:    1791
Attributes:   9
              brand
              module_type
              speed
              number_of_modules
              module_size
              first_word_latency
              cas_timing
              error_correction
              price
Evaluation mode:    evaluate on all training data



=== Attribute Selection on all input data ===

Search Method:
 Attribute ranking.

Attribute Evaluator (unsupervised):
 Principal Components Attribute Transformer

Correlation matrix
  1     -0.1   -0.05  -0.11  -0.02  -0     -0.01  -0.01  -0.08  -0.01  -0.03  -0.01  -0.01  -0.04  -0.02  -0.01  -0.04  -0.02  -0.01  -0      0      0.07  -0.07   0.04  -0.06  -0.04   0      0.07  -0.04 
 -0.1    1     -0.15  -0.37  -0.05  -0.01  -0.02  -0.02  -0.27  -0.03  -0.1   -0.03  -0.04  -0.14  -0.05  -0.02  -0.12  -0.06  -0.03  -0.01  -0.04   0.11  -0.1    0.12   0.24   0.06  -0.13   0.09  -0.14 
 -0.05  -0.15   1     -0.17  -0.03  -0.01  -0.01  -0.01  -0.13  -0.02  -0.05  -0.01  -0.02  -0.07  -0.02  -0.01  -0.06  -0.03  -0.02  -0.01  -0.02   0.03  -0.03   0.02  -0.1    0.12   0.07   0.07   0.21 
 -0.11  -0.37  -0.17   1     -0.06  -0.02  -0.03  -0.02  -0.31  -0.04  -0.12  -0.03  -0.04  -0.16  -0.06  -0.02  -0.13  -0.07  -0.04  -0.02  -0.04   0.13  -0.12   0.17   0.14   0.02  -0.2    0.09  -0.15 
 -0.02  -0.05  -0.03  -0.06   1     -0     -0     -0     -0.05  -0.01  -0.02  -0.01  -0.01  -0.02  -0.01  -0     -0.02  -0.01  -0.01  -0     -0.01  -0.06   0.06  -0.08  -0.04  -0.05   0.07  -0.06  -0.02 
 -0     -0.01  -0.01  -0.02  -0      1     -0     -0     -0.01  -0     -0     -0     -0     -0.01  -0     -0     -0     -0     -0     -0     -0      0.02  -0.01   0.02  -0      0.07  -0.02   0.01  -0.01 
 -0.01  -0.02  -0.01  -0.03  -0     -0      1     -0     -0.02  -0     -0.01  -0     -0     -0.01  -0     -0     -0.01  -0     -0     -0     -0.01  -0.06   0.07  -0.06  -0.04   0      0.05  -0.05  -0.01 
 -0.01  -0.02  -0.01  -0.02  -0     -0     -0      1     -0.02  -0     -0.01  -0     -0     -0.01  -0     -0     -0.01  -0     -0     -0     -0     -0.05   0.05  -0.05  -0.03   0.03   0.04  -0.04   0.14 
 -0.08  -0.27  -0.13  -0.31  -0.05  -0.01  -0.02  -0.02   1     -0.03  -0.09  -0.03  -0.03  -0.12  -0.04  -0.02  -0.1   -0.05  -0.03  -0.01   0.04  -0.27   0.26  -0.27  -0.15  -0.08   0.18  -0.24   0.22 
 -0.01  -0.03  -0.02  -0.04  -0.01  -0     -0     -0     -0.03   1     -0.01  -0     -0     -0.01  -0.01  -0     -0.01  -0.01  -0     -0     -0.01  -0.05   0.05  -0      0.01  -0.03  -0.05  -0.03  -0.01 
 -0.03  -0.1   -0.05  -0.12  -0.02  -0     -0.01  -0.01  -0.09  -0.01   1     -0.01  -0.01  -0.04  -0.02  -0.01  -0.04  -0.02  -0.01  -0      0.11  -0.15   0.12  -0.18  -0.08  -0.06   0.16  -0.15  -0    
 -0.01  -0.03  -0.01  -0.03  -0.01  -0     -0     -0     -0.03  -0     -0.01   1     -0     -0.01  -0     -0     -0.01  -0.01  -0     -0      0.17  -0.08   0.04  -0.08   0.01  -0.06  -0.03  -0.12  -0.01 
 -0.01  -0.04  -0.02  -0.04  -0.01  -0     -0     -0     -0.03  -0     -0.01  -0      1     -0.02  -0.01  -0     -0.01  -0.01  -0     -0     -0.01   0.02  -0.02   0     -0.03  -0      0.04   0.04  -0.01 
 -0.04  -0.14  -0.07  -0.16  -0.02  -0.01  -0.01  -0.01  -0.12  -0.01  -0.04  -0.01  -0.02   1     -0.02  -0.01  -0.05  -0.03  -0.01  -0.01   0.03  -0.01   0.01  -0.06  -0.11  -0.08   0.1   -0.01  -0.06 
 -0.02  -0.05  -0.02  -0.06  -0.01  -0     -0     -0     -0.04  -0.01  -0.02  -0     -0.01  -0.02   1     -0     -0.02  -0.01  -0.01  -0     -0.01  -0.01   0.01  -0.04  -0.08   0.2    0.13   0.04   0.25 
 -0.01  -0.02  -0.01  -0.02  -0     -0     -0     -0     -0.02  -0     -0.01  -0     -0     -0.01  -0      1     -0.01  -0     -0     -0     -0     -0.05   0.05  -0.04  -0.03  -0.02   0.04  -0.03  -0.01 
 -0.04  -0.12  -0.06  -0.13  -0.02  -0     -0.01  -0.01  -0.1   -0.01  -0.04  -0.01  -0.01  -0.05  -0.02  -0.01   1     -0.02  -0.01  -0     -0.03   0.09  -0.09   0.07  -0.06  -0.03  -0.05   0.08  -0.05 
 -0.02  -0.06  -0.03  -0.07  -0.01  -0     -0     -0     -0.05  -0.01  -0.02  -0.01  -0.01  -0.03  -0.01  -0     -0.02   1     -0.01  -0     -0.01   0.07  -0.07   0.14  -0.02  -0.01  -0.1    0.12  -0.03 
 -0.01  -0.03  -0.02  -0.04  -0.01  -0     -0     -0     -0.03  -0     -0.01  -0     -0     -0.01  -0.01  -0     -0.01  -0.01   1     -0     -0.01  -0.09   0.09  -0.08  -0.05  -0.04   0.04  -0.08  -0.01 
 -0     -0.01  -0.01  -0.02  -0     -0     -0     -0     -0.01  -0     -0     -0     -0     -0.01  -0     -0     -0     -0     -0      1     -0      0.02  -0.01  -0.01  -0.02  -0.01   0.03   0.02  -0.01 
  0     -0.04  -0.02  -0.04  -0.01  -0     -0.01  -0      0.04  -0.01   0.11   0.17  -0.01   0.03  -0.01  -0     -0.03  -0.01  -0.01  -0      1     -0.19  -0.08  -0.28  -0.08  -0.14   0.17  -0.33  -0.01 
  0.07   0.11   0.03   0.13  -0.06   0.02  -0.06  -0.05  -0.27  -0.05  -0.15  -0.08   0.02  -0.01  -0.01  -0.05   0.09   0.07  -0.09   0.02  -0.19   1     -0.96   0.81   0.25   0.38  -0.36   0.9   -0.16 
 -0.07  -0.1   -0.03  -0.12   0.06  -0.01   0.07   0.05   0.26   0.05   0.12   0.04  -0.02   0.01   0.01   0.05  -0.09  -0.07   0.09  -0.01  -0.08  -0.96   1     -0.75  -0.24  -0.35   0.32  -0.82   0.16 
  0.04   0.12   0.02   0.17  -0.08   0.02  -0.06  -0.05  -0.27  -0     -0.18  -0.08   0     -0.06  -0.04  -0.04   0.07   0.14  -0.08  -0.01  -0.28   0.81  -0.75   1      0.31   0.35  -0.72   0.86  -0.2  
 -0.06   0.24  -0.1    0.14  -0.04  -0     -0.04  -0.03  -0.15   0.01  -0.08   0.01  -0.03  -0.11  -0.08  -0.03  -0.06  -0.02  -0.05  -0.02  -0.08   0.25  -0.24   0.31   1      0.16  -0.36   0.19  -0.19 
 -0.04   0.06   0.12   0.02  -0.05   0.07   0      0.03  -0.08  -0.03  -0.06  -0.06  -0     -0.08   0.2   -0.02  -0.03  -0.01  -0.04  -0.01  -0.14   0.38  -0.35   0.35   0.16   1     -0.11   0.43   0.18 
  0     -0.13   0.07  -0.2    0.07  -0.02   0.05   0.04   0.18  -0.05   0.16  -0.03   0.04   0.1    0.13   0.04  -0.05  -0.1    0.04   0.03   0.17  -0.36   0.32  -0.72  -0.36  -0.11   1     -0.3    0.31 
  0.07   0.09   0.07   0.09  -0.06   0.01  -0.05  -0.04  -0.24  -0.03  -0.15  -0.12   0.04  -0.01   0.04  -0.03   0.08   0.12  -0.08   0.02  -0.33   0.9   -0.82   0.86   0.19   0.43  -0.3    1     -0.07 
 -0.04  -0.14   0.21  -0.15  -0.02  -0.01  -0.01   0.14   0.22  -0.01  -0     -0.01  -0.01  -0.06   0.25  -0.01  -0.05  -0.03  -0.01  -0.01  -0.01  -0.16   0.16  -0.2   -0.19   0.18   0.31  -0.07   1    


eigenvalue proportion cumulative
  4.48219   0.15456   0.15456 -0.443speed-0.436module_type=DDR4-0.423cas_timing+0.409module_type=DDR3+0.282first_word_latency...
  1.81325   0.06253   0.21708 0.526error_correction=True+0.356module_size+0.341brand=Samsung+0.32 brand=Crucial+0.303first_word_latency...
  1.39028   0.04794   0.26502 0.735brand=Corsair-0.54brand=G.Skill+0.264number_of_modules-0.137brand=Patriot+0.132module_size...
  1.33149   0.04591   0.31094 0.448brand=G.Skill-0.371brand=Patriot-0.328module_type=DDR2+0.263number_of_modules-0.261brand=ADATA...
  1.21862   0.04202   0.35296 0.498module_type=DDR2+0.356brand=OCZ-0.353brand=Kingston+0.282brand=Mushkin+0.257brand=G.Skill...
  1.13876   0.03927   0.39223 0.554brand=Kingston+0.403brand=OCZ+0.334module_type=DDR2-0.262brand=Patriot-0.237brand=GeIL...
  1.07384   0.03703   0.42926 -0.644brand=Crucial+0.311brand=Kingston-0.296brand=Klevv-0.231brand=OCZ+0.229brand=Samsung...
  1.04958   0.03619   0.46545 0.647brand=Patriot-0.54brand=Team-0.365brand=Mushkin+0.231brand=Crucial-0.185brand=ADATA...
  1.04109   0.0359    0.50135 0.494brand=Team-0.488brand=ADATA+0.34 brand=Samsung+0.32 brand=Patriot-0.316brand=Crucial...
  1.0313    0.03556   0.53691 0.518brand=ADATA+0.464brand=Thermaltake-0.326brand=Team+0.312brand=Samsung-0.294brand=Crucial...
  1.02354   0.03529   0.5722  0.595brand=Thermaltake+0.525brand=Mushkin-0.421brand=ADATA-0.244brand=OCZ-0.189brand=IBM...
  1.01413   0.03497   0.60717 -0.653brand=Gigabyte+0.578brand=IBM-0.361brand=HP-0.124brand=Transcend-0.117module_size...
  1.01154   0.03488   0.64206 0.698brand=GeIL+0.293brand=PNY-0.292brand=Mushkin+0.287brand=Thermaltake+0.269brand=OCZ...
  1.00666   0.03471   0.67677 0.544brand=GeIL-0.461brand=Transcend-0.415brand=PNY+0.314brand=Klevv-0.238brand=Silicon Power...
  1.00445   0.03464   0.7114  -0.714brand=PNY-0.622brand=Klevv+0.222brand=Transcend+0.165brand=Thermaltake+0.122brand=HP...
  1.00252   0.03457   0.74597 -0.59brand=HP+0.528brand=Transcend-0.445brand=IBM+0.23 brand=Samsung-0.182brand=Gigabyte...
  1.00228   0.03456   0.78054 -0.495brand=Gigabyte+0.431brand=Silicon Power-0.394brand=IBM-0.327brand=Transcend-0.294brand=PNY...
  1.0013    0.03453   0.81506 0.791brand=Silicon Power-0.508brand=HP+0.294brand=Gigabyte+0.09 brand=IBM-0.088brand=V7...
  1.0009    0.03451   0.84958 -0.907brand=V7-0.278brand=Gigabyte-0.197brand=IBM-0.154brand=Klevv+0.137brand=PNY...
  0.90201   0.0311    0.88068 -0.419brand=Transcend-0.329module_type=DDR2-0.312brand=Klevv-0.286brand=Kingston+0.249brand=Samsung...
  0.84579   0.02917   0.90985 -0.533brand=OCZ-0.486brand=Mushkin+0.411module_type=DDR2+0.206brand=Thermaltake+0.202brand=Corsair...
  0.7087    0.02444   0.93428 -0.603number_of_modules+0.286brand=Corsair+0.271brand=OCZ-0.258module_type=DDR2+0.248first_word_latency...
  0.65263   0.0225    0.95679 -0.505first_word_latency+0.402brand=Samsung-0.389number_of_modules-0.24brand=Thermaltake+0.237speed...

Eigenvectors
 V1  V2  V3  V4  V5  V6  V7  V8  V9  V10  V11  V12  V13  V14  V15  V16  V17  V18  V19  V20  V21  V22  V23 
-0.0252  0.0215 -0.1047 -0.2615 -0.1056  0.0583  0.0886 -0.1851 -0.4882  0.5179 -0.4208 -0.0324 -0.1963  0.0497  0.0386 -0.0212 -0.0068  0.0054  0.0305  0.2132 -0.0256 -0.1818 -0.1114 brand=ADATA
-0.0919 -0.1662  0.7353 -0.1014  0.098  -0.1223  0.0381  0.0022 -0.0079 -0.0065 -0.0273  0.0509  0.0136 -0.0522  0.0246 -0.0043  0.0102  0.0001 -0.0025  0.0651  0.2024  0.2857 -0.0089 brand=Corsair
-0.0061  0.3201 -0.0261 -0.0282  0.0844 -0.0461 -0.6438  0.2306 -0.3156 -0.2944 -0.0175 -0.0835 -0.0261 -0.005   0.0215  0.0102  0.0178 -0.0027  0.014   0.1537  0.0931 -0.1606  0.1664 brand=Crucial
-0.105  -0.2299 -0.5401  0.4484  0.2568 -0.1334  0.0943 -0.0001 -0.0246 -0.0345 -0.0242  0.0413  0.0096 -0.0346  0.0174 -0.003   0.0091 -0      -0.0004  0.036   0.1702  0.1774 -0.1423 brand=G.Skill
 0.0416 -0.0188 -0.0153 -0.0512 -0.0714 -0.2374 -0.0313 -0.1068 -0.1079 -0.0156 -0.1262  0.022   0.6983  0.5441  0.0484  0.0146 -0.0784  0.0086  0.0257 -0.2435 -0.1037 -0.0906  0.0566 brand=GeIL
-0.013   0.0305  0.0149  0.0274  0.0408  0.0062  0.0208 -0.0231  0.1267  0.0901  0.0922 -0.6528 -0.098   0.2149  0.0365 -0.1822 -0.4947  0.2944 -0.2781  0.0958  0.0977  0.0466  0.0023 brand=Gigabyte
 0.0351 -0.0034  0.0051  0.0182 -0.0265 -0.1788 -0.0665 -0.0626  0.0526  0.1037 -0.0309 -0.3607  0.0233 -0.2048  0.1219 -0.5904  0.26   -0.5076  0.0568 -0.2474 -0.0562 -0.0343  0.0409 brand=HP
 0.0316  0.1053  0.0343  0.118   0.0522 -0.0411 -0.1354  0.0022  0.1755  0.2155 -0.1891  0.5782 -0.0974 -0.0195 -0.0018 -0.4449 -0.3942  0.0895 -0.1972 -0.1812 -0.0124 -0.0061  0.1446 brand=IBM
 0.1775  0.1678  0.0494  0.1758 -0.3531  0.5543  0.3106  0.0411 -0.0807 -0.1132  0.0618 -0.0627 -0.0106  0.0065  0.0104  0.0061  0.0013 -0.0003  0.0018 -0.2864 -0.155  -0.0349  0.1567 brand=Kingston
 0.0133 -0.0575  0.0176  0.0667 -0.1026 -0.0107 -0.296   0.0398  0.1484  0.2701 -0.0184 -0.076  -0.2485  0.3142 -0.6224  0.1797  0.2369 -0.0835 -0.1537 -0.3123  0.0642  0.0876 -0.0967 brand=Klevv
 0.0994 -0.0137 -0.0364 -0.1935  0.2817 -0.1657 -0.0112 -0.3646 -0.1271 -0.0135  0.5246  0.1062 -0.2921  0.1353  0.0064  0.0152 -0.0407 -0.0032  0.0171 -0.0715 -0.4859 -0.0273  0.1197 brand=Mushkin
 0.0466 -0.1123 -0.0128 -0.1111  0.3561  0.4028 -0.2314  0.0951  0.2008  0.1109 -0.2436 -0.0973  0.2687 -0.1045 -0.047  -0.0094  0.0368  0.0104 -0.0185  0.1605 -0.5327  0.2714 -0.1414 brand=OCZ
-0.005   0.0336 -0.0371 -0.0997 -0.0373 -0.0807  0.125  -0.0706 -0.1216 -0.0715  0.1018 -0.0347  0.2934 -0.4145 -0.7135 -0.1268 -0.2944 -0.0026  0.1373  0.1112 -0.0111 -0.131   0.0253 brand=PNY
 0.0306  0.0064 -0.1368 -0.3709 -0.0941 -0.2617  0.2121  0.6475  0.3198 -0.0142 -0.0305  0.0286 -0.1282  0.0661  0.0132 -0.0045 -0.0165  0.0013  0.0193  0.0394 -0.1904 -0.2323 -0.0474 brand=Patriot
 0.0145  0.3414  0.0456  0.121   0.242  -0.1391  0.2287 -0.1495  0.3397  0.3118 -0.0593 -0.0581  0.118   0.0033 -0.0381  0.2302  0.1989 -0.0494  0.093   0.2493  0.0876 -0.1268  0.4016 brand=Samsung
 0.0277 -0.008  -0.0052 -0.0025 -0.0482 -0.1413 -0.0599 -0.0419 -0.0028  0.0698 -0.0156 -0.0635  0.0614 -0.2379  0.0156 -0.1803  0.4315  0.7912  0.0509 -0.2107 -0.0571 -0.054   0.0223 brand=Silicon Power
-0.0407  0.0142 -0.1118 -0.2414 -0.2507  0.0726 -0.1764 -0.5399  0.4939 -0.3258 -0.1888  0.0313 -0.067   0.004   0.0292 -0.0139  0.0122  0.004   0.0215  0.1284  0.0926 -0.1775 -0.1823 brand=Team
-0.0565  0.004  -0.0626 -0.0923 -0.1982  0.1867 -0.2221  0.0632  0.1095  0.4643  0.5949  0.1124  0.2869 -0.0638  0.1654 -0.0372  0.0157 -0.0238  0.0167  0.1366  0.2059 -0.071  -0.2404 brand=Thermaltake
 0.0501 -0.038  -0.0015  0.0206 -0.0874 -0.2041 -0.1941 -0.0516  0.0564  0.1739 -0.1111 -0.1242  0.0178 -0.4607  0.222   0.528  -0.3267 -0.0605 -0.0148 -0.4193 -0.0942 -0.0428 -0.0014 brand=Transcend
-0.001   0.0234 -0.0266 -0.0701 -0.0097 -0.0493  0.1038 -0.0436 -0.0938 -0.0811  0.0273  0.0575  0.1569 -0.1735 -0.0044  0.0701  0.2154 -0.0876 -0.9072  0.0904 -0.0377 -0.0988  0.0277 brand=V7
 0.1267 -0.0878 -0.0829 -0.3283  0.4975  0.3341  0.0265  0.019   0.0181 -0.003   0.0056 -0.0131  0.024  -0.0102 -0.0006  0.0006  0.0047  0.0017 -0.0011 -0.329   0.4113 -0.2576  0.1555 module_type=DDR2
-0.4361  0.1196 -0.0586 -0.1471  0.0182  0.0301  0.0806 -0.0098 -0.0296 -0.0432 -0.013   0.0134  0.0057  0.0041  0.0034  0.0025 -0.0049 -0.0036 -0.0033 -0.1403 -0.0081  0.139  -0.0456 module_type=DDR4
 0.4086 -0.0976  0.0821  0.2389 -0.154  -0.1216 -0.0891  0.0048  0.0251  0.0447  0.0117 -0.01   -0.0123 -0.0014 -0.0033 -0.0027  0.0037  0.0032  0.0037  0.2323 -0.1038 -0.071   0.0039 module_type=DDR3
-0.4431 -0.0092 -0.0247  0.0364 -0.101   0.065  -0.0639  0.018   0.0167  0.0386  0.0339  0.0077 -0.0045 -0.003   0.0021 -0.0027  0.0019  0.0027  0.0026  0.0006 -0.0917 -0.0178  0.2369 speed
-0.1921 -0.2644  0.2644  0.2625  0.163   0.0522  0.049   0.0206 -0.0324 -0.054  -0.0075  0.0263 -0.0138  0.0357 -0.0141  0.004  -0.0009 -0.0012  0.0066 -0.0283 -0.1038 -0.6035 -0.3893 number_of_modules
-0.2103  0.356   0.1318  0.2003  0.1996 -0.0466  0.0518 -0.0185  0.0604  0.0215  0.0208 -0.1167 -0.0134  0.0144  0.0041 -0.0058 -0.009   0.0031 -0.0002 -0.0928 -0.138  -0.2166 -0.2112 module_size
 0.2815  0.3034 -0.0253 -0.1979  0.1121 -0.1658  0.176  -0.0318 -0.0793 -0.0694  0.0111  0.0185  0.0253 -0.0116  0.0008  0.0004  0.0088 -0.0003 -0.0012 -0.1295  0.1268  0.2479 -0.5053 first_word_latency
-0.423   0.2071 -0.0414 -0.0836 -0.0834 -0.0331  0.048  -0.0013 -0.0296 -0.0006  0.0294  0.0154  0.0092 -0.0092 -0.0118  0.0034  0.0108  0.0027  0.0004 -0.0687 -0.0778  0.1766 -0.0703 cas_timing
 0.1177  0.5261  0.0584  0.1985  0.0772  0.1052 -0.0497  0.0203  0.0266  0.0232 -0.0294  0.0944 -0.0061  0.0028 -0.0024 -0.0005 -0.0027 -0.0016  0.0005  0.003  -0.01   -0.012  -0.2244 error_correction=True

Ranked attributes:
 0.8454    1 -0.443speed-0.436module_type=DDR4-0.423cas_timing+0.409module_type=DDR3+0.282first_word_latency...
 0.7829    2 0.526error_correction=True+0.356module_size+0.341brand=Samsung+0.32 brand=Crucial+0.303first_word_latency...
 0.735     3 0.735brand=Corsair-0.54brand=G.Skill+0.264number_of_modules-0.137brand=Patriot+0.132module_size...
 0.6891    4 0.448brand=G.Skill-0.371brand=Patriot-0.328module_type=DDR2+0.263number_of_modules-0.261brand=ADATA...
 0.647     5 0.498module_type=DDR2+0.356brand=OCZ-0.353brand=Kingston+0.282brand=Mushkin+0.257brand=G.Skill...
 0.6078    6 0.554brand=Kingston+0.403brand=OCZ+0.334module_type=DDR2-0.262brand=Patriot-0.237brand=GeIL...
 0.5707    7 -0.644brand=Crucial+0.311brand=Kingston-0.296brand=Klevv-0.231brand=OCZ+0.229brand=Samsung...
 0.5346    8 0.647brand=Patriot-0.54brand=Team-0.365brand=Mushkin+0.231brand=Crucial-0.185brand=ADATA...
 0.4987    9 0.494brand=Team-0.488brand=ADATA+0.34 brand=Samsung+0.32 brand=Patriot-0.316brand=Crucial...
 0.4631   10 0.518brand=ADATA+0.464brand=Thermaltake-0.326brand=Team+0.312brand=Samsung-0.294brand=Crucial...
 0.4278   11 0.595brand=Thermaltake+0.525brand=Mushkin-0.421brand=ADATA-0.244brand=OCZ-0.189brand=IBM...
 0.3928   12 -0.653brand=Gigabyte+0.578brand=IBM-0.361brand=HP-0.124brand=Transcend-0.117module_size...
 0.3579   13 0.698brand=GeIL+0.293brand=PNY-0.292brand=Mushkin+0.287brand=Thermaltake+0.269brand=OCZ...
 0.3232   14 0.544brand=GeIL-0.461brand=Transcend-0.415brand=PNY+0.314brand=Klevv-0.238brand=Silicon Power...
 0.2886   15 -0.714brand=PNY-0.622brand=Klevv+0.222brand=Transcend+0.165brand=Thermaltake+0.122brand=HP...
 0.254    16 -0.59brand=HP+0.528brand=Transcend-0.445brand=IBM+0.23 brand=Samsung-0.182brand=Gigabyte...
 0.2195   17 -0.495brand=Gigabyte+0.431brand=Silicon Power-0.394brand=IBM-0.327brand=Transcend-0.294brand=PNY...
 0.1849   18 0.791brand=Silicon Power-0.508brand=HP+0.294brand=Gigabyte+0.09 brand=IBM-0.088brand=V7...
 0.1504   19 -0.907brand=V7-0.278brand=Gigabyte-0.197brand=IBM-0.154brand=Klevv+0.137brand=PNY...
 0.1193   20 -0.419brand=Transcend-0.329module_type=DDR2-0.312brand=Klevv-0.286brand=Kingston+0.249brand=Samsung...
 0.0902   21 -0.533brand=OCZ-0.486brand=Mushkin+0.411module_type=DDR2+0.206brand=Thermaltake+0.202brand=Corsair...
 0.0657   22 -0.603number_of_modules+0.286brand=Corsair+0.271brand=OCZ-0.258module_type=DDR2+0.248first_word_latency...
 0.0432   23 -0.505first_word_latency+0.402brand=Samsung-0.389number_of_modules-0.24brand=Thermaltake+0.237speed...

Selected attributes: 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23 : 23
```

</details>

### Processing dei dati

Il prezzo è un valore continuo, possiamo quindi scegliere vari algoritmi specializziati per regressione con valori numerici:

- LinearRegression
- Alberi:
  - RandomTree
  - RandomForest
  - M5P

Per quanto riguarda il testing, creare un dataset specificatamente per esso era fuori discussione.

Si è quindi optato per una k-fold cross-validation.
I test sono stati effettuati con 5, 10 e 15 partizioni (fold), data la ridotta dimensione del dataset, i risultati migliori si sono ottenuti con 10 fold.

> **NOTA**: dato che stiamo cercando di creare un modello per la predizione del prezzo, il nostro obiettivo è quello di ridurre quanto più possibile lo scarto quadratico.

#### Linear regression

- Modello risultante:
  
  ```text
  price =

     66.561  * brand=HP,OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte +
   -110.007  * brand=OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte +
     27.7063 * brand=Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte +
     16.9202 * brand=PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte +
    -39.1303 * brand=IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte +
    -19.4201 * brand=Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte +
     41.2542 * brand=Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte +
    -17.8709 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte +
    -28.8788 * brand=Samsung,G.Skill,Corsair,Gigabyte +
     57.8274 * brand=G.Skill,Corsair,Gigabyte +
     15.5456 * brand=Corsair,Gigabyte +
   -191.5702 * module_type=DDR3,DDR4 +
   -149.078  * module_type=DDR4 +
    104.3092 * number_of_modules +
     10.9041 * module_size +
    -18.723  * first_word_latency +
     20.0331 * cas_timing +
    -69.9652 * error_correction=False +
    137.2024
  ```

- Risultati del testing:

  | Coefficiente di correlazione | Errore medio assoluto | Errore quadratico medio | Errore assoluto relativo | Errore quadratico relativo |
  |------------------------------|-----------------------|-------------------------|--------------------------|----------------------------|
  | 0.8056                       | 81.1316               | 133.8985                | 59.6785 %                | 59.2254 %                  |

- Visualizzazione dei risultati:</br>
  ![LinearRegressionResult](https://imgur.com/wCmst5p.png)

<details>
<summary>Output completo</summary>

```text
=== Run information ===

Scheme:       weka.classifiers.functions.LinearRegression -S 0 -R 1.0E-8 -num-decimal-places 4
Relation:     memory-weka.filters.unsupervised.attribute.Remove-R2,7-weka.filters.unsupervised.attribute.Remove-R6
Instances:    1791
Attributes:   9
              brand
              module_type
              speed
              number_of_modules
              module_size
              first_word_latency
              cas_timing
              error_correction
              price
Test mode:    10-fold cross-validation

=== Classifier model (full training set) ===


Linear Regression Model

price =

     66.561  * brand=HP,OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte +
   -110.007  * brand=OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte +
     27.7063 * brand=Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte +
     16.9202 * brand=PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte +
    -39.1303 * brand=IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte +
    -19.4201 * brand=Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte +
     41.2542 * brand=Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte +
    -17.8709 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte +
    -28.8788 * brand=Samsung,G.Skill,Corsair,Gigabyte +
     57.8274 * brand=G.Skill,Corsair,Gigabyte +
     15.5456 * brand=Corsair,Gigabyte +
   -191.5702 * module_type=DDR3,DDR4 +
   -149.078  * module_type=DDR4 +
    104.3092 * number_of_modules +
     10.9041 * module_size +
    -18.723  * first_word_latency +
     20.0331 * cas_timing +
    -69.9652 * error_correction=False +
    137.2024

Time taken to build model: 0.05 seconds

=== Cross-validation ===
=== Summary ===

Correlation coefficient                  0.8056
Mean absolute error                     81.1316
Root mean squared error                133.8985
Relative absolute error                 59.6785 %
Root relative squared error             59.2254 %
Total Number of Instances             1791     
```

</details>

L'algoritmo di linear regression è quindi risultato discreto. Come si può vedere tende a sovrastimare il prezzo. Esploriamo altri algoritmi.

#### RandomTree

- Albero risultante:</br>
  ![RandomTreeResult](https://imgur.com/YBvk6Fx.png)

- Risultati del testing:</br>
  | Coefficiente di correlazione | Errore medio assoluto | Errore quadratico medio | Errore assoluto relativo | Errore quadratico relativo |
  |------------------------------|-----------------------|-------------------------|--------------------------|----------------------------|
  | 0.8915                       | 53.9916               | 105.6178                | 39.7149 %                | 46.7164 %                  |

- Visualizzazione dei risultati:</br>
  ![RandomTreeResult](https://imgur.com/AMSGfPs.png)

Come si può notare si ottengono risultati migliori, anche se l'albero risultante è molto grande, probabilmente a causa dell'attributo Brand; anche senza il Brand la performance non cambiava di molto.

<details>
<summary>Output completo</summary>

```text
=== Run information ===

Scheme:       weka.classifiers.trees.RandomTree -K 0 -M 1.0 -V 0.001 -S 1
Relation:     memory-weka.filters.unsupervised.attribute.Remove-R2,7-weka.filters.unsupervised.attribute.Remove-R6
Instances:    1791
Attributes:   9
              brand
              module_type
              speed
              number_of_modules
              module_size
              first_word_latency
              cas_timing
              error_correction
              price
Test mode:    10-fold cross-validation

=== Classifier model (full training set) ===


RandomTree
==========

number_of_modules < 7
|   number_of_modules < 3.5
|   |   number_of_modules < 1.5
|   |   |   error_correction = False
|   |   |   |   brand = ADATA
|   |   |   |   |   cas_timing < 16.5
|   |   |   |   |   |   first_word_latency < 12.25
|   |   |   |   |   |   |   module_size < 12
|   |   |   |   |   |   |   |   first_word_latency < 11.34 : 61.76 (3/49.33)
|   |   |   |   |   |   |   |   first_word_latency >= 11.34 : 39.77 (2/1.5)
|   |   |   |   |   |   |   module_size >= 12
|   |   |   |   |   |   |   |   first_word_latency < 11.34 : 113.3 (1/0)
|   |   |   |   |   |   |   |   first_word_latency >= 11.34 : 98.04 (2/4.56)
|   |   |   |   |   |   first_word_latency >= 12.25
|   |   |   |   |   |   |   module_type = DDR2 : 44.45 (1/0)
|   |   |   |   |   |   |   module_type = DDR4 : 26.36 (1/0)
|   |   |   |   |   |   |   module_type = DDR3
|   |   |   |   |   |   |   |   module_size < 6 : 23.99 (1/0)
|   |   |   |   |   |   |   |   module_size >= 6 : 82.58 (1/0)
|   |   |   |   |   cas_timing >= 16.5
|   |   |   |   |   |   speed < 2533 : 46.43 (2/0.64)
|   |   |   |   |   |   speed >= 2533 : 43.76 (2/138.42)
|   |   |   |   brand = Corsair
|   |   |   |   |   speed < 2533
|   |   |   |   |   |   cas_timing < 12.5
|   |   |   |   |   |   |   first_word_latency < 12.82
|   |   |   |   |   |   |   |   first_word_latency < 11.88 : 32.69 (16/49)
|   |   |   |   |   |   |   |   first_word_latency >= 11.88 : 44.8 (1/0)
|   |   |   |   |   |   |   first_word_latency >= 12.82
|   |   |   |   |   |   |   |   speed < 1199.5 : 22.63 (3/6.02)
|   |   |   |   |   |   |   |   speed >= 1199.5
|   |   |   |   |   |   |   |   |   first_word_latency < 13.63
|   |   |   |   |   |   |   |   |   |   module_size < 6 : 22.17 (7/11.12)
|   |   |   |   |   |   |   |   |   |   module_size >= 6 : 37.49 (5/18.83)
|   |   |   |   |   |   |   |   |   first_word_latency >= 13.63
|   |   |   |   |   |   |   |   |   |   module_size < 6 : 22.99 (3/4.69)
|   |   |   |   |   |   |   |   |   |   module_size >= 6 : 39.96 (2/13.99)
|   |   |   |   |   |   cas_timing >= 12.5
|   |   |   |   |   |   |   module_size < 24
|   |   |   |   |   |   |   |   module_size < 12
|   |   |   |   |   |   |   |   |   module_size < 6 : 27.05 (7/34.59)
|   |   |   |   |   |   |   |   |   module_size >= 6 : 42.11 (7/9.32)
|   |   |   |   |   |   |   |   module_size >= 12 : 72.3 (4/35.33)
|   |   |   |   |   |   |   module_size >= 24 : 174.99 (1/0)
|   |   |   |   |   speed >= 2533
|   |   |   |   |   |   module_size < 24
|   |   |   |   |   |   |   cas_timing < 17
|   |   |   |   |   |   |   |   first_word_latency < 10.33
|   |   |   |   |   |   |   |   |   cas_timing < 15.5 : 74.99 (1/0)
|   |   |   |   |   |   |   |   |   cas_timing >= 15.5
|   |   |   |   |   |   |   |   |   |   module_size < 12 : 43.49 (2/2.25)
|   |   |   |   |   |   |   |   |   |   module_size >= 12 : 79.49 (2/90.25)
|   |   |   |   |   |   |   |   first_word_latency >= 10.33
|   |   |   |   |   |   |   |   |   first_word_latency < 11.34
|   |   |   |   |   |   |   |   |   |   module_size < 12 : 41.17 (1/0)
|   |   |   |   |   |   |   |   |   |   module_size >= 12 : 68.89 (1/0)
|   |   |   |   |   |   |   |   |   first_word_latency >= 11.34 : 47.7 (3/45.69)
|   |   |   |   |   |   |   cas_timing >= 17
|   |   |   |   |   |   |   |   module_size < 12 : 48.95 (2/0.89)
|   |   |   |   |   |   |   |   module_size >= 12
|   |   |   |   |   |   |   |   |   speed < 3133 : 74.16 (2/33.93)
|   |   |   |   |   |   |   |   |   speed >= 3133 : 92.99 (1/0)
|   |   |   |   |   |   module_size >= 24
|   |   |   |   |   |   |   cas_timing < 17
|   |   |   |   |   |   |   |   first_word_latency < 11.34 : 169.5 (1/0)
|   |   |   |   |   |   |   |   first_word_latency >= 11.34 : 129.99 (1/0)
|   |   |   |   |   |   |   cas_timing >= 17 : 154.72 (1/0)
|   |   |   |   brand = Crucial
|   |   |   |   |   module_size < 6
|   |   |   |   |   |   speed < 1733
|   |   |   |   |   |   |   cas_timing < 8 : 15.66 (1/0)
|   |   |   |   |   |   |   cas_timing >= 8
|   |   |   |   |   |   |   |   module_size < 3 : 24.73 (2/49.07)
|   |   |   |   |   |   |   |   module_size >= 3
|   |   |   |   |   |   |   |   |   cas_timing < 10 : 39.31 (1/0)
|   |   |   |   |   |   |   |   |   cas_timing >= 10 : 38.49 (2/132.37)
|   |   |   |   |   |   speed >= 1733
|   |   |   |   |   |   |   first_word_latency < 14.16 : 65.97 (3/39.34)
|   |   |   |   |   |   |   first_word_latency >= 14.16 : 44.73 (2/715.56)
|   |   |   |   |   module_size >= 6
|   |   |   |   |   |   module_type = DDR2 : 0 (0/0)
|   |   |   |   |   |   module_type = DDR4
|   |   |   |   |   |   |   cas_timing < 17
|   |   |   |   |   |   |   |   first_word_latency < 12.03 : 56.11 (1/0)
|   |   |   |   |   |   |   |   first_word_latency >= 12.03 : 120.12 (3/48.97)
|   |   |   |   |   |   |   cas_timing >= 17
|   |   |   |   |   |   |   |   module_size < 24 : 138.61 (3/12.93)
|   |   |   |   |   |   |   |   module_size >= 24 : 109.74 (3/43.77)
|   |   |   |   |   |   module_type = DDR3
|   |   |   |   |   |   |   first_word_latency < 12.03 : 74.48 (1/0)
|   |   |   |   |   |   |   first_word_latency >= 12.03 : 72.37 (2/129.73)
|   |   |   |   brand = G.Skill
|   |   |   |   |   speed < 2533
|   |   |   |   |   |   first_word_latency < 15
|   |   |   |   |   |   |   module_size < 6 : 31.65 (22/28.35)
|   |   |   |   |   |   |   module_size >= 6
|   |   |   |   |   |   |   |   module_size < 12
|   |   |   |   |   |   |   |   |   first_word_latency < 12.92
|   |   |   |   |   |   |   |   |   |   module_type = DDR2 : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   module_type = DDR4 : 81.17 (2/1582.85)
|   |   |   |   |   |   |   |   |   |   module_type = DDR3
|   |   |   |   |   |   |   |   |   |   |   first_word_latency < 10.98 : 53.65 (1/0)
|   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 10.98
|   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 11.88 : 70.4 (3/510.75)
|   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 11.88 : 59.23 (1/0)
|   |   |   |   |   |   |   |   |   first_word_latency >= 12.92
|   |   |   |   |   |   |   |   |   |   speed < 2266.5
|   |   |   |   |   |   |   |   |   |   |   speed < 1466.5 : 57.25 (5/55.08)
|   |   |   |   |   |   |   |   |   |   |   speed >= 1466.5 : 52.14 (8/43.89)
|   |   |   |   |   |   |   |   |   |   speed >= 2266.5 : 41.88 (2/17.72)
|   |   |   |   |   |   |   |   module_size >= 12 : 77.37 (4/14.37)
|   |   |   |   |   |   first_word_latency >= 15 : 121.06 (1/0)
|   |   |   |   |   speed >= 2533
|   |   |   |   |   |   module_size < 24
|   |   |   |   |   |   |   first_word_latency < 12.38
|   |   |   |   |   |   |   |   module_size < 12
|   |   |   |   |   |   |   |   |   speed < 2900 : 109.53 (1/0)
|   |   |   |   |   |   |   |   |   speed >= 2900
|   |   |   |   |   |   |   |   |   |   first_word_latency < 10.33 : 45.38 (2/95.55)
|   |   |   |   |   |   |   |   |   |   first_word_latency >= 10.33 : 53.68 (3/20.07)
|   |   |   |   |   |   |   |   module_size >= 12
|   |   |   |   |   |   |   |   |   cas_timing < 15.5 : 104.27 (2/9.15)
|   |   |   |   |   |   |   |   |   cas_timing >= 15.5 : 84.63 (4/45.96)
|   |   |   |   |   |   |   first_word_latency >= 12.38
|   |   |   |   |   |   |   |   first_word_latency < 13.88 : 39.08 (1/0)
|   |   |   |   |   |   |   |   first_word_latency >= 13.88
|   |   |   |   |   |   |   |   |   module_size < 12 : 45.97 (2/48.51)
|   |   |   |   |   |   |   |   |   module_size >= 12 : 80.84 (1/0)
|   |   |   |   |   |   module_size >= 24 : 146.95 (2/32.83)
|   |   |   |   brand = GeIL
|   |   |   |   |   module_size < 6 : 40.39 (2/10.14)
|   |   |   |   |   module_size >= 6
|   |   |   |   |   |   first_word_latency < 13.78 : 61.12 (2/92.45)
|   |   |   |   |   |   first_word_latency >= 13.78 : 56.99 (1/0)
|   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   brand = HP
|   |   |   |   |   first_word_latency < 13.63
|   |   |   |   |   |   module_size < 12 : 103 (1/0)
|   |   |   |   |   |   module_size >= 12 : 44.41 (1/0)
|   |   |   |   |   first_word_latency >= 13.63 : 159.02 (1/0)
|   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   brand = Kingston
|   |   |   |   |   module_size < 12
|   |   |   |   |   |   speed < 3800
|   |   |   |   |   |   |   cas_timing < 9.5
|   |   |   |   |   |   |   |   first_word_latency < 11.88
|   |   |   |   |   |   |   |   |   first_word_latency < 10.45 : 53.77 (1/0)
|   |   |   |   |   |   |   |   |   first_word_latency >= 10.45
|   |   |   |   |   |   |   |   |   |   module_size < 6 : 29.86 (3/23.88)
|   |   |   |   |   |   |   |   |   |   module_size >= 6 : 95 (1/0)
|   |   |   |   |   |   |   |   first_word_latency >= 11.88
|   |   |   |   |   |   |   |   |   cas_timing < 3.5 : 20.69 (1/0)
|   |   |   |   |   |   |   |   |   cas_timing >= 3.5
|   |   |   |   |   |   |   |   |   |   module_size < 6
|   |   |   |   |   |   |   |   |   |   |   module_size < 1.5
|   |   |   |   |   |   |   |   |   |   |   |   speed < 1066.5 : 19.98 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   speed >= 1066.5 : 2 (1/0)
|   |   |   |   |   |   |   |   |   |   |   module_size >= 1.5
|   |   |   |   |   |   |   |   |   |   |   |   module_type = DDR2 : 38.34 (4/28.67)
|   |   |   |   |   |   |   |   |   |   |   |   module_type = DDR4 : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   module_type = DDR3 : 25.23 (14/40.23)
|   |   |   |   |   |   |   |   |   |   module_size >= 6 : 50.8 (7/31.62)
|   |   |   |   |   |   |   cas_timing >= 9.5
|   |   |   |   |   |   |   |   first_word_latency < 13.96
|   |   |   |   |   |   |   |   |   module_size < 6
|   |   |   |   |   |   |   |   |   |   first_word_latency < 13.25 : 31.52 (17/50.29)
|   |   |   |   |   |   |   |   |   |   first_word_latency >= 13.25
|   |   |   |   |   |   |   |   |   |   |   module_size < 3 : 21.89 (1/0)
|   |   |   |   |   |   |   |   |   |   |   module_size >= 3 : 49.69 (6/2211.95)
|   |   |   |   |   |   |   |   |   module_size >= 6
|   |   |   |   |   |   |   |   |   |   first_word_latency < 12.25
|   |   |   |   |   |   |   |   |   |   |   first_word_latency < 11.9
|   |   |   |   |   |   |   |   |   |   |   |   cas_timing < 12 : 57 (8/16.72)
|   |   |   |   |   |   |   |   |   |   |   |   cas_timing >= 12
|   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 10.95
|   |   |   |   |   |   |   |   |   |   |   |   |   |   speed < 3100
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 10.11
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed < 2833 : 47.63 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 2833 : 49.99 (2/100)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 10.11 : 61.97 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 3100 : 44.47 (4/7.23)
|   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 10.95 : 38.74 (1/0)
|   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 11.9 : 94.14 (2/2931.68)
|   |   |   |   |   |   |   |   |   |   first_word_latency >= 12.25
|   |   |   |   |   |   |   |   |   |   |   cas_timing < 13
|   |   |   |   |   |   |   |   |   |   |   |   cas_timing < 10.5 : 48.38 (5/25.16)
|   |   |   |   |   |   |   |   |   |   |   |   cas_timing >= 10.5 : 48.83 (7/86.93)
|   |   |   |   |   |   |   |   |   |   |   cas_timing >= 13
|   |   |   |   |   |   |   |   |   |   |   |   speed < 2933 : 40.58 (3/29.17)
|   |   |   |   |   |   |   |   |   |   |   |   speed >= 2933 : 54.99 (1/0)
|   |   |   |   |   |   |   |   first_word_latency >= 13.96
|   |   |   |   |   |   |   |   |   module_size < 6
|   |   |   |   |   |   |   |   |   |   speed < 2533 : 27.55 (4/169.08)
|   |   |   |   |   |   |   |   |   |   speed >= 2533 : 25.95 (2/1.1)
|   |   |   |   |   |   |   |   |   module_size >= 6 : 41.93 (8/50.96)
|   |   |   |   |   |   speed >= 3800 : 100.63 (1/0)
|   |   |   |   |   module_size >= 12
|   |   |   |   |   |   cas_timing < 17.5
|   |   |   |   |   |   |   first_word_latency < 11.84
|   |   |   |   |   |   |   |   first_word_latency < 9.49 : 101.99 (1/0)
|   |   |   |   |   |   |   |   first_word_latency >= 9.49
|   |   |   |   |   |   |   |   |   speed < 2833 : 71.24 (3/15.2)
|   |   |   |   |   |   |   |   |   speed >= 2833 : 79.99 (7/51)
|   |   |   |   |   |   |   first_word_latency >= 11.84
|   |   |   |   |   |   |   |   cas_timing < 16.5
|   |   |   |   |   |   |   |   |   first_word_latency < 12.25 : 141.76 (2/3652.39)
|   |   |   |   |   |   |   |   |   first_word_latency >= 12.25 : 89 (1/0)
|   |   |   |   |   |   |   |   cas_timing >= 16.5 : 74.56 (5/37.02)
|   |   |   |   |   |   cas_timing >= 17.5
|   |   |   |   |   |   |   first_word_latency < 11.88 : 267.16 (1/0)
|   |   |   |   |   |   |   first_word_latency >= 11.88
|   |   |   |   |   |   |   |   speed < 2933 : 71.89 (2/81.09)
|   |   |   |   |   |   |   |   speed >= 2933 : 84.89 (1/0)
|   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   brand = Mushkin
|   |   |   |   |   speed < 2399.5
|   |   |   |   |   |   cas_timing < 10
|   |   |   |   |   |   |   module_size < 3 : 29.5 (3/17.96)
|   |   |   |   |   |   |   module_size >= 3
|   |   |   |   |   |   |   |   first_word_latency < 13.32 : 56.43 (2/2.43)
|   |   |   |   |   |   |   |   first_word_latency >= 13.32 : 33.24 (3/38.42)
|   |   |   |   |   |   cas_timing >= 10
|   |   |   |   |   |   |   module_size < 12
|   |   |   |   |   |   |   |   module_type = DDR2 : 0 (0/0)
|   |   |   |   |   |   |   |   module_type = DDR4
|   |   |   |   |   |   |   |   |   module_size < 6 : 53.29 (3/438)
|   |   |   |   |   |   |   |   |   module_size >= 6 : 57.73 (1/0)
|   |   |   |   |   |   |   |   module_type = DDR3 : 59.27 (2/18.75)
|   |   |   |   |   |   |   module_size >= 12
|   |   |   |   |   |   |   |   speed < 1866.5 : 197.1 (1/0)
|   |   |   |   |   |   |   |   speed >= 1866.5 : 120.99 (1/0)
|   |   |   |   |   speed >= 2399.5 : 217.89 (1/0)
|   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   brand = PNY
|   |   |   |   |   module_size < 12 : 33.59 (2/27.35)
|   |   |   |   |   module_size >= 12 : 71.59 (1/0)
|   |   |   |   brand = Patriot
|   |   |   |   |   module_size < 6
|   |   |   |   |   |   module_size < 3
|   |   |   |   |   |   |   speed < 1066.5 : 17.37 (3/11.73)
|   |   |   |   |   |   |   speed >= 1066.5
|   |   |   |   |   |   |   |   module_size < 1.5 : 16.64 (1/0)
|   |   |   |   |   |   |   |   module_size >= 1.5 : 29.35 (3/141.24)
|   |   |   |   |   |   module_size >= 3
|   |   |   |   |   |   |   cas_timing < 16.5
|   |   |   |   |   |   |   |   first_word_latency < 13.91 : 22.96 (8/28.58)
|   |   |   |   |   |   |   |   first_word_latency >= 13.91 : 44.33 (1/0)
|   |   |   |   |   |   |   cas_timing >= 16.5 : 40.95 (3/294.28)
|   |   |   |   |   module_size >= 6
|   |   |   |   |   |   module_size < 12
|   |   |   |   |   |   |   first_word_latency < 14.12
|   |   |   |   |   |   |   |   first_word_latency < 13.91
|   |   |   |   |   |   |   |   |   first_word_latency < 13.63
|   |   |   |   |   |   |   |   |   |   module_type = DDR2 : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   module_type = DDR4 : 38.4 (2/0.25)
|   |   |   |   |   |   |   |   |   |   module_type = DDR3 : 73.83 (2/1227.45)
|   |   |   |   |   |   |   |   |   first_word_latency >= 13.63 : 37.94 (3/25.38)
|   |   |   |   |   |   |   |   first_word_latency >= 13.91 : 83.92 (2/1502.34)
|   |   |   |   |   |   |   first_word_latency >= 14.12
|   |   |   |   |   |   |   |   first_word_latency < 14.21 : 43.29 (5/63.92)
|   |   |   |   |   |   |   |   first_word_latency >= 14.21 : 34.9 (1/0)
|   |   |   |   |   |   module_size >= 12 : 65.78 (8/8.36)
|   |   |   |   brand = Samsung
|   |   |   |   |   speed < 1733 : 18.98 (1/0)
|   |   |   |   |   speed >= 1733
|   |   |   |   |   |   speed < 2266 : 120 (1/0)
|   |   |   |   |   |   speed >= 2266
|   |   |   |   |   |   |   module_size < 24 : 176.1 (2/9196.81)
|   |   |   |   |   |   |   module_size >= 24 : 154.77 (1/0)
|   |   |   |   brand = Silicon Power : 37.48 (2/33.7)
|   |   |   |   brand = Team
|   |   |   |   |   module_type = DDR2 : 0 (0/0)
|   |   |   |   |   module_type = DDR4
|   |   |   |   |   |   first_word_latency < 12.92
|   |   |   |   |   |   |   module_size < 12
|   |   |   |   |   |   |   |   speed < 2833
|   |   |   |   |   |   |   |   |   speed < 2533 : 43.36 (1/0)
|   |   |   |   |   |   |   |   |   speed >= 2533 : 63.38 (2/19.58)
|   |   |   |   |   |   |   |   speed >= 2833 : 44.24 (2/7.48)
|   |   |   |   |   |   |   module_size >= 12
|   |   |   |   |   |   |   |   first_word_latency < 11.58 : 86.4 (2/74.05)
|   |   |   |   |   |   |   |   first_word_latency >= 11.58 : 160.75 (1/0)
|   |   |   |   |   |   first_word_latency >= 12.92
|   |   |   |   |   |   |   module_size < 12
|   |   |   |   |   |   |   |   first_word_latency < 13.42 : 32.58 (2/29.81)
|   |   |   |   |   |   |   |   first_word_latency >= 13.42
|   |   |   |   |   |   |   |   |   first_word_latency < 13.88 : 55.67 (2/19.71)
|   |   |   |   |   |   |   |   |   first_word_latency >= 13.88 : 37.89 (1/0)
|   |   |   |   |   |   |   module_size >= 12 : 84.9 (1/0)
|   |   |   |   |   module_type = DDR3
|   |   |   |   |   |   speed < 1466.5 : 57.17 (2/573.6)
|   |   |   |   |   |   speed >= 1466.5 : 31.86 (2/21.48)
|   |   |   |   brand = Thermaltake : 45.03 (1/0)
|   |   |   |   brand = Transcend
|   |   |   |   |   module_size < 3 : 23.53 (1/0)
|   |   |   |   |   module_size >= 3 : 43.02 (4/23.98)
|   |   |   |   brand = V7 : 35.97 (1/0)
|   |   |   error_correction = True
|   |   |   |   module_size < 24
|   |   |   |   |   module_size < 12
|   |   |   |   |   |   first_word_latency < 13.32 : 189.87 (1/0)
|   |   |   |   |   |   first_word_latency >= 13.32
|   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   brand = Corsair : 0 (0/0)
|   |   |   |   |   |   |   brand = Crucial
|   |   |   |   |   |   |   |   cas_timing < 16
|   |   |   |   |   |   |   |   |   module_type = DDR2 : 0 (0/0)
|   |   |   |   |   |   |   |   |   module_type = DDR4 : 182.89 (1/0)
|   |   |   |   |   |   |   |   |   module_type = DDR3
|   |   |   |   |   |   |   |   |   |   speed < 1733
|   |   |   |   |   |   |   |   |   |   |   module_size < 6 : 145.6 (1/0)
|   |   |   |   |   |   |   |   |   |   |   module_size >= 6 : 128.3 (2/33.52)
|   |   |   |   |   |   |   |   |   |   speed >= 1733 : 59.75 (1/0)
|   |   |   |   |   |   |   |   cas_timing >= 16
|   |   |   |   |   |   |   |   |   first_word_latency < 14.21 : 110.24 (3/501.25)
|   |   |   |   |   |   |   |   |   first_word_latency >= 14.21
|   |   |   |   |   |   |   |   |   |   speed < 2799.5 : 62.74 (1/0)
|   |   |   |   |   |   |   |   |   |   speed >= 2799.5 : 119 (1/0)
|   |   |   |   |   |   |   brand = G.Skill : 0 (0/0)
|   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   brand = Kingston
|   |   |   |   |   |   |   |   speed < 1733
|   |   |   |   |   |   |   |   |   first_word_latency < 13.63
|   |   |   |   |   |   |   |   |   |   module_size < 6
|   |   |   |   |   |   |   |   |   |   |   module_size < 3 : 28.8 (1/0)
|   |   |   |   |   |   |   |   |   |   |   module_size >= 3 : 49.88 (1/0)
|   |   |   |   |   |   |   |   |   |   module_size >= 6 : 102.43 (3/1704.21)
|   |   |   |   |   |   |   |   |   first_word_latency >= 13.63
|   |   |   |   |   |   |   |   |   |   module_type = DDR2 : 102.01 (1/0)
|   |   |   |   |   |   |   |   |   |   module_type = DDR4 : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   module_type = DDR3
|   |   |   |   |   |   |   |   |   |   |   module_size < 6 : 81.79 (5/2957.22)
|   |   |   |   |   |   |   |   |   |   |   module_size >= 6 : 94.42 (6/3604.57)
|   |   |   |   |   |   |   |   speed >= 1733
|   |   |   |   |   |   |   |   |   module_size < 6 : 56.95 (2/9.83)
|   |   |   |   |   |   |   |   |   module_size >= 6
|   |   |   |   |   |   |   |   |   |   first_word_latency < 14.05 : 76.45 (1/0)
|   |   |   |   |   |   |   |   |   |   first_word_latency >= 14.05 : 60.25 (5/49.62)
|   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   brand = Mushkin : 54.38 (1/0)
|   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   module_size >= 12
|   |   |   |   |   |   speed < 2266.5
|   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   brand = Corsair : 0 (0/0)
|   |   |   |   |   |   |   brand = Crucial
|   |   |   |   |   |   |   |   first_word_latency < 13.91
|   |   |   |   |   |   |   |   |   first_word_latency < 13.44 : 132.37 (2/3074.15)
|   |   |   |   |   |   |   |   |   first_word_latency >= 13.44 : 64 (1/0)
|   |   |   |   |   |   |   |   first_word_latency >= 13.91
|   |   |   |   |   |   |   |   |   module_type = DDR2 : 0 (0/0)
|   |   |   |   |   |   |   |   |   module_type = DDR4 : 259.99 (1/0)
|   |   |   |   |   |   |   |   |   module_type = DDR3 : 236.94 (1/0)
|   |   |   |   |   |   |   brand = G.Skill : 0 (0/0)
|   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   brand = IBM
|   |   |   |   |   |   |   |   speed < 1466.5 : 111.99 (1/0)
|   |   |   |   |   |   |   |   speed >= 1466.5 : 151.33 (1/0)
|   |   |   |   |   |   |   brand = Kingston
|   |   |   |   |   |   |   |   speed < 1999.5
|   |   |   |   |   |   |   |   |   first_word_latency < 13.84
|   |   |   |   |   |   |   |   |   |   speed < 1466.5 : 157.49 (3/437.69)
|   |   |   |   |   |   |   |   |   |   speed >= 1466.5 : 251.46 (4/10680.14)
|   |   |   |   |   |   |   |   |   first_word_latency >= 13.84 : 117.26 (2/92.93)
|   |   |   |   |   |   |   |   speed >= 1999.5 : 289.68 (3/4822.83)
|   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   brand = Mushkin : 62.07 (2/0.55)
|   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   brand = Samsung
|   |   |   |   |   |   |   |   speed < 1866.5 : 131.93 (2/487.31)
|   |   |   |   |   |   |   |   speed >= 1866.5 : 86 (1/0)
|   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   speed >= 2266.5
|   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   brand = Corsair : 0 (0/0)
|   |   |   |   |   |   |   brand = Crucial
|   |   |   |   |   |   |   |   cas_timing < 19 : 180.53 (1/0)
|   |   |   |   |   |   |   |   cas_timing >= 19 : 122.26 (1/0)
|   |   |   |   |   |   |   brand = G.Skill : 0 (0/0)
|   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   brand = Kingston
|   |   |   |   |   |   |   |   cas_timing < 18 : 57.73 (2/340.22)
|   |   |   |   |   |   |   |   cas_timing >= 18 : 100.56 (3/14.7)
|   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   brand = Samsung : 259 (1/0)
|   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   module_size >= 24
|   |   |   |   |   module_type = DDR2 : 0 (0/0)
|   |   |   |   |   module_type = DDR4
|   |   |   |   |   |   speed < 2266.5
|   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   brand = Corsair : 0 (0/0)
|   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   brand = G.Skill : 0 (0/0)
|   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   brand = Kingston : 186.2 (1/0)
|   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   brand = Samsung : 140 (1/0)
|   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   speed >= 2266.5
|   |   |   |   |   |   |   first_word_latency < 14.21
|   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Corsair : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Crucial : 342 (2/3249)
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
|   |   |   |   |   |   |   |   brand = Samsung : 314.85 (2/78.32)
|   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   first_word_latency >= 14.21
|   |   |   |   |   |   |   |   speed < 2799.5
|   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Corsair : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Crucial
|   |   |   |   |   |   |   |   |   |   module_size < 48 : 213 (1/0)
|   |   |   |   |   |   |   |   |   |   module_size >= 48 : 499 (1/0)
|   |   |   |   |   |   |   |   |   brand = G.Skill : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Kingston
|   |   |   |   |   |   |   |   |   |   module_size < 48 : 178.39 (3/18.04)
|   |   |   |   |   |   |   |   |   |   module_size >= 48 : 555.66 (1/0)
|   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Samsung : 180.99 (1/0)
|   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   speed >= 2799.5
|   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Corsair : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = G.Skill : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Kingston : 167.99 (1/0)
|   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Samsung : 340 (1/0)
|   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   module_type = DDR3
|   |   |   |   |   |   speed < 1466.5 : 432.31 (1/0)
|   |   |   |   |   |   speed >= 1466.5
|   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   brand = Corsair : 0 (0/0)
|   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   brand = G.Skill : 0 (0/0)
|   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   brand = Kingston : 276 (1/0)
|   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   brand = Samsung : 409.93 (1/0)
|   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   number_of_modules >= 1.5
|   |   |   speed < 4450
|   |   |   |   module_size < 12
|   |   |   |   |   module_size < 6
|   |   |   |   |   |   first_word_latency < 10.16
|   |   |   |   |   |   |   first_word_latency < 8.17 : 287.74 (1/0)
|   |   |   |   |   |   |   first_word_latency >= 8.17
|   |   |   |   |   |   |   |   cas_timing < 11.5
|   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   |   |   speed < 1733 : 109 (1/0)
|   |   |   |   |   |   |   |   |   |   speed >= 1733 : 45.63 (1/0)
|   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   |   |   cas_timing < 9.5
|   |   |   |   |   |   |   |   |   |   |   speed < 1733 : 80.2 (1/0)
|   |   |   |   |   |   |   |   |   |   |   speed >= 1733
|   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 9.04 : 100.15 (3/754.35)
|   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 9.04 : 101.67 (2/802.31)
|   |   |   |   |   |   |   |   |   |   cas_timing >= 9.5
|   |   |   |   |   |   |   |   |   |   |   speed < 2266.5 : 62.4 (1/0)
|   |   |   |   |   |   |   |   |   |   |   speed >= 2266.5 : 78.91 (3/29.72)
|   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Kingston
|   |   |   |   |   |   |   |   |   |   module_size < 3
|   |   |   |   |   |   |   |   |   |   |   number_of_modules < 2.5 : 89.03 (1/0)
|   |   |   |   |   |   |   |   |   |   |   number_of_modules >= 2.5
|   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 9.32
|   |   |   |   |   |   |   |   |   |   |   |   |   module_size < 1.5 : 86.24 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   module_size >= 1.5 : 151.03 (2/59.68)
|   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 9.32
|   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 9.82 : 54.57 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 9.82 : 77.86 (1/0)
|   |   |   |   |   |   |   |   |   |   module_size >= 3 : 68.91 (1/0)
|   |   |   |   |   |   |   |   |   brand = Klevv : 28.19 (1/0)
|   |   |   |   |   |   |   |   |   brand = Mushkin : 86.99 (2/4)
|   |   |   |   |   |   |   |   |   brand = OCZ
|   |   |   |   |   |   |   |   |   |   speed < 1200 : 73.68 (1/0)
|   |   |   |   |   |   |   |   |   |   speed >= 1200
|   |   |   |   |   |   |   |   |   |   |   first_word_latency < 9.38 : 184.7 (1/0)
|   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 9.38
|   |   |   |   |   |   |   |   |   |   |   |   number_of_modules < 2.5 : 91.85 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   number_of_modules >= 2.5 : 142.01 (1/0)
|   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Patriot
|   |   |   |   |   |   |   |   |   |   first_word_latency < 9.58
|   |   |   |   |   |   |   |   |   |   |   speed < 2200 : 71.04 (1/0)
|   |   |   |   |   |   |   |   |   |   |   speed >= 2200 : 111.95 (1/0)
|   |   |   |   |   |   |   |   |   |   first_word_latency >= 9.58 : 62.74 (2/0.7)
|   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Transcend : 76.06 (1/0)
|   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   cas_timing >= 11.5
|   |   |   |   |   |   |   |   |   speed < 3400
|   |   |   |   |   |   |   |   |   |   cas_timing < 15.5
|   |   |   |   |   |   |   |   |   |   |   first_word_latency < 8.79
|   |   |   |   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Corsair : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = G.Skill : 122.35 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Klevv : 227.73 (1/0)
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
|   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 8.79
|   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 9.88
|   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 9.38 : 132 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 9.38 : 69.17 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 9.88
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Corsair : 168.63 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = G.Skill : 129.98 (5/359.11)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Patriot : 122.56 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Team : 137.83 (2/188.51)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   cas_timing >= 15.5
|   |   |   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Corsair : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = G.Skill : 93.01 (5/681.04)
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
|   |   |   |   |   |   |   |   |   |   |   brand = Team : 133.62 (1/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   |   speed >= 3400
|   |   |   |   |   |   |   |   |   |   first_word_latency < 9.75 : 131.5 (2/32.43)
|   |   |   |   |   |   |   |   |   |   first_word_latency >= 9.75 : 229 (1/0)
|   |   |   |   |   |   first_word_latency >= 10.16
|   |   |   |   |   |   |   brand = ADATA
|   |   |   |   |   |   |   |   cas_timing < 16.5
|   |   |   |   |   |   |   |   |   speed < 2000 : 85.03 (2/22.47)
|   |   |   |   |   |   |   |   |   speed >= 2000 : 106.38 (2/7.32)
|   |   |   |   |   |   |   |   cas_timing >= 16.5 : 133.86 (1/0)
|   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   first_word_latency < 13.63
|   |   |   |   |   |   |   |   |   module_size < 3 : 34.64 (7/12.88)
|   |   |   |   |   |   |   |   |   module_size >= 3
|   |   |   |   |   |   |   |   |   |   speed < 1466.5 : 44.76 (6/23.96)
|   |   |   |   |   |   |   |   |   |   speed >= 1466.5
|   |   |   |   |   |   |   |   |   |   |   cas_timing < 14.5
|   |   |   |   |   |   |   |   |   |   |   |   number_of_modules < 2.5
|   |   |   |   |   |   |   |   |   |   |   |   |   module_type = DDR2 : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   module_type = DDR4 : 58.38 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   module_type = DDR3 : 51.81 (8/82.52)
|   |   |   |   |   |   |   |   |   |   |   |   number_of_modules >= 2.5 : 61.85 (1/0)
|   |   |   |   |   |   |   |   |   |   |   cas_timing >= 14.5 : 47.57 (8/18.24)
|   |   |   |   |   |   |   |   first_word_latency >= 13.63
|   |   |   |   |   |   |   |   |   first_word_latency < 13.91 : 76.94 (2/668.74)
|   |   |   |   |   |   |   |   |   first_word_latency >= 13.91 : 50.34 (3/30.72)
|   |   |   |   |   |   |   brand = Crucial
|   |   |   |   |   |   |   |   module_type = DDR2 : 76.19 (1/0)
|   |   |   |   |   |   |   |   module_type = DDR4 : 68.67 (1/0)
|   |   |   |   |   |   |   |   module_type = DDR3 : 91.44 (4/37.97)
|   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   cas_timing < 7.5
|   |   |   |   |   |   |   |   |   module_type = DDR2 : 54.11 (1/0)
|   |   |   |   |   |   |   |   |   module_type = DDR4 : 0 (0/0)
|   |   |   |   |   |   |   |   |   module_type = DDR3
|   |   |   |   |   |   |   |   |   |   speed < 1199.5
|   |   |   |   |   |   |   |   |   |   |   module_size < 2.5 : 33.9 (1/0)
|   |   |   |   |   |   |   |   |   |   |   module_size >= 2.5 : 49.32 (3/36.23)
|   |   |   |   |   |   |   |   |   |   speed >= 1199.5 : 33.89 (1/0)
|   |   |   |   |   |   |   |   cas_timing >= 7.5
|   |   |   |   |   |   |   |   |   speed < 1466.5
|   |   |   |   |   |   |   |   |   |   module_size < 3
|   |   |   |   |   |   |   |   |   |   |   cas_timing < 8.5 : 73.98 (1/0)
|   |   |   |   |   |   |   |   |   |   |   cas_timing >= 8.5 : 61.76 (2/539.87)
|   |   |   |   |   |   |   |   |   |   module_size >= 3 : 83.33 (5/4055.71)
|   |   |   |   |   |   |   |   |   speed >= 1466.5
|   |   |   |   |   |   |   |   |   |   first_word_latency < 11.34
|   |   |   |   |   |   |   |   |   |   |   speed < 1733
|   |   |   |   |   |   |   |   |   |   |   |   module_size < 3 : 55.08 (2/342.44)
|   |   |   |   |   |   |   |   |   |   |   |   module_size >= 3 : 55.53 (4/7.29)
|   |   |   |   |   |   |   |   |   |   |   speed >= 1733 : 66.8 (3/3.44)
|   |   |   |   |   |   |   |   |   |   first_word_latency >= 11.34 : 49.43 (15/38.56)
|   |   |   |   |   |   |   brand = GeIL
|   |   |   |   |   |   |   |   module_size < 3 : 21.26 (1/0)
|   |   |   |   |   |   |   |   module_size >= 3 : 54.29 (2/230.89)
|   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   brand = Kingston
|   |   |   |   |   |   |   |   first_word_latency < 11.46
|   |   |   |   |   |   |   |   |   first_word_latency < 10.98
|   |   |   |   |   |   |   |   |   |   speed < 1599.5 : 41.96 (1/0)
|   |   |   |   |   |   |   |   |   |   speed >= 1599.5 : 55.77 (5/36.61)
|   |   |   |   |   |   |   |   |   first_word_latency >= 10.98
|   |   |   |   |   |   |   |   |   |   module_size < 3
|   |   |   |   |   |   |   |   |   |   |   module_size < 1.5 : 28 (2/3.71)
|   |   |   |   |   |   |   |   |   |   |   module_size >= 1.5 : 43.44 (1/0)
|   |   |   |   |   |   |   |   |   |   module_size >= 3 : 352.77 (3/176555.83)
|   |   |   |   |   |   |   |   first_word_latency >= 11.46
|   |   |   |   |   |   |   |   |   module_size < 1.5 : 29.34 (2/11.9)
|   |   |   |   |   |   |   |   |   module_size >= 1.5
|   |   |   |   |   |   |   |   |   |   number_of_modules < 2.5
|   |   |   |   |   |   |   |   |   |   |   speed < 2133
|   |   |   |   |   |   |   |   |   |   |   |   speed < 1733
|   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 13 : 61.13 (5/57.57)
|   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 13
|   |   |   |   |   |   |   |   |   |   |   |   |   |   speed < 1466.5
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   module_size < 3 : 58 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   module_size >= 3 : 58.13 (8/245.44)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 1466.5 : 54.99 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   speed >= 1733 : 65.25 (1/0)
|   |   |   |   |   |   |   |   |   |   |   speed >= 2133 : 37.99 (1/0)
|   |   |   |   |   |   |   |   |   |   number_of_modules >= 2.5
|   |   |   |   |   |   |   |   |   |   |   speed < 1199.5 : 49.08 (1/0)
|   |   |   |   |   |   |   |   |   |   |   speed >= 1199.5
|   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 13.63
|   |   |   |   |   |   |   |   |   |   |   |   |   module_size < 3 : 55.55 (2/57.53)
|   |   |   |   |   |   |   |   |   |   |   |   |   module_size >= 3 : 141.54 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 13.63 : 99.95 (1/0)
|   |   |   |   |   |   |   brand = Klevv : 82.54 (1/0)
|   |   |   |   |   |   |   brand = Mushkin
|   |   |   |   |   |   |   |   cas_timing < 6.5
|   |   |   |   |   |   |   |   |   module_size < 3 : 53.12 (1/0)
|   |   |   |   |   |   |   |   |   module_size >= 3
|   |   |   |   |   |   |   |   |   |   speed < 733.5 : 195.8 (1/0)
|   |   |   |   |   |   |   |   |   |   speed >= 733.5 : 244.99 (1/0)
|   |   |   |   |   |   |   |   cas_timing >= 6.5
|   |   |   |   |   |   |   |   |   speed < 1866.5
|   |   |   |   |   |   |   |   |   |   first_word_latency < 13.32 : 41.72 (2/41.41)
|   |   |   |   |   |   |   |   |   |   first_word_latency >= 13.32 : 52.38 (6/27.36)
|   |   |   |   |   |   |   |   |   speed >= 1866.5
|   |   |   |   |   |   |   |   |   |   speed < 2266.5
|   |   |   |   |   |   |   |   |   |   |   module_type = DDR2 : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   module_type = DDR4 : 57.27 (1/0)
|   |   |   |   |   |   |   |   |   |   |   module_type = DDR3 : 88.99 (1/0)
|   |   |   |   |   |   |   |   |   |   speed >= 2266.5 : 89.49 (2/2.25)
|   |   |   |   |   |   |   brand = OCZ : 83.36 (1/0)
|   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   brand = Patriot
|   |   |   |   |   |   |   |   speed < 1866.5
|   |   |   |   |   |   |   |   |   first_word_latency < 12.38 : 51.18 (3/3.09)
|   |   |   |   |   |   |   |   |   first_word_latency >= 12.38 : 39.95 (5/49.28)
|   |   |   |   |   |   |   |   speed >= 1866.5
|   |   |   |   |   |   |   |   |   speed < 2533
|   |   |   |   |   |   |   |   |   |   first_word_latency < 12.81 : 191.63 (1/0)
|   |   |   |   |   |   |   |   |   |   first_word_latency >= 12.81
|   |   |   |   |   |   |   |   |   |   |   cas_timing < 15.5 : 60.66 (2/10.92)
|   |   |   |   |   |   |   |   |   |   |   cas_timing >= 15.5 : 41.9 (2/4)
|   |   |   |   |   |   |   |   |   speed >= 2533
|   |   |   |   |   |   |   |   |   |   first_word_latency < 11.63
|   |   |   |   |   |   |   |   |   |   |   speed < 2833 : 103.16 (1/0)
|   |   |   |   |   |   |   |   |   |   |   speed >= 2833 : 49.9 (1/0)
|   |   |   |   |   |   |   |   |   |   first_word_latency >= 11.63 : 43.4 (2/0.25)
|   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   brand = Team
|   |   |   |   |   |   |   |   speed < 2700
|   |   |   |   |   |   |   |   |   speed < 2000 : 97.31 (1/0)
|   |   |   |   |   |   |   |   |   speed >= 2000 : 84.2 (2/232.56)
|   |   |   |   |   |   |   |   speed >= 2700 : 143.07 (1/0)
|   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   module_size >= 6
|   |   |   |   |   |   cas_timing < 17.5
|   |   |   |   |   |   |   speed < 3766.5
|   |   |   |   |   |   |   |   number_of_modules < 2.5
|   |   |   |   |   |   |   |   |   first_word_latency < 9.7
|   |   |   |   |   |   |   |   |   |   brand = ADATA : 158.25 (4/800.56)
|   |   |   |   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   |   |   |   first_word_latency < 9.3
|   |   |   |   |   |   |   |   |   |   |   |   speed < 3333 : 234.99 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   speed >= 3333 : 166.88 (3/1193.41)
|   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 9.3
|   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 9.49 : 79.99 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 9.49 : 135.94 (2/64.96)
|   |   |   |   |   |   |   |   |   |   brand = Crucial : 110.59 (5/388.23)
|   |   |   |   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   |   |   |   first_word_latency < 8.06 : 264.52 (2/1774.09)
|   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 8.06
|   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 9.5
|   |   |   |   |   |   |   |   |   |   |   |   |   speed < 3100
|   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing < 10.5
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed < 2266.5
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed < 1866.5 : 141.61 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 1866.5
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing < 9.5 : 146.06 (2/276.56)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing >= 9.5 : 122.88 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 2266.5 : 155.6 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing >= 10.5
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed < 2700 : 107 (2/24.3)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 2700 : 147.38 (2/262.28)
|   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 3100
|   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing < 14.5 : 185.34 (7/2718.5)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing >= 14.5
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed < 3666.5
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 8.61 : 202.79 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 8.61
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed < 3433
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed < 3300 : 151.22 (3/1406.66)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 3300 : 122.56 (2/722.53)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 3433
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed < 3533 : 211.87 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 3533
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing < 16.5 : 150.57 (8/1384.98)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing >= 16.5 : 158.9 (5/1269.64)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 3666.5 : 203.99 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 9.5
|   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 9.62
|   |   |   |   |   |   |   |   |   |   |   |   |   |   speed < 3133 : 202.81 (2/1.39)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 3133 : 294.8 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 9.62 : 158.28 (1/0)
|   |   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Kingston
|   |   |   |   |   |   |   |   |   |   |   first_word_latency < 9.52
|   |   |   |   |   |   |   |   |   |   |   |   speed < 3533 : 121.56 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   speed >= 3533 : 163.73 (2/5584.57)
|   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 9.52 : 87.77 (1/0)
|   |   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Mushkin : 139.99 (1/0)
|   |   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Patriot
|   |   |   |   |   |   |   |   |   |   |   speed < 2900 : 173.27 (1/0)
|   |   |   |   |   |   |   |   |   |   |   speed >= 2900
|   |   |   |   |   |   |   |   |   |   |   |   cas_timing < 16.5 : 102.34 (2/647.45)
|   |   |   |   |   |   |   |   |   |   |   |   cas_timing >= 16.5
|   |   |   |   |   |   |   |   |   |   |   |   |   speed < 3666.5 : 87.23 (3/68.26)
|   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 3666.5 : 90.11 (2/67.49)
|   |   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Team : 179.86 (1/0)
|   |   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   |   first_word_latency >= 9.7
|   |   |   |   |   |   |   |   |   |   cas_timing < 13.5
|   |   |   |   |   |   |   |   |   |   |   cas_timing < 9.5
|   |   |   |   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   |   |   |   |   |   speed < 1466.5 : 80.92 (4/71.19)
|   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 1466.5 : 76.51 (11/91.41)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Crucial : 146.61 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 12.38 : 89.3 (3/129.02)
|   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 12.38 : 120.05 (4/3093.15)
|   |   |   |   |   |   |   |   |   |   |   |   brand = GeIL : 59.99 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Kingston
|   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 12.38 : 148.57 (3/1287.56)
|   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 12.38 : 94.93 (6/45.84)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Mushkin
|   |   |   |   |   |   |   |   |   |   |   |   |   speed < 1199.5 : 120.99 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 1199.5 : 78.94 (3/30.58)
|   |   |   |   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Patriot : 79.8 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Team : 174.93 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   cas_timing >= 9.5
|   |   |   |   |   |   |   |   |   |   |   |   module_type = DDR2 : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   module_type = DDR4 : 85.63 (3/25.08)
|   |   |   |   |   |   |   |   |   |   |   |   module_type = DDR3
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = ADATA : 139.94 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing < 10.5 : 93.82 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing >= 10.5 : 69.99 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 12.14
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed < 1999.5 : 119.25 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 1999.5 : 141.71 (2/303.28)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 12.14 : 89.31 (5/43.57)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = GeIL
|   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing < 10.5 : 107.24 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing >= 10.5 : 15.48 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Kingston
|   |   |   |   |   |   |   |   |   |   |   |   |   |   speed < 1733
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing < 10.5 : 94.58 (5/75.63)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing >= 10.5 : 95.95 (3/9.87)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 1733
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing < 10.5 : 113.66 (4/330.78)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing >= 10.5 : 112.48 (2/42.12)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Mushkin : 99.33 (2/469.16)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = PNY : 248.13 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Team : 99 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   cas_timing >= 13.5
|   |   |   |   |   |   |   |   |   |   |   speed < 2266.5
|   |   |   |   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Corsair : 78.68 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = G.Skill : 117.95 (8/5463.83)
|   |   |   |   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Kingston : 330 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Mushkin : 134.99 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Patriot
|   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 13.6 : 103.09 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 13.6 : 227.67 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   speed >= 2266.5
|   |   |   |   |   |   |   |   |   |   |   |   cas_timing < 16.5
|   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 12.92
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = ADATA
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 10.33 : 103.61 (7/139.74)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 10.33
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 11.34 : 158.75 (5/1953.16)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 11.34 : 124.86 (6/1598.4)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed < 2799.5
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 11.46 : 113.55 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 11.46
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing < 15 : 97.37 (3/603.9)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing >= 15 : 102.05 (11/204.94)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 2799.5
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing < 15.5 : 123.91 (11/1365.24)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing >= 15.5
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 10.33 : 129.76 (17/2895.84)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 10.33
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed < 2966.5 : 121.58 (3/2378.34)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 2966.5 : 116.74 (5/1707.38)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Crucial
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing < 15.5 : 95.98 (5/378.34)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing >= 15.5
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 10.33 : 103.93 (6/96.8)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 10.33
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 11.34 : 94.48 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 11.34 : 102.18 (4/3344.48)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed < 2533 : 102.93 (9/1609.69)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 2533
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 10.69
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 10.33
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing < 15.5 : 124.6 (10/2449.79)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing >= 15.5 : 106.98 (17/661.88)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 10.33 : 104.84 (8/760.05)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 10.69
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing < 15.5
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed < 2733 : 113.47 (4/1191.83)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 2733 : 213.71 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing >= 15.5 : 155.57 (2/1910.13)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = GeIL : 94.4 (2/73.96)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Kingston
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing < 15.5
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed < 2533 : 135.64 (3/531.68)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 2533
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed < 2799.5 : 73.06 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 2799.5
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed < 2966.5 : 95.53 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 2966.5 : 108.98 (4/1039.95)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing >= 15.5
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed < 2933 : 146.43 (2/2870.28)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 2933 : 99.45 (3/108.13)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Mushkin
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 11.25 : 107.43 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 11.25 : 109.97 (2/529.92)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = PNY
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed < 2800 : 261.48 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 2800 : 76.98 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Patriot
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 10.96
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed < 3100
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 10.33 : 89.9 (2/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 10.33 : 74.57 (3/0.22)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 3100 : 88.78 (5/348.85)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 10.96
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 11.46 : 131.03 (3/1112.62)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 11.46 : 133.44 (2/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Team
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing < 15.5
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 11.25 : 194.52 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 11.25 : 108.02 (2/402.4)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing >= 15.5
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed < 3100 : 129.92 (10/3231.74)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 3100 : 117.02 (12/1968.23)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Thermaltake : 116.89 (6/11.67)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 12.92
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = ADATA : 135.75 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Corsair : 75.84 (4/14.67)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Crucial : 65.8 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = G.Skill : 86.72 (4/168.97)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = GeIL : 129.65 (3/2257.29)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Patriot : 99.99 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   cas_timing >= 16.5
|   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 13.16
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Corsair : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = G.Skill : 77.03 (2/56.18)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Mushkin : 94.23 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 13.16
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Corsair : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = G.Skill : 92.63 (5/160.33)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Patriot : 69.9 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   number_of_modules >= 2.5
|   |   |   |   |   |   |   |   |   speed < 1466.5
|   |   |   |   |   |   |   |   |   |   error_correction = False : 131.68 (1/0)
|   |   |   |   |   |   |   |   |   |   error_correction = True : 233.5 (1/0)
|   |   |   |   |   |   |   |   |   speed >= 1466.5 : 376.94 (1/0)
|   |   |   |   |   |   |   speed >= 3766.5
|   |   |   |   |   |   |   |   speed < 3900 : 381.63 (1/0)
|   |   |   |   |   |   |   |   speed >= 3900
|   |   |   |   |   |   |   |   |   speed < 4333
|   |   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Corsair : 289.99 (1/0)
|   |   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   |   |   |   speed < 4066.5 : 265.75 (2/3607.8)
|   |   |   |   |   |   |   |   |   |   |   speed >= 4066.5 : 299 (1/0)
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
|   |   |   |   |   |   |   |   |   speed >= 4333 : 256.52 (1/0)
|   |   |   |   |   |   cas_timing >= 17.5
|   |   |   |   |   |   |   speed < 3933
|   |   |   |   |   |   |   |   speed < 2733
|   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Corsair : 79.99 (1/0)
|   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   |   |   cas_timing < 18.5 : 101.3 (2/299.64)
|   |   |   |   |   |   |   |   |   |   cas_timing >= 18.5 : 72.7 (4/142.56)
|   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Mushkin : 134.99 (1/0)
|   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Patriot : 69.9 (1/0)
|   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Team : 86.49 (1/0)
|   |   |   |   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   speed >= 2733
|   |   |   |   |   |   |   |   |   brand = ADATA : 116.42 (1/0)
|   |   |   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   |   |   cas_timing < 18.5 : 134.98 (9/2822.94)
|   |   |   |   |   |   |   |   |   |   cas_timing >= 18.5 : 96.87 (3/4.73)
|   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   |   |   first_word_latency < 9.66 : 195.14 (1/0)
|   |   |   |   |   |   |   |   |   |   first_word_latency >= 9.66
|   |   |   |   |   |   |   |   |   |   |   speed < 3533
|   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 11.62 : 237.03 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 11.62 : 127.59 (1/0)
|   |   |   |   |   |   |   |   |   |   |   speed >= 3533
|   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 10.28 : 123.67 (4/657.69)
|   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 10.28 : 130.71 (4/1115.68)
|   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Kingston : 219.41 (1/0)
|   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Patriot : 93.9 (1/0)
|   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Team : 149.03 (5/1492.11)
|   |   |   |   |   |   |   |   |   brand = Thermaltake : 129.85 (4/159.27)
|   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   speed >= 3933
|   |   |   |   |   |   |   |   speed < 4199.5
|   |   |   |   |   |   |   |   |   brand = ADATA : 161 (1/0)
|   |   |   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   |   |   speed < 4066.5
|   |   |   |   |   |   |   |   |   |   |   cas_timing < 18.5 : 169.99 (2/25)
|   |   |   |   |   |   |   |   |   |   |   cas_timing >= 18.5 : 228.97 (3/10768.51)
|   |   |   |   |   |   |   |   |   |   speed >= 4066.5 : 336.39 (1/0)
|   |   |   |   |   |   |   |   |   brand = Crucial : 246.92 (3/6315.63)
|   |   |   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   |   |   cas_timing < 18.5 : 198.11 (3/1980.78)
|   |   |   |   |   |   |   |   |   |   cas_timing >= 18.5
|   |   |   |   |   |   |   |   |   |   |   speed < 4066.5 : 182.84 (1/0)
|   |   |   |   |   |   |   |   |   |   |   speed >= 4066.5 : 192.55 (2/176.89)
|   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Kingston : 212.16 (2/3734.43)
|   |   |   |   |   |   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Patriot : 133.88 (1/0)
|   |   |   |   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Team : 169.19 (3/687.39)
|   |   |   |   |   |   |   |   |   brand = Thermaltake : 153.55 (5/231.09)
|   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   speed >= 4199.5
|   |   |   |   |   |   |   |   |   first_word_latency < 8.41 : 390 (1/0)
|   |   |   |   |   |   |   |   |   first_word_latency >= 8.41
|   |   |   |   |   |   |   |   |   |   first_word_latency < 8.7
|   |   |   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Corsair : 394.56 (1/0)
|   |   |   |   |   |   |   |   |   |   |   brand = Crucial : 206.9 (1/0)
|   |   |   |   |   |   |   |   |   |   |   brand = G.Skill : 197 (1/0)
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
|   |   |   |   |   |   |   |   |   |   |   brand = Thermaltake : 177.64 (4/21.19)
|   |   |   |   |   |   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   first_word_latency >= 8.7
|   |   |   |   |   |   |   |   |   |   |   speed < 4299.5
|   |   |   |   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Corsair : 374.99 (3/8550)
|   |   |   |   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   |   |   brand = G.Skill : 241.48 (5/2311.05)
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
|   |   |   |   |   |   |   |   |   |   |   speed >= 4299.5 : 294.99 (1/0)
|   |   |   |   module_size >= 12
|   |   |   |   |   first_word_latency < 9.55
|   |   |   |   |   |   speed < 3933
|   |   |   |   |   |   |   speed < 3533
|   |   |   |   |   |   |   |   first_word_latency < 9.35
|   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Corsair : 235.39 (2/29.11)
|   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   |   |   speed < 3333
|   |   |   |   |   |   |   |   |   |   |   first_word_latency < 9.04 : 258.76 (7/514.31)
|   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 9.04 : 242.8 (2/36)
|   |   |   |   |   |   |   |   |   |   speed >= 3333 : 233.15 (1/0)
|   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Kingston : 192.67 (1/0)
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
|   |   |   |   |   |   |   |   first_word_latency >= 9.35
|   |   |   |   |   |   |   |   |   cas_timing < 15.5 : 322.86 (4/1190.28)
|   |   |   |   |   |   |   |   |   cas_timing >= 15.5 : 280.87 (1/0)
|   |   |   |   |   |   |   speed >= 3533
|   |   |   |   |   |   |   |   first_word_latency < 9.38
|   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Corsair : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Crucial
|   |   |   |   |   |   |   |   |   |   module_size < 24 : 220.58 (3/2689.38)
|   |   |   |   |   |   |   |   |   |   module_size >= 24 : 367.83 (1/0)
|   |   |   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   |   |   cas_timing < 17.5
|   |   |   |   |   |   |   |   |   |   |   cas_timing < 16.5 : 336.14 (8/8694.39)
|   |   |   |   |   |   |   |   |   |   |   cas_timing >= 16.5 : 365.07 (1/0)
|   |   |   |   |   |   |   |   |   |   cas_timing >= 17.5 : 260.13 (1/0)
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
|   |   |   |   |   |   |   |   first_word_latency >= 9.38
|   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Corsair : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = G.Skill : 181.75 (2/110.78)
|   |   |   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Kingston : 202.84 (1/0)
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
|   |   |   |   |   |   speed >= 3933
|   |   |   |   |   |   |   module_size < 24
|   |   |   |   |   |   |   |   cas_timing < 18.5
|   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   brand = Corsair : 351.43 (1/0)
|   |   |   |   |   |   |   |   |   brand = Crucial : 383.45 (2/1060.15)
|   |   |   |   |   |   |   |   |   brand = G.Skill : 387.2 (2/362.52)
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
|   |   |   |   |   |   |   |   cas_timing >= 18.5
|   |   |   |   |   |   |   |   |   first_word_latency < 9.35
|   |   |   |   |   |   |   |   |   |   speed < 4266.5 : 574.99 (1/0)
|   |   |   |   |   |   |   |   |   |   speed >= 4266.5 : 496 (1/0)
|   |   |   |   |   |   |   |   |   first_word_latency >= 9.35
|   |   |   |   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = Corsair : 516.64 (2/27773.89)
|   |   |   |   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   |   |   |   brand = G.Skill : 426.36 (3/8604.9)
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
|   |   |   |   |   |   |   module_size >= 24 : 765.9 (1/0)
|   |   |   |   |   first_word_latency >= 9.55
|   |   |   |   |   |   brand = ADATA
|   |   |   |   |   |   |   cas_timing < 17
|   |   |   |   |   |   |   |   first_word_latency < 12
|   |   |   |   |   |   |   |   |   first_word_latency < 10.33 : 191.68 (2/488.19)
|   |   |   |   |   |   |   |   |   first_word_latency >= 10.33 : 176.5 (1/0)
|   |   |   |   |   |   |   |   first_word_latency >= 12 : 504.3 (1/0)
|   |   |   |   |   |   |   cas_timing >= 17 : 209.9 (1/0)
|   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   module_size < 24
|   |   |   |   |   |   |   |   first_word_latency < 11.29
|   |   |   |   |   |   |   |   |   first_word_latency < 10.61
|   |   |   |   |   |   |   |   |   |   cas_timing < 15.5 : 192.01 (7/7952.9)
|   |   |   |   |   |   |   |   |   |   cas_timing >= 15.5
|   |   |   |   |   |   |   |   |   |   |   first_word_latency < 9.8 : 170.4 (1/0)
|   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 9.8
|   |   |   |   |   |   |   |   |   |   |   |   cas_timing < 18.5
|   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing < 17 : 216.58 (15/11073.18)
|   |   |   |   |   |   |   |   |   |   |   |   |   cas_timing >= 17 : 210.18 (4/2570.64)
|   |   |   |   |   |   |   |   |   |   |   |   cas_timing >= 18.5 : 195.9 (1/0)
|   |   |   |   |   |   |   |   |   first_word_latency >= 10.61
|   |   |   |   |   |   |   |   |   |   speed < 2966.5 : 184.8 (1/0)
|   |   |   |   |   |   |   |   |   |   speed >= 2966.5 : 238.43 (4/14035.6)
|   |   |   |   |   |   |   |   first_word_latency >= 11.29
|   |   |   |   |   |   |   |   |   speed < 2533
|   |   |   |   |   |   |   |   |   |   cas_timing < 13.5 : 111.23 (1/0)
|   |   |   |   |   |   |   |   |   |   cas_timing >= 13.5
|   |   |   |   |   |   |   |   |   |   |   speed < 2266.5 : 162.13 (1/0)
|   |   |   |   |   |   |   |   |   |   |   speed >= 2266.5 : 131.93 (2/3.74)
|   |   |   |   |   |   |   |   |   speed >= 2533
|   |   |   |   |   |   |   |   |   |   first_word_latency < 12.75 : 163.71 (6/3886.76)
|   |   |   |   |   |   |   |   |   |   first_word_latency >= 12.75 : 139.99 (1/0)
|   |   |   |   |   |   |   module_size >= 24
|   |   |   |   |   |   |   |   speed < 3100
|   |   |   |   |   |   |   |   |   first_word_latency < 11.34 : 297 (2/4.02)
|   |   |   |   |   |   |   |   |   first_word_latency >= 11.34
|   |   |   |   |   |   |   |   |   |   first_word_latency < 12.67 : 277.48 (2/305.9)
|   |   |   |   |   |   |   |   |   |   first_word_latency >= 12.67 : 254.99 (1/0)
|   |   |   |   |   |   |   |   speed >= 3100
|   |   |   |   |   |   |   |   |   cas_timing < 17 : 333.25 (2/280.06)
|   |   |   |   |   |   |   |   |   cas_timing >= 17 : 333.1 (2/293.09)
|   |   |   |   |   |   brand = Crucial
|   |   |   |   |   |   |   error_correction = False
|   |   |   |   |   |   |   |   first_word_latency < 10.33
|   |   |   |   |   |   |   |   |   cas_timing < 15.5 : 150.99 (4/927.96)
|   |   |   |   |   |   |   |   |   cas_timing >= 15.5
|   |   |   |   |   |   |   |   |   |   module_size < 24 : 159.51 (6/343.41)
|   |   |   |   |   |   |   |   |   |   module_size >= 24 : 320.84 (6/777.24)
|   |   |   |   |   |   |   |   first_word_latency >= 10.33
|   |   |   |   |   |   |   |   |   first_word_latency < 14
|   |   |   |   |   |   |   |   |   |   speed < 2833 : 131.32 (3/0.22)
|   |   |   |   |   |   |   |   |   |   speed >= 2833
|   |   |   |   |   |   |   |   |   |   |   first_word_latency < 12.21 : 186.76 (2/48.16)
|   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 12.21
|   |   |   |   |   |   |   |   |   |   |   |   module_size < 24 : 140.37 (2/384.16)
|   |   |   |   |   |   |   |   |   |   |   |   module_size >= 24 : 214.99 (1/0)
|   |   |   |   |   |   |   |   |   first_word_latency >= 14 : 209.57 (1/0)
|   |   |   |   |   |   |   error_correction = True
|   |   |   |   |   |   |   |   speed < 1733 : 298.64 (1/0)
|   |   |   |   |   |   |   |   speed >= 1733 : 414.85 (1/0)
|   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   module_size < 24
|   |   |   |   |   |   |   |   speed < 2733
|   |   |   |   |   |   |   |   |   first_word_latency < 13.42
|   |   |   |   |   |   |   |   |   |   cas_timing < 15.5 : 173 (4/2130.64)
|   |   |   |   |   |   |   |   |   |   cas_timing >= 15.5 : 163.96 (3/2757.69)
|   |   |   |   |   |   |   |   |   first_word_latency >= 13.42
|   |   |   |   |   |   |   |   |   |   first_word_latency < 14.12
|   |   |   |   |   |   |   |   |   |   |   first_word_latency < 13.78 : 144.69 (1/0)
|   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 13.78 : 143.43 (4/412.72)
|   |   |   |   |   |   |   |   |   |   first_word_latency >= 14.12
|   |   |   |   |   |   |   |   |   |   |   first_word_latency < 14.21 : 149.25 (3/728.22)
|   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 14.21 : 141.33 (1/0)
|   |   |   |   |   |   |   |   speed >= 2733
|   |   |   |   |   |   |   |   |   speed < 2966.5
|   |   |   |   |   |   |   |   |   |   speed < 2866.5 : 186.08 (2/48.72)
|   |   |   |   |   |   |   |   |   |   speed >= 2866.5 : 387.29 (1/0)
|   |   |   |   |   |   |   |   |   speed >= 2966.5
|   |   |   |   |   |   |   |   |   |   speed < 3400
|   |   |   |   |   |   |   |   |   |   |   cas_timing < 17
|   |   |   |   |   |   |   |   |   |   |   |   cas_timing < 15.5 : 169.89 (2/86.68)
|   |   |   |   |   |   |   |   |   |   |   |   cas_timing >= 15.5
|   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency < 10.33 : 179.72 (13/2210.5)
|   |   |   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 10.33 : 175.7 (8/1484.29)
|   |   |   |   |   |   |   |   |   |   |   cas_timing >= 17 : 211.24 (1/0)
|   |   |   |   |   |   |   |   |   |   speed >= 3400
|   |   |   |   |   |   |   |   |   |   |   first_word_latency < 10.28 : 194.55 (4/718.97)
|   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 10.28 : 219.98 (3/3469.46)
|   |   |   |   |   |   |   module_size >= 24
|   |   |   |   |   |   |   |   speed < 3400
|   |   |   |   |   |   |   |   |   speed < 2933 : 286.52 (1/0)
|   |   |   |   |   |   |   |   |   speed >= 2933 : 347.3 (3/822.43)
|   |   |   |   |   |   |   |   speed >= 3400 : 436.07 (5/6865.67)
|   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   brand = Gigabyte : 357.27 (1/0)
|   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   brand = Kingston
|   |   |   |   |   |   |   module_size < 24
|   |   |   |   |   |   |   |   first_word_latency < 12.25
|   |   |   |   |   |   |   |   |   speed < 2833
|   |   |   |   |   |   |   |   |   |   first_word_latency < 11.46 : 127.45 (1/0)
|   |   |   |   |   |   |   |   |   |   first_word_latency >= 11.46 : 141.81 (2/7.92)
|   |   |   |   |   |   |   |   |   speed >= 2833
|   |   |   |   |   |   |   |   |   |   speed < 3100 : 166.82 (3/587.61)
|   |   |   |   |   |   |   |   |   |   speed >= 3100 : 147.4 (3/11.61)
|   |   |   |   |   |   |   |   first_word_latency >= 12.25 : 223.9 (2/6314.69)
|   |   |   |   |   |   |   module_size >= 24
|   |   |   |   |   |   |   |   speed < 2933 : 269.52 (1/0)
|   |   |   |   |   |   |   |   speed >= 2933 : 269.71 (2/161.67)
|   |   |   |   |   |   brand = Klevv : 143.74 (1/0)
|   |   |   |   |   |   brand = Mushkin
|   |   |   |   |   |   |   first_word_latency < 12.95 : 149.75 (2/47.82)
|   |   |   |   |   |   |   first_word_latency >= 12.95
|   |   |   |   |   |   |   |   module_type = DDR2 : 0 (0/0)
|   |   |   |   |   |   |   |   module_type = DDR4
|   |   |   |   |   |   |   |   |   cas_timing < 17 : 204 (2/168.87)
|   |   |   |   |   |   |   |   |   cas_timing >= 17 : 250.99 (1/0)
|   |   |   |   |   |   |   |   module_type = DDR3 : 325.2 (1/0)
|   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   brand = PNY : 129.05 (1/0)
|   |   |   |   |   |   brand = Patriot
|   |   |   |   |   |   |   cas_timing < 17
|   |   |   |   |   |   |   |   module_size < 24 : 131.32 (4/39.48)
|   |   |   |   |   |   |   |   module_size >= 24 : 229.89 (1/0)
|   |   |   |   |   |   |   cas_timing >= 17
|   |   |   |   |   |   |   |   first_word_latency < 11.88 : 250.89 (2/0)
|   |   |   |   |   |   |   |   first_word_latency >= 11.88 : 219.9 (1/0)
|   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   brand = Team
|   |   |   |   |   |   |   first_word_latency < 10.33 : 223.74 (5/9766.07)
|   |   |   |   |   |   |   first_word_latency >= 10.33
|   |   |   |   |   |   |   |   first_word_latency < 11.88
|   |   |   |   |   |   |   |   |   speed < 2833 : 170.41 (2/0)
|   |   |   |   |   |   |   |   |   speed >= 2833 : 164.48 (4/87.87)
|   |   |   |   |   |   |   |   first_word_latency >= 11.88 : 227.96 (1/0)
|   |   |   |   |   |   brand = Thermaltake
|   |   |   |   |   |   |   module_size < 24 : 199.89 (1/0)
|   |   |   |   |   |   |   module_size >= 24 : 362.99 (1/0)
|   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   speed >= 4450
|   |   |   |   first_word_latency < 7.66
|   |   |   |   |   speed < 4833
|   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   brand = Corsair : 804.99 (2/5625)
|   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   brand = G.Skill : 740.55 (1/0)
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
|   |   |   |   |   speed >= 4833
|   |   |   |   |   |   speed < 4983 : 1004.99 (1/0)
|   |   |   |   |   |   speed >= 4983 : 925.64 (1/0)
|   |   |   |   first_word_latency >= 7.66
|   |   |   |   |   cas_timing < 18.5 : 525.72 (3/1352.25)
|   |   |   |   |   cas_timing >= 18.5
|   |   |   |   |   |   first_word_latency < 8.35 : 658.23 (3/4557.98)
|   |   |   |   |   |   first_word_latency >= 8.35 : 527.57 (1/0)
|   number_of_modules >= 3.5
|   |   module_size < 12
|   |   |   cas_timing < 16.5
|   |   |   |   first_word_latency < 9.38
|   |   |   |   |   cas_timing < 10.5
|   |   |   |   |   |   module_size < 6
|   |   |   |   |   |   |   speed < 2266.5 : 155.66 (1/0)
|   |   |   |   |   |   |   speed >= 2266.5 : 193.57 (1/0)
|   |   |   |   |   |   module_size >= 6 : 253.38 (1/0)
|   |   |   |   |   cas_timing >= 10.5
|   |   |   |   |   |   cas_timing < 14.5
|   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   brand = Corsair : 495 (1/0)
|   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   first_word_latency < 8.96
|   |   |   |   |   |   |   |   |   first_word_latency < 8.26 : 475.9 (1/0)
|   |   |   |   |   |   |   |   |   first_word_latency >= 8.26 : 367.7 (7/3409.79)
|   |   |   |   |   |   |   |   first_word_latency >= 8.96
|   |   |   |   |   |   |   |   |   module_type = DDR2 : 0 (0/0)
|   |   |   |   |   |   |   |   |   module_type = DDR4 : 270.35 (2/607.62)
|   |   |   |   |   |   |   |   |   module_type = DDR3 : 231 (1/0)
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
|   |   |   |   |   |   cas_timing >= 14.5
|   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   speed < 3333 : 290 (1/0)
|   |   |   |   |   |   |   |   speed >= 3333
|   |   |   |   |   |   |   |   |   first_word_latency < 9.06 : 364.99 (1/0)
|   |   |   |   |   |   |   |   |   first_word_latency >= 9.06 : 403.55 (2/5.27)
|   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   speed < 3533
|   |   |   |   |   |   |   |   |   module_size < 6 : 240.08 (1/0)
|   |   |   |   |   |   |   |   |   module_size >= 6 : 315.79 (2/198.81)
|   |   |   |   |   |   |   |   speed >= 3533 : 298.19 (4/1072.85)
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
|   |   |   |   first_word_latency >= 9.38
|   |   |   |   |   cas_timing < 10.5
|   |   |   |   |   |   number_of_modules < 5
|   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   module_size < 6 : 84.15 (2/17.14)
|   |   |   |   |   |   |   |   module_size >= 6 : 160.29 (5/260.78)
|   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   module_size < 6 : 126.87 (1/0)
|   |   |   |   |   |   |   |   module_size >= 6
|   |   |   |   |   |   |   |   |   first_word_latency < 11.88 : 193.54 (1/0)
|   |   |   |   |   |   |   |   |   first_word_latency >= 11.88
|   |   |   |   |   |   |   |   |   |   first_word_latency < 13 : 167.85 (2/73.1)
|   |   |   |   |   |   |   |   |   |   first_word_latency >= 13 : 172.2 (1/0)
|   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   brand = Kingston
|   |   |   |   |   |   |   |   module_size < 3 : 97.15 (1/0)
|   |   |   |   |   |   |   |   module_size >= 3
|   |   |   |   |   |   |   |   |   speed < 1599.5
|   |   |   |   |   |   |   |   |   |   module_size < 6
|   |   |   |   |   |   |   |   |   |   |   error_correction = False : 157.03 (1/0)
|   |   |   |   |   |   |   |   |   |   |   error_correction = True : 181.33 (1/0)
|   |   |   |   |   |   |   |   |   |   module_size >= 6 : 203.99 (1/0)
|   |   |   |   |   |   |   |   |   speed >= 1599.5 : 226.18 (2/84.18)
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
|   |   |   |   |   |   number_of_modules >= 5
|   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   brand = Corsair : 225.69 (1/0)
|   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   brand = G.Skill : 0 (0/0)
|   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   brand = Kingston : 250.91 (1/0)
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
|   |   |   |   |   cas_timing >= 10.5
|   |   |   |   |   |   brand = ADATA : 380.37 (2/12.32)
|   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   speed < 2266.5
|   |   |   |   |   |   |   |   module_size < 6 : 88.46 (3/124.14)
|   |   |   |   |   |   |   |   module_size >= 6 : 149.99 (1/0)
|   |   |   |   |   |   |   speed >= 2266.5
|   |   |   |   |   |   |   |   first_word_latency < 10.33
|   |   |   |   |   |   |   |   |   speed < 3100
|   |   |   |   |   |   |   |   |   |   module_size < 6 : 213.12 (2/180.36)
|   |   |   |   |   |   |   |   |   |   module_size >= 6 : 272.82 (5/6525.15)
|   |   |   |   |   |   |   |   |   speed >= 3100
|   |   |   |   |   |   |   |   |   |   speed < 3250
|   |   |   |   |   |   |   |   |   |   |   module_size < 6 : 233 (1/0)
|   |   |   |   |   |   |   |   |   |   |   module_size >= 6 : 232.2 (13/8757.7)
|   |   |   |   |   |   |   |   |   |   speed >= 3250 : 206.08 (1/0)
|   |   |   |   |   |   |   |   first_word_latency >= 10.33
|   |   |   |   |   |   |   |   |   speed < 2799.5
|   |   |   |   |   |   |   |   |   |   module_size < 6
|   |   |   |   |   |   |   |   |   |   |   first_word_latency < 11.88 : 166.27 (3/9402.87)
|   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 11.88 : 108.48 (3/448.95)
|   |   |   |   |   |   |   |   |   |   module_size >= 6
|   |   |   |   |   |   |   |   |   |   |   cas_timing < 15.5
|   |   |   |   |   |   |   |   |   |   |   |   speed < 2533 : 337.11 (3/28551.97)
|   |   |   |   |   |   |   |   |   |   |   |   speed >= 2533 : 373.4 (1/0)
|   |   |   |   |   |   |   |   |   |   |   cas_timing >= 15.5 : 175.18 (4/1019.49)
|   |   |   |   |   |   |   |   |   speed >= 2799.5
|   |   |   |   |   |   |   |   |   |   speed < 2966.5 : 164.99 (1/0)
|   |   |   |   |   |   |   |   |   |   speed >= 2966.5
|   |   |   |   |   |   |   |   |   |   |   module_size < 6 : 97.85 (1/0)
|   |   |   |   |   |   |   |   |   |   |   module_size >= 6 : 149.06 (1/0)
|   |   |   |   |   |   brand = Crucial
|   |   |   |   |   |   |   speed < 2533 : 167.02 (1/0)
|   |   |   |   |   |   |   speed >= 2533 : 142.91 (1/0)
|   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   module_size < 6
|   |   |   |   |   |   |   |   speed < 2533
|   |   |   |   |   |   |   |   |   speed < 2266.5 : 216.47 (1/0)
|   |   |   |   |   |   |   |   |   speed >= 2266.5 : 101.66 (3/5.38)
|   |   |   |   |   |   |   |   speed >= 2533
|   |   |   |   |   |   |   |   |   speed < 3350
|   |   |   |   |   |   |   |   |   |   speed < 2983 : 168.99 (1/0)
|   |   |   |   |   |   |   |   |   |   speed >= 2983 : 241.89 (1/0)
|   |   |   |   |   |   |   |   |   speed >= 3350 : 161.85 (1/0)
|   |   |   |   |   |   |   module_size >= 6
|   |   |   |   |   |   |   |   speed < 3300
|   |   |   |   |   |   |   |   |   cas_timing < 15.5
|   |   |   |   |   |   |   |   |   |   first_word_latency < 13.28
|   |   |   |   |   |   |   |   |   |   |   speed < 2900
|   |   |   |   |   |   |   |   |   |   |   |   speed < 2533 : 202.12 (6/1740)
|   |   |   |   |   |   |   |   |   |   |   |   speed >= 2533 : 187.72 (2/34.69)
|   |   |   |   |   |   |   |   |   |   |   speed >= 2900 : 204.57 (3/2.8)
|   |   |   |   |   |   |   |   |   |   first_word_latency >= 13.28 : 397.84 (1/0)
|   |   |   |   |   |   |   |   |   cas_timing >= 15.5
|   |   |   |   |   |   |   |   |   |   first_word_latency < 10.33 : 199.5 (9/1225.44)
|   |   |   |   |   |   |   |   |   |   first_word_latency >= 10.33
|   |   |   |   |   |   |   |   |   |   |   speed < 2700 : 198.69 (3/862.01)
|   |   |   |   |   |   |   |   |   |   |   speed >= 2700 : 269.3 (4/1414.99)
|   |   |   |   |   |   |   |   speed >= 3300 : 468.6 (1/0)
|   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   brand = Kingston
|   |   |   |   |   |   |   speed < 2266.5
|   |   |   |   |   |   |   |   module_size < 6 : 340.32 (1/0)
|   |   |   |   |   |   |   |   module_size >= 6 : 360.75 (1/0)
|   |   |   |   |   |   |   speed >= 2266.5
|   |   |   |   |   |   |   |   first_word_latency < 12.25
|   |   |   |   |   |   |   |   |   first_word_latency < 9.88 : 131.74 (1/0)
|   |   |   |   |   |   |   |   |   first_word_latency >= 9.88
|   |   |   |   |   |   |   |   |   |   first_word_latency < 11.63
|   |   |   |   |   |   |   |   |   |   |   speed < 2533 : 170.22 (1/0)
|   |   |   |   |   |   |   |   |   |   |   speed >= 2533
|   |   |   |   |   |   |   |   |   |   |   |   speed < 2966.5 : 211.63 (2/0.61)
|   |   |   |   |   |   |   |   |   |   |   |   speed >= 2966.5
|   |   |   |   |   |   |   |   |   |   |   |   |   speed < 3100 : 207.23 (3/211.3)
|   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 3100 : 209.15 (2/1337.73)
|   |   |   |   |   |   |   |   |   |   first_word_latency >= 11.63 : 171.5 (2/42.25)
|   |   |   |   |   |   |   |   first_word_latency >= 12.25
|   |   |   |   |   |   |   |   |   module_size < 6 : 105.66 (2/223.95)
|   |   |   |   |   |   |   |   |   module_size >= 6 : 161.3 (1/0)
|   |   |   |   |   |   brand = Klevv : 420.29 (1/0)
|   |   |   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   |   |   brand = Patriot : 246.09 (1/0)
|   |   |   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   |   |   brand = Team
|   |   |   |   |   |   |   speed < 2700 : 541.11 (1/0)
|   |   |   |   |   |   |   speed >= 2700 : 215.99 (1/0)
|   |   |   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   |   |   brand = V7 : 0 (0/0)
|   |   |   cas_timing >= 16.5
|   |   |   |   speed < 3666.5
|   |   |   |   |   first_word_latency < 12.38
|   |   |   |   |   |   cas_timing < 18.5
|   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   brand = Corsair : 297.55 (8/6777.61)
|   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   first_word_latency < 10.63 : 303.16 (3/8167.11)
|   |   |   |   |   |   |   |   first_word_latency >= 10.63 : 307.38 (1/0)
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
|   |   |   |   |   |   cas_timing >= 18.5 : 287.33 (1/0)
|   |   |   |   |   first_word_latency >= 12.38
|   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   brand = Corsair : 0 (0/0)
|   |   |   |   |   |   brand = Crucial
|   |   |   |   |   |   |   speed < 2533 : 193.11 (1/0)
|   |   |   |   |   |   |   speed >= 2533 : 271.18 (1/0)
|   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   first_word_latency < 14.21
|   |   |   |   |   |   |   |   first_word_latency < 13.84 : 186.47 (1/0)
|   |   |   |   |   |   |   |   first_word_latency >= 13.84 : 195.81 (2/1378.64)
|   |   |   |   |   |   |   first_word_latency >= 14.21 : 164.69 (1/0)
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
|   |   |   |   speed >= 3666.5
|   |   |   |   |   first_word_latency < 8.95
|   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   brand = Corsair : 1022.64 (1/0)
|   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   brand = G.Skill : 506.22 (2/446.05)
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
|   |   |   |   |   first_word_latency >= 8.95
|   |   |   |   |   |   speed < 3900
|   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   speed < 3766.5 : 588.54 (1/0)
|   |   |   |   |   |   |   |   speed >= 3766.5 : 352.24 (1/0)
|   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   brand = G.Skill : 331.03 (1/0)
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
|   |   |   |   |   |   speed >= 3900
|   |   |   |   |   |   |   first_word_latency < 9.1 : 440.35 (3/13891.3)
|   |   |   |   |   |   |   first_word_latency >= 9.1
|   |   |   |   |   |   |   |   first_word_latency < 9.35 : 584.99 (1/0)
|   |   |   |   |   |   |   |   first_word_latency >= 9.35 : 538.43 (5/26871.83)
|   |   module_size >= 12
|   |   |   speed < 3866.5
|   |   |   |   first_word_latency < 9.17
|   |   |   |   |   cas_timing < 15 : 995.17 (4/189586.53)
|   |   |   |   |   cas_timing >= 15
|   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   first_word_latency < 9 : 926.53 (2/488.41)
|   |   |   |   |   |   |   first_word_latency >= 9 : 824.99 (1/0)
|   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   brand = G.Skill : 612.23 (7/60597.31)
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
|   |   |   |   first_word_latency >= 9.17
|   |   |   |   |   module_size < 24
|   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   first_word_latency < 12.1
|   |   |   |   |   |   |   |   cas_timing < 17
|   |   |   |   |   |   |   |   |   first_word_latency < 9.8
|   |   |   |   |   |   |   |   |   |   speed < 3399.5 : 808.8 (1/0)
|   |   |   |   |   |   |   |   |   |   speed >= 3399.5 : 581.97 (1/0)
|   |   |   |   |   |   |   |   |   first_word_latency >= 9.8
|   |   |   |   |   |   |   |   |   |   cas_timing < 14.5 : 265.47 (1/0)
|   |   |   |   |   |   |   |   |   |   cas_timing >= 14.5
|   |   |   |   |   |   |   |   |   |   |   cas_timing < 15.5 : 453.04 (3/22130.39)
|   |   |   |   |   |   |   |   |   |   |   cas_timing >= 15.5
|   |   |   |   |   |   |   |   |   |   |   |   speed < 2799.5 : 469.76 (6/51611.66)
|   |   |   |   |   |   |   |   |   |   |   |   speed >= 2799.5
|   |   |   |   |   |   |   |   |   |   |   |   |   speed < 2966.5 : 314.99 (1/0)
|   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 2966.5
|   |   |   |   |   |   |   |   |   |   |   |   |   |   speed < 3100 : 437.26 (3/61397.34)
|   |   |   |   |   |   |   |   |   |   |   |   |   |   speed >= 3100 : 443.08 (11/18188.6)
|   |   |   |   |   |   |   |   cas_timing >= 17 : 600.43 (7/26290.92)
|   |   |   |   |   |   |   first_word_latency >= 12.1
|   |   |   |   |   |   |   |   cas_timing < 17
|   |   |   |   |   |   |   |   |   speed < 2266.5 : 227.06 (2/167.18)
|   |   |   |   |   |   |   |   |   speed >= 2266.5 : 244.99 (1/0)
|   |   |   |   |   |   |   |   cas_timing >= 17 : 300.95 (1/0)
|   |   |   |   |   |   brand = Crucial : 213.33 (1/0)
|   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   first_word_latency < 11.08
|   |   |   |   |   |   |   |   cas_timing < 16.5
|   |   |   |   |   |   |   |   |   cas_timing < 15.5 : 354.9 (1/0)
|   |   |   |   |   |   |   |   |   cas_timing >= 15.5
|   |   |   |   |   |   |   |   |   |   speed < 3333
|   |   |   |   |   |   |   |   |   |   |   first_word_latency < 10.79
|   |   |   |   |   |   |   |   |   |   |   |   speed < 3100 : 334.78 (4/2080)
|   |   |   |   |   |   |   |   |   |   |   |   speed >= 3100 : 333.37 (6/5183.76)
|   |   |   |   |   |   |   |   |   |   |   first_word_latency >= 10.79 : 386.7 (1/0)
|   |   |   |   |   |   |   |   |   |   speed >= 3333 : 407.53 (1/0)
|   |   |   |   |   |   |   |   cas_timing >= 16.5
|   |   |   |   |   |   |   |   |   first_word_latency < 9.72 : 395.03 (2/1.45)
|   |   |   |   |   |   |   |   |   first_word_latency >= 9.72
|   |   |   |   |   |   |   |   |   |   first_word_latency < 10.28 : 399.53 (2/5721.41)
|   |   |   |   |   |   |   |   |   |   first_word_latency >= 10.28 : 401.54 (2/2257.68)
|   |   |   |   |   |   |   first_word_latency >= 11.08
|   |   |   |   |   |   |   |   first_word_latency < 13.75
|   |   |   |   |   |   |   |   |   first_word_latency < 11.88 : 329.7 (1/0)
|   |   |   |   |   |   |   |   |   first_word_latency >= 11.88
|   |   |   |   |   |   |   |   |   |   cas_timing < 15.5 : 265.57 (3/1369.71)
|   |   |   |   |   |   |   |   |   |   cas_timing >= 15.5 : 284.58 (1/0)
|   |   |   |   |   |   |   |   first_word_latency >= 13.75 : 422.82 (1/0)
|   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   brand = Kingston
|   |   |   |   |   |   |   first_word_latency < 12.81
|   |   |   |   |   |   |   |   cas_timing < 15.5
|   |   |   |   |   |   |   |   |   first_word_latency < 11.25
|   |   |   |   |   |   |   |   |   |   first_word_latency < 9.88 : 298.37 (1/0)
|   |   |   |   |   |   |   |   |   |   first_word_latency >= 9.88
|   |   |   |   |   |   |   |   |   |   |   speed < 2700 : 319.01 (1/0)
|   |   |   |   |   |   |   |   |   |   |   speed >= 2700 : 324.32 (3/1599.87)
|   |   |   |   |   |   |   |   |   first_word_latency >= 11.25 : 258.89 (2/11.56)
|   |   |   |   |   |   |   |   cas_timing >= 15.5
|   |   |   |   |   |   |   |   |   first_word_latency < 9.42 : 429.99 (1/0)
|   |   |   |   |   |   |   |   |   first_word_latency >= 9.42
|   |   |   |   |   |   |   |   |   |   speed < 2933 : 342.47 (1/0)
|   |   |   |   |   |   |   |   |   |   speed >= 2933
|   |   |   |   |   |   |   |   |   |   |   speed < 3266.5 : 293.69 (4/144.16)
|   |   |   |   |   |   |   |   |   |   |   speed >= 3266.5 : 336.16 (1/0)
|   |   |   |   |   |   |   first_word_latency >= 12.81 : 556.42 (1/0)
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
|   |   |   |   |   module_size >= 24
|   |   |   |   |   |   first_word_latency < 10.33
|   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   speed < 3400 : 773.42 (4/13805.46)
|   |   |   |   |   |   |   |   speed >= 3400 : 619.87 (1/0)
|   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   brand = G.Skill
|   |   |   |   |   |   |   |   speed < 3400 : 818.52 (3/861.31)
|   |   |   |   |   |   |   |   speed >= 3400 : 724.89 (3/550.68)
|   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   brand = Kingston : 592.91 (2/487.97)
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
|   |   |   |   |   |   first_word_latency >= 10.33
|   |   |   |   |   |   |   first_word_latency < 13.42
|   |   |   |   |   |   |   |   first_word_latency < 12.67
|   |   |   |   |   |   |   |   |   first_word_latency < 11.34 : 642.61 (2/6153.62)
|   |   |   |   |   |   |   |   |   first_word_latency >= 11.34 : 643.99 (2/3474.51)
|   |   |   |   |   |   |   |   first_word_latency >= 12.67 : 524.99 (1/0)
|   |   |   |   |   |   |   first_word_latency >= 13.42 : 804.83 (1/0)
|   |   |   speed >= 3866.5
|   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   brand = Corsair : 1764.48 (2/5315.87)
|   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   brand = G.Skill : 828.99 (2/1494.21)
|   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   brand = V7 : 0 (0/0)
number_of_modules >= 7
|   speed < 3533
|   |   speed < 2533
|   |   |   module_size < 6 : 238.38 (1/0)
|   |   |   module_size >= 6
|   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   brand = Corsair : 0 (0/0)
|   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   brand = G.Skill
|   |   |   |   |   module_size < 12 : 337.7 (1/0)
|   |   |   |   |   module_size >= 12 : 611.09 (1/0)
|   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   brand = Kingston : 618.64 (1/0)
|   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   brand = V7 : 0 (0/0)
|   |   speed >= 2533
|   |   |   module_size < 24
|   |   |   |   cas_timing < 14.5
|   |   |   |   |   module_size < 12 : 927.28 (2/22.18)
|   |   |   |   |   module_size >= 12 : 1174.99 (1/0)
|   |   |   |   cas_timing >= 14.5
|   |   |   |   |   cas_timing < 15.5
|   |   |   |   |   |   first_word_latency < 9.69 : 444.1 (1/0)
|   |   |   |   |   |   first_word_latency >= 9.69
|   |   |   |   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   |   |   |   brand = Corsair
|   |   |   |   |   |   |   |   speed < 2833 : 577.83 (1/0)
|   |   |   |   |   |   |   |   speed >= 2833 : 653.54 (1/0)
|   |   |   |   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   |   |   |   brand = G.Skill : 0 (0/0)
|   |   |   |   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   |   |   |   brand = Kingston : 749 (1/0)
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
|   |   |   |   |   cas_timing >= 15.5
|   |   |   |   |   |   speed < 3333
|   |   |   |   |   |   |   module_size < 12
|   |   |   |   |   |   |   |   speed < 2933 : 809.95 (2/101)
|   |   |   |   |   |   |   |   speed >= 2933 : 639.96 (3/46867.27)
|   |   |   |   |   |   |   module_size >= 12
|   |   |   |   |   |   |   |   first_word_latency < 10.33 : 892 (4/17182.22)
|   |   |   |   |   |   |   |   first_word_latency >= 10.33 : 684.99 (1/0)
|   |   |   |   |   |   speed >= 3333 : 680.99 (1/0)
|   |   |   module_size >= 24
|   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   brand = Corsair
|   |   |   |   |   first_word_latency < 10.33 : 1215.69 (2/77.62)
|   |   |   |   |   first_word_latency >= 10.33
|   |   |   |   |   |   first_word_latency < 11.34 : 1249.62 (2/1198.89)
|   |   |   |   |   |   first_word_latency >= 11.34 : 1214.99 (1/0)
|   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   brand = G.Skill : 1500.47 (1/0)
|   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   brand = V7 : 0 (0/0)
|   speed >= 3533
|   |   module_size < 12
|   |   |   cas_timing < 18.5
|   |   |   |   cas_timing < 16 : 1012.88 (1/0)
|   |   |   |   cas_timing >= 16
|   |   |   |   |   speed < 3800 : 628.51 (2/1894.43)
|   |   |   |   |   speed >= 3800 : 635 (1/0)
|   |   |   cas_timing >= 18.5 : 1537.22 (1/0)
|   |   module_size >= 12
|   |   |   speed < 3700
|   |   |   |   brand = ADATA : 0 (0/0)
|   |   |   |   brand = Corsair
|   |   |   |   |   module_size < 24 : 1600.61 (2/2640.42)
|   |   |   |   |   module_size >= 24 : 1614.99 (1/0)
|   |   |   |   brand = Crucial : 0 (0/0)
|   |   |   |   brand = G.Skill : 1552.3 (4/23061.59)
|   |   |   |   brand = GeIL : 0 (0/0)
|   |   |   |   brand = Gigabyte : 0 (0/0)
|   |   |   |   brand = HP : 0 (0/0)
|   |   |   |   brand = IBM : 0 (0/0)
|   |   |   |   brand = Kingston : 0 (0/0)
|   |   |   |   brand = Klevv : 0 (0/0)
|   |   |   |   brand = Mushkin : 0 (0/0)
|   |   |   |   brand = OCZ : 0 (0/0)
|   |   |   |   brand = PNY : 0 (0/0)
|   |   |   |   brand = Patriot : 0 (0/0)
|   |   |   |   brand = Samsung : 0 (0/0)
|   |   |   |   brand = Silicon Power : 0 (0/0)
|   |   |   |   brand = Team : 0 (0/0)
|   |   |   |   brand = Thermaltake : 0 (0/0)
|   |   |   |   brand = Transcend : 0 (0/0)
|   |   |   |   brand = V7 : 0 (0/0)
|   |   |   speed >= 3700 : 2357.37 (2/217729.56)

Size of the tree : 2193

Time taken to build model: 0.04 seconds

=== Cross-validation ===
=== Summary ===

Correlation coefficient                  0.8915
Mean absolute error                     53.9916
Root mean squared error                105.6178
Relative absolute error                 39.7149 %
Root relative squared error             46.7164 %
Total Number of Instances             1791     
```

</details>

Vediamo se si può migliorare ulteriormente.

#### RandomForest

- Risultati del testing:</br>
  | Coefficiente di correlazione | Errore medio assoluto | Errore quadratico medio | Errore assoluto relativo | Errore quadratico relativo |
  |------------------------------|-----------------------|-------------------------|--------------------------|----------------------------|
  | 0.9128                       | 48.4379               | 92.3005                 | 35.6298 %                | 40.826 %                   |

- Visualizzazione dei risultati:</br>
  ![RandomForestResult](https://imgur.com/nAu1c3Z.png)

<details>
<summary>Output completo</summary>

```text
=== Run information ===

Scheme:       weka.classifiers.trees.RandomForest -P 100 -I 100 -num-slots 1 -K 0 -M 1.0 -V 0.001 -S 1
Relation:     memory-weka.filters.unsupervised.attribute.Remove-R2,7-weka.filters.unsupervised.attribute.Remove-R6
Instances:    1791
Attributes:   9
              brand
              module_type
              speed
              number_of_modules
              module_size
              first_word_latency
              cas_timing
              error_correction
              price
Test mode:    10-fold cross-validation

=== Classifier model (full training set) ===

RandomForest

Bagging with 100 iterations and base learner

weka.classifiers.trees.RandomTree -K 0 -M 1.0 -V 0.001 -S 1 -do-not-check-capabilities

Time taken to build model: 0.3 seconds

=== Cross-validation ===
=== Summary ===

Correlation coefficient                  0.9128
Mean absolute error                     48.4379
Root mean squared error                 92.3005
Relative absolute error                 35.6298 %
Root relative squared error             40.826  %
Total Number of Instances             1791     
```

</details>

Il RandomForest ha migliorato ulteriormente la performance, riducendo ancora l'errore medio.

Possiamo fare ancora di meglio?

#### M5P

> L'algoritmo M5P combina un convenzionale albero decisionale con la possibilità di una funzione di regressione lineare alle foglie.

L'algoritmo ha presentato risultati migliori se si effettuava il pruning. I risultati seguenti sono con pruning.

- Albero risultante:</br>
  ![M5PResult](https://imgur.com/4CzfatG.png)

- Risultati del testing:</br>
  | Coefficiente di correlazione | Errore medio assoluto | Errore quadratico medio | Errore assoluto relativo | Errore quadratico relativo |
  |------------------------------|-----------------------|-------------------------|--------------------------|----------------------------|
  | 0.8951                       | 52.6507               | 101.0392                | 38.7286 %                | 44.6912 %                  |

- Visualizzazione dei risultati:</br>
  ![M5PResult](https://imgur.com/yzSxAgt.png)

<details>
<summary>Output completo</summary>

```text
=== Run information ===

Scheme:       weka.classifiers.trees.M5P -M 4.0 -num-decimal-places 4
Relation:     memory-weka.filters.unsupervised.attribute.Remove-R2,7-weka.filters.unsupervised.attribute.Remove-R6
Instances:    1791
Attributes:   9
              brand
              module_type
              speed
              number_of_modules
              module_size
              first_word_latency
              cas_timing
              error_correction
              price
Test mode:    10-fold cross-validation

=== Classifier model (full training set) ===

M5 pruned model tree:
(using smoothed linear models)

number_of_modules <= 2.5 : 
|   speed <= 3100 : 
|   |   module_size <= 12 : LM1 (807/19.714%)
|   |   module_size >  12 : LM2 (215/29.019%)
|   speed >  3100 : 
|   |   speed <= 3666.5 : LM3 (321/24.619%)
|   |   speed >  3666.5 : 
|   |   |   first_word_latency <= 8.472 : 
|   |   |   |   speed <= 4450 : LM4 (7/23.972%)
|   |   |   |   speed >  4450 : LM5 (12/27.988%)
|   |   |   first_word_latency >  8.472 : LM6 (67/31.549%)
number_of_modules >  2.5 : 
|   module_size <= 12 : 
|   |   module_size <= 6 : LM7 (61/24.97%)
|   |   module_size >  6 : LM8 (160/60.27%)
|   module_size >  12 : 
|   |   cas_timing <= 17.5 : LM9 (111/87.589%)
|   |   cas_timing >  17.5 : LM10 (30/106.806%)

LM num: 1
price = 
 0.7176 * brand=Mushkin,HP,OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 74.9676 * brand=HP,OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 71.9322 * brand=OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 14.2186 * brand=PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 1.0062 * brand=ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 0.2491 * brand=Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 1.5272 * brand=IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 0.72 * brand=Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 0.8674 * brand=Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 15.0297 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 30.5804 * brand=Samsung,G.Skill,Corsair,Gigabyte 
 + 0.576 * brand=G.Skill,Corsair,Gigabyte 
 - 14.722 * brand=Corsair,Gigabyte 
 + 20.7398 * module_type=DDR4 
 + 0.0581 * speed 
 + 45.4793 * number_of_modules 
 + 7.1408 * module_size 
 + 5.7953 * first_word_latency 
 - 12.1849 * cas_timing 
 - 41.4013 * error_correction=False 
 - 50.1919

LM num: 2
price = 
 1.3055 * brand=Mushkin,HP,OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 2.5724 * brand=HP,OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 3.4996 * brand=OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 4.4453 * brand=PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 72.927 * brand=ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 75.7091 * brand=Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 3.2932 * brand=IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 2.5732 * brand=Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 0.8674 * brand=Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 0.1874 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 1.4258 * brand=Samsung,G.Skill,Corsair,Gigabyte 
 + 0.576 * brand=G.Skill,Corsair,Gigabyte 
 - 0.6684 * brand=Corsair,Gigabyte 
 - 0.8012 * module_type=DDR4 
 + 0.262 * speed 
 + 96.4379 * number_of_modules 
 + 6.3677 * module_size 
 + 56.3635 * first_word_latency 
 - 42.8655 * cas_timing 
 - 69.1833 * error_correction=False 
 - 745.7445

LM num: 3
price = 
 44.6888 * brand=Mushkin,HP,OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 1.6308 * brand=HP,OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 2.7075 * brand=OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 39.8645 * brand=PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 50.8754 * brand=ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 3.9567 * brand=IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 24.0078 * brand=Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 21.1571 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 60.0755 * brand=Samsung,G.Skill,Corsair,Gigabyte 
 + 0.576 * brand=G.Skill,Corsair,Gigabyte 
 + 1.6116 * brand=Corsair,Gigabyte 
 - 2.9224 * module_type=DDR4 
 + 0.4309 * speed 
 + 83.6457 * number_of_modules 
 + 9.1378 * module_size 
 + 104.904 * first_word_latency 
 - 78.2038 * cas_timing 
 - 3.1145 * error_correction=False 
 - 1342.7986

LM num: 4
price = 
 9.8011 * brand=Mushkin,HP,OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 1.6308 * brand=HP,OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 2.7075 * brand=OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 27.9677 * brand=PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 0.7944 * brand=ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 10.0279 * brand=IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 41.0328 * brand=Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 1.699 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 1.1871 * brand=Samsung,G.Skill,Corsair,Gigabyte 
 + 0.576 * brand=G.Skill,Corsair,Gigabyte 
 + 36.9729 * brand=Corsair,Gigabyte 
 - 2.9224 * module_type=DDR4 
 + 0.1694 * speed 
 + 12.5754 * number_of_modules 
 + 9.5933 * module_size 
 - 109.8316 * first_word_latency 
 + 37.5355 * cas_timing 
 - 3.1145 * error_correction=False 
 - 209.979

LM num: 5
price = 
 9.8011 * brand=Mushkin,HP,OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 1.6308 * brand=HP,OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 2.7075 * brand=OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 27.9677 * brand=PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 0.7944 * brand=ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 10.0279 * brand=IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 41.0328 * brand=Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 1.699 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 1.1871 * brand=Samsung,G.Skill,Corsair,Gigabyte 
 + 0.576 * brand=G.Skill,Corsair,Gigabyte 
 + 64.4716 * brand=Corsair,Gigabyte 
 - 2.9224 * module_type=DDR4 
 + 0.3219 * speed 
 + 12.5754 * number_of_modules 
 + 9.5933 * module_size 
 - 109.8316 * first_word_latency 
 + 37.5355 * cas_timing 
 - 3.1145 * error_correction=False 
 - 891.347

LM num: 6
price = 
 9.8011 * brand=Mushkin,HP,OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 1.6308 * brand=HP,OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 2.7075 * brand=OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 11.007 * brand=PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 0.7944 * brand=ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 10.0279 * brand=IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 66.898 * brand=Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 1.699 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 1.1871 * brand=Samsung,G.Skill,Corsair,Gigabyte 
 + 0.576 * brand=G.Skill,Corsair,Gigabyte 
 + 59.8276 * brand=Corsair,Gigabyte 
 - 2.9224 * module_type=DDR4 
 + 12.5754 * number_of_modules 
 + 19.9007 * module_size 
 - 93.2742 * first_word_latency 
 + 41.6755 * cas_timing 
 - 3.1145 * error_correction=False 
 + 74.6287

LM num: 7
price = 
 2.1495 * brand=HP,OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 2.8641 * brand=OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 39.3149 * brand=PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 64.5078 * brand=IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 1.254 * brand=Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 0.7105 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 18.1656 * brand=Samsung,G.Skill,Corsair,Gigabyte 
 + 2.2062 * brand=G.Skill,Corsair,Gigabyte 
 + 3.7627 * brand=Corsair,Gigabyte 
 - 42.5401 * module_type=DDR4 
 + 0.1079 * speed 
 + 57.4589 * number_of_modules 
 + 4.2816 * module_size 
 + 1.4577 * first_word_latency 
 - 5.6788 * cas_timing 
 - 28.1947 * error_correction=False 
 - 193.7363

LM num: 8
price = 
 2.1495 * brand=HP,OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 2.8641 * brand=OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 177.581 * brand=PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 18.9981 * brand=IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 1.254 * brand=Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 0.7105 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 142.4661 * brand=Samsung,G.Skill,Corsair,Gigabyte 
 + 2.2062 * brand=G.Skill,Corsair,Gigabyte 
 + 3.7627 * brand=Corsair,Gigabyte 
 - 154.659 * module_type=DDR4 
 + 0.0447 * speed 
 + 109.9551 * number_of_modules 
 + 2.8467 * module_size 
 - 29.3245 * first_word_latency 
 + 20.7831 * cas_timing 
 - 196.1385 * error_correction=False 
 - 6.6242

LM num: 9
price = 
 2.1495 * brand=HP,OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 2.8641 * brand=OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 128.3113 * brand=PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 14.7291 * brand=IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 1.254 * brand=Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 0.7105 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 11.6164 * brand=Samsung,G.Skill,Corsair,Gigabyte 
 + 2.2062 * brand=G.Skill,Corsair,Gigabyte 
 + 13.3923 * brand=Corsair,Gigabyte 
 - 24.1184 * module_type=DDR4 
 + 0.0677 * speed 
 + 108.4639 * number_of_modules 
 + 19.0523 * module_size 
 - 58.7271 * first_word_latency 
 - 1.838 * cas_timing 
 - 2.715 * error_correction=False 
 + 76.1341

LM num: 10
price = 
 2.1495 * brand=HP,OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 2.8641 * brand=OCZ,Kingston,PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 21.7301 * brand=PNY,ADATA,Team,IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 14.7291 * brand=IBM,Thermaltake,Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 + 1.254 * brand=Crucial,Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 0.7105 * brand=Klevv,Samsung,G.Skill,Corsair,Gigabyte 
 - 11.6164 * brand=Samsung,G.Skill,Corsair,Gigabyte 
 + 2.2062 * brand=G.Skill,Corsair,Gigabyte 
 + 259.2701 * brand=Corsair,Gigabyte 
 - 24.1184 * module_type=DDR4 
 + 1.9184 * speed 
 + 197.4194 * number_of_modules 
 + 21.8022 * module_size 
 + 459.7992 * first_word_latency 
 + 10.9511 * cas_timing 
 - 2.715 * error_correction=False 
 - 12446.6091

Number of Rules : 10

Time taken to build model: 0.16 seconds

=== Cross-validation ===
=== Summary ===

Correlation coefficient                  0.8951
Mean absolute error                     52.6507
Root mean squared error                101.0392
Relative absolute error                 38.7286 %
Root relative squared error             44.6912 %
Total Number of Instances             1791     
```

</details>

Possiamo dire che l'algoritmo M5P ha presentato ottime prestazioni.

### Scelta dell'algoritmo

Mettiamo a confronto le performance dei vari algoritmi usati

| Algoritmo         | Coefficiente di correlazione | Errore medio assoluto | Errore quadratico medio | Errore assoluto relativo | Errore quadratico relativo |
|-------------------|------------------------------|-----------------------|-------------------------|--------------------------|----------------------------|
| Linear Regression | 0.8056                       | 81.1316               | 133.8985                | 59.6785 %                | 59.2254 %                  |
| Random Tree       | 0.8915                       | 53.9916               | 105.6178                | 39.7149 %                | 46.7164 %                  |
| Random Forest     | 0.9128                       | 48.4379               | 92.3005                 | 35.6298 %                | 40.826 %                   |
| M5P               | 0.8951                       | 52.6507               | 101.0392                | 38.7286 %                | 44.6912 %                  |

Il Random Forest è stato quindi l'algoritmo che ha presentato risultati migliori; si sperava in migliori performance con l' M5P,
ma forse il dataset di partenza non era sufficientemente grande.

## Implementazione

L'implementazione del modello è stato realizzato con Java, attraverso l'utilizzo delle API di Weka.

### PCPFiller.java

Il "core" del nostro modulo Java; si occuperà di allenare, salvare e/o caricare il modello, di fare il filling dei dati e di salvare poi il risultato.

### PCPClassifier.java

`PCPClassifier` è un semplice wrapper per il `Classifier` di Weka, che offre alcuni metodi per facilitare e astrarre le operazioni che dobbiamo eseguire per lo scopo predisposto da PCPFiller.

### PCPart.java

Rappresenta l'instanza del nostro dataset

## TO-DO

...
