package net.jcip.lock;


public class SingleThreadedExecutionMain {
    public static void main(String[] args) {
        System.out.println("Testing Gate, hit CTRL+C to exit.");
        Gate gate = new Gate();
        CLHLock clhLock = new CLHLock();
        gate.setClhLock(clhLock);
        new UserThread(gate, "Alice", "Alaska").start();
        new UserThread(gate, "Bobby", "Brazil").start();
        new UserThread(gate, "Chris", "Canada").start();
    }
}
