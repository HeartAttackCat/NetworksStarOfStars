# NetworksStarOfStars
CSE353 Networks project, creating a star of stars in Java.

## Building and running the project.

The project uses gradle java build tool for building and running the project.

To build the project use
`./gradlew build`

To run the project
`./gradlew run --args="--hub {number of hubs} --node {number of nodes}"`

IMPORTANT: The node files and `firewall.txt` need to be located under the directory `files/`

## Purpose of Files
### Model package
- FrameLibrary.java - Designed to centeralize functions for reading and generating network frames. These functions are shared between the main hub, lower hubs, and nodes.
### Node package
- NetworkFrame.java - Designed to model the object design for network frame specification.
- Node.java - The main class for node that initializes the node thread and handles the nodes behaviour
- NodeConfig.java - The class for handling the node configuration files and generates the frames for the node.
- NodeWriter.java - The class for handling the output files/messages of the node output file.
- NodeLib.java - The class for allowing access to functions between SendNetworkFrame and Node. Removes code duplciation and source of errors. 
- SendNetworkFrame.java - The class used for timing out sending messages to nodes. The class enables the use of Callable and Executor for timeout functionality.
### Switchers Package
- FirewallConfig.java - The class to handle; the reading of firewall frames and the firewall file, the generation of firewall frames, and the checking of frames against the firewall policies.
- SwitchHub.java - The main hub class that handles packets from the hubs, network level firewall policies, and the global termination of the nodes.
- SwitchNode.java - The hub class resposnible for switching packets in the network, applying local network firewall policies, and sending frames cross network by passing them to the main hub.
### io.hunter (Root) package
- Application.java - Resposnible for handling command line arguments and starting all the switch and node threads.
## Checklist
See checklist.pdf for the checklist.
## Frame Design
[`Source byte`][`Network Source byte`][`Destination byte`][`Network Destination byte`][`control byte`][`CRC byte`][`size 1-255`][`meat_0 byte`]...[`meat_255 byte`]
