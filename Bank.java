package przyklady04;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Bank {

    private static abstract class Account {

        protected final int accountNumber;

        public Account(int accountNumber) {
            this.accountNumber = accountNumber;
        }
        abstract void addMoney();
        abstract long getBalance();
        int getAccountNumber() {
            return accountNumber;
        }

    }

    private static class NonAtomicAccount extends Account {

        private long balance = 0;

        public NonAtomicAccount(int accountNumber) {
            super(accountNumber);
        }

        @Override
        public void addMoney() {
            balance++;
        }

        @Override
        public long getBalance() {
            return balance;
        }
    }

    private static class AtomicAccount extends Account {

        private final AtomicLong balance = new AtomicLong(0);

        public AtomicAccount(int accountNumber) {
            super(accountNumber);
        }

        @Override
        public void addMoney() {
            balance.incrementAndGet();
        }

        @Override
        public long getBalance() {
            return balance.get();
        }
    }

    private static final int SOURCES_OF_INCOME_COUNT = 100; // number of threads
    private static final int DOLLARS_FROM_EACH_SOURCE = 100000;
    private static final String[] HOLDER_NAMES = {"Jan Kowalski",
        "Donald Trump", "Britney Spears", "Clark Kent", "Franz Kafka",
        "Queen Elizabeth II", "Adam Mickiewicz", "Mariusz Pudzianowski",
        "Magda Gessler", "Virginia Woolf"};

    private static final Map<String, Account> bankAccountsNoSync = new HashMap<>();
    private static final Map<String, Account> bankAccountsSync = Collections.synchronizedMap(
        new HashMap<>());
    private static final Map<String, Account> bankAccountsConcurrent = new ConcurrentHashMap<>();

    private static final AtomicInteger accountCounter = new AtomicInteger(1);

    // This latch allows us to start all the threads simultaneously
    private static final CountDownLatch latch = new CountDownLatch(1);

    private static class MoneyMaker implements Runnable {

        private final Map<String, Account> bank;
        private final boolean isAtomic;

        private MoneyMaker(Map<String, Account> bank, boolean isAtomic) {
            this.bank = bank;
            this.isAtomic = isAtomic;
        }

        @Override
        public void run() {
            try {
                latch.await();
                for (int i = 0; i < DOLLARS_FROM_EACH_SOURCE; i++) {
                    for (String holder : HOLDER_NAMES) {
                        bank.computeIfAbsent(holder, key ->
                            isAtomic ? new AtomicAccount(accountCounter.getAndIncrement())
                                : new NonAtomicAccount(accountCounter.getAndIncrement())
                        );
                        bank.get(holder).addMoney();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static Map<String, Account> selectBank(Scanner scanner) {
        System.out.println("Please choose a bank:");
        System.out.println("1 - SimpleHashMap Ltd.");
        System.out.println("2 - Synchronized Banking Solution Inc.");
        System.out.println("3 - Concurrent Money sp. z o.o.");
        int bankOption = scanner.nextInt();
        switch (bankOption) {
            case 1:
                return bankAccountsNoSync;
            case 2:
                return bankAccountsSync;
            case 3:
                return bankAccountsConcurrent;
            default:
                throw new RuntimeException("Wrong option");
        }
    }

    static boolean selectAtomic(Scanner scanner) {
        System.out.println("Please choose an account type:");
        System.out.println("1 - non-atomic");
        System.out.println("2 - atomic");
        int accountTypeOption = scanner.nextInt();
        switch (accountTypeOption) {
            case 1:
                return false;
            case 2:
                return true;
            default:
                throw new RuntimeException("Wrong option");
        }
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            Map<String, Account> selectedBank = selectBank(scanner);
            boolean accountIsAtomic = selectAtomic(scanner);

            Thread[] threads = new Thread[SOURCES_OF_INCOME_COUNT];
            for (int i = 0; i < SOURCES_OF_INCOME_COUNT; i++) {
                threads[i] = new Thread(new MoneyMaker(selectedBank, accountIsAtomic));
            }

            for (Thread thread : threads) {
                thread.start();
            }

            latch.countDown();

            System.out.println("Money making has started!");
            Instant timeWhenStarted = Clock.systemUTC().instant();

            for (Thread thread : threads) {
                thread.join();
            }

            Instant timeNow = Clock.systemUTC().instant();

            System.out.println(
                "Money making has taken " + Duration.between(timeWhenStarted, timeNow).toString()
                    .replace("PT", ""));

            for (Entry<String, Account> entry : selectedBank.entrySet()) {
                System.out.println(entry.getKey() + " has " + entry.getValue().getBalance()
                    + "$ in account number " + entry.getValue().getAccountNumber());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
