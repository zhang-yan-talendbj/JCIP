package net.jcip.lock.aqs;

import java.util.Date;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class Mutex extends AbstractQueuedSynchronizer {

    public static class Sync extends AbstractQueuedSynchronizer {

        public Sync() {
            setState(100); // set the initial state, being unlocked.
        }

        @Override
        protected boolean tryAcquire(int ignore) {
            boolean result = compareAndSetState(100, 1);
            print("尝试获取锁" + (result ? "成功" : "失败"));
            return result;
        }

        @Override
        protected boolean tryRelease(int ignore) {
            setState(100);
            return true;
        }
    }

    private final Sync sync = new Sync();

    public void lock() {
        sync.acquire(0);
    }

    public void unLock() {
        sync.release(0);
    }

    public static void main(String[] args) throws InterruptedException {
        Mutex mutex = new Mutex();
        mutex.lock();

        Thread thread = new Thread(() -> {
            print("调用 mutex.lock() 之前");
            mutex.lock();
            print("调用 mutex.lock() 之后");
        });

        thread.start();

        print("main 线程 Sleep 之前");
        Thread.sleep(5000);
        print("main 线程 Sleep 之后");
        mutex.unLock();
    }

    public static void print(String print) {
        System.out.println(String.format("时间 - %s\t\t%s\t\t%s", new Date(), Thread.currentThread(), print));
    }
}