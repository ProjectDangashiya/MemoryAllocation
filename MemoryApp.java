import java.io.*;
import java.util.*;

class Job {
    int id;
    int startTime;
    int size;
    int duration;
    String state;
    boolean active;
    int endTime;

    public Job(int id, int start, int size, int duration, String state) {
        this.id = id;
        this.startTime = start;
        this.size = size;
        this.duration = duration;
        this.state = state;
        this.active = false;
        this.endTime = start + duration;
    }
}


public class MemoryApp {
    static final int TOTAL_PAGES = 20;
    static String[] memory = new String[TOTAL_PAGES];
    static List<Job> jobs = new ArrayList<>();

    public static void main(String[] args) {
        try {
            System.out.println("=== Running First-Fit ===");
            resetMemory();
            loadJobs("Assignment1.txt");
            runSimulation("FirstFit");

            System.out.println("\n=== Running Best-Fit ===");
            resetMemory();
            loadJobs("Assignment1.txt");
            runSimulation("BestFit");

            System.out.println("\n=== Running Worst-Fit ===");
            resetMemory();
            loadJobs("Assignment1.txt");
            runSimulation("WorstFit");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void runSimulation(String strategy) {
        for (int time = 0; time <= 60; time++) {
            startJobs(time, strategy);
            endJobs(time);
            System.out.print("Time " + time + " - Memory: ");
            printMemory();
        }
    }

    static void resetMemory() {
        Arrays.fill(memory, null);
        jobs.clear();
    }

    static void loadJobs(String fileName) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty() || line.startsWith("#")) continue;
            String[] parts = line.trim().split("\\s+");
            if (parts.length < 5) continue;
            int id = Integer.parseInt(parts[0]);
            int start = Integer.parseInt(parts[1]);
            int size = Integer.parseInt(parts[2]);
            int duration = Integer.parseInt(parts[3]);
            String state = parts[4];
            jobs.add(new Job(id, start, size, duration, state));
        }
        br.close();
    }

    static void startJobs(int time, String strategy) {
        for (Job job : jobs) {
            if (!job.active && job.startTime == time) {
                boolean allocated = allocate(job, strategy);
                if (allocated) {
                    job.active = true;
                    System.out.println("Time " + time + ": Job " + job.id + " started.");
                } else {
                    System.out.println("Time " + time + ": Job " + job.id + " could not be allocated.");
                }
            }
        }
    }

    static void endJobs(int time) {
        for (Job job : jobs) {
            if (job.active && job.endTime == time && job.state.equalsIgnoreCase("End")) {
                deallocate(job);
                job.active = false;
                System.out.println("Time " + time + ": Job " + job.id + " ended and memory freed.");
            }
        }
    }

    static boolean allocate(Job job, String strategy) {
        List<List<Integer>> blocks = getFreeBlocks();
        blocks.removeIf(block -> block.size() < job.size);

        if (blocks.isEmpty()) return false;

        List<Integer> selectedBlock = null;

        switch (strategy) {
            case "FirstFit":
                selectedBlock = blocks.get(0);
                break;
            case "BestFit":
                selectedBlock = blocks.stream().min(Comparator.comparingInt(List::size)).orElse(null);
                break;
            case "WorstFit":
                selectedBlock = blocks.stream().max(Comparator.comparingInt(List::size)).orElse(null);
                break;
        }

        if (selectedBlock == null) return false;

        for (int i = 0; i < job.size; i++) {
            memory[selectedBlock.get(i)] = "J" + job.id;
        }
        return true;
    }

    static void deallocate(Job job) {
        for (int i = 0; i < TOTAL_PAGES; i++) {
            if (("J" + job.id).equals(memory[i])) {
                memory[i] = null;
            }
        }
    }

    static List<List<Integer>> getFreeBlocks() {
        List<List<Integer>> blocks = new ArrayList<>();
        List<Integer> current = new ArrayList<>();

        for (int i = 0; i < TOTAL_PAGES; i++) {
            if (memory[i] == null) {
                current.add(i);
            } else if (!current.isEmpty()) {
                blocks.add(new ArrayList<>(current));
                current.clear();
            }
        }

        if (!current.isEmpty()) blocks.add(current);
        return blocks;
    }

    static void printMemory() {
        for (int i = 0; i < TOTAL_PAGES; i++) {
            System.out.print(memory[i] == null ? "[ ]" : "[" + memory[i] + "]");
        }
        System.out.println();
    }
}

