package GuardedSuspension;

import java.util.Queue;
import java.util.LinkedList;

public class RequestQueue {
    private final Queue<Request> queue = new LinkedList<Request>();
    public synchronized Request getRequest() {
        while (queue.peek()==null) {
            try {
                wait();//wait释放了锁,被唤醒后,一定要再次检查queue.peek() == null,所以不能使用if,要使用while
                //调用的是RequestQueue的wai,notify,而不是线程的Wait,notify方法.
                //可以调用线程的inerrupt方法中断
            } catch (InterruptedException e) {
            }
        }
        return queue.remove();
    }
    public synchronized void putRequest(Request request) {
        queue.offer(request);
        notifyAll();
    }
}
