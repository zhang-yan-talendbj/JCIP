package net.jcip.lock.spin.clh;

import net.jcip.lock.spin.AbstractLock;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class CLHLock extends AbstractLock {
    private static class Node {
        private volatile boolean nextPlzWait = true;
    }

    public ThreadLocal<Node> threadLocal = new ThreadLocal();

    public volatile Node tail;

    private AtomicReferenceFieldUpdater<CLHLock, Node> updater = AtomicReferenceFieldUpdater.newUpdater(CLHLock.class, Node.class, "tail");

    public void lock() {
        Node node = threadLocal.get();
        if (node==null) {
            node = new Node();
            threadLocal.set(node);
        }

        Node previous = updater.getAndSet(this, node);
        if (previous!=null) {
            while (previous.nextPlzWait) {

            }
        }
    }

    public void unlock() {

        Node node = threadLocal.get();
        if (node==null||!node.nextPlzWait) {
            return;
        }
        threadLocal.remove();
        if (!updater.compareAndSet(this, node, null)) {
            node.nextPlzWait = false;
        }
    }

}
