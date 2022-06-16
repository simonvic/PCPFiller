
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

Dato che ci interessa principalmente la previsione di campi correlati al prezzo, commentiamo la loro relazione

- Brand
  </br>![Brand-Price](https://imgur.com/wYf7UOE.png)
  </br>Come previsto, alcuni brand più blasonati sono soliti scegliere prezzi piu alti per i propri prodotti

- Generazione Modulo
  </br>![-Price](https://imgur.com/bwnYbOS.png)
  </br>Nessuna sorpresa, le ultime generazioni hanno un prezzo piu elevato

Lo stesso possiamo dire per gli altri parametri di performance del modulo. Maggiori performance risultano in un prezzo piu alto.

- Frequenza
  </br>![Speed-Price](https://imgur.com/thitmXA.png)

- Dimensione modulo
  </br>![Size-Price](https://imgur.com/WMf46fx.png)

- FWLatency
  </br>![FWL-Price](https://imgur.com/JRfz2Zr.png)

Di seguito una overview delle relazioni tra i vari campi
  </br>![Overview](https://imgur.com/0CwtbBq.png)


### Preprocessing dei dati

PCPFiller scarterà automaticamente tutte le entries che presentano campi vuoti / mancanti, transformando alcuni dati per facilitarne l'analisi.


