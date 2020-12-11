package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.function.Supplier;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;


public class ScenarioGenerator {  
  public static final int NUMBER_OF_INSTANCES = 1;
  
  public static final int NUMBER_OF_AGENTS = 10;
  
  public static final int NUMBER_OF_CLIENTS = 3;
  
  public static final int NUMBER_OF_SERVICES = 3;
  
  public static final int NUMBER_OF_DCOP_DEMAND_PER_SERVICE = 10;
  
  public static final String DIRECTORY = "scenario/random-network/d" + NUMBER_OF_AGENTS + "/";

  public static final Random rand = new Random();
  
  public static final int NUMBER_OF_CLIENTS_PER_POOL = 200;
  
  public static final double TASK_CONTAINER = 0.1;
  
  public static final int TIME_BETWEEN_REQUESTS = 60000; // milliseconds
  
//  public static final Map<String, Set<String>> edgeMap = new HashMap<>();

  public static void main(String[] args) throws IOException {    
    for (int instanceID = 0; instanceID < NUMBER_OF_INSTANCES; instanceID++) {
      List<String> clientList = new ArrayList<>();
      
      while (clientList.size() < NUMBER_OF_CLIENTS) {
        int randomNum = rand.nextInt(NUMBER_OF_AGENTS) + 1;
        //System.out.println(randomNum);
        String client = String.valueOf(randomNum);
        if (!clientList.contains(client)) {
          clientList.add(client);
        }
      }
      
      generateGraph(instanceID, clientList, NUMBER_OF_AGENTS);
//      generateSingleGraph(instanceID);
    }
  }
  
  
  //           / - B - \
  // Server - A         Client
  //           \ - C - /
  public static void generateSingleGraph(int instanceID) throws IOException {
    List<String> nodes = new ArrayList<>();
    List<String[]> edges = new ArrayList<>();
    List<String> clientList = new ArrayList<>();
    
    String client = "Client";
    String a = "A";
    String b = "B";
    String c = "C";
    String server = "Server";
    
    nodes.add(client); nodes.add(a); nodes.add(b); nodes.add(c); nodes.add(server);
    clientList.add(client);
    
    Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
    for (String node : nodes) {
      graph.addVertex(node);
    }
    // Server - A
    edges.add(new String[]{server, a});
    graph.addEdge(server, a);
    
    // A - B
    edges.add(new String[]{a, b});
    graph.addEdge(a, b);
    
    // A - C
    edges.add(new String[]{a, c});
    graph.addEdge(a, c);
    
    // B - Client
    edges.add(new String[]{b, client});
    graph.addEdge(b, client);
    
    // C- Client
    edges.add(new String[]{c, client});
    graph.addEdge(c, client);
    
    ConnectivityInspector<String, DefaultEdge> inspector = new ConnectivityInspector<>(graph);
    System.out.println("Instance ID=" + instanceID + ": " + inspector.isConnected());
    
    createScenarioAndDemandFile(server, clientList, nodes, edges, instanceID);
  }
  
  /** 
   * Generate 
   * @param instanceID
   * @param clientList
   * @throws IOException
   */
  public static void generateGraph(int instanceID, List<String> clientList, int numberOfAgents) throws IOException {
    List<String> nodes = new ArrayList<String>(); // Node_1, Node_2, Node_3
    List<String[]> edges = new ArrayList<String[]>();
    
    // Adjacent list
    Graph<String, DefaultEdge> randomGraph = generateGraph(numberOfAgents, 0.5);
    nodes.addAll(randomGraph.vertexSet());

    for (DefaultEdge edge : randomGraph.edgeSet()) {
      String vertexOne = randomGraph.getEdgeSource(edge);
      String vertexTwo = randomGraph.getEdgeTarget(edge);
      String[] edgeList = new String[] {vertexOne, vertexTwo};
      edges.add(edgeList);
    }
    
//    for (int one = 0; one < nodes.size() - 1; one++) {
//      String nodeOne = nodes.get(one);
//      for (int two = one + 1; two < nodes.size(); two++) {
//        String nodeTwo = nodes.get(two);
//        
//        Set<String> edgeOne = edgeMap.getOrDefault(nodeOne, new HashSet<>());
//        edgeOne.add(nodeTwo);
//        edgeMap.put(nodeOne, edgeOne);
//      }
//    }
    
    ConnectivityInspector<String, DefaultEdge> inspector = new ConnectivityInspector<>(randomGraph);
    System.out.println("Instance ID=" + instanceID + ": " + inspector.isConnected());
    String server = chooseServer(nodes, edges);
    
    createScenarioAndDemandFile(server, clientList, nodes, edges, instanceID);
  }
  
  public static Graph<String, DefaultEdge> generateGraph(int nodeCount, double p1) {
    // Create the VertexFactory so the generator can create vertices
    Supplier<String> vSupplier = new Supplier<String>()
    {
        private int id = 1;

        @Override
        public String get()
        {
            return String.valueOf(id++);
        }
    };

    // Create the graph object
    Graph<String, DefaultEdge> randomGraph =
        new SimpleGraph<>(vSupplier, SupplierUtil.createDefaultEdgeSupplier(), false);

    // Create the CompleteGraphGenerator object
    GnpRandomGraphGenerator<String, DefaultEdge> completeGenerator =
        new GnpRandomGraphGenerator<>(nodeCount, p1);

    // Use the CompleteGraphGenerator object to make completeGraph a
    // complete graph with [size] number of vertices
    completeGenerator.generateGraph(randomGraph);
    
    return randomGraph;
  }

  private static String chooseServer(List<String> nodes, List<String[]> edges) {
    Map<String, Integer> nodeDegree = new HashMap<>();
    for (String[] edge :edges) {
      for (String node : edge) {
        increment(nodeDegree, node);
      }
    }
    
    int maxDegree = Integer.MIN_VALUE;
    String chosenServer = null;
    for (Entry<String, Integer> entry : nodeDegree.entrySet()) {
      if (entry.getValue() > maxDegree) {
        maxDegree = entry.getValue();
        chosenServer = entry.getKey();
      }
    }
    
    return chosenServer;
  }
  
  private static void increment(Map<String, Integer> nodeDegree, String node) {
    int degree = nodeDegree.getOrDefault(node, 0) + 1;
    nodeDegree.put(node, degree);
  }

//  public static String convertToLetter(String from) {
//    return String.valueOf((char) (Integer.parseInt(from.substring(2)) + 64));
//  }

  /**
   * @param clientList
   * @param nodeList
   * @param edgeList
   * @param instanceID
   * @throws IOException
   */
  public static void createScenarioAndDemandFile(String server, List<String> clientList, List<String> nodeList, List<String[]> edgeList, int instanceID)
      throws IOException {
    // text for topology.ns
    String topology = "# CP Paper\n" 
                + "\n" 
                + "set ns [new Simulator]\n"
                + "source tb_compact.tcl\n"
                + "\n"
                + "# Clients\n";
    for (int i = 0; i < clientList.size(); i++) {
      topology += "set clientPool" + clientList.get(i) + " [$ns node]\n"
            + "tb-set-node-os $clientPool" + clientList.get(i) + " XEN46-64-STD\n";
    }
    topology += "\n"
              + "# NCPs\n";
    for (String node : nodeList) {
      topology += "set node" + node + " [$ns node]\n"
                + "tb-set-node-os $node" + node + " XEN46-64-STD\n"
                + "tb-set-hardware $node" + node + " ";
      topology += node.equals(server) ? "large" : "simple" ;
      topology += "\n\n";
    }
    topology += "# Links\n";
    
//    for (Entry<String, Set<String>> entry : edgeMap.entrySet()) {
//      String nodeOne = entry.getKey();
//      
//      for (String nodeTwo : entry.getValue()) {
//        String[] temp1 = new String[] {nodeOne, nodeTwo};
//        String[] temp2 = new String[] {nodeTwo, nodeOne};
//        
//        if (contains(edgeList, temp1) || contains(edgeList, temp2)) {
//          topology += "set link" + nodeOne + nodeTwo + " [$ns duplex-link $node" + nodeOne + " $node"
//              + nodeTwo + " 100000.0kb 0.0ms DropTail]\n";
//        } else {
//          topology += "set link" + nodeOne + nodeTwo + " [$ns duplex-link $node" + nodeOne + " $node"
//              + nodeTwo + " 900000.0kb 0.0ms DropTail]\n";
//        }
//      }
//    }
    
    for (String[] edge : edgeList) {
      String nodeOne = edge[0];
      String nodeTwo = edge[1];

      topology += "set link" + nodeOne + nodeTwo + " [$ns duplex-link $node" + nodeOne + " $node" + nodeTwo + " 100000.0kb 0.0ms DropTail]\n";
    }
    
    topology += "\n";
    for (int i = 0; i < clientList.size(); i++) {
      topology += "set linkClient" + clientList.get(i) + " [$ns duplex-link $clientPool" + clientList.get(i) + " $node" + clientList.get(i)
                + " 100000.0kb 0.0ms DropTail]\n";
    }
    topology += "\n"
              + "$ns rtproto Static\n"
              + "$ns run";
    writeToFile(instanceID, topology, clientList, nodeList, server);
  }
  
  public static boolean contains(List<String[]> list, String[] array) {
    for (String[] element : list) {
      if (Arrays.equals(element, array)) {
        return true;
      }      
    }
    
    return false;
  }

  public static void writeToFile(int instanceID, String topology, List<String> clients, List<String> nodes, String defaultNode)
      throws IOException {
    // Create a directory; all non-existent ancestor directories are automatically created
    File folder = new File(DIRECTORY + instanceID);
    if (!folder.exists()) {
      folder.mkdirs();
    }
    
    BufferedWriter writer = new BufferedWriter(new FileWriter(DIRECTORY + instanceID + "/topology.ns"));
    writer.write(topology);
    writer.close();

    // generate clientPool files
    for (int i = 0; i < clients.size(); i++) {
      BufferedWriter clientWriter = new BufferedWriter(
          new FileWriter(DIRECTORY + "" + instanceID + "/clientPool" + clients.get(i) + ".json"));
      clientWriter.write("{\n\t\"region\": \"" + clients.get(i) + "\",\n\t\"client\": true,\n\t\"numClients\": 100\n}");
      clientWriter.close();
    }

    // generate files per region using nodes
    for (int i = 0; i < nodes.size(); i++) {
      BufferedWriter writerNode = new BufferedWriter(new FileWriter(DIRECTORY + "" + instanceID + "/node" + nodes.get(i) + ".json"));
      writerNode.write("{\n\t\"region\": \"" + nodes.get(i) + "\",\n\t\"dns\": true,\n\t\"DCOP\": true,\n\t\"RLG\": true\n}");
      writerNode.close();
    }

    // hardware configuration: simple and large
    String hardware = "[\n";

    hardware += " {\n\"name\": \"simple\",\n\t\"capacity\": {\n\t  \"CPU\": 4,\n\t  \"MEMORY\": 8000000000,"
        + "\n\t \"DISK\": 10,\n\t \"TASK_CONTAINERS\": 20\n\t}\n},\n";
    hardware += " {\n\"name\": \"large\",\n\t\"capacity\": {\n\t  \"CPU\": 8,\n\t  \"MEMORY\": 16000000000,"
        + "\n\t \"DISK\": 10,\n\t \"TASK_CONTAINERS\": 8\n\t}\n}\n";
    hardware += "]";
    BufferedWriter hardwareWriter = new BufferedWriter(new FileWriter(DIRECTORY + instanceID + "/hardware-configurations.json"));
    hardwareWriter.write(hardware);
    hardwareWriter.close();

    // service configuration
    String service = "[\n";

    for (int i = 1; i <= NUMBER_OF_SERVICES; i++) {
      if (i != 1) {
        service += ",\n";
      }
      service += " {\n"
          + "\t\"service\": {\n"
          + "\t \"group\": \"com.bbn\",\n"
          + "\t \"artifact\": \"test-service" + i + "\",\n"
          + "\t \"version\": \"1\"\n"
          + "\t},\n"
          + "\t\"hostname\": \"test-service\",\n"
          + "\t\"defaultNode\": \"node" + defaultNode + "\",\n"
          + "\t\"defaultNodeRegion\": \"" + defaultNode + "\",\n"
          + "\t\"initialInstances\": \"1\",\n"
          + "\t\"computeCapacity\": {\n"
          + "\t  \"TASK_CONTAINERS\": \"1\",\n"
          + "\t\"CPU\": \"1\"\n"
          + "\t},\n"
          + "\t\"networkCapacity\": {\n"
          + "\t  \"DATARATE_TX\": \"100\",\n"
          + "\t  \"DATARATE_RX\": \"100\"\n"
          + "\t},\n"
          + "\t\"priority\": " + i + ",\n"
          + "\t\"trafficType\": \"RX_GREATER\"\n }";
    }
    service += "\n]";

    BufferedWriter serviceConfigurationWriter = new BufferedWriter(new FileWriter(DIRECTORY + instanceID + "/service-configurations.json"));
    serviceConfigurationWriter.write(service);
    serviceConfigurationWriter.close();

    // client demand
    String demand = "[\n";
    int startTime = 30000;
    for (int i = 0; i < NUMBER_OF_DCOP_DEMAND_PER_SERVICE; i++) {
      for (int serviceID = 1; serviceID <= NUMBER_OF_SERVICES; serviceID++) {
        demand += " {\n"
                + "\t\"startTime\": " + startTime + ",\n"
                + "\t\"serverDuration\": " + TIME_BETWEEN_REQUESTS + ",\n"
                + "\t\"networkDuration\": " + TIME_BETWEEN_REQUESTS + ",\n"
                + "\t\"numClients\": " + NUMBER_OF_CLIENTS_PER_POOL + ",\n"
                + "\t\"service\": {\n"
                +  "\t  \"group\": \"com.bbn\",\n"
                + "\t  \"artifact\": \"test-service" + serviceID + "\",\n"
                + "\t  \"version\": \"1\"\n"
                + "\t},\n"
                + "\t\"nodeLoad\": {\n"
                + "\t  \"TASK_CONTAINERS\": " + 0.1 + "\n"
                + "\t},\n"
                + "\t\"networkLoad\": {\n"
                + "\t  \"DATARATE_TX\": 0.001,\n"
                + "\t  \"DATARATE_RX\": 0.002\n"
                + "\t}\n"
                +" }";
          if (serviceID != NUMBER_OF_SERVICES || i != NUMBER_OF_DCOP_DEMAND_PER_SERVICE - 1) {
            demand += ",\n";
          }
        }
      startTime += TIME_BETWEEN_REQUESTS;
    }
    demand += "\n]";
    
    // create demand folder
    File demandFolder = new File(DIRECTORY + "" + instanceID + "/demand");
    if (!demandFolder.exists())
      demandFolder.mkdir();

    for (int i = 0; i < clients.size(); i++) {
      BufferedWriter writerDemand = new BufferedWriter(
          new FileWriter(DIRECTORY + "" + instanceID + "/Demand/clientPool" + clients.get(i) + ".json"));
      writerDemand.write(demand);
      writerDemand.close();
    }
  }
}
