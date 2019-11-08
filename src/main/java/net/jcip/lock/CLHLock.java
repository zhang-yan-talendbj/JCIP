package net.jcip.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class CLHLock implements Lock {

    private AtomicReference<QNode> tail = new AtomicReference<>(new QNode());

    private ThreadLocal<QNode> preThreadLocal;
    private ThreadLocal<QNode> myThreadLocal;

    public CLHLock() {
        tail = new AtomicReference<>(new QNode());
        //每个线程都会生成一个新的node
        myThreadLocal = ThreadLocal.withInitial(QNode::new);
        preThreadLocal = ThreadLocal.withInitial(() -> null);
    }

    @Override
    public void lock() {
        QNode qNode = myThreadLocal.get();//node和线程绑定
        qNode.lock = true;
        //tail是唯一的,多个线程通过tail连接
        QNode pred = tail.getAndSet(qNode);
        preThreadLocal.set(pred);
        while (pred.lock){
            System.out.println(Thread.currentThread().getName()+":"+pred.lock);
        };
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
        QNode qnode = myThreadLocal.get();
        qnode.lock = false;
        QNode preNode = preThreadLocal.get();
        myThreadLocal.set(preNode);
    }

    @Override
    public Condition newCondition() {
        return null;
    }

    class QNode {
        boolean lock;
    }
}