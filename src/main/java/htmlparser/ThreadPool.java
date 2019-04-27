package htmlparser;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Пул потоков для парсинга сайта несколькими потоками.
 */
public class ThreadPool {
    private final List<Thread> threads = new LinkedList<>();
    private final SimpleBlockingQueue<ParsingTask> tasks = new SimpleBlockingQueue<>();
    private final int size = Runtime.getRuntime().availableProcessors();

    private final CopyOnWriteArrayList<Vacancy> listV = new CopyOnWriteArrayList<>();

    public void threadPoolInit() {
        for (int i = 0; i < size; i++) {
            Thread t = new ThreadTask(tasks);
            t.start();
            threads.add(t);
        }
    }

    public List<Vacancy> getResultList() {
        return this.listV;
    }

    public void work(ParsingTask job) throws InterruptedException {
        tasks.offer(job);
    }

    public void joiningThreads() throws InterruptedException {
        for (Thread t : threads) {
            t.join();
        }
    }

    public void shutdown() {
        for (Thread t : threads) {
            t.interrupt();
        }
    }

    /**
     * Нить, которая работает с задачами из SimpleBlockingQueue<Runnable> tasks.
     */
    class ThreadTask extends Thread {

        private final SimpleBlockingQueue<ParsingTask> inputTasks;

        public ThreadTask(SimpleBlockingQueue<ParsingTask> inputTasks) {
            this.inputTasks = inputTasks;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted() && !this.inputTasks.isEmpty()) {
                try {
                    List<Vacancy> list = inputTasks.poll().call();
                    if (list != null) {
                        listV.addAllAbsent(list);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}