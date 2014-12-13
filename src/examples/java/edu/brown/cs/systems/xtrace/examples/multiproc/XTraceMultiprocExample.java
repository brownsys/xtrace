package edu.brown.cs.systems.xtrace.examples.multiproc;

import edu.brown.cs.systems.pubsub.PubSub;
import edu.brown.cs.systems.pubsub.PubSubProtos.StringMessage;
import edu.brown.cs.systems.pubsub.Subscriber.Callback;
import edu.brown.cs.systems.xtrace.XTrace;

public class XTraceMultiprocExample {
  
  private static final XTrace.Logger x = XTrace.getLogger("XTraceMultiprocExample");
  
  public static void main(String[] args) throws InterruptedException {
    
    System.out.println("Starting a new task every second");
    
    while (!Thread.currentThread().isInterrupted()) {      
      // Start a new task
      XTrace.startTask(true);
      System.out.println("Started a new XTrace task! " + XTrace.getTaskID());
      
      x.log("Starting XTraceMultiprocExample!");
      
      Thread.sleep(100);
      
      x.log("Sending message to topic " + args[0]);
      
      PubSub.publish(args[0], StringMessage.newBuilder().setMessage(XTrace.base64()).build());
      
      Thread.sleep(1000);
      
      // Stop the current task
      XTrace.stop();
    }
    
  }

}
