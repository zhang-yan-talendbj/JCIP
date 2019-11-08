package Immutable.Q2_6;

public class CrackerThread extends Thread {

    private final MutablePerson mutablePerson;

    @Override
    public void run() {

        while (true) {
            ImmutablePerson immutablePerson = new ImmutablePerson(mutablePerson);
            if (!immutablePerson.getName().equals(immutablePerson.getAddress())) {
                System.out.println("BROKEN:" + immutablePerson.getName() + "," + immutablePerson.getAddress());
            }
        }

    }

    public CrackerThread(MutablePerson mutablePerson) {
        this.mutablePerson = mutablePerson;
    }
}
