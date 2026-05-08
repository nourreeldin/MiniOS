package DiskScheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CLOOK {
    public static void apply(int[] arr, int head, String direction) {
        int seekCount = 0;
        int distance, currentTrack;
        int size = arr.length;
        ArrayList<Integer> left = new ArrayList<>(), right = new ArrayList<>();
        ArrayList<Integer> seekSequence = new ArrayList<>();

        for(int i = 0; i < size; i++) {
            if(arr[i] < head) left.add(arr[i]);
            else if(arr[i] > head) right.add(arr[i]);
            else seekSequence.add(arr[i]);
        }

        Collections.sort(left);
        Collections.sort(right);

        if(direction.equals("right")) {
            for(int i = 0; i < right.size(); i++) {
                currentTrack = right.get(i);
                seekSequence.add(currentTrack);
                distance = Math.abs(currentTrack - head);
                seekCount += distance;
                head = currentTrack;
            }
            if(!left.isEmpty()) {
                head = left.getFirst();
                seekSequence.add(head);
            }
            for(int i = 1; i < left.size(); i++) {
                currentTrack = left.get(i);
                seekSequence.add(currentTrack);
                distance = Math.abs(currentTrack - head);
                seekCount += distance;
                head = currentTrack;
            }
        } else if(direction.equals("left")) {
            for(int i = left.size() - 1; i >= 0; i--) {
                currentTrack = left.get(i);
                seekSequence.add(currentTrack);
                distance = Math.abs(currentTrack - head);
                seekCount += distance;
                head = currentTrack;
            }
            if(!right.isEmpty()) {
                head = right.getLast();
                seekSequence.add(head);
            }
            for(int i = right.size() - 2; i >= 0; i--) {
                currentTrack = right.get(i);
                seekSequence.add(currentTrack);
                distance = Math.abs(currentTrack - head);
                seekCount += distance;
                head = currentTrack;
            }
        }

        System.out.print("Total number of seek operations = " + seekCount + "\n");
        System.out.print("Seek Sequence is\n");
        for(int i = 0; i < seekSequence.size(); i++) {
            System.out.println(seekSequence.get(i));
        }
    }

    public static List<Integer> run(int[] requests, int head, String direction) {
        List<Integer> left = new ArrayList<>(), right = new ArrayList<>();
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
            if (!left.isEmpty()) { head = left.get(0); seq.add(head); }
            for (int i = 1; i < left.size(); i++) { head = left.get(i); seq.add(head); }
        } else {
            for (int i = left.size() - 1; i >= 0; i--) { head = left.get(i); seq.add(head); }
            if (!right.isEmpty()) { head = right.get(right.size() - 1); seq.add(head); }
            for (int i = right.size() - 2; i >= 0; i--) { head = right.get(i); seq.add(head); }
        }
        return seq;
    }
}
