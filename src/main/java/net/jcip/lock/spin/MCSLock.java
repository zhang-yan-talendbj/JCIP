package net.jcip.lock.spin;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class MCSLock implements Lock {
    AtomicReference<QNode> tail;
    ThreadLocal<QNode> currentThreadLocal;

    @Override
    public void lock() {
        tail = new AtomicReference<QNode>(new QNode());
        QNode currentNode = currentThreadLocal.get();
        QNode pred = tail.getAndSet(currentNode);
        if (pred!=null) {
            currentNode.locked = true;
            pred.next = currentNode;

            // wait until predecessor gives up the lock
            while (currentNode.locked) {
            }
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {
        QNode qnode = currentThreadLocal.get();
        if (qnode.next==null) {
            if (tail.compareAndSet(qnode, null))
                return;

            // wait until predecessor fills in its next field
            while (qnode.next==null) {
            }
        }
        qnode.next.locked = false;
        qnode.next = null;
    }

    @Override
    public Condition newCondition() {
        return null;
    }

    class QNode {
        boolean locked = false;
        QNode next = null;
    }
}