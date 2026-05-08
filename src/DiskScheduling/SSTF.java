package DiskScheduling;

import java.util.*;

public class SSTF {
    private static class Node {
        int distance = 0;
        boolean accessed = false;
    }

    public static void calculateDifference(int[] queue, int head, Node[] diff) {
        for(int i = 0; i < diff.length; i++) {
            diff[i].distance = Math.abs(queue[i] - head);
        }
    }

    public static int findMin(Node[] diff) {
        int index = -1, minimum = Integer.MAX_VALUE;
        for(int i = 0; i < diff.length; i++) {
            if(!diff[i].accessed && minimum > diff[i].distance) {
                minimum = diff[i].distance;
                index = i;
            }
        }
        return index;
    }

    public static void apply(int[] request, int head) {
        if(request.length == 0) return;
        Node[] diff = new Node[request.length];
        for(int i = 0; i < diff.length; i++) { diff[i] = new Node(); }
        int seekCount = 0;

        int[] seekSequence = new int[request.length + 1];
        for(int i = 0; i < request.length; i++) {
            seekSequence[i] = head;
            calculateDifference(request, head, diff);
            int index = findMin(diff);
            diff[index].accessed = true;
            seekCount += diff[index].distance;
            head = request[index];
        }
        seekSequence[seekSequence.length - 1] = head;
        System.out.println("Total number of seek operations = " + seekCount);
        System.out.println("Seek Sequence is");
        for(int i = 0; i < seekSequence.length; i++) { System.out.println(seekSequence[i]); }
    }

    public static List<Integer> run(int[] requests, int head) {
        List<Integer> seq = new ArrayList<>();
        seq.add(head);
        boolean[] visited = new boolean[requests.length];
        for (int i = 0; i < requests.length; i++) {
            int minDist = Integer.MAX_VALUE, minIdx = -1;
            for (int j = 0; j < requests.length; j++) {
                if (!visited[j] && Math.abs(requests[j] - head) < minDist) {
                    minDist = Math.abs(requests[j] - head);
                    minIdx = j;
                }
            }
            visited[minIdx] = true;
            head = requests[minIdx];
            seq.add(head);
        }
        return seq;
    }
}

