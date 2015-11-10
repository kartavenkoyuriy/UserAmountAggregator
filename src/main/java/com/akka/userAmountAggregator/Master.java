package com.akka.userAmountAggregator;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.RoundRobinRouter;
import akka.util.Duration;
import com.google.common.collect.Lists;
import com.google.common.math.IntMath;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Master extends UntypedActor {

    private final long start = System.currentTimeMillis();
    private final int nrOfMessages;
    private final int nrOfElements;
    private final String PATH = "D:\\java\\Projects\\UserAmountAggregator\\src\\main\\resources\\test.txt";


    private final ActorRef listener;
    private final ActorRef workerRouter;

    private Map<Long, BigDecimal> userIdToAmount = new HashMap<Long, BigDecimal>();

    private int nrOfResults;

    public Master(int nrOfWorkers, int nrOfMessages, int nrOfElements, ActorRef listener) {
        this.nrOfMessages = nrOfMessages;
        this.nrOfElements = nrOfElements;
        this.listener = listener;

        workerRouter = this.getContext().actorOf(new Props(Worker.class).withRouter(new RoundRobinRouter(nrOfWorkers)),
                "workerRouter");
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof App.Calculate) {
            //read the file
            List<String> file = getFile();

            int partitionSize = IntMath.divide(file.size(), nrOfMessages, RoundingMode.UP);
            List<List<String>> partitions = Lists.partition(file, partitionSize);

            for (List<String> partition : partitions) {
                workerRouter.tell(new App.Work(partition), getSelf());
            }

        } else if (message instanceof App.Result) {
            App.Result result = (App.Result) message;
            for (Map.Entry<Long, BigDecimal> longBigDecimalEntry : result.getAmountResult().entrySet()) {
                if(userIdToAmount.containsKey(longBigDecimalEntry.getKey())){
                    userIdToAmount.put(longBigDecimalEntry.getKey(), userIdToAmount.get(longBigDecimalEntry.getKey()).add(longBigDecimalEntry.getValue()));
                } else {
                    userIdToAmount.put(longBigDecimalEntry.getKey(), longBigDecimalEntry.getValue());
                }
            }
            nrOfResults += 1;
            if (nrOfResults == nrOfMessages) {
                Duration duration = Duration.create(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
                listener.tell(new AmountAggregation(userIdToAmount, duration), getSelf());
                getContext().stop(getSelf());
            }
        } else {
            unhandled(message);
        }
    }

    private List<String> getFile() {
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get(PATH), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }
}
