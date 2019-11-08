package net.jcip.lock;

public class UserThread extends Thread {

    private String myName;
    private String myAddress;
    private Gate gate;

    public UserThread(Gate gate, String name, String address) {
        this.gate = gate;
        this.myName = name;
        this.myAddress = address;
    }

    @Override
    public void run() {
        while (true) {
            gate.pass(myName, myAddress);
        }
    }
}
