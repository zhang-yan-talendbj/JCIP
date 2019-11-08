package net.jcip.examples;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class UnsafeSequenceTest {


    private Integer value = null;

    @Test
    public void name() throws InterruptedException {
        final UnsafeSequence unsafeSequence = new UnsafeSequence();
        CopyOnWriteArrayList<Integer> result = new CopyOnWriteArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<Integer> objects = new ArrayList<>();
        for (int i = 0; i<10; i++) {

            executorService.execute(new Runnable() {
                private Object flag=new Object();
                public void run() {

                    while (objects.isEmpty()) {
                        synchronized (flag) {
                            int next = unsafeSequence.getNext();
                            Random random = new Random();
                            try {
                                Thread.sleep(random.nextInt(10));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            result.add(next);
//                            checkSequence(next);
                        }
                    }
                }
            });

        }

        executorService.shutdown();

        executorService.awaitTermination(10, TimeUnit.SECONDS);

        objects.add(1);

        Thread.sleep(1000);

        int size = result.size();

        HashSet<Integer> integers = new HashSet<>(result);

        System.out.println(size);
        System.out.println(integers.size());

        for (Integer integer : integers) {

            int count=0;
            for (Integer integer1 : result) {
                if(integer.equals(integer1)){
                    count++;
                }
            }
            if(count>1){
                System.out.println(integer+":"+count);
            }
        }

    }

    private synchronized void checkSequence(int next) {
        if (this.value==null) {
            this.value = next;
            return;
        }
        if (next==this.value + 1) {
            this.value = next;
        } else {
            System.out.printf("error value: %d,next: %d", value, next);
            throw new RuntimeException("");
        }

    }
}