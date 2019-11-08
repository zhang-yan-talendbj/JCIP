package ProducerConsumer.Sample;

public class InterruptMain {
    public static void main(String[] args) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {

                    System.out.println(e.getLocalizedMessage());
                    System.out.println(e.getMessage());
                }
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();

        thread.interrupt();
    }
}
