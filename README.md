# Dane współdzielone

Klasy gwarantujące poprawność użycia obiektów wF programach współbieżych, bez konieczności dodatkowej synchronizacji, nazywamy bezpiecznymi dla wątków (ang. thread-safe).

Tematem zajęć są bezpieczne dla wątków struktury danych.

Do scenariusza dołączone są programy przykładowe:

- [TwoWritersSharedArray.java](https://github.com/Emilo77/SEM5-PW-LAB04/blob/master/TwoWritersSharedArray.java)

- [ProducersConsumersQueue.java](https://github.com/Emilo77/SEM5-PW-LAB04/blob/master/ProducersConsumersQueue.java)

- [ParallelSumAtomic.java](https://github.com/Emilo77/SEM5-PW-LAB04/blob/master/ParallelSumAtomic.java)

- [Bank.java](https://github.com/Emilo77/SEM5-PW-LAB04/blob/master/Bank.java)

### Zmienne atomowe

Zmienne typów zdefiniowanych klasami z pakietu `java.util.concurrent.atomic` gwarantują atomowość dostępu.

Klasa [AtomicInteger](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/atomic/AtomicInteger.html) jest opakowaniem zmiennej typu `int`. Ma metodę `getAndIncrement()` która, niepodzielnie, czyta aktualną wartość zmiennej i zwiększa ją o 1.

Program [ParallelSumAtomic.java](https://github.com/Emilo77/SEM5-PW-LAB04/blob/master/ParallelSumAtomic.java) demonstruje zastosowanie klasy `AtomicInteger`.

W tym przykładzie dwa wątki jednocześnie zwiększają wspólny licznik. Z pierwszych zajęć pamiętamy, że oznaczenie zmiennej jako volatile nie rozwiązywało problemów z atomowością.

### Kolekcje synchronizowane

Większość standardowych kolekcji Javy nie jest bezpieczna dla wątków. Problem ten można rozwiązać, umieszczając operacje na kolekcji w sekcji krytycznej. Tak zabezpieczoną kolekcję nazywamy kolekcją synchronizowaną.

Kolekcje synchronizowane są tworzone przez statyczne metody klasy [Collections](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/Collections.html#synchronizedCollection(java.util.Collection)). Np. metoda `synchronizedList(list)` daje synchronizowaną listę elementów listy `list`.

Wykonanie **każdej** operacji na kolekcji synchronizowanej blokuje dostęp do **całej** kolekcji.

### Kolekcje współbieżne

W pakiecie `java.util.concurrent` są definicje kolekcji wpółbieżnych. Tak, jak kolekcje synchronizowane, są one bezpieczne dla wątków, ale umożliwiają przy tym współbieżne wykonanie niektórych operacji.

Klasa [ConcurrentHashMap](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/ConcurrentHashMap.html) implementuje interfejs współbieżnej mapy `ConcurrentMap`.

### Kolekcje synchronizowane a współbieżne

Skoro mamy zatem dwa różne "smaki" kolekcji bezpiecznych dla wątków, to kiedy powinniśmy używać jednego rodzaju, a kiedy drugiego? [Różnice między nimi są subtelne](https://stackoverflow.com/questions/1291836/concurrenthashmap-vs-synchronized-hashmap), zwłaszcza przy używaniu iteratorów. Najważniejsze jest to, że kolekcje współbieżne na ogół będą działać szybciej w środowisku wielowątkowym.

Program [Bank.java](https://github.com/Emilo77/SEM5-PW-LAB04/blob/master/Bank.java) demonstruje różnice między HashMapą bez synchronizacji, z synchronizacją, oraz HashMapą współbieżną.

### Zadanie niepunktowane (Bank)

Uruchom program Bank ze wszystkimi kombinacjami parametrów i wyjaśnij różnice działaniu. Następnie dodaj nowy rodzaj konta korzystający z klasy [LongAdder](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/atomic/LongAdder.html) i porównaj jego działanie z pozostałymi dwoma. Jaka kombinacja typu banku (HashMapy) i konta działa najwydajniej?

### Kolejki blokujące

Klasa [LinkedBlockingQueue](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/LinkedBlockingQueue.html), implementująca interfejs kolejki blokującej `BlockingQueue`, jest bezpieczna dla wątków i jednocześnie sama synchronizuje wątki.

Metoda `put(e)` kolejki blokującej wstawia argument `e` na koniec kolejki.

Metoda `take()` wstrzymuje wykonujący ją wątek do chwili, gdy kolejka będzie niepusta, a następnie pobiera z kolejki pierwszy element.

Program [ProducersConsumersQueue.java](https://github.com/Emilo77/SEM5-PW-LAB04/blob/master/ProducersConsumersQueue.java) demonstruje zastosowanie kolejki blokującej do rozwiązania problemu producentów i konsumentów.

Kolejka blokująca również ma swój współbieżny odpowiednik. [Różnice między nimi są podobne jak w przypadku kolekcji synchronizowanych i współbieżnych opisanych powyżej](https://stackoverflow.com/questions/19179046/concurrentlinkeddeque-vs-linkedblockingdeque).

### Ćwiczenie punktowane (MatrixRowSumsThreadsafe)

W rozwiązaniu zadania z poprzednich zajęć wątek, który obliczył element macierzy, przed przejściem do wiersza następnego musiał zaczekać na zsumowanie aktualnego i wypisanie sumy.

Dodatkowo, liczenie sumy zaczynało się dopiero po obliczeniu wszystkich elementów wiersza.

### Polecenie

Napisz nową wersję programu, umożliwiającą

- obliczanie elementów kolejnych wierszy bez oczekiwania na zsumowanie poprzednich,

- rozpoczęcie sumowania wiersza zanim wszystkie jego elementy zostaną obliczone.

W rozwiązaniu zastosuj bezpieczne dla wątków struktury danych.

Zadbaj o efektywność rozwiązania w przypadku, gdy liczba kolumn macierzy jest mała a liczba wierszy bardzo duża.

Pamiętaj o poprawnej obsłudze przerwań.
