package net.jcip.lock.spin;

public class SpinLockThread extends Thread {

    private CLHLock spinLock;

    public SpinLockThread(CLHLock spinLock) {
        this.spinLock = spinLock;
    }


    @Override
    public void run() {

        try {

            spinLock.lock();

            System.out.println(Thread.currentThread().getName() + ": lock");
            Thread.sleep(1);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            System.out.println(Thread.currentThread().getName() + ": unlock");
            spinLock.unlock();
        }

    }

    public static void main(String[] args) {
        CLHLock spinLock = new CLHLock();
        new SpinLockThread(spinLock).start();
        new SpinLockThread(spinLock).start();
        new SpinLockThread(spinLock).start();
        new SpinLockThread(spinLock).start();
    }
}
