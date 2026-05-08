package DiskScheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SCAN {
    public static void apply(int[] arr, int head, String direction, int diskSize) {
        int seekCount = 0;
        int distance, currentTrack;
        int size = arr.length;
        ArrayList<Integer> left = new ArrayList<>(), right = new ArrayList<>();
        ArrayList<Integer> seekSequence = new ArrayList<>();

        if (direction.equals("left")) left.add(0);
        else if (direction.equals("right")) right.add(diskSize - 1);

        for(int i = 0; i < size; i++) {
            if(arr[i] < head) left.add(arr[i]);
            else if(arr[i] > head) right.add(arr[i]);
            else seekSequence.add(arr[i]);
        }

        Collections.sort(left);
        Collections.sort(right);

        int run = 2;
        while(run-- > 0) {
            if(direction.equals("left")) {
                for(int i = left.size() - 1; i >= 0; i--) {
                    currentTrack = left.get(i);
                    seekSequence.add(currentTrack);
                    distance = Math.abs(currentTrack - head);
                    seekCount += distance;
                    head = currentTrack;
                }
                direction = "right";
            }
            else if(direction.equals("right")) {
                for(int i = 0; i < right.size(); i++) {
                    currentTrack = right.get(i);
                    seekSequence.add(currentTrack);
                    distance = Math.abs(currentTrack - head);
                    seekCount += distance;
                    head = currentTrack;
                }
                direction = "left";
            }
        }
        System.out.print("Total number of seek operations = " + seekCount + "\n");
        System.out.print("Seek Sequence is" + "\n");
        for(int i = 0; i < seekSequence.size(); i++) {
            System.out.print(seekSequence.get(i) + "\n");
        }
    }

    public static List<Integer> run(int[] requests, int head, String direction, int diskSize) {
        List<Integer> left = new ArrayList<>(), right = new ArrayList<>();
        if (direction.equals("left")) left.add(0);
        else right.add(diskSize - 1);
        for (int req : requests) {
            if (req < head) left.add(req);
            else if (req > head) right.add(req);
        }
        Collections.sort(left);
        Collections.sort(right);
        List<Integer> seq = new ArrayList<>();
        seq.add(head);
        if (direction.equals("right")) {
            for (int t : right) { head = t; seq.add(head); }
            for (int i = left.size() - 1; i >= 0; i--) { head = left.get(i); seq.add(head); }
        } else {
            for (int i = left.size() - 1; i >= 0; i--) { head = left.get(i); seq.add(head); }
            for (int t : right) { head = t; seq.add(head); }
        }
        return seq;
    }
}

