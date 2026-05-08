package PageReplacement;

import java.util.*;

public class MRU {
    public static int pageFaults(int[] pages, int size, int capacity) {
        if(capacity == 0) return size; 
        HashSet<Integer> set = new HashSet<>(capacity);
        HashMap<Integer, Integer> indexes = new HashMap<>();
        int cnt = 0;
        for(int i = 0; i < size; i++) {
            if(set.size() < capacity) {
                if(!set.contains(pages[i])) {
                    set.add(pages[i]);
                    cnt++;
                }
                indexes.put(pages[i], i);
            }
            else {
                if(!set.contains(pages[i])) {
                    int mru = Integer.MIN_VALUE;
                    int val = -1;
                    Iterator<Integer> itr = set.iterator();
                    while(itr.hasNext()) {
                        int temp = itr.next();
                        if(indexes.get(temp) > mru) {
                            mru = indexes.get(temp);
                            val = temp;
                        }
                    }
                    set.remove(val);
                    indexes.remove(val);
                    set.add(pages[i]);
                    cnt++;
                }
                indexes.put(pages[i], i);
            }
        }
        return cnt;
    }

    public static List<int[]> simulate(int[] pages, int capacity) {
        List<int[]> steps = new ArrayList<>();
        if (capacity == 0) return steps;
        int[] frames = new int[capacity];
        Arrays.fill(frames, -1);
        Map<Integer, Integer> lastUsed = new HashMap<>();
        int time = 0;
        for (int page : pages) {
            boolean fault = !lastUsed.containsKey(page);
            int replaced = -1;
            if (fault) {
                if (lastUsed.size() < capacity) {
                    for (int i = 0; i < capacity; i++) {
                        if (frames[i] == -1) { frames[i] = page; break; }
                    }
                } else {
                    int mruPage = lastUsed.entrySet().stream()
                            .max(Map.Entry.comparingByValue()).get().getKey();
                    lastUsed.remove(mruPage);
                    replaced = mruPage;
                    for (int i = 0; i < capacity; i++) {
                        if (frames[i] == mruPage) { frames[i] = page; break; }
                    }
                }
            }
            lastUsed.put(page, time++);
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

