package net.jcip.lock.spin;

import java.util.concurrent.atomic.AtomicReference;

public class SimpleSpinLock extends AbstractLock {
    public static final Thread FREE_LOCK = null;
    //null 标记为锁可用
    private AtomicReference<Thread> lock = new AtomicReference<>();

    public void lock() {
        Thread thread = Thread.currentThread();
        while (!lock.compareAndSet(FREE_LOCK, thread)) {
            System.out.println(thread.getName() + "--spin...");
        }
    }

    public void unlock() {
        Thread thread = Thread.currentThread();
        lock.compareAndSet(thread, FREE_LOCK);
    }


}
