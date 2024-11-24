package io.hunter;

import io.hunter.node.Node;
import io.hunter.switchers.SwitchHub;
import io.hunter.switchers.SwitchNode;

public class Application {

    private static int[] networks = {25565, 2010, 2030, 10800};

    private static int hubs = 3;
    private static int nodes = 2;

    public static void main(String[] args) {
        new SwitchHub(hubs, 7777);

        for (int hub = 1; hub <= hubs; hub++) {
            System.out.println("[Main] Initializing hub " + hub);
            new SwitchNode(nodes, (byte) hub, networks[hub]);

            for (int node = 1; node < nodes + 1; node++) {
                System.out.println("[Main] Initializing node " + node + " for network " + hub);
                new Node((byte) hub, (byte) node, networks[hub]);
            }
        }
    }
}