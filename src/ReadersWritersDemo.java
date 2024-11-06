import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.ArrayList;
import java.util.List;

// Клас банківської бази даних, яка зберігає інформацію про рахунки
class BankDatabase {
    private final List<String> accounts = new ArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true); // Справедлива черга

    private long totalReadWaitTime = 0;  // Загальний час очікування для читачів
    private long totalWriteWaitTime = 0; // Загальний час очікування для письменників
    private int readAttempts = 0;        // Кількість спроб читання
    private int writeAttempts = 0;       // Кількість спроб запису

    // Читання балансу рахунку
    public void readBalance(String readerName) {
        long waitStart = System.nanoTime();
        lock.readLock().lock();
        long waitEnd = System.nanoTime();

        synchronized (this) {
            totalReadWaitTime += (waitEnd - waitStart);
            readAttempts++;
        }

        try {
            System.out.println(readerName + " перевіряє рахунки: " + accounts);
            Thread.sleep(1500); // Симуляція часу на читання
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            System.out.println(readerName + " завершив перевірку рахунків.");
            lock.readLock().unlock();
        }
    }

    // Оновлення балансу рахунку
    public void updateBalance(String writerName, String transaction) {
        long waitStart = System.nanoTime();
        lock.writeLock().lock();
        long waitEnd = System.nanoTime();

        synchronized (this) {
            totalWriteWaitTime += (waitEnd - waitStart);
            writeAttempts++;
        }

        try {
            System.out.println(writerName + " проводить транзакцію: " + transaction);
            accounts.add(transaction);
            Thread.sleep(1500); // Симуляція часу на запис
            System.out.println(writerName + " завершив транзакцію. Поточний список рахунків: " + accounts);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.writeLock().unlock();
        }
    }

    // Метод для виведення середнього часу простою для читачів і письменників
    public void printWaitTimes() {
        double averageReadWaitTime = totalReadWaitTime / (double) readAttempts;
        double averageWriteWaitTime = totalWriteWaitTime / (double) writeAttempts;
        System.out.println("Середній час простою для читачів: " + averageReadWaitTime / 1_000_000 + " мс");
        System.out.println("Середній час простою для письменників: " + averageWriteWaitTime / 1_000_000 + " мс");

        System.out.println("Загальний час простою для читачів: " + totalReadWaitTime / 1_000_000 + " мс");
        System.out.println("Загальний час простою для письменників: " + totalWriteWaitTime / 1_000_000 + " мс");
    }
}

// Потік для читача (працівника банку)
class BankEmployee implements Runnable {
    private final BankDatabase database;
    private final String name;

    public BankEmployee(BankDatabase database, String name) {
        this.database = database;
        this.name = name;
    }

    @Override
    public void run() {
        for (int i = 0; i < 6; i++) { // Читач багаторазово перевіряє
            database.readBalance(name);
        }
    }
}

// Потік для письменника (система платежів)
class PaymentSystem implements Runnable {
    private final BankDatabase database;
    private final String name;
    private final String transaction;

    public PaymentSystem(BankDatabase database, String name, String transaction) {
        this.database = database;
        this.name = name;
        this.transaction = transaction;
    }

    @Override
    public void run() {
        for (int i = 0; i < 4; i++) { // Письменник багаторазово записує
            database.updateBalance(name, transaction + " #" + (i + 1));
        }
    }
}

// Головний клас для запуску програми
public class ReadersWritersDemo {
    public static void main(String[] args) {
        BankDatabase database = new BankDatabase();

        // Створення декількох працівників банку (читачів)
        Thread employee1 = new Thread(new BankEmployee(database, "Працівник 1"));
        Thread employee2 = new Thread(new BankEmployee(database, "Працівник 2"));
        Thread employee3 = new Thread(new BankEmployee(database, "Працівник 3"));
        Thread employee4 = new Thread(new BankEmployee(database, "Працівник 4"));
        Thread employee5 = new Thread(new BankEmployee(database, "Працівник 3"));
        Thread employee6 = new Thread(new BankEmployee(database, "Працівник 4"));
        Thread employee7 = new Thread(new BankEmployee(database, "Працівник 3"));
        Thread employee8 = new Thread(new BankEmployee(database, "Працівник 4"));

        // Створення кількох платіжних систем (письменників)
        Thread payment1 = new Thread(new PaymentSystem(database, "Система платежів 1", "Депозит на рахунок 500 грн"));
        Thread payment2 = new Thread(new PaymentSystem(database, "Система платежів 2", "Зняття з рахунку 300 грн"));
        Thread payment3 = new Thread(new PaymentSystem(database, "Система платежів 3", "Зняття з рахунку 200 грн"));
        Thread payment4 = new Thread(new PaymentSystem(database, "Система платежів 4", "Депозит на рахунок 400 грн"));
        Thread payment5 = new Thread(new PaymentSystem(database, "Система платежів 5", "Депозит на рахунок 500 грн"));
        Thread payment6 = new Thread(new PaymentSystem(database, "Система платежів 6", "Зняття з рахунку 300 грн"));
        Thread payment7 = new Thread(new PaymentSystem(database, "Система платежів 7", "Зняття з рахунку 200 грн"));
        Thread payment8 = new Thread(new PaymentSystem(database, "Система платежів 8", "Депозит на рахунок 400 грн"));

        // Запуск потоків
        employee1.start();
        employee2.start();
        payment1.start();
        employee3.start();
        employee4.start();
        payment2.start();
        payment3.start();
        payment4.start();

        employee5.start();
        employee6.start();
        payment5.start();
        employee7.start();
        employee8.start();
        payment6.start();
        payment7.start();
        payment8.start();

        try {
            employee1.join();
            employee2.join();
            payment1.join();
            employee3.join();
            employee4.join();
            payment2.join();
            payment3.join();
            payment4.join();

            employee5.join();
            employee6.join();
            payment5.join();
            employee7.join();
            employee8.join();
            payment6.join();
            payment7.join();
            payment8.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        database.printWaitTimes();
        System.out.println("Усі операції завершено.");
    }
}

