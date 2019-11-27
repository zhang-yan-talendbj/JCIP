package net.jcip.lock;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

public class LockSupportTest {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("start");
        LockSupport.parkNanos(1000000000);
        System.out.println("end");
//一开始会block线程，直到给定时间过去后才往下走

        System.out.println("start");
        LockSupport.unpark(Thread.currentThread());
        LockSupport.parkNanos(1000000000);
        System.out.println("end");
//不会block，因为一开始给了一个permit
        
        System.out.println("start");
        LockSupport.unpark(Thread.currentThread());
        LockSupport.unpark(Thread.currentThread());
        LockSupport.parkNanos(1000000000);
        System.out.println("inter");
        LockSupport.parkNanos(1000000000);
        System.out.println("end");
//第一个park不会block，第2个会，因为permit不会因为多次调用unpark就积累
    }

    @Test
    public void should_unblock_parked_thread() throws InterruptedException {
        List<Integer> iteratedNumbers = new ArrayList<>();
        Thread thread1 = new Thread(() -> {
            int i = 0;
            // park() blocks thread invoking this method
            LockSupport.park();
            while (true) {
                try {
                    Thread.sleep(1000L);
                    iteratedNumbers.add(i);
                    i++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        });
        thread1.start();

        Thread thread2 = new Thread(() -> {
            try {
                Thread.sleep(2_600L);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            // unpark(Thread) releases thread specified
            // in the parameter
            LockSupport.unpark(thread1);
        });
        thread2.start();

        Thread.sleep(5_000L);
        thread1.interrupt();

        Assert.assertEquals(iteratedNumbers.size(),2);
//        assertThat(iteratedNumbers).hasSize(2);
        // Only 2 numbers are expected:
        // * thread1 blocks before starting the iteration
        // * thread2 wakes up after ~3 seconds and releases blocked thread1
        // * from 5 seconds allocated to execution, thread1 has only 2
        //   seconds to execute and since the sleep between iterations
        //   is 1 second, it should make only 2 iterations
//        assertThat(iteratedNumbers).containsOnly(0, 1);
    }

    @Test
    public void should_block_thread_with_deadline() throws InterruptedException {
        List<Integer> iteratedNumbers = new ArrayList<>();
        Thread thread1 = new Thread(() -> {
            int i = 0;
            // park() blocks thread invoking this method
            long lockReleaseTimestamp = System.currentTimeMillis()+2_600L;
            LockSupport.parkUntil(lockReleaseTimestamp);
            while (true) {
                try {
                    Thread.sleep(1_000L);
                    iteratedNumbers.add(i);
                    i++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        });
        thread1.start();

        Thread.sleep(5_000L);
        thread1.interrupt();

//        assertThat(iteratedNumbers).hasSize(2);
        // Only 2 numbers are expected because lock is held during ~3 seconds:
        // * thread1 blocks before starting the iteration during ~3 seconds
        // * from 5 seconds allocated to execution, thread1 has only 2 seconds
        //   of execution time and since the sleep between iterations is 1 second,
        //   it should make only 2 iterations
//        assertThat(iteratedNumbers).containsOnly(0, 1);
    }

    @Test
    public void should_prove_that_blocker_is_not_an_exclusive_lock() throws InterruptedException {
        Object lock = new Object();
        boolean[] blocks = new boolean[2];
        Thread thread1 = new Thread(() -> {
            LockSupport.park(lock);
            blocks[0] = true;
        });
        thread1.start();

        Thread thread2 = new Thread(() -> {
            LockSupport.park(lock);
            blocks[1] = true;
        });
        thread2.start();

        Thread.sleep(2_000L);

        // Both threads stopped with the same blocker object (Object lock)
        // It shows that blocker can't work as an exclusive lock mechanism
        Object blockerThread1 = LockSupport.getBlocker(thread1);
        Object blockerThread2 = LockSupport.getBlocker(thread2);

//        assertThat(blockerThread1).isEqualTo(lock);
//        assertThat(blockerThread2).isEqualTo(lock);
//        assertThat(blocks[0]).isFalse();
//        assertThat(blocks[1]).isFalse();
    }

    @Test
    public void should_implement_locking_mechanism_with_blocker() throws InterruptedException {
        Object lock = new Object();
        Thread thread1 = new Thread(() -> {
            LockSupport.parkUntil(lock, System.currentTimeMillis()+3_000L);
        });
        thread1.start();

        long timeBeforeLockAcquire = System.currentTimeMillis();

        // Give some guarantee to thread1 to acquire lock
        Thread.sleep(10L);

        // Try to lock current thread as long as
        // thread1 doesn't release its lock - let's suppose
        // that thread1 is making some job needed by current thread
        while (LockSupport.getBlocker(thread1) != null) {
        }
        LockSupport.parkUntil(lock, System.currentTimeMillis()+1_000L);
        long timeAfterLockRelease = System.currentTimeMillis();
        long duration = timeAfterLockRelease - timeBeforeLockAcquire;

        // Duration should be ~4 seconds because of 3 seconds of lock
        // acquired by thread1 and 1-second blocking of current thread
//        assertThat(duration).isEqualTo(4_000L);
    }

}
