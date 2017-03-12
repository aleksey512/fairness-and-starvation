package info.biosfood.fairness.and.starvation;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class UnfairObjectAccess {

    private volatile int value = 100;
    private ReentrantReadWriteLock lock;

    public UnfairObjectAccess() {
        lock = createLock();
    }

    protected ReentrantReadWriteLock createLock() {
        return new ReentrantReadWriteLock();
    }

    public int getValue() {
        try {
            lock.readLock().lock();
            return value;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setValue(int value) {
        lock.writeLock().lock();

        this.value = value;

        lock.writeLock().unlock();
    }

}
