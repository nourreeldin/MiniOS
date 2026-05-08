package PageReplacement;

import java.util.*;

public class Clock {
    public static int pageFaults(int[] pages, int size, int capacity) {
        if(capacity == 0) return size;
        int[] frames = new int[capacity];
        boolean[] secondChance = new boolean[capacity];
        Arrays.fill(frames, -1);
        HashSet<Integer> set = new HashSet<>(capacity);
        int pointer = 0;
        int cnt = 0;
        for(int i = 0; i < size; i++) {
            int page = pages[i];
            boolean found = false;
            for(int j = 0; j < capacity; j++) {
                if(frames[j] == page) {
                    secondChance[j] = true;
                    found = true;
                    break;
                }
            }
            if(found) continue;
            cnt++;
            while(true) {
                if(frames[pointer] == -1) {
                    frames[pointer] = page;
                    secondChance[pointer] = false;
                    pointer = (pointer + 1) % capacity;
                    break;
                }
                if(!secondChance[pointer]) {
                    set.remove(frames[pointer]);
                    frames[pointer] = page;
                    secondChance[pointer] = false;
                    pointer = (pointer + 1) % capacity;
                    break;
                }
                secondChance[pointer] = false;
                pointer = (pointer + 1) % capacity;
            }
            set.add(page);
        }
        return cnt;
    }

    public static List<int[]> simulate(int[] pages, int capacity) {
        List<int[]> steps = new ArrayList<>();
        if (capacity == 0) return steps;
        int[] frames = new int[capacity];
        boolean[] refBits = new boolean[capacity];
        Arrays.fill(frames, -1);
        int pointer = 0;
        for (int page : pages) {
            boolean found = false;
            for (int j = 0; j < capacity; j++) {
                if (frames[j] == page) {
                    refBits[j] = true;   
                    found = true;
                    break;
                }
            }
            int replaced = -1;
            if (!found) {
                while (true) {
                    if (frames[pointer] == -1) {
                        frames[pointer] = page;
                        refBits[pointer] = false;
                        pointer = (pointer + 1) % capacity;
                        break;
                    }
                    if (!refBits[pointer]) {
                        replaced = frames[pointer];
                        frames[pointer] = page;
                        refBits[pointer] = false;
                        pointer = (pointer + 1) % capacity;
                        break;
                    }
                    refBits[pointer] = false;  
                    pointer = (pointer + 1) % capacity;
                }
            }
            int[] entry = new int[3 + capacity * 2];
            entry[0] = page;
            entry[1] = found ? 0 : 1;
            entry[2] = replaced;
            for (int j = 0; j < capacity; j++) entry[3 + j] = frames[j];
            for (int j = 0; j < capacity; j++) entry[3 + capacity + j] = refBits[j] ? 1 : 0;
            steps.add(entry);
        }
        return steps;
    }
}
