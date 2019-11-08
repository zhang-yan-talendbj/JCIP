package SingleThreadedExecution;

public class Gate {
    private Object lock=new Object();
    private int counter = 0;
    private String name = "Nobody";
    private String address = "Nowhere";

    public  void pass(String name, String address) {
        synchronized (this.lock) {
            this.counter++;
            this.name = name;
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }
            this.address = address;
            check();
        }
    }
    public  String  toString() {
        synchronized (this.lock) {
            return "No." + counter + ": " + name + ", " + address;
        }
    }
    private void check() {
        if (name.charAt(0) != address.charAt(0)) {
            System.out.println("***** BROKEN ***** " + toString());
        }
    }
}
