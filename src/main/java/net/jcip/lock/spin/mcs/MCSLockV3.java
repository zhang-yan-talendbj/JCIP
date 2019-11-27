package net.jcip.lock.spin.mcs;

import net.jcip.lock.spin.AbstractLock;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class MCSLockV3 extends AbstractLock {
    private volatile Node tail;
    private ThreadLocal<Node> threadLocal = new ThreadLocal();
    private AtomicReferenceFieldUpdater<MCSLockV3, Node> updater = AtomicReferenceFieldUpdater.newUpdater(MCSLockV3.class, Node.class, "tail");

    public void lock() {
        Node currentNode = threadLocal.get();
        if (currentNode==null) {
            currentNode = new Node();
            threadLocal.set(currentNode);
        }

        Node previous = updater.getAndSet(this, currentNode);
        if (previous!=null) {
            previous.next = currentNode;
            while (currentNode.wait) {

            }
        } else {
            currentNode.wait = false;
        }
    }

    public void unlock() {
        Node node = threadLocal.get();

        if (node==null||node.wait) {
            return ;
        }

        if (node.next==null&&!updater.compareAndSet(this, node, null)) {

            while (node.next==null) {

            }
        }

        if (node.next!=null) {
            node.next.wait = false;
            node.next = null;
        }
        threadLocal.remove();

    }


    private static class Node {
        private volatile Node next;
        private volatile boolean wait = true;
    }
}
