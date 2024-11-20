package io.hunter;

import io.hunter.node.Node;

public class Application {

    public static void main(String[] args) {
        Node node = new Node((byte) 1, (byte)1);
        node.start();
    }
}