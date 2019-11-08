package net.jcip.lock.spin;

import java.util.concurrent.atomic.AtomicInteger;

public class TicketLock extends AbstractLock{

    /**
     * 当前正在接受服务的号码
     */
    private AtomicInteger serviceNum = new AtomicInteger(1);

    /**
     * 希望得到服务的排队号码
     */
    private AtomicInteger ticketNum = new AtomicInteger(1);

    private ThreadLocal<Integer> currentThreadLocal = new ThreadLocal<>();

    /**
     * 尝试获取锁
     *
     * @return
     */
    public void lock() {
        // 获取排队号
        int acquiredTicketNum = ticketNum.getAndIncrement();

        // 当排队号不等于服务号的时候开始自旋等待
        while (acquiredTicketNum!=serviceNum.get()) {

        }
        currentThreadLocal.set(acquiredTicketNum);

    }

    /**
     * 释放锁
     *
     */
    public void unlock() {
        int currentTicket = currentThreadLocal.get();
        // 服务号增加，准备服务下一位
        int nextServiceNum = serviceNum.get() + 1;

        // 只有当前线程拥有者才能释放锁
        serviceNum.compareAndSet(currentTicket, nextServiceNum);
    }

}