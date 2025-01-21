import java.util.*;

class Pod {
    String id;
    int cpu;
    int memory;
    Map<String, Integer> affinities;
    Map<String, Integer> antiAffinities;

    public Pod(String id, int cpu, int memory) {
        this.id = id;
        this.cpu = cpu;
        this.memory = memory;
        this.affinities = new HashMap<>();
        this.antiAffinities = new HashMap<>();
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
        Map<String, Map<String, Integer>> trafficMatrix = new HashMap<>();
        Map<String, Map<String, Integer>> latencyMatrix = new HashMap<>();

        // Sample data initialization
        podQueue.add(new Pod("pod1", 2, 4));
        podQueue.add(new Pod("pod2", 1, 2));
        podQueue.add(new Pod("pod3", 3, 6));

        nodes.add(new Node("node1", 4, 8));
        nodes.add(new Node("node2", 2, 4));
        nodes.add(new Node("node3", 6, 10));

        System.out.println("POD SCHEDULING STARTED...");
        System.out.println("Number of Pods: " + podQueue.size());
        System.out.println("Number of Nodes: " + nodes.size());
        System.out.println("\nPod Details:");
        for (Pod pod : podQueue) {
            System.out.println("- " + pod.id + " [CPU: " + pod.cpu + ", Memory: " + pod.memory + "]");
        }
        
        System.out.println("\nNode Details:");
        for (Node node : nodes) {
            System.out.println("- " + node.id + " [CPU: " + node.cpuCapacity + ", Memory: " + node.memoryCapacity + "]");
        }

        Map<String, String> podAssignments = assignPodsToNodes(podQueue, nodes, trafficMatrix, latencyMatrix);
        
        System.out.println("\nPod Assignments:");
        for (Map.Entry<String, String> entry : podAssignments.entrySet()) {
            System.out.println("Pod " + entry.getKey() + " assigned to Node " + entry.getValue());
        }
    }

    public static Map<String, String> assignPodsToNodes(Queue<Pod> podQueue, List<Node> nodes, 
                                                         Map<String, Map<String, Integer>> trafficMatrix, 
                                                         Map<String, Map<String, Integer>> latencyMatrix) {
        Map<String, String> assignments = new HashMap<>();
        while (!podQueue.isEmpty()) {
            Pod pod = podQueue.poll();
            Node bestNode = null;
            double bestScore = Double.NEGATIVE_INFINITY;

            for (Node node : nodes) {
                double resourceCostScore = calculateResourceCostScore(pod, node);
                double trafficCostScore = calculateTrafficCostScore(pod, node, assignments, trafficMatrix, latencyMatrix);
                double finalScore = ALPHA * resourceCostScore - BETA * trafficCostScore;

                if (finalScore > bestScore && checkConstraints(pod, node, assignments)) {
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

    private static double calculateTrafficCostScore(Pod pod, Node node, 
                                                    Map<String, String> assignments, 
                                                    Map<String, Map<String, Integer>> trafficMatrix, 
                                                    Map<String, Map<String, Integer>> latencyMatrix) {
        double cost = 0;
        for (Map.Entry<String, String> entry : assignments.entrySet()) {
            String scheduledPod = entry.getKey();
            String scheduledNode = entry.getValue();
            if (trafficMatrix.containsKey(pod.id) && trafficMatrix.get(pod.id).containsKey(scheduledPod)) {
                cost += trafficMatrix.get(pod.id).get(scheduledPod) * latencyMatrix.get(node.id).get(scheduledNode);
            }
        }
        return cost;
    }

    private static boolean checkConstraints(Pod pod, Node node, Map<String, String> assignments) {
        for (Map.Entry<String, String> entry : assignments.entrySet()) {
            String scheduledPod = entry.getKey();
            String scheduledNode = entry.getValue();
            if (pod.antiAffinities.containsKey(scheduledPod) && scheduledNode.equals(node.id)) {
                return false;
            }
        }
        return true;
    }
}
