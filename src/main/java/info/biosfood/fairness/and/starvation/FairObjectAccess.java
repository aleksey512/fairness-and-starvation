package info.biosfood.fairness.and.starvation;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FairObjectAccess extends UnfairObjectAccess {

    protected ReentrantReadWriteLock createLock() {
        return new ReentrantReadWriteLock(true);
    }

}
