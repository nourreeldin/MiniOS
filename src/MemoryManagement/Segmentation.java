package MemoryManagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Segmentation {
    private int totalMemory;
    private int nextAvailableAddress;
    private Map<Integer, SegmentTable> processSegmentTables;

    public static class Segment {
        public String name;
        public int size;
        public int base;
        public int limit;

        public Segment(String name, int size) {
            this.name = name;
            this.size = size;
            this.limit = size;
        }
    }

    public static class SegmentTable {
        public int processId;
        public ArrayList<Segment> segments;

        public SegmentTable(int processId) {
            this.processId = processId;
            this.segments = new ArrayList<>();
        }
    }

    public static class ProcessInfo {
        public int processId;
        public ArrayList<Segment> segments;

        public ProcessInfo(int processId) {
            this.processId = processId;
            this.segments = new ArrayList<>();
        }

        public void addSegment(String name, int size) {
            segments.add(new Segment(name, size));
        }

        public int getTotalSize() {
            int total = 0;
            for (Segment seg : segments) {
                total += seg.size;
            }
            return total;
        }
    }

    public Segmentation(int totalMemory) {
        this.totalMemory = totalMemory;
        this.nextAvailableAddress = 0;
        this.processSegmentTables = new HashMap<>();
    }

    public boolean allocateProcess(ProcessInfo process) {
        int totalSize = process.getTotalSize();
        if (nextAvailableAddress + totalSize > totalMemory) {
            return false;
        }

        SegmentTable segmentTable = new SegmentTable(process.processId);

        for (Segment seg : process.segments) {
            Segment allocatedSeg = new Segment(seg.name, seg.size);
            allocatedSeg.base = nextAvailableAddress;
            nextAvailableAddress += seg.size;
            segmentTable.segments.add(allocatedSeg);
        }

        processSegmentTables.put(process.processId, segmentTable);
        return true;
    }

    public Integer translateAddress(int processId, int segmentNumber, int offset) {
        if (!processSegmentTables.containsKey(processId)) {
            return null;
        }

        SegmentTable segmentTable = processSegmentTables.get(processId);
        if (segmentNumber >= segmentTable.segments.size()) {
            return null;
        }

        Segment segment = segmentTable.segments.get(segmentNumber);
        if (offset >= segment.limit) {
            return null;
        }

        return segment.base + offset;
    }

    public int getTotalMemory() {
        return totalMemory;
    }

    public int getUsedMemory() {
        return nextAvailableAddress;
    }

    public int getFreeMemory() {
        return totalMemory - nextAvailableAddress;
    }

    public Map<Integer, SegmentTable> getProcessSegmentTables() {
        return processSegmentTables;
    }
}