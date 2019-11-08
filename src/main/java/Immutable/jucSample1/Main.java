package Immutable.jucSample1;

import java.util.List;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        List<Integer> list = new ArrayList<Integer>();
        new ReaderThread(list).start();
        new WriterThread(list).start();
    }
}