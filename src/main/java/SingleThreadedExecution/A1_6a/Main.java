package SingleThreadedExecution.A1_6a;

public class Main {
    public static void main(String[] args) {
        System.out.println("Testing EaterThread, hit CTRL+C to exit.");
        Tool spoon = new Tool("Spoon");
        Tool fork = new Tool("Fork");
        //如果顺序一致,就不会发生死锁.
        new EaterThread("Alice", spoon, fork).start();
        new EaterThread("Bobby", spoon,fork).start();
    }
}
