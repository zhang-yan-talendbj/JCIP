package net.jcip.lock.spin;

import java.util.ArrayList;
import java.util.List;

public class LogList {

    private List<String> arrayList = new ArrayList<>();

    public synchronized void addLog(String s) {

        String name = Thread.currentThread().getName();

        String e = name + s;

        int size = arrayList.size();
        if (size==0) {
            arrayList.add(e);
        } else {
            String last = arrayList.get(size - 1);
            if (!last.equals(e)) {
                arrayList.add(e);
            }
        }
    }

    public synchronized void print(){
        for (String s : arrayList) {
            System.out.println(s);
        }
    }
}
