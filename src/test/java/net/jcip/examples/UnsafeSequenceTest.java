package net.jcip.examples;

import org.junit.Test;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class UnsafeSequenceTest {


    private Integer value=null;

    @Test
    public void name() throws InterruptedException {
        final AtomicInteger value = new AtomicInteger(1);

        CopyOnWriteArrayList<Integer> result = new CopyOnWriteArrayList<>();
        final UnsafeSequence unsafeSequence = new UnsafeSequence();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i<10; i++) {

            executorService.execute(new Runnable() {
                public void run() {

                    while (true) {
                        int next = 0;
                        try {
                            next = unsafeSequence.getNext();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        checkSequence(next);
                    }
                }
            });

        }

        executorService.shutdown();

        executorService.awaitTermination(10, TimeUnit.SECONDS);

    }

    private synchronized void checkSequence(int next) {
        if (this.value==null) {
            this.value = next;
            return ;
        }
        if ( next==this.value + 1) {
            this.value = next;
        } else {
            System.out.printf("error value: %d,next: %d",value,next);
            throw new RuntimeException("");
        }

    }
}