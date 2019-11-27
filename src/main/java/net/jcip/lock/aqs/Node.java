package net.jcip.lock.aqs;

/**
 * Wait queue node class.
 *
 * <p>The wait queue is a variant of a "CLH" (Craig, Landin, and
 * Hagersten) lock queue. CLH locks are normally used for
 * spinlocks.  We instead use them for blocking synchronizers, but
 * use the same basic tactic of holding some of the control
 * information about a thread in the predecessor of its node.  A
 * "status" field in each node keeps track of whether a thread
 * should block.  A node is signalled when its predecessor
 * releases.  Each node of the queue otherwise serves as a
 * specific-notification-style monitor holding a single waiting
 * thread. The status field does NOT control whether threads are
 * granted locks etc though.  A thread may try to acquire if it is
 * first in the queue. But being first does not guarantee success;
 * it only gives the right to contend.  So the currently released
 * contender thread may need to rewait.
 *
 * <p>To enqueue into a CLH lock, you atomically splice it in as new
 * tail. To dequeue, you just set the head field.
 * <pre>
 *      +------+  prev +-----+       +-----+
 * head |      | <---- |     | <---- |     |  tail
 *      +------+       +-----+       +-----+
 * </pre>
 *
 * <p>Insertion into a CLH queue requires only a single atomic
 * operation on "tail", so there is a simple atomic point of
 * demarcation from unqueued to queued. Similarly, dequeuing
 * involves only updating the "head". However, it takes a bit
 * more work for nodes to determine who their successors are,
 * in part to deal with possible cancellation due to timeouts
 * and interrupts.
 *
 * <p>The "prev" links (not used in original CLH locks), are mainly
 * needed to handle cancellation. If a node is cancelled, its
 * successor is (normally) relinked to a non-cancelled
 * predecessor. For explanation of similar mechanics in the case
 * of spin locks, see the papers by Scott and Scherer at
 * http://www.cs.rochester.edu/u/scott/synchronization/
 *
 * <p>We also use "next" links to implement blocking mechanics.
 * The thread id for each node is kept in its own node, so a
 * predecessor signals the next node to wake up by traversing
 * next link to determine which thread it is.  Determination of
 * successor must avoid races with newly queued nodes to set
 * the "next" fields of their predecessors.  This is solved
 * when necessary by checking backwards from the atomically
 * updated "tail" when a node's successor appears to be null.
 * (Or, said differently, the next-links are an optimization
 * so that we don't usually need a backward scan.)
 *
 * <p>Cancellation introduces some conservatism to the basic
 * algorithms.  Since we must poll for cancellation of other
 * nodes, we can miss noticing whether a cancelled node is
 * ahead or behind us. This is dealt with by always unparking
 * successors upon cancellation, allowing them to stabilize on
 * a new predecessor, unless we can identify an uncancelled
 * predecessor who will carry this responsibility.
 *
 * <p>CLH queues need a dummy header node to get started. But
 * we don't create them on construction, because it would be wasted
 * effort if there is never contention. Instead, the node
 * is constructed and head and tail pointers are set upon first
 * contention.
 *
 * <p>Threads waiting on Conditions use the same nodes, but
 * use an additional link. Conditions only need to link nodes
 * in simple (non-concurrent) linked queues because they are
 * only accessed when exclusively held.  Upon await, a node is
 * inserted into a condition queue.  Upon signal, the node is
 * transferred to the main queue.  A special value of status
 * field is used to mark which queue a node is on.
 *
 * <p>Thanks go to Dave Dice, Mark Moir, Victor Luchangco, Bill
 * Scherer and Michael Scott, along with members of JSR-166
 * expert group, for helpful ideas, discussions, and critiques
 * on the design of this class.
 */
final class Node {
    /** Marker to indicate a node is waiting in shared mode */
    static final Node SHARED = new Node();
    /** Marker to indicate a node is waiting in exclusive mode */
    static final Node EXCLUSIVE = null;

    /** waitStatus value to indicate thread has cancelled */
    static final int CANCELLED =  1;
    /** waitStatus value to indicate successor's thread needs unparking */
    static final int SIGNAL    = -1;
    /** waitStatus value to indicate thread is waiting on condition */
    static final int CONDITION = -2;
    /**
     * waitStatus value to indicate the next acquireShared should
     * unconditionally propagate
     */
    static final int PROPAGATE = -3;

    /**
     * Status field, taking on only the values:
     *   SIGNAL:     The successor of this node is (or will soon be)
     *               blocked (via park), so the current node must
     *               unpark its successor when it releases or
     *               cancels. To avoid races, acquire methods must
     *               first indicate they need a signal,
     *               then retry the atomic acquire, and then,
     *               on failure, block.
     *   CANCELLED:  This node is cancelled due to timeout or interrupt.
     *               Nodes never leave this state. In particular,
     *               a thread with cancelled node never again blocks.
     *   CONDITION:  This node is currently on a condition queue.
     *               It will not be used as a sync queue node
     *               until transferred, at which time the status
     *               will be set to 0. (Use of this value here has
     *               nothing to do with the other uses of the
     *               field, but simplifies mechanics.)
     *   PROPAGATE:  A releaseShared should be propagated to other
     *               nodes. This is set (for head node only) in
     *               doReleaseShared to ensure propagation
     *               continues, even if other operations have
     *               since intervened.
     *   0:          None of the above
     *
     * The values are arranged numerically to simplify use.
     * Non-negative values mean that a node doesn't need to
     * signal. So, most code doesn't need to check for particular
     * values, just for sign.
     *
     * The field is initialized to 0 for normal sync nodes, and
     * CONDITION for condition nodes.  It is modified using CAS
     * (or when possible, unconditional volatile writes).
     */
    volatile int waitStatus;

    /**
     * Link to predecessor node that current node/thread relies on
     * for checking waitStatus. Assigned during enqueuing, and nulled
     * out (for sake of GC) only upon dequeuing.  Also, upon
     * cancellation of a predecessor, we short-circuit while
     * finding a non-cancelled one, which will always exist
     * because the head node is never cancelled: A node becomes
     * head only as a result of successful acquire. A
     * cancelled thread never succeeds in acquiring, and a thread only
     * cancels itself, not any other node.
     */
    volatile Node prev;

    /**
     * Link to the successor node that the current node/thread
     * unparks upon release. Assigned during enqueuing, adjusted
     * when bypassing cancelled predecessors, and nulled out (for
     * sake of GC) when dequeued.  The enq operation does not
     * assign next field of a predecessor until after attachment,
     * so seeing a null next field does not necessarily mean that
     * node is at end of queue. However, if a next field appears
     * to be null, we can scan prev's from the tail to
     * double-check.  The next field of cancelled nodes is set to
     * point to the node itself instead of null, to make life
     * easier for isOnSyncQueue.
     */
    volatile Node next;

    /**
     * The thread that enqueued this node.  Initialized on
     * construction and nulled out after use.
     */
    volatile Thread thread;

    /**
     * Link to next node waiting on condition, or the special
     * value SHARED.  Because condition queues are accessed only
     * when holding in exclusive mode, we just need a simple
     * linked queue to hold nodes while they are waiting on
     * conditions. They are then transferred to the queue to
     * re-acquire. And because conditions can only be exclusive,
     * we save a field by using special value to indicate shared
     * mode.
     */
    Node nextWaiter;


    boolean isCancelled(){
        return waitStatus>0;
    }
    /**
     * Returns true if node is waiting in shared mode.
     */
    final boolean isShared() {
        return nextWaiter == SHARED;
    }

    /**
     * Returns previous node, or throws NullPointerException if null.
     * Use when predecessor cannot be null.  The null check could
     * be elided, but is present to help the VM.
     *
     * @return the predecessor of this node
     */
    final Node predecessor() throws NullPointerException {
        Node p = prev;
        if (p == null)
            throw new NullPointerException();
        else
            return p;
    }

    Node() {    // Used to establish initial head or SHARED marker
    }

    Node(Thread thread, Node mode) {     // Used by addWaiter
        this.nextWaiter = mode;
        this.thread = thread;
    }

    Node(Thread thread, int waitStatus) { // Used by Condition
        this.waitStatus = waitStatus;
        this.thread = thread;
    }
}
