package edu.brown.cs.systems.xtrace.examples.multihelloworld;

import edu.brown.cs.systems.xtrace.XTrace;

public class XTraceMultiHelloWorld {
  
  private static final XTrace.Logger x = XTrace.getLogger(XTraceMultiHelloWorld.class);
  
  private static class HelloWorldThread extends Thread {
    
    private final String name;
    
    public HelloWorldThread(String name) {
      this.name = name;
      start();
    }
    
    public void run() {
      
      XTrace.startTask(true);
      
      x.log("Hello from " + name);
      
      x.log("Goodbye from " + name);
      
      
    }
  }
  
  public static void main(String[] args) throws InterruptedException {

    Thread a = new HelloWorldThread("Rodrigo Fonseca");
    Thread b = new HelloWorldThread("Jonathan Mace");
    Thread c = new HelloWorldThread("Peter Bodik");
    
    a.join();
    b.join();
    c.join();
    
    XTrace.shutdown();
  }

}
