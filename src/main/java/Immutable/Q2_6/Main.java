package Immutable.Q2_6;

public class Main {
    public static void main(String[] args) {

        MutablePerson mutablePerson = new MutablePerson("aa", "aa");

        new CrackerThread(mutablePerson).start();
        new CrackerThread(mutablePerson).start();
        new CrackerThread(mutablePerson).start();

        for (int i = 0; true; i++) {
            mutablePerson.setPerson(""+i,""+i);
        }
    }
}
