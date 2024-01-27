package defaultplayer.util;

/**
 * Interface to represent ints up to 65535 (2^16-1)
 */
public class FastIterableIntSet {
    public StringBuilder keys;
    public int maxlen;
    public int[] ints;
    public int size;
    private int earliestRemoved;

    public FastIterableIntSet() {
        this(500);
    }

    public FastIterableIntSet(int len) {
        keys = new StringBuilder();
        size = 0;
        maxlen = len;
        earliestRemoved = size;
        ints = new int[maxlen];
    }

    public int size() {
        return size;
    }

    public int get(int index) {
        return keys.charAt(index);
    }
    public void add(int i) {
        String key = String.valueOf((char) i);
        if (keys.indexOf(key) < 0) {
            keys.append(key);
            size++;
        }
    }

    public void remove(int i) {
        String key = String.valueOf((char) i);
        int index;
        if ((index = keys.indexOf(key)) >= 0) {
            keys.deleteCharAt(index);
            size--;

            if(earliestRemoved > index)
                earliestRemoved = index;
        }
    }

    public boolean contains(int i) {
        return keys.indexOf(String.valueOf((char) i)) >= 0;
    }

    public void clear() {
        size = 0;
        keys = new StringBuilder();
        earliestRemoved = size;
    }

    public void updateIterable() {
        for (int i = earliestRemoved; i < size; i++) {
            ints[i] = keys.charAt(i);
        }
        earliestRemoved = size;
    }

}