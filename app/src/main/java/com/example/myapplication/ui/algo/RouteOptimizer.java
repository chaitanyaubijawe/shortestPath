package com.example.myapplication.ui.algo;

import java.util.*;
import java.lang.*;

public class RouteOptimizer {
    // Number of vertices in the graph
    private int SIZE;



    // A utility function to find the vertex with minimum weight(distance/time)
    // value, from the set of vertices not yet included in MST
    int minWeight(double weight[], Boolean mstSet[])
    {
        // Initialize min value
        double min = Integer.MAX_VALUE;
        int min_index = -1;

        for (int v = 0; v < SIZE; v++)
            if (mstSet[v] == false && weight[v] < min) {
                min = weight[v];
                min_index = v;
            }

        return min_index;
    }

    // A utility function to print the constructed MST stored in
    // parent[]
    void printMST(int parent[], int graph[][],int key[])
    {
        System.out.println("Edge \tWeight");
        for (int i = 1; i < SIZE; i++)
            System.out.println(parent[i] + " - " + i + "\t" + key[i]);
    }

    // Function to construct and print MST for a graph represented
    // using adjacency matrix representation
    List<List<Integer>> findOptimizedRoute(List<List<Double>> graph)
    {
        this.SIZE = graph.size();

        //Keeps the route for each destinations node
        List routes[] = new List[SIZE];

        //Reduce to only max routes.
        List<List<Integer>> outputRouts = new ArrayList<>();

        // Array to store constructed MST
        int parent[] = new int[SIZE];

        // Weight values used to pick minimum weight edge in cut
        double weight[] = new double[SIZE];

        // To represent set of vertices not yet included in MST
        Boolean visited[] = new Boolean[SIZE];

        // Initialize all keys as INFINITE
        for (int i = 0; i < SIZE; i++) {
            weight[i] = Integer.MAX_VALUE;
            visited[i] = false;
        }

        // Always include first 1st vertex in MST.
        weight[0] = 0; // Make key 0 so that this vertex is
        // picked as first vertex
        parent[0] = -1; // First node is always root of MST

        // The MST will have V vertices
        for (int count = 0; count < SIZE - 1; count++) {
            // Pick thd minimum weighted vertex from the set of vertices
            // not yet visited for MST
            int u = minWeight(weight, visited);

            visited[u] = true;

            // Update weight value and parent index of the adjacent
            // vertices of the picked vertex. Consider only those
            // vertices which are not yet included in MST
            for (int v = 0; v < SIZE; v++) {
                // graph[u][v] is non zero only for adjacent vertices of m
                // visited[v] is false for vertices not yet included in MST
                // Update the key only if graph[u][v] is smaller than key[v]
                if (graph.get(u).get(v) != 0 && visited[v] == false &&  graph.get(u).get(v) < weight[v]) {
                    parent[v] = u;
                    weight[v] = weight[u] + graph.get(u).get(v);
                }
            }
        }


        for (int i = 0; i < SIZE; i++) {
            routes[i] = new LinkedList();
            this.constructRoute((LinkedList)routes[i], i, parent);
            System.out.println(routes[i]);
        }

        System.out.println("Doing Trim Routes");
        for (int i = 0; i< SIZE; i++){
            this.trimRoutes(routes, i, parent);
        }

        System.out.println("After Trim Routes");

        for (int i = 0; i< SIZE; i++){
            if(!routes[i].isEmpty()) {
                outputRouts.add(routes[i]);
                System.out.println(routes[i]);
            }
        }

        return outputRouts;

        // print the constructed MST
        //printMST(parent, graph, weight);
    }

    List <Integer> constructRoute (List <Integer> route, int index, int[] parent){
        if(parent[index] != -1){
            List parentRoute = this.constructRoute(route, parent[index], parent);
            parentRoute.add(index);
            return parentRoute;
        } else {
            route.add(index);
            return route;
        }
    }

    void trimRoutes (List routes[], int index, int[] parent){
        if(parent[index] != -1){
            this.trimRoutes(routes, parent[index], parent);
            routes[parent[index]].clear();
        } else {
            routes[index].clear();
        }
    }

//    public static void main(String[] args)
//    {
//        RouteOptimizer t = new RouteOptimizer();
//
//        int graph[][] = new int[][] { { 0, 4, 0, 0, 0, 0, 0, 8, 0 },
//                { 4, 0, 8, 0, 0, 0, 0, 11, 0 },
//                { 0, 8, 0, 7, 0, 4, 0, 0, 2 },
//                { 0, 0, 7, 0, 9, 14, 0, 0, 0 },
//                { 0, 0, 0, 9, 0, 10, 0, 0, 0 },
//                { 0, 0, 4, 14, 10, 0, 2, 0, 0 },
//                { 0, 0, 0, 0, 0, 2, 0, 1, 6 },
//                { 8, 11, 0, 0, 0, 0, 1, 0, 7 },
//                { 0, 0, 2, 0, 0, 0, 6, 7, 0 } };
//
//        List<List<Double>> graphList = new ArrayList<>();
//        int sizeOfList = graph[0].length;
//        for(int row=0; row< sizeOfList ; row ++){
//            List<Double> rowList = new ArrayList<>();
//            for (int column=0;column< sizeOfList; column ++){
//                double value = graph[row][column];
//                rowList.add(value);
//            }
//            graphList.add(rowList);
//        }
//        t.findOptimizedRoute(graphList);
//    }
}