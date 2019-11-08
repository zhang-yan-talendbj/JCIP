package net.jcip.examples;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import net.jcip.annotations.ThreadSafe;

/**
 * OneShotLatch
 * <p/>
 * Binary latch using AbstractQueuedSynchronizer
 *
 * @author Brian Goetz and Tim Peierls
 */
@ThreadSafe
public class OneShotLatch {
    private final Sync sync;

    public OneShotLatch() {
        sync = new Sync(0);
    }

    public void signal() {
        sync.releaseShared(1);
    }

    public void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    private class Sync extends AbstractQueuedSynchronizer {
        protected int tryAcquireShared(int ignored) {
            // Succeed if latch is open (state == 1), else fail
            return (getState() == ignored) ? 1 : -1;
        }

        Sync(int i) {
            setState(i);
        }

        protected boolean tryReleaseShared(int signal) {
            setState(signal); // Latch is now open
            return true; // Other threads may now be able to acquire

        }
    }

    public static void main(String[] args) throws InterruptedException {
        final OneShotLatch oneShotLatch = new OneShotLatch();

        createNewThread(oneShotLatch).start();
        createNewThread(oneShotLatch).start();
        createNewThread(oneShotLatch).start();
        createNewThread(oneShotLatch).start();
        createNewThread(oneShotLatch).start();
        createNewThread(oneShotLatch).start();
        createNewThread(oneShotLatch).start();

        Thread.sleep(3000);

        Thread thread = new Thread(new Runnable() {
            public void run() {
                System.out.println("signal start");
                oneShotLatch.signal();
                System.out.println("signal end");
            }
        });
        thread.start();

    }

    private static Thread createNewThread(final OneShotLatch oneShotLatch) {
        return new Thread(new Runnable() {
            public void run() {
                try {
                    System.out.println("await start");
                    oneShotLatch.await();
                    System.out.println("await over");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
