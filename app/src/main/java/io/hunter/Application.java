package io.hunter;

import io.hunter.node.Node;
import io.hunter.switchers.SwitchHub;

public class Application {

    public static void main(String[] args) {
        SwitchHub hub = new SwitchHub(3, (byte) 1);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Node node = new Node((byte) 1, (byte) 1);
        Node node2 = new Node((byte) 1, (byte) 2);
        Node node3 = new Node((byte) 1, (byte) 3);
    }
}