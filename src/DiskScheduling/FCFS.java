package DiskScheduling;

import java.util.*;

public class FCFS {
    public static void apply(int[] arr, int head) {
        int seekCount = 0;
        int distance, currentTrack;
        int size = arr.length;
        for(int i = 0; i < size; i++) {
            currentTrack = arr[i];
            distance = Math.abs(currentTrack - head);
            seekCount += distance;
            head = currentTrack;
        }
        System.out.println("Total number of " + "seek operations = " + seekCount);
        System.out.println("Seek Sequence is");
        for(int i = 0; i < size; i++) { System.out.println(arr[i]); }
    }

    public static List<Integer> run(int[] requests, int head) {
        List<Integer> seq = new ArrayList<>();
        seq.add(head);
        for (int req : requests) {
            head = req;
            seq.add(head);
        }
        return seq;
    }
}

