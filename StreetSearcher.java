package hw8;

import exceptions.InsertionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.Scanner;


/**
 * Search for the shortest path between two endpoints using
 * Djikstra's. We use a HashMap to store all the vertices so we can
 * find them by name (i.e. their coordinates) when inserting for a
 * fast duplicates check.
 * 
 * Vertex data is the coordinates, stored as a String.
 * Vertex label is the Edge into it on the path found.
 * Edge data is the road name, stored as a String.
 * Edge label is the road length, stored as a Double.
 *
 */
public final class StreetSearcher {

    // useful for marking distance to nodes, or use Double.POSITIVE_INFINITY
    private static final double MAX_DISTANCE = 1e18;

    // Global variables, bleh
    private static Map<String, Vertex<String>> vertices = new HashMap<>();
    private static SparseGraph<String, String> graph = new SparseGraph<>();

    // Silencing checkstyle
    private StreetSearcher() {}

    // Get the path by tracing labels back from end to start.
    private static List<Edge<String>> getPath(Vertex<String> end,
                                              Vertex<String> start) {
        if (graph.label(end) != null) {
            List<Edge<String>> path = new ArrayList<>();

            Vertex<String> cur = end;
            Edge<String> road;
            while (cur != start) {
                road = (Edge<String>) graph.label(cur);  // unchecked cast ok
                path.add(road);
                cur = graph.from(road);
            }
            return path;
        }
        return null;
    }

    // Print the path found.
    private static void printPath(List<Edge<String>> path,
                                  double totalDistance) {
        if (path == null) {
            System.out.println("No path found");
            return;
        }

        System.out.println("Total Distance: " + totalDistance);
        for (int i = path.size() - 1; i >= 0; i--) {
            System.out.println(path.get(i).get() + " "
                    + graph.label(path.get(i)));
        }
    }



    // Djikstra's Algorithm to find shortest path.
    private static void findShortestPath(String startName, String endName) {
        Vertex<String> start = vertices.get(startName);
        Vertex<String> end = vertices.get(endName);


        double totalDist = -1;

        //if start and end vertices are same distance is zero
        if (start.equals(end)) {
            totalDist = 0;
            System.out.println("total Distance: " + totalDist);
            return;
        }

        //vertex priority queue using comparator written in helper method below
        PriorityQueue<Vertex<String>> pq = new
                PriorityQueue<>(new VertexComp());
        //
        //hashSet containing explored vertices coordinates
        Set<String> explored = new HashSet<>();

        // TODO - write this!
        /* 
           The biggest issue here is how to create and maintain an
           adaptable priority queue somehow. You do not have to write
           the most efficient implementation possible. It does need to
           be correct. You will need to keep track of distances for
           vertices. Feel free to update the SparseGraph
           implementation to handle that for you. Another option is to
           create a nested class here for comparable objects that have
           vertex info and distance info to use in a standard priority
           queue.
        */
        //add start vertex to priority queue and set distance to zero
        graph.setDist(start, 0);
        pq.add(start);

        //while not all vertices explored
        while (explored.size() != vertices.size() && !pq.isEmpty()) {

            //remove min distance vertex and mark as explored
            Vertex<String> v = pq.remove();
            explored.add(v.get());


            ArrayList<Edge<String>> outgoing = (ArrayList<Edge<String>>)
                    graph.outgoing(v);
            //iterate through all neighbors of v
            for (int i = 0; i < outgoing.size(); i++) {
                Vertex<String> cur = graph.to(outgoing.get(i));
                //if neighbor is not already explored
                if (!explored.contains(cur.get())) {
                    double newDist = graph.getDist(v) +
                            (double) graph.label(outgoing.get(i));
                    //if new distance less than prev distance to vertex, replace
                    if (newDist < graph.getDist(cur)) {
                        graph.setDist(cur, newDist);
                        graph.label(cur, outgoing.get(i));
                        //add neighbors to priority queue
                        pq.add(cur);
                    }

                }
            }
        }

        // These method calls will create and print the path for you
        List<Edge<String>> path = getPath(end, start);
        if (path != null) {
            totalDist = 0;
            for (int i = 0; i < path.size(); i++) {
                totalDist += (double) graph.label(path.get(i));
            }
        }
        printPath(path, totalDist);
    }

    /**
     * Comparator class for vertices, for use in priority queue.
     */
    private static class VertexComp implements Comparator<Vertex<String>> {
        public int compare(Vertex<String> node1, Vertex<String> node2) {
            if (graph.getDist(node1) < graph.getDist(node2)) {
                return -1;
            }
            if (graph.getDist(node1) > graph.getDist(node2)) {
                return 1;
            }
            return 0;
        }
    }


    // Add an endpoint to the network if it is a new endpoint
    private static Vertex<String> addLocation(String name) {
        if (!vertices.containsKey(name)) {
            Vertex<String> v = graph.insert(name);
            vertices.put(name, v);
            return v;
        }
        return vertices.get(name);
    }


    // Load network from fileName, returns number of roads
    private static int loadNetwork(String fileName)
            throws FileNotFoundException {

        int numRoads = 0;

        // Read in from file fileName
        Scanner input = new Scanner(new FileInputStream(new File(fileName)));
        while (input.hasNext()) {

            // Parse the line in to <end1> <end2> <road-distance> <road-name>
            String[] tokens = input.nextLine().split(" ");
            String fromName = tokens[0];
            String toName = tokens[1];
            double roadDistance = Double.parseDouble(tokens[2]);
            String roadName = tokens[3];

            // Get the from and to endpoints, adding if necessary
            Vertex<String> from = addLocation(fromName);
            Vertex<String> to =  addLocation(toName);

            // Add the road to the network - We assume all roads are two-way and
            // ignore if we've already added the road as a reverse of another
            try {
                Edge<String> road = graph.insert(from, to, roadName);
                Edge<String> backwardsRoad = graph.insert(to, from, roadName);
                numRoads += 2;

                // Label each road with it's weight
                graph.label(road, roadDistance);
                graph.label(backwardsRoad, roadDistance);

            } catch (InsertionException ignored) {
                // Nothing to do.
            }
        }

        return numRoads;
    }

    private static void checkValidEndpoint(String endpointName) {
        if (!vertices.containsKey(endpointName)) {
            throw new IllegalArgumentException(endpointName);
        }
    }

    /**
     * Main method.
     * @param args See usage.
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: " +
                    "StreetSearcher <map_name> <start_coords> <end_coords>");
            return;
        }

        String fileName  = args[0];
        String startName = args[1];
        String endName   = args[2];

        try {

            int numRoads = loadNetwork(fileName);
            System.out.println("Network Loaded!");
            System.out.println("Loaded " + numRoads + " roads");
            System.out.println("Loaded " + vertices.size() + " endpoints");

            checkValidEndpoint(startName);
            checkValidEndpoint(endName);

        } catch (FileNotFoundException e) {
            System.err.println("Could not find file " + fileName);
            return;
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid Endpoint: " + e.getMessage());
            return;
        }

        findShortestPath(startName, endName);
    }
}
