package pt.tecnico.sauron.silo.client;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class TimestampVector implements Comparable<TimestampVector>{

    private List<Integer> values;

    public TimestampVector(Integer size) {
        values = new ArrayList<>();
        for (int i = 0; i < size; ++i)
            values.add(0);
    }

    public TimestampVector(List<Integer> values) {
        this.values = List.copyOf(values);
    }

    public List<Integer> getValues() {
        return values;
    }

    public Integer get(Integer index) {
        return values.get(index);
    }

    public void set(Integer index, Integer value) {
        this.values.set(index, value);
    }

    @Override
    public int compareTo(TimestampVector other) {
        int result = 0;
        ListIterator<Integer> it1 = this.values.listIterator();
        ListIterator<Integer> it2 = other.values.listIterator();
        while (it1.hasNext() && it2.hasNext()) {
            Integer n1 = it1.next();
            Integer n2 = it2.next();
            if (n1 > n2)  {
                if (result == -1) return 0;
                else result = 1;
            } else if (n1 < n2) {
                if (result == 1) return 0;
                else result = -1;
            }
        }
        return result;
    }

    public void merge(TimestampVector t2) {
        ListIterator<Integer> it1 = this.values.listIterator();
        ListIterator<Integer> it2 = t2.values.listIterator();
        while (it1.hasNext() && it2.hasNext()) {
            it1.set(Integer.max(it1.next(), it2.next()));
        }
    }

}
