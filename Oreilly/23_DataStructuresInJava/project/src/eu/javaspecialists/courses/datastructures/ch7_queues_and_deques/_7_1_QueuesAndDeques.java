package eu.javaspecialists.courses.datastructures.ch7_queues_and_deques;

import java.util.*;
import java.util.concurrent.*;

/**
 * Not always FIFO
 */
public class _7_1_QueuesAndDeques {
  public static void main(String... args) {
    Queue<?>[] queues = {
        new ConcurrentLinkedQueue<>(),
        new PriorityQueue<>() // not thread-safe
    };

    Deque<?>[] deques = { // extends Queue
        new ArrayDeque<>(), // not thread-safe
        new ConcurrentLinkedDeque<>(),
        new LinkedList<>() // not thread-safe
    };

    BlockingQueue<?>[] blockingQueues = { // extends Queue
        new ArrayBlockingQueue<>(10),
        new DelayQueue<>(), // Used for scheduled executor service (timer pool)
        new LinkedBlockingQueue<>(),
        new PriorityBlockingQueue<>(),
        new SynchronousQueue<>(), // Used for cached thread pool
    };

    TransferQueue<?>[] transferQueues = { // extends BlockingQueue
        new LinkedTransferQueue<>() // created for work-stealing - not used
    };

    BlockingDeque<?>[] blockingDeques = { // extends BlockingQueue, Deque
        new LinkedBlockingDeque<>()
    };
  }
}
