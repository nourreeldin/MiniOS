package PageReplacement;

import java.util.*;

public class OPT {
    public static int pageFaults(int[] pages, int size, int capacity) {
        if(capacity == 0) return size;
        HashSet<Integer> set = new HashSet<>(capacity);
        int cnt = 0;
        for(int i = 0; i < size; i++) {
            if(set.size() < capacity) {
                if(!set.contains(pages[i])) {
                    set.add(pages[i]);
                    cnt++;
                }
            }
            else {
                if(!set.contains(pages[i])) {
                    int farthest = -1;
                    int val = -1;
                    Iterator<Integer> itr = set.iterator();
                    while(itr.hasNext()) {
                        int temp = itr.next();
                        int j;
                        for(j = i + 1; j < size; j++) {
                            if(pages[j] == temp) {
                                if(j > farthest) {
                                    farthest = j;
                                    val = temp;
                                }
                                break;
                            }
                        }
                        if(j == size) {
                            val = temp;
                            break;
                        }
                    }
                    set.remove(val);
                    set.add(pages[i]);
                    cnt++;
                }
            }
        }
        return cnt;
    }

    public static List<int[]> simulate(int[] pages, int capacity) {
        List<int[]> steps = new ArrayList<>();
        if (capacity == 0) return steps;
        int[] frames = new int[capacity];
        Arrays.fill(frames, -1);
        Set<Integer> set = new HashSet<>();
        for (int i = 0; i < pages.length; i++) {
            int page = pages[i];
            boolean fault = !set.contains(page);
            int replaced = -1;
            if (fault) {
                if (set.size() < capacity) {
                    for (int j = 0; j < capacity; j++) {
                        if (frames[j] == -1) { frames[j] = page; break; }
                    }
                } else {
                    int farthest = -1, evict = -1;
                    for (int frame : frames) {
                        int nextUse = Integer.MAX_VALUE;
                        for (int j = i + 1; j < pages.length; j++) {
                            if (pages[j] == frame) { nextUse = j; break; }
                        }
                        if (nextUse > farthest) { farthest = nextUse; evict = frame; }
                    }
                    set.remove(evict);
                    replaced = evict;
                    for (int j = 0; j < capacity; j++) {
                        if (frames[j] == evict) { frames[j] = page; break; }
                    }
                }
                set.add(page);
            }
            int[] entry = new int[3 + capacity];
            entry[0] = page;
            entry[1] = fault ? 1 : 0;
            entry[2] = replaced;
            System.arraycopy(frames, 0, entry, 3, capacity);
            steps.add(entry);
        }
        return steps;
    }
}

