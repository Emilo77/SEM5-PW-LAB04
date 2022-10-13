package przyklady04;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelSumAtomic {
    private static final int THREADS_COUNT = 100;
    private static final int COUNT = 1000;
    // Because non-primitive types (i.e. other than boolean, char, int, long, float, double)
    // are stored as references
    // marking a variable holding a reference as final
    // makes it impossible to change the reference,
    // but it doesn't make the referenced object const/final/frozen.
    private static final AtomicInteger sum = new AtomicInteger(0);

    // We create 100 threads, each simultaneously
    // trying to increase a single shared variable 1000 times
    // but this time we use an atomic variable
    public static void main(String[] args) {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < THREADS_COUNT; i++) {
            threads.add(new Thread(() -> {
                for (int j = 0; j < COUNT; j++) {
                    sum.incrementAndGet();
                }
            }));
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.err.println("Main thread interrupted");
            }
        }

        // It should print exactly 100000 ;)
        System.out.println(sum);

    }
}
