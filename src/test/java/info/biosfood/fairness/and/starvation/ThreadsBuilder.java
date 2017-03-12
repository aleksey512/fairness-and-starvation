package info.biosfood.fairness.and.starvation;

import java.util.ArrayList;
import java.util.List;

public class ThreadsBuilder {

    public static ThreadsBuilder create() {
        return new ThreadsBuilder();
    }

    private List<Pair> threads = new ArrayList<Pair>();

    public ThreadsBuilder withThread(int priority, Runnable job) {
        withThread(priority, 1, job);

        return this;
    }

    public ThreadsBuilder withThread(int priority, int repeat, Runnable job) {
        for(int i = 0; i < repeat; i++) {
            threads.add(new Pair(priority, job));
        }

        return this;
    }

    public void start() {
        for(Pair pair : threads) {
            Thread t = new Thread(pair.job);
            t.setPriority(pair.priority);

            t.start();
        }
    }

    private class Pair {
        private Runnable job;
        private int priority;

        public Pair(int priority, Runnable job) {
            this.job = job;
            this.priority = priority;
        }
    }

}
