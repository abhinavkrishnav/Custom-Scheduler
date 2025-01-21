import java.util.*;

class Pod {
    String id;
    int cpu;
    int memory;

    public Pod(String id, int cpu, int memory) {
        this.id = id;
        this.cpu = cpu;
        this.memory = memory;
    }
}

class Node {
    String id;
    int cpuCapacity;
    int memoryCapacity;

    public Node(String id, int cpuCapacity, int memoryCapacity) {
        this.id = id;
        this.cpuCapacity = cpuCapacity;
        this.memoryCapacity = memoryCapacity;
    }
}

public class PodScheduler {
    private static final double CPU_COST_FACTOR = 1.0;
    private static final double MEMORY_COST_FACTOR = 1.0;
    private static final double ALPHA = 0.7;
    private static final double BETA = 0.3;

    public static void main(String[] args) {
        Queue<Pod> podQueue = new LinkedList<>();
        List<Node> nodes = new ArrayList<>();

        // Initialize 50 pods with random CPU and memory requirements
        for (int i = 1; i <= 50; i++) {
            podQueue.add(new Pod("pod" + i, (int) (Math.random() * 4 + 1), (int) (Math.random() * 8 + 1)));
        }

        // Initialize 10 nodes with random CPU and memory capacities
        for (int i = 1; i <= 10; i++) {
            nodes.add(new Node("node" + i, (int) (Math.random() * 16 + 8), (int) (Math.random() * 32 + 16)));
        }

        System.out.println("Initial Pod Queue:");
        for (Pod pod : podQueue) {
            System.out.println(pod.id + " - CPU: " + pod.cpu + ", Memory: " + pod.memory);
        }

        System.out.println("\nAvailable Nodes:");
        for (Node node : nodes) {
            System.out.println(node.id + " - CPU: " + node.cpuCapacity + ", Memory: " + node.memoryCapacity);
        }

        Map<String, String> podAssignments = assignPodsToNodes(podQueue, nodes);
        
        System.out.println("\nPod Assignments:");
        for (Map.Entry<String, String> entry : podAssignments.entrySet()) {
            System.out.println(entry.getKey() + " assigned to " + entry.getValue());
        }

        System.out.println("\nNode Status After Assignment:");
        for (Node node : nodes) {
            System.out.println(node.id + " - Remaining CPU: " + node.cpuCapacity + ", Remaining Memory: " + node.memoryCapacity);
        }
    }

    public static Map<String, String> assignPodsToNodes(Queue<Pod> podQueue, List<Node> nodes) {
        Map<String, String> assignments = new HashMap<>();
        while (!podQueue.isEmpty()) {
            Pod pod = podQueue.poll();
            Node bestNode = null;
            double bestScore = Double.NEGATIVE_INFINITY;

            for (Node node : nodes) {
                double resourceCostScore = calculateResourceCostScore(pod, node);
                double finalScore = ALPHA * resourceCostScore;

                if (finalScore > bestScore && checkConstraints(pod, node)) {
                    bestScore = finalScore;
                    bestNode = node;
                }
            }

            if (bestNode != null) {
                assignments.put(pod.id, bestNode.id);
                bestNode.cpuCapacity -= pod.cpu;
                bestNode.memoryCapacity -= pod.memory;
            }
        }
        return assignments;
    }

    private static double calculateResourceCostScore(Pod pod, Node node) {
        double costAvailable = node.cpuCapacity * CPU_COST_FACTOR + node.memoryCapacity * MEMORY_COST_FACTOR;
        double costRequested = pod.cpu * CPU_COST_FACTOR + pod.memory * MEMORY_COST_FACTOR;
        return costAvailable - costRequested;
    }

    private static boolean checkConstraints(Pod pod, Node node) {
        return node.cpuCapacity >= pod.cpu && node.memoryCapacity >= pod.memory;
    }
}
