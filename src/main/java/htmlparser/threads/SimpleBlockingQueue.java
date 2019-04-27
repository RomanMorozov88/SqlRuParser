package htmlparser.threads;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Очередь для класса ThreadPool
 * @param <T>
 */
public class SimpleBlockingQueue<T> {

    private Queue<T> queue = new LinkedList<>();

    public SimpleBlockingQueue() {
    }

    public synchronized boolean isEmpty() {
        return this.queue.isEmpty();
    }

    public synchronized void offer(T value) throws InterruptedException {
        this.queue.offer(value);
        notify();
    }

    public synchronized T poll() throws InterruptedException {
        while (this.queue.size() < 1) {
            wait();
        }
        notify();
        return this.queue.poll();
    }
}