package net.jcip.lock.spin.clh;


import net.jcip.lock.spin.AbstractLock;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.LockSupport;

public class CLHLock1 extends AbstractLock {

    public static class CLHNode {

        private volatile Thread isLocked;

    }


    @SuppressWarnings("unused")
    private volatile CLHNode tail;

    private static final ThreadLocal<CLHNode> THREAD_LOCAL = new ThreadLocal<CLHNode>();

    private static final AtomicReferenceFieldUpdater<CLHLock1, CLHNode> UPDATER = AtomicReferenceFieldUpdater.newUpdater(CLHLock1.class,

            CLHNode.class, "tail");


    public void lock() {

        CLHNode node = new CLHNode();

        THREAD_LOCAL.set(node);

        CLHNode preNode = UPDATER.getAndSet(this, node);

        if (preNode!=null) {

            preNode.isLocked = Thread.currentThread();

            LockSupport.park();

            preNode = null;

            THREAD_LOCAL.set(node);

        }

    }


    public void unlock() {

        CLHNode node = THREAD_LOCAL.get();

        if (!UPDATER.compareAndSet(this, node, null)) {

            System.out.println("unlock\t" + node.isLocked.getName());

            LockSupport.unpark(node.isLocked);

        }

        node = null;

    }
}
