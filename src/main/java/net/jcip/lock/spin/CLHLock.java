package net.jcip.lock.spin;

import java.util.concurrent.atomic.AtomicReference;

public class CLHLock {

    public static class CLHNode {
         boolean isLocked = false;
    }

    private AtomicReference<CLHNode> tail;
    private ThreadLocal<CLHNode> previous;
    private ThreadLocal<CLHNode> current;

    public CLHLock() {

        tail = new AtomicReference<CLHNode>(new CLHNode());
        current = new ThreadLocal<CLHNode>() {
            protected CLHNode initialValue() {
                return new CLHNode();
            }
        };
        previous = new ThreadLocal<CLHNode>() {
            protected CLHNode initialValue() {
                return null;
            }
        };
    }

    public void lock() {
        CLHNode node = current.get();
        node.isLocked = true;
        CLHNode pre = tail.getAndSet(node);
        previous.set(pre);
        while (pre.isLocked) {
            System.out.println(pre.isLocked);
        }
    }

    public void unlock() {
        CLHNode node = current.get();
        node.isLocked = false;
        current.set(null); // 去掉引用,可以被垃圾回收
        previous.set(null);// 前置从链表中移除
    }
}