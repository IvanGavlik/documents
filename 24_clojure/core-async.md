# Core Async

Asynchronous programming and communication using channels. 

Write concurrent code without explicit callbacks

## Chanels

Belts for values. Can put and take values 

Chanel is a **queue** + **cordination mechanism**

internal structure

+ buffer - holds values (if buffered channel)

+ takes queue - list of operations waiting to take a value

+ puts queue - list of operations waiting to put a value

+ closed?

+ lock - for thread safe operations

### Blocking take `<!!`

Uses threas blocking (OS puts thread to sleep)

1. `<!!` is caled

2. lock the channel (thread safe)

3. check buffer: is there data ?
   
   1. yes take it unlock return 
   
   2. no go to step 4

4. Create a promose (a parking sopt for our thread)

5. add promise to "takes queue"

6. unlock chanel

7. wait on promise (block here is happening)

8. when `>!!`puts data it delivers the promise

### Non blocking take `<!`

Parking / Non Blocking `<!` uses state machine + callbacks (This is why we need go block (it creates state machine))

```clojure
 (go
    (println "Step 1")
    (let [val (<! ch)]  ;; Parking point
      (println "Step 2:" val)))

  ;; The 'go' macro transforms your code into a state machine:

  (defn run-state-machine [state]
    (case (:step state)
      1 (do
          (println "Step 1")
          ;; Try to take from channel
          (let [result (try-take ch)]
            (if (= result ::parked)
              ;; Register callback and park
              (register-callback ch
                (fn [val]
                  ;; When value arrives, resume with next step
                  (run-state-machine (assoc state :step 2 :val val))))
              ;; Value available immediately
              (run-state-machine (assoc state :step 2 :val result)))))

      2 (println "Step 2:" (:val state))))
```

 When <! parks, it registers a callback. When data arrives, the **callback re-submits the state machine to the thread pool**! TODO explain this 

### Buffered channel

## Blocking vs non-blocking operations

Blocking `>!! <!!`    on regular threads, block the thread until get read or write 

You order food and wait until you get your food. You can't do anything else just wait

Non-blocking `>! <!` inside `go` block Threas is not blocked 

The single `!` inside `go`block means pause this go block but let the other things run

## Go Block

Creates lightweight process that can park wothout blocking OS threasreads 

Go blocks do NOT create threads. It uses two thread pools

* compute pool (size of CUP cores + 2) - for go blocks

* IO poll (unlimiter) - for blocking operations

### Go Blocks are State Machines

```clojure
  (def ch (chan))

  (go
    (println "A")
    (<! ch)  ;; Parking point
    (println "B"))

  ;; Another thread
  (go
    (Thread/sleep 2000)
    (>! ch "hello"))
```

```clojure
 ;; Pseudo-code of what the compiler generates
  {:state 0
   :locals {}
   :states {0 (fn [] (println "Step 1") (goto-state 1))
            1 (fn [] (park-and-wait-for ch))
            2 (fn [x] (println "Step 2:" x) (println "Step 3"))}}
```

Your Code:
  Thread-1 executing Go-Block-A
      ↓
  Thread-1 hits <! ch
      ↓
  Go-Block-A saves its state: {locals: {...}, next-step: 2}
      ↓
  Thread-1 becomes FREE
      ↓
  Thread-1 picks up Go-Block-B (does useful work!)
      ↓
  When channel receives value:
      ↓
  Any free thread picks up Go-Block-A and continues

  OS Level:
  CPU → [Thread-1 keeps working, never sleeps!]

Work is not done on current thread it's dont on thread pool threads current thread justs SUBMITS the go block to the pool and continues Thread pool provides:

* parralle execution (multiple go blocks at once)

* doesn't block the caller 

* better cpu utilization

### Mental Model Update

You write:
    (go (do-work))

  What happens:
    Current Thread: "Hey thread pool, run this go block"
                    └─> Creates state machine
                        └─> Submits to queue
                            └─> Returns immediately

    Thread Pool Worker: "I'll take that!"
                        └─> Picks up from queue
                            └─> Runs go block
                                └─> Parks when needed
                                    └─> Picks up another go block

### Thread vs Thread pool

Threas is like hiring a worker for a single task, they are expensive because you have to create and destory them and in this process :

* allocates 1 MB of stack memory

* system calls to OS

* take 1-2 miliseconds

* JVM bookkeppping

* conext switching

Thread pool is hiring a team or workers who handle tasks form a queue the JVM creates thread-1 ... thread-n (ONE TIME COST) and then they wait for work in a queue so one thread can handle lots of tasks 

**Core async's combines thread pools with state machines**

```clojure
  ;; Internally:
  ;; 1. Create state machine (cheap data structure)
  ;; 2. Submit to thread pool
  ;; 3. Thread runs state machine until it parks
  ;; 4. Thread becomes available for other go blocks
  ;; 5. When channel ready, re-submit state machine to pool
  ;; 6. ANY thread can resume it
```

### Exceptions in the go block

Exceptions are silently swallowed code after exception never runs. Why ? 

* go block runs in the thread pool by some random thread

* so who's there to catch exception by default they are logged 

* your code never sees it 

The go block is disconnected from your calling code

Options

* try-catch inside go block

* return erros on channels

* return erros as values 

* use error chnnels

* global exception handler for the thread pool

* 

`>!` if chanel is closed it throws ex

`<!` if chanel is closed it returns nil 

### Producder consumer pattern

### Buffered channels

### Timeouts

### `alt alts!` Multiple Channels

### Pipeline processing

## Best practices

Always close channels

Use buffered channels 

Prefere go blocks 

Handle nil values `<!`retunrs nill when channel is closed

Dont mix blocking and non blocking ops

## Common Patterns

### Loop until channel closes

### Fan-out (one producer many consumers)

### Fan-in (many producers one channel)

### Write concurent code without explicit callbacks

A callback is when you say: when this think finishes call this function now with core async 

```clojure
  (go
    (let [user   (<! (fetch-user user-id))      ;; Wait for user
          orders (<! (fetch-orders (:id user))) ;; Then wait for orders
          total  (<! (calculate-total orders))  ;; Then calculate total
          result (<! (send-email user total))]  ;; Then send email
      (println "Done!" result)))
```
