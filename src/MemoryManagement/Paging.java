package MemoryManagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Paging {
    private int totalMemory;
    private int frameSize;
    private int totalFrames;
    private Map<Integer, FrameInfo> frameTable;
    private Map<Integer, PageTable> processPageTables;
    private ArrayList<Integer> freeFrames;

    public static class FrameInfo {
        public int processId;
        public int pageNumber;
        public boolean isFree;

        public FrameInfo() {
            this.isFree = true;
            this.processId = -1;
            this.pageNumber = -1;
        }

        public FrameInfo(int processId, int pageNumber) {
            this.processId = processId;
            this.pageNumber = pageNumber;
            this.isFree = false;
        }
    }

    public static class PageTable {
        public int processId;
        public Map<Integer, Integer> pageToFrame;

        public PageTable(int processId) {
            this.processId = processId;
            this.pageToFrame = new HashMap<>();
        }
    }

    public static class ProcessInfo {
        public int processId;
        public int processSize;
        public int numPages;

        public ProcessInfo(int processId, int processSize, int frameSize) {
            this.processId = processId;
            this.processSize = processSize;
            this.numPages = (int) Math.ceil((double) processSize / frameSize);
        }
    }

    public Paging(int totalMemory, int frameSize) {
        this.totalMemory = totalMemory;
        this.frameSize = frameSize;
        this.totalFrames = totalMemory / frameSize;
        this.frameTable = new HashMap<>();
        this.processPageTables = new HashMap<>();
        this.freeFrames = new ArrayList<>();

        for (int i = 0; i < totalFrames; i++) {
            frameTable.put(i, new FrameInfo());
            freeFrames.add(i);
        }
    }

    public boolean allocateProcess(ProcessInfo process) {
        if (freeFrames.size() < process.numPages) {
            return false;
        }

        PageTable pageTable = new PageTable(process.processId);

        for (int page = 0; page < process.numPages; page++) {
            int frame = freeFrames.remove(0);
            pageTable.pageToFrame.put(page, frame);
            frameTable.put(frame, new FrameInfo(process.processId, page));
        }

        processPageTables.put(process.processId, pageTable);
        return true;
    }

    public Integer translateAddress(int processId, int pageNumber, int offset) {
        if (!processPageTables.containsKey(processId)) {
            return null;
        }

        PageTable pageTable = processPageTables.get(processId);
        if (!pageTable.pageToFrame.containsKey(pageNumber)) {
            return null;
        }

        int frame = pageTable.pageToFrame.get(pageNumber);
        return (frame * frameSize) + offset;
    }

    public int getTotalMemory() {
        return totalMemory;
    }

    public int getFrameSize() {
        return frameSize;
    }

    public int getTotalFrames() {
        return totalFrames;
    }

    public Map<Integer, FrameInfo> getFrameTable() {
        return frameTable;
    }

    public Map<Integer, PageTable> getProcessPageTables() {
        return processPageTables;
    }

    public int getFreeFramesCount() {
        return freeFrames.size();
    }
}