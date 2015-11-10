package com.akka.userAmountAggregator;

import akka.actor.*;
import akka.util.Duration;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class App {

    public static void main(String[] args) {
        App app = new App();
        app.calculate(2, 3, 3);
    }

    public void calculate(final int numberOfWorkers, final int numberOfElements, final int nrOfMessages) {
        ActorSystem system = ActorSystem.create("PiSystem");

        final ActorRef listener = system.actorOf(new Props(Listener.class), "listener");

        ActorRef master = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new Master(numberOfWorkers, nrOfMessages, numberOfElements, listener);
            }
        }), "master");

        master.tell(new Calculate());
    }

    static class Calculate {

    }

    static class Work {
        private List<String> partition;

        public Work(List<String> partition) {
            this.partition = partition;
        }

        public List<String> getPartition() {
            return partition;
        }
    }

    static class Result {
        private Map<Long, BigDecimal> amountResult;

        public Result(Map<Long, BigDecimal> amountResult) {
            this.amountResult = amountResult;
        }

        public Map<Long, BigDecimal> getAmountResult() {
            return amountResult;
        }
    }

    static class AmountAggregation {
        private final Map<Long, BigDecimal> userIdToAmount;
        private final Duration duration;

        public AmountAggregation(Map<Long, BigDecimal> userIdToAmount, Duration duration) {
            this.userIdToAmount = userIdToAmount;
            this.duration = duration;
        }

        public Map<Long, BigDecimal> getUserIdToAmount() {
            return userIdToAmount;
        }

        public Duration getDuration() {
            return duration;
        }
    }
}
