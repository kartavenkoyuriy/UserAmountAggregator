package com.akka.userAmountAggregator;

import akka.actor.UntypedActor;

public class Listener extends UntypedActor {
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof App.AmountAggregation) {
            App.AmountAggregation amountAggregation = (App.AmountAggregation) message;
            System.out.println(String.format("Calculation time: \t%s", amountAggregation.getDuration()));
            getContext().system().shutdown();
        } else {
            unhandled(message);
        }
    }
}
