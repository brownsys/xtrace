package edu.brown.cs.systems.xtrace.examples.multiproc;

import edu.brown.cs.systems.pubsub.PubSub;
import edu.brown.cs.systems.pubsub.PubSubProtos.StringMessage;
import edu.brown.cs.systems.pubsub.Subscriber.Callback;
import edu.brown.cs.systems.xtrace.XTrace;

public class MultiprocListener extends Callback<StringMessage> {

  private final XTrace.Logger x;

  private final String name, topicToPublishTo;

  public MultiprocListener(String name, String topicToListenTo, String topicToPublishTo) {
    x = XTrace.getLogger(name);
    this.name = name;
    this.topicToPublishTo = topicToPublishTo;
    PubSub.subscribe(topicToListenTo, this);
  }

  @Override
  protected void OnMessage(StringMessage message) {
    // extract the xtrace context
    XTrace.set(message.getMessage());

    x.log("Received the message in " + name + "!");

    try {
      Thread.sleep(100);
    } catch (Exception e) {

    }

    x.log("Sending a message from " + name + " on topic " + topicToPublishTo + "!");

    PubSub.publish(topicToPublishTo, StringMessage.newBuilder().setMessage(XTrace.base64()).build());
  }

  public static void main(String[] args) {
    // Just create the listener, subscribing
    new MultiprocListener(args[0], args[1], args[2]);
    System.out.println("Listener " + args[0] + " created, listening to " + args[1] + " and publishing to " + args[2]);
  }

}
