package DiskScheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CSCAN {
    public static void apply(int[] arr, int head, String direction, int diskSize) {
        int seekCount = 0;
        int distance, currenttrack;
        int size = arr.length;
        ArrayList<Integer> left = new ArrayList<>(), right = new ArrayList<>();
        ArrayList<Integer> seekSequence = new ArrayList<>();

        left.add(0);
        right.add(diskSize - 1);

        for(int i = 0; i < size; i++) {
            if(arr[i] < head) left.add(arr[i]);
            else if(arr[i] > head) right.add(arr[i]);
            else seekSequence.add(arr[i]);
        }

        Collections.sort(left);
        Collections.sort(right);

        if(direction.equals("right")) {
            for(int i = 0; i < right.size(); i++) {
                currenttrack = right.get(i);
                seekSequence.add(currenttrack);
                distance = Math.abs(currenttrack - head);
                seekCount += distance;
                head = currenttrack;
            }

            head = 0;
            seekCount += (diskSize - 1);
            seekSequence.add(0);

            for(int i = 0; i < left.size(); i++) {
                currenttrack = left.get(i);
                seekSequence.add(currenttrack);
                distance = Math.abs(currenttrack - head);
                seekCount += distance;
                head = currenttrack;
            }

        } else if(direction.equals("left")) {
            for(int i = left.size() - 1; i >= 0; i--) {
                currenttrack = left.get(i);
                seekSequence.add(currenttrack);
                distance = Math.abs(currenttrack - head);
                seekCount += distance;
                head = currenttrack;
            }

            head = diskSize - 1;
            seekCount += (diskSize - 1);
            seekSequence.add(diskSize - 1);

            for(int i = right.size() - 1; i >= 0; i--) {
                currenttrack = right.get(i);
                seekSequence.add(currenttrack);
                distance = Math.abs(currenttrack - head);
                seekCount += distance;
                head = currenttrack;
            }
        }

        System.out.print("Total number of seek operations = " + seekCount + "\n");
        System.out.print("Seek Sequence is\n");
        for(int i = 0; i < seekSequence.size(); i++) {
            System.out.println(seekSequence.get(i));
        }
    }

    public static List<Integer> run(int[] requests, int head, String direction, int diskSize) {
        List<Integer> left = new ArrayList<>(), right = new ArrayList<>();
        left.add(0); right.add(diskSize - 1);
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
            head = 0; seq.add(0);
            for (int t : left) { head = t; seq.add(head); }
        } else {
            for (int i = left.size() - 1; i >= 0; i--) { head = left.get(i); seq.add(head); }
            head = diskSize - 1; seq.add(head);
            for (int i = right.size() - 1; i >= 0; i--) { head = right.get(i); seq.add(head); }
        }
        return seq;
    }
}
