package net.jcip.lock.spin.mcs;

import net.jcip.lock.spin.AbstractLock;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class MCSLockV3 extends AbstractLock {

    private ThreadLocal<Node> threadLocal = ThreadLocal.withInitial(Node::new);
    private volatile Node tail;

    public void lock() {

        Node current = threadLocal.get();
        if (current==null) {
            current = new Node();
            threadLocal.set(current);
        }

        Node previousNode = update.getAndSet(this, current);

        if (previousNode==null) {
            //the first node,active this node,get the lock and return
            current.active = true;
        } else {
            previousNode.next = current;
            while (!current.active) {
                logList.addLog(" spin for lock.");

            }
            //get the lock ,return
        }

    }

    public void unlock() {

        Node current = threadLocal.get();

        if (current==null||!current.active) {
            return;
        }


        Node next = current.next;

        if (next==null&&!update.compareAndSet(this, current, null)) {
            while (current.next==null) {
                logList.addLog(" spin for unlock.");
            }
        }
        if (next!=null) {
            next.active = true;
            current.next = null;
        }

        threadLocal.remove();

    }

    private AtomicReferenceFieldUpdater<MCSLockV3, Node> update = AtomicReferenceFieldUpdater.newUpdater(MCSLockV3.class, Node.class, "tail");

    static class Node {
        volatile Node next;
        volatile boolean active = false;
    }
}
