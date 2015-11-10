package com.akka.userAmountAggregator;

import akka.actor.UntypedActor;

import java.math.BigDecimal;
import java.util.Map;

public class Listener extends UntypedActor {
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof AmountAggregation) {
            AmountAggregation amountAggregation = (AmountAggregation) message;
            for (Map.Entry<Long, BigDecimal> longBigDecimalEntry : amountAggregation.getUserIdToAmount().entrySet()) {
                System.out.println("id:" + longBigDecimalEntry.getKey() + ", amount:" + longBigDecimalEntry.getValue());
            }
            System.out.println(String.format("Calculation time: \t%s", amountAggregation.getDuration()));
            getContext().system().shutdown();
        } else {
            unhandled(message);
        }
    }
}
