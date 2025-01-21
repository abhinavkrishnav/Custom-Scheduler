import java.util.*;

class Pod {
    String id;
    int cpuRequest;
    int memoryRequest;

    public Pod(String id, int cpuRequest, int memoryRequest) {
        this.id = id;
        this.cpuRequest = cpuRequest;
        this.memoryRequest = memoryRequest;
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

        // Generate sample pods and nodes
        for (int i = 1; i <= 50; i++) {
            podQueue.add(new Pod("Pod" + i, (i % 4) + 1, (i % 8) + 2));
        }

        for (int i = 1; i <= 10; i++) {
            nodes.add(new Node("Node" + i, 20, 40));
        }

        // Print initial pod and node details
        printPodsAndNodes(podQueue, nodes);

        // Assign pods to nodes
        Map<String, String> assignments = assignPodsToNodes(podQueue, nodes);

        // Print assignment results
        printAssignments(assignments);

        // Print final node stats
        printNodeStats(nodes);
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

                if (finalScore > bestScore && node.cpuCapacity >= pod.cpuRequest && node.memoryCapacity >= pod.memoryRequest) {
                    bestScore = finalScore;
                    bestNode = node;
                }
            }

            if (bestNode != null) {
                assignments.put(pod.id, bestNode.id);
                bestNode.cpuCapacity -= pod.cpuRequest;
                bestNode.memoryCapacity -= pod.memoryRequest;
            }
        }
        return assignments;
    }

    private static double calculateResourceCostScore(Pod pod, Node node) {
        double costAvailable = node.cpuCapacity * CPU_COST_FACTOR + node.memoryCapacity * MEMORY_COST_FACTOR;
        double costRequested = pod.cpuRequest * CPU_COST_FACTOR + pod.memoryRequest * MEMORY_COST_FACTOR;
        return costAvailable - costRequested;
    }

    private static void printPodsAndNodes(Queue<Pod> pods, List<Node> nodes) {
        System.out.println("\nInitial Pod and Node Details:");
        System.out.println("------------------------------------------------");
        System.out.printf("%-10s %-10s %-10s\n", "Pod", "CPU", "Memory");
        for (Pod pod : pods) {
            System.out.printf("%-10s %-10d %-10d\n", pod.id, pod.cpuRequest, pod.memoryRequest);
        }
        System.out.println("------------------------------------------------");
        System.out.printf("%-10s %-10s %-10s\n", "Node", "CPU", "Memory");
        for (Node node : nodes) {
            System.out.printf("%-10s %-10d %-10d\n", node.id, node.cpuCapacity, node.memoryCapacity);
        }
    }

    private static void printAssignments(Map<String, String> assignments) {
        System.out.println("\nPod Assignments:");
        System.out.println("------------------------------------------------");
        System.out.printf("%-10s %-10s\n", "Pod", "Node");
        for (Map.Entry<String, String> entry : assignments.entrySet()) {
            System.out.printf("%-10s %-10s\n", entry.getKey(), entry.getValue());
        }
    }

    private static void printNodeStats(List<Node> nodes) {
        System.out.println("\nFinal Node Stats After Assignment:");
        System.out.println("------------------------------------------------");
        System.out.printf("%-10s %-10s %-10s\n", "Node", "CPU Left", "Memory Left");
        for (Node node : nodes) {
            System.out.printf("%-10s %-10d %-10d\n", node.id, node.cpuCapacity, node.memoryCapacity);
        }
    }
}
