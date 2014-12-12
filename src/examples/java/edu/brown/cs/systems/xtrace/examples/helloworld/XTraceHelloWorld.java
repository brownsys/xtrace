package edu.brown.cs.systems.xtrace.examples.helloworld;

import edu.brown.cs.systems.pubsub.PubSub;
import edu.brown.cs.systems.pubsub.PubSubProtos.StringMessage;
import edu.brown.cs.systems.xtrace.XTrace;

/**
 * This example creates an XTrace task and prints hello world
 */
public class XTraceHelloWorld {
	
	public static final XTrace.Logger x = XTrace.getLogger(XTraceHelloWorld.class);
	
	public static void main(String[] args) throws InterruptedException {
		
		XTrace.startTask(true);
		
		Thread.sleep(1000);
		
		x.log("Hello World!");
		
		x.log("Goodbye World!");
    
    Thread.sleep(1000);
		
		XTrace.shutdown();
		
	}

}
