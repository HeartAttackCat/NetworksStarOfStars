package io.hunter;

import io.hunter.node.Node;

public class Application {

    public static void main(String[] args) {
        Node node = new Node((byte) 1, (byte)1);
        node.start();
        Node node2 = new Node((byte) 1, (byte)2);
        node2.start();
        Node node3 = new Node((byte) 1, (byte)3);
        node3.start();
    }
}