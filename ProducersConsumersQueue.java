package przyklady04;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class ProducersConsumersQueue {

    private enum Product {
        TOILET_PAPER, TOOTHPASTE, RICE, BOTTLED_WATER
    }

    private static final int CONSUMERS_COUNT = 8;
    private static final int[] PRODUCER_COUNTS = {10, 7, 2, 1}; // for each type of product

    private static final int PRODUCED_COUNT = 200;
    private static final int CONSUMED_COUNT = 500;

    private static final int BUFFER_SIZE = 10;

    private static final BlockingQueue<Product> buffer = new LinkedBlockingQueue<>(BUFFER_SIZE);

    private static Product consume() throws InterruptedException {
        return buffer.take();
    }

    private static void produce(Product product) throws InterruptedException {
        buffer.put(product);
    }

    private static class Producer implements Runnable {

        private final Product productType;
        private final int productionDelay;
        private int producedProducts = 0;

        public Producer(Product productType, int productionDelay) {
            this.productType = productType;
            this.productionDelay = productionDelay;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < PRODUCED_COUNT; ++i) {
                    Thread.sleep(productionDelay);
                    produce(productType);
                    producedProducts++;
                }
                System.out.println(
                    Thread.currentThread().getName() + " has produced " + producedProducts
                        + " units of " + productType);
            } catch (InterruptedException e) {
                Thread t = Thread.currentThread();
                t.interrupt();
                System.err.println(t.getName() + " interrupted");
            }
        }

    }

    private static class Consumer implements Runnable {

        private final int consumingDelay;
        private final int[] shoppingBag;

        private Consumer(int consumingDelay) {
            this.consumingDelay = consumingDelay;
            this.shoppingBag = new int[Product.values().length];
        }

        @Override
        public void run() {
            Thread t = Thread.currentThread();
            try {
                for (int i = 0; i < CONSUMED_COUNT; ++i) {
                    Thread.sleep(consumingDelay);
                    Product product = consume();
                    shoppingBag[product.ordinal()]++;
                }
                System.out.println(t.getName() + " has consumed:");
                for (int i = 0; i < Product.values().length; i++) {
                    System.out.println(
                        " - " + shoppingBag[i] + " units of " + Product.values()[i]);
                }
            } catch (InterruptedException e) {
                t.interrupt();
                System.err.println(t.getName() + " interrupted");
            }
        }

    }

    public static void main(String[] args) {
        assert PRODUCER_COUNTS.length == Product.values().length;
        assert Arrays.stream(PRODUCER_COUNTS).sum() * PRODUCED_COUNT
            == CONSUMERS_COUNT * CONSUMED_COUNT;

        List<Thread> threads = new ArrayList<>();
        for (int producerType = 0; producerType < PRODUCER_COUNTS.length; producerType++) {
            for (int i = 0; i < PRODUCER_COUNTS[producerType]; i++) {
                Product product = Product.values()[producerType];
                int productionDelay = ThreadLocalRandom.current().nextInt(1,100);
                Runnable runnable = new Producer(product, productionDelay);
                Thread thread = new Thread(runnable, "Producer" + product + i);
                threads.add(thread);
            }
        }

        for (int i = 0; i < CONSUMERS_COUNT; ++i) {
            int consumingDelay = ThreadLocalRandom.current().nextInt(1, 100);
            Runnable r = new Consumer(consumingDelay);
            Thread t = new Thread(r, "Consumer" + i);
            threads.add(t);
        }
        for (Thread t : threads) {
            t.start();
        }
        try {
            for (Thread t : threads) {
                t.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Main interrupted");
        }
    }

}
