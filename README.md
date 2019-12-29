# Fairness and starvation
## Introduction
> *Starvation* is a situation when a thread is not granted CPU time to perform his job, because other threads prevent time allocation during their execution.
> *Fairness* is an opposite solution for *starvation* when all threads are granted CPU time and executed.

There are few causes of `starvation`:
- threads with high priority consume CPU time and don't allow threads with lower priority to work;
- threads wait for acquire a synchronization lock and other threads acquire the lock before them;
- threads wait for an object (the threads called a method `wait`) undefined time while other threads 
  awake first when `notify()` or `notifyAll()` are called.

## Threads with high priority drive away threads with lower priority
After a thread's object has been created, then a developer can setup a thread's priority with a method `setPriority(int priority)`. 
Priority number varies between 1 and 10, 10 is highest priority. A default priority is 5, it means a priority 5 is assign 
when a thread object is created. For example if we have an object with a single field and threads with high priority 
reads the value then threads with lower priority will wait for free CPU time.

### Acquire a synchronization lock
The problem with acquiring a synchronization lock is that Java doesn't provide any guarantee about a sequence of 
the synchronization lock acquisition. There is a potential problem, because a thread waits for a long time because other 
threads grab the lock first.

### Threads wait for a notification
It is quite similar issue with synchronization lock acquisition. Java doesn't provide any clear sequence of threads 
awakening when `notify()` or `notifyAll()` is called. If so, there is a possibility that some threads will work later 
then other even if they started to observe an object earlier.

## How to rich the fairness? Use locks
Lets say in the beginning that it's very difficult to reach absolute fairness in Java. The main approach is to use locks 
instead of synchronization block. The simplest lock is `ReentrantLock`. The lock implements an interface `Lock` 
and provides a simple facility in your code to guard a code's block.

##### Example of ReentrantLock
```java
public class ValueHolder {
ReentrantLock lock = new ReentrantLock(true);
...

public void call() {
lock.lock();
... do some stuff here
lock.unlock();
}

}
```

You should know the `ReentrantLock` has two policies of work. Fair and Nonfair policy. It's simple to manage the policy.
Constructor of the class accepts a `boolean` parameter - fairness. Pass `true` as a parameter to the constructor 
and you will work with *fair* lock. If you don't pass the `true` parameter then the lock doesn't guarantee fair work.
[Read a documentation here about the lock](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/locks/ReentrantLock.html).

## Example of fair/unfair field update
#### Unfair update with starvation symptoms
An object has one field which is read and written. For test I have created 60 thread with ninth priority and one thread with priority two.

##### Data access object. Read and write operations
```java
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
```

##### Abstract test, contains general method for read/write jobs
```java
public class AbstractTest {

public Runnable createReadJob(final Logger LOG, final UnfairObjectAccess subject) {
return () -&gt; {
int value = subject.getValue();
if(value != 101) {
LOG.debug("Read a default value");
} else {
LOG.debug("A value has been updated");
}
};
}

public Runnable createWriteJob(final Logger LOG, final UnfairObjectAccess subject) {
return () -&gt; {
LOG.debug("Setting a new value");
subject.setValue(101);
LOG.debug("Done setting the new value");
};
}

}
```

##### Starvation test. Write job has smaller priority than read jobs
```java
public class StarvationTest extends AbstractTest {

private static final Logger LOG = Logger.getLogger(StarvationTest.class);

private final UnfairObjectAccess subject = new UnfairObjectAccess();

@Test
public void testStarvationForWriteOperation() throws InterruptedException {
ThreadsBuilder.create()
//  priority 9, repeat 30 times the given job
.withThread(9, 30, createReadJob(LOG, subject))
//  priority 2, repeat 1 time the given job
.withThread(2, createWriteJob(LOG, subject))
//  priority 9, repeat 30 times the given job
.withThread(9, 30, createReadJob(LOG, subject))
.start();

Thread.sleep(1000);
}

}
```

##### Result output of starvation test
It's clear that a value's update happened closer to the end of threads execution.
```text
Line has been repeated 45 times
DEBUG info.biosfood.fairness.and.starvation.StarvationTest: Read a default value

DEBUG info.biosfood.fairness.and.starvation.StarvationTest: Setting a new value
DEBUG info.biosfood.fairness.and.starvation.StarvationTest: Read a default value
DEBUG info.biosfood.fairness.and.starvation.StarvationTest: Done setting the new value

Line has been repeated 13 times
DEBUG info.biosfood.fairness.and.starvation.StarvationTest: A value has been updated
```

#### Fairness test
Following test doesn't differ too much from previous one, the same threads with the same priorities. 
It's only one difference - fair police for `ReentrantReadWriteLock`.

##### FairObjectAccess. Read and write operations are extended from UnfairObjectAccess
```java
public class FairObjectAccess extends UnfairObjectAccess {

protected ReentrantReadWriteLock createLock() {
return new ReentrantReadWriteLock(true);
}

}
```

##### Fairness test
```java
public class FairnessTest extends AbstractTest {

private static final Logger LOG = Logger.getLogger(FairnessTest.class);

private final FairObjectAccess subject = new FairObjectAccess();

@Test
public void testFairnessForWriteOperation() throws InterruptedException {
ThreadsBuilder.create()
//  priority 9, repeat 30 times given job
.withThread(9, 30, createReadJob(LOG, subject))
//  priority 2, repeat 1 time given job
.withThread(2, createWriteJob(LOG, subject))
//  priority 9, repeat 30 times given job
.withThread(9, 30, createReadJob(LOG, subject))
.start();

Thread.sleep(1000);
}

}
```

##### Result output of fairness test
```text
Line has been repeated 30 times
DEBUG info.biosfood.fairness.and.starvation.FairnessTest: Read a default value
DEBUG info.biosfood.fairness.and.starvation.FairnessTest: Setting a new value
DEBUG info.biosfood.fairness.and.starvation.FairnessTest: Done setting the new value

Line has been repeated 30 times
DEBUG info.biosfood.fairness.and.starvation.FairnessTest: A value has been updated
```

In this particular case I had clear result. The value has been updated right after first 30 threads. 
Of course result may vary. I run that code few times and some time the update happened earlier, sometimes later, 
even closer to the end of executing threads.

## Useful links
- [ReentrantLock documentation](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/locks/ReentrantLock.html)
- [ReentrantReadWriteLock documentation](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/locks/ReentrantReadWriteLock.html)
