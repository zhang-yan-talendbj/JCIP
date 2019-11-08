package net.jcip.lock.spin.mcs;

import net.jcip.lock.spin.AbstractLock;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class MCSLockV2 extends AbstractLock {

    /**
     * MCS锁节点
     */
    public static class MCSNodeV2 {

        /**
         * 后继节点
         */
        volatile MCSNodeV2 next;

        /**
         * 默认状态为等待锁
         */
        volatile boolean waite = true;

    }

    /**
     * 线程到节点的映射
     */
    private ThreadLocal<MCSNodeV2> currentThreadNode = new ThreadLocal<>();

    /**
     * 指向最后一个申请锁的MCSNode
     */
    volatile MCSNodeV2 tail;

    /**
     * 原子更新器
     */
    private static final AtomicReferenceFieldUpdater UPDATER = AtomicReferenceFieldUpdater
            .newUpdater(
                    MCSLockV2.class,
                    MCSLockV2.MCSNodeV2.class,
                    "tail");



    /**
     * MCS获取锁操作
     */
    public void lock() {
        MCSNodeV2 currentNode = currentThreadNode.get();

        if (currentNode==null) {
            // 初始化节点对象
            currentNode = new MCSNodeV2();
            currentThreadNode.set(currentNode);
        }

        // 将当前申请锁的线程置为queue并返回旧值
        MCSNodeV2 predecessor = (MCSNodeV2) UPDATER.getAndSet(this, currentNode); // step 1

        if (predecessor!=null) {
            // 形成链表结构(单向)
            predecessor.next = currentNode; // step 2

            // 当前线程处于等待状态时自旋(MCSNode的blocked初始化为true)
            // 等待前驱节点主动通知，即将blocked设置为false，表示当前线程可以获取到锁
            while (currentNode.waite) {
                logList.addLog(" spin.");
            }
        } else {
            // 只有一个线程在使用锁，没有前驱来通知它，所以得自己标记自己为非阻塞 - 表示已经加锁成功
            currentNode.waite = false;
        }
    }

    /**
     * MCS释放锁操作
     */
    public void unlock() {
        // 获取当前线程对应的节点
        MCSNodeV2 cNode = currentThreadNode.get();

        if (cNode==null||cNode.waite) {
            // 当前线程对应存在节点
            // 并且
            // 锁拥有者进行释放锁才有意义 - 当blocked未true时，表示此线程处于等待状态中，并没有获取到锁，因此没有权利释放锁
            return;
        }

        if (cNode.next==null && !UPDATER.compareAndSet(this, cNode, null)) {
            // 没有后继节点的情况，将queue置为空
            // 如果CAS操作失败了表示突然有节点排在自己后面了，可能还不知道是谁，下面是等待后续者
            // 这里之所以要忙等是因为上述的lock操作中step 1执行完后，step 2可能还没执行完
            while (cNode.next==null) {
                    logList.addLog(" spin.");
            }
        }

        if (cNode.next!=null) {
            // 通知后继节点可以获取锁
            cNode.next.waite = false;

            // 将当前节点从链表中断开，方便对当前节点进行GC
            cNode.next = null; // for GC
        }

        // 清空当前线程对应的节点信息
        currentThreadNode.remove();

    }


}