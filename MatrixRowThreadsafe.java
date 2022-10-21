package przyklady04;

import java.util.concurrent.*;
import java.util.function.IntBinaryOperator;

public class MatrixRowThreadsafe {

    private static final int ROWS = 3;
    private static final int COLUMNS = 10;
    private static final ConcurrentHashMap<Integer,
            BlockingQueue<Integer>> mapConcurrent =
            new ConcurrentHashMap<>();

    private static Integer takeElement(Integer row) throws InterruptedException {
        return mapConcurrent.get(row).take();
    }

    private static void putElement(Integer row, Integer obj) throws InterruptedException {
        mapConcurrent.get(row).put(obj);
    }

    private static class Matrix {

        private final int rows;
        private final int columns;
        private final IntBinaryOperator definition;

        public Matrix(int rows, int columns, IntBinaryOperator definition) {
            this.rows = rows;
            this.columns = columns;
            this.definition = definition;
        }

        private static class HelperColumn implements Runnable {
            int id;
            private final IntBinaryOperator definition;

            public HelperColumn(int id, IntBinaryOperator definition) {
                this.id = id;
                this.definition = definition;
            }

            @Override
            public void run() {
                for (int row = 0; row < ROWS; row++) {
                    try {
                        mapConcurrent.computeIfAbsent(row,
                                k -> new LinkedBlockingQueue<>(COLUMNS));
                        Integer element = definition.applyAsInt(row, id);
                        putElement(row, element);

                    } catch (InterruptedException e) {
                        Thread t = Thread.currentThread();
                        t.interrupt();
                        System.err.println(t.getName() + " interrupted");
                    }
                }
            }
        }

        private static class HelperRow implements Runnable {
            int rowId;
            int[] resultArray;

            public HelperRow(int rowId, int[] resultArray) {
                this.rowId = rowId;
                this.resultArray = resultArray;
            }

            public void run() {
                for (int column = 0; column < COLUMNS; column++) {
                    mapConcurrent.computeIfAbsent(rowId,
                            k -> new LinkedBlockingQueue<>(COLUMNS));
                    try {
                        Integer element = takeElement(rowId);
                        resultArray[rowId] += element;
                    } catch (InterruptedException e) {
                        Thread t = Thread.currentThread();
                        t.interrupt();
                        System.err.println(t.getName() + " interrupted");
                    }
                }
                mapConcurrent.remove(rowId);
            }
        }

        public int[] rowSums() {
            int[] rowSums = new int[rows];
            for (int row = 0; row < rows; ++row) {
                int sum = 0;
                for (int column = 0; column < columns; ++column) {
                    sum += definition.applyAsInt(row, column);
                }
                rowSums[row] = sum;
            }
            return rowSums;
        }

        public int[] rowSumsThreadsafe() {
            int[] rowSums = new int[rows];

            Thread[] threadColumns = new Thread[COLUMNS];
            Thread[] threadRows = new Thread[ROWS];

            for (int columnId = 0; columnId < COLUMNS; columnId++) {
                threadColumns[columnId] = new Thread(new HelperColumn(columnId, definition));
            }

            for (int rowId = 0; rowId < ROWS; rowId++) {
                threadRows[rowId] = new Thread(new HelperRow(rowId, rowSums));
            }

            for (Thread t: threadColumns) {
                t.start();
            }
            for (Thread t: threadRows) {
                t.start();
            }

            try {
                for (Thread t: threadColumns) {
                    t.join();
                }
                for (Thread t: threadRows) {
                    t.join();
                }

            } catch (InterruptedException e) {
                Thread t = Thread.currentThread();
                t.interrupt();
                System.err.println(t.getName() + " interrupted");
            }

            mapConcurrent.clear();

            return rowSums;
        }
    }

    public static void main(String[] args) {

        System.out.println("Starting...");

        Matrix matrix = new Matrix(ROWS, COLUMNS, (row, column) -> {
            int a = 2 * column + 1;
            int cellId = column + row * COLUMNS;
            try {
                Thread.sleep((1000 - (cellId % 13) * 1000 / 12));
            } catch (InterruptedException e) {
                Thread t = Thread.currentThread();
                t.interrupt();
                System.err.println(t.getName() + " interrupted");
            }
            return (row + 1) * (a % 4 - 2) * a;
        });
        long startTime = System.currentTimeMillis();
        int[] rowSums = matrix.rowSums();
        long usedTime = System.currentTimeMillis() - startTime;
        System.out.println("Sequential execution took: " + usedTime + "ms");
        System.out.println("Result:");
        for (int i = 0; i < rowSums.length; i++) {
            System.out.println(i + " -> " + rowSums[i]);
        }


        // concurrent computations
        startTime = System.currentTimeMillis();
        rowSums = matrix.rowSumsThreadsafe();
        usedTime = System.currentTimeMillis() - startTime;
        System.out.println("Concurrent execution took: " + usedTime + "ms");
        System.out.println("Result:");
        for (int i = 0; i < rowSums.length; i++) {
            System.out.println(i + " -> " + rowSums[i]);
        }
    }
}