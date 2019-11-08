package net.jcip.examples;

import net.jcip.annotations.NotThreadSafe;

/**
 * UnsafeSequence
 *
 * @author Brian Goetz and Tim Peierls
 */

@NotThreadSafe
public class UnsafeSequence {
    private int value;

    /**
     * Returns a unique value.
     */
    public int getNext()  {
        //        Random random = new Random();

//        Thread.sleep(random.nextInt(100));
        return value++;
    }
}
