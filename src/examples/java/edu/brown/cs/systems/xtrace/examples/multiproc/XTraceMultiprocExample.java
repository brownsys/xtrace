package edu.brown.cs.systems.xtrace.examples.multiproc;

import edu.brown.cs.systems.pubsub.PubSub;
import edu.brown.cs.systems.pubsub.PubSubProtos.StringMessage;
import edu.brown.cs.systems.xtrace.XTrace;

public class XTraceMultiprocExample {
  
  private static final XTrace.Logger x = XTrace.getLogger("XTraceMultiprocExample");
  
  public static void main(String[] args) throws InterruptedException {
    
    System.out.println("XTrace Multiproc example.  This process starts tasks and they continue in other processes");
    
    while (!Thread.currentThread().isInterrupted()) {
      XTrace.startTask(true);
      System.out.println("Started a new XTrace task! " + XTrace.getTaskID());
      
      // Send 10 times then start a new task
      for (int i = 0; i < 10; i++) {
        x.log("Here I am at the start of the loop!");
        
        Thread.sleep(100);
        
        x.log("Sending message to topic " + args[0]);
        
        PubSub.publish(args[0], StringMessage.newBuilder().setMessage(XTrace.base64()).build());
        
        System.out.println("Sent a message in task " + XTrace.getTaskID());
        x.log("Message sent, sleeping");
        
        Thread.sleep(1000);
      }
    }
    
  }

}
