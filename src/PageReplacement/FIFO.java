package PageReplacement;

import java.util.*;

public class FIFO {
    public static int pageFaults(int[] pages, int size, int capacity) {
        if(capacity == 0) return size; 
        HashSet<Integer> set = new HashSet<>(capacity);
        Queue<Integer> queue = new LinkedList<>();
        int cnt = 0;
        for(int i = 0; i < size; i++) {
            if(set.size() < capacity) {
                if(!set.contains(pages[i])) {
                    set.add(pages[i]);
                    queue.add(pages[i]);
                    cnt++;
                }
            }
            else {
                if(!set.contains(pages[i])) {
                    int val = queue.peek();
                    queue.poll();
                    set.remove(val);
                    set.add(pages[i]);
                    queue.add(pages[i]);
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
        HashSet<Integer> set = new HashSet<>(capacity);
        Queue<Integer> queue = new LinkedList<>();
        for (int page : pages) {
            boolean fault = !set.contains(page);
            int replaced = -1;
            if (fault) {
                if (set.size() < capacity) {
                    for (int i = 0; i < capacity; i++) {
                        if (frames[i] == -1) { frames[i] = page; break; }
                    }
                } else {
                    int evict = queue.poll();
                    set.remove(evict);
                    replaced = evict;
                    for (int i = 0; i < capacity; i++) {
                        if (frames[i] == evict) { frames[i] = page; break; }
                    }
                }
                set.add(page);
                queue.add(page);
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

