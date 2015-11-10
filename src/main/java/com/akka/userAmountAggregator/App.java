package com.akka.userAmountAggregator;

import akka.actor.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class App {

    public static void main(String[] args) {
        App app = new App();
        app.calculate(2, 3, 3);
    }

    public void calculate(final int nrOfWorkers, final int nrOfElements, final int nrOfMessages) {
        ActorSystem system = ActorSystem.create("PiSystem");

        final ActorRef listener = system.actorOf(new Props(Listener.class), "listener");

        ActorRef master = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new Master(nrOfWorkers, nrOfMessages, nrOfElements, listener);
            }
        }), "master");

        // start the calculation
        master.tell(new Calculate());

    }


    static class Calculate{

    }

    static class Work{
        private List<String> partition;

        public Work(List<String> partition) {
            this.partition = partition;
        }

        public List<String> getPartition() {
            return partition;
        }
    }

    static class Result{
        private Map<Long, BigDecimal> amountResult;

        public Result(Map<Long, BigDecimal> amountResult) {
            this.amountResult = amountResult;
        }

        public Map<Long, BigDecimal> getAmountResult() {
            return amountResult;
        }
    }


}
