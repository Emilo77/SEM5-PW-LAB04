package przyklady04;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class TwoWritersSharedArray {

    private static final int LINES_COUNT = 5;
    private static final int LINE_LENGTH = 60;
    private static final int WRITERS_COUNT = 2;
    private static final int HOW_MANY_LETTERS_EACH_WRITER_WRITES = LINE_LENGTH * LINES_COUNT / WRITERS_COUNT;

    // Two threads can write to this shared buffer because thanks to
    // the atomic counter we know that each subsequent write will take place
    // at a different index
    private static final char[] text = new char[WRITERS_COUNT * HOW_MANY_LETTERS_EACH_WRITER_WRITES];

    private static final char EMPTY = ' ';

    private static final AtomicInteger NEXT_LETTER_INDEX = new AtomicInteger(0);

    private static class Writer implements Runnable {

        private final char firstChar;
        private final char lastChar;

        public Writer(char firstChar, char lastChar) {
            this.firstChar = firstChar;
            this.lastChar = lastChar;
        }

        @Override
        public void run() {
            try {
                char c = firstChar;
                for (int i = 0; i < HOW_MANY_LETTERS_EACH_WRITER_WRITES; ++i) {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1,10));
                    int index = NEXT_LETTER_INDEX.getAndIncrement();
                    text[index] = c;
                    ++c;
                    if (c > lastChar) {
                        c = firstChar;
                    }
                }
            } catch (InterruptedException e) {
                Thread t = Thread.currentThread();
                t.interrupt();
                System.err.println(t.getName() + " interrupted");
            }
        }

    }

    public static void main(String[] args) {
        Arrays.fill(text, EMPTY);
        Thread letters = new Thread(new Writer('a', 'z'), "Letters");
        Thread digits = new Thread(new Writer('0', '9'), "Digits");
        letters.start();
        digits.start();
        try {
            letters.join();
            digits.join();
            int column = 0;
            for (char c : text) {
                System.out.print(c);
                ++column;
                if (column == LINE_LENGTH) {
                    System.out.println();
                    column = 0;
                }
            }
            System.out.println();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Main thread interrupted");
        }
    }

}
