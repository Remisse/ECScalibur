# Retrospettiva

Lo sviluppo di questo framework ha rappresentato un'ottima opportunità per apprendere alcuni degli aspetti più avanzati di Scala 3, come la scrittura di macro e di MacroAnnotation. L'intenzione originale era quella di usare altri costrutti avanzati come l'annotazione `@specialized` su collection create appositamente per il framework, ma Scala 3 non supporta attualmente questa funzionalità. Inoltre, le prestazioni del framework lasciano molto a desiderare (l'esecuzione della demo con 100 iterazioni per 30.000 entità richiede circa 4,5 secondi, in confronto ai circa 100 ms della demo OOP), per cui un possibile sviluppo futuro consisterà sicuramente nell'ottimizzazione (estrema) di questo framework.

ECS trae grande vantaggio in termini di prestazioni da una disposizione in memoria di entità e componenti effettuata secondo l'approccio *data-oriented*. Nei linguaggi che permettono di allocare oggetti sullo stack, questo fa sì che il principio di *cache locality* venga sfruttato al meglio.
In questo progetto, tale principio viene quasi totalmente meno poiché Scala, come qualsiasi altro linguaggio che si appoggi sulla Java Virtual Machine, non permette allocazioni esplicite sullo stack se non per i tipi primitivi.
È consentita l'allocazione di oggetti *off-heap*, ma questa possibilità non è stata esplorata, così come non sono state applicate numerose ottimizzazioni che avrebbero compromesso il design in modi poco manutenibili e che, in ogni caso, non sarebbero state pertinenti al corso e agli obiettivi di questo progetto d'esame. Sarebbe interessante studiare e attuare anche questi concetti.