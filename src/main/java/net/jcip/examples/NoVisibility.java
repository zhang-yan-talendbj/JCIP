package net.jcip.examples;

import java.util.Random;

/**
 * NoVisibility
 * <p/>
 * Sharing variables without synchronization
 *
 * @author Brian Goetz and Tim Peierls
 */

public class NoVisibility {
    private static boolean ready;
    private static int number;
    private static int number2;

    private static class ReaderThread extends Thread {
        public void run() {
            while (!ready)
                Thread.yield();
            System.out.println(number);
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i<100; i++) {
        aa();
        }
    }

    private static void aa() {
        Random random = new Random();
        new ReaderThread().start();
        synchronized (random) {
            number = random.nextInt(100);
            number2 = number2;
        }
        ready = true;
    }
}
