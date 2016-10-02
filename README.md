### AIS to DIS Gateway

A simple gateway that translates from AIS to Distributed Interactive Simulation (DIS).

Much work remains to be done. The broadcast address is hardwired in 
Network.java. The EntityType is set in the Ship constructor.
There's a thread to periodically send DIS heartbeat messages.
