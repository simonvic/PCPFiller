
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

| PEAS        |                                                                                                                                                                                                                                                                                   |
|-------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Performance | La performance dell'agente è dettata dalla precisione e accuratezza dei dati mancanti predetti                                                                                                                                                                                    |
| Enviroment  | - Completamente osservabile, dato che ha accesso a tutte le informazioni di un componente </br> - Deterministico, in quanto i dati predetti dipendono solamente dallo stato iniziale dei componenti e dalle modifiche dell'agente </br> - Sequenziale, in quanto i dati in output possono variare in baso alle iterazioni precedenti </br> - Statico, i dati non variano mentre l'agente sta operando </br> - Singolo agente, in quanto PCPFiller è l'unico a manipolare i dati </br> - Continuo, la decisione del dato predetto si evolve in modo continuo |
| Actuators   | Gli attuatori consistono nei dati predetti inseriti nel dataset                                                                                                                                                                                                                                                                            |
| Sensors     | I sensori dell'agente consistono nei dati già presenti di un dato componente nel dataset                                                                                                                                                                                                                                                                              |

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

