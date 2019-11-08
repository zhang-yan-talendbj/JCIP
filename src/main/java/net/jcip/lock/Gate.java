package net.jcip.lock;

public class Gate {
    private String name;
    private String address;

    private CLHLock clhLock;

    public void setClhLock(CLHLock clhLock) {
        this.clhLock = clhLock;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Gate{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                '}';
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void pass(String myName, String myAddress) {

        clhLock.lock();
        this.name = myName;
        this.address = myAddress;
        check();
        clhLock.unlock();
    }

    public void check() {
        if (name.charAt(0)!=address.charAt(0)) {
            System.out.println("****** BROKEN  " + toString());
        }
    }
}
