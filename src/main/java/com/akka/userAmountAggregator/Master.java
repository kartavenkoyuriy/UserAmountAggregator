package com.akka.userAmountAggregator;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.RoundRobinRouter;
import akka.util.Duration;
import com.google.common.collect.Lists;
import com.google.common.math.IntMath;

import java.io.File;
import java.io.FileWriter;
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
    private final int numberOfWorkers;
    private final int numberOfMessages;
    private final int numberOfElements;
    private int numberOfResults;

    private final File INPUT_FILE = new File("src/main/resources/test.txt");
    private final File OUTPUT_FILE = new File("src/main/resources/output.txt");
    private final ActorRef listener;

    private final ActorRef workerRouter;

    private Map<Long, BigDecimal> userIdToAmount = new HashMap<>();

    public Master(int numberOfWorkers, int numberOfMessages, int numberOfElements, ActorRef listener) {
        this.numberOfWorkers = numberOfWorkers;
        this.numberOfMessages = numberOfMessages;
        this.numberOfElements = numberOfElements;
        this.listener = listener;

        workerRouter = this.getContext().actorOf(new Props(Worker.class).withRouter(new RoundRobinRouter(numberOfWorkers)),
                "workerRouter");
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof App.Calculate) {
            List<String> file = getFile();

            int partitionSize = IntMath.divide(file.size(), numberOfMessages, RoundingMode.UP);
            List<List<String>> partitions = Lists.partition(file, partitionSize);

            for (List<String> partition : partitions) {
                workerRouter.tell(new App.Work(partition), getSelf());
            }

        } else if (message instanceof App.Result) {
            App.Result result = (App.Result) message;
            for (Map.Entry<Long, BigDecimal> longBigDecimalEntry : result.getAmountResult().entrySet()) {
                if (userIdToAmount.containsKey(longBigDecimalEntry.getKey())) {
                    userIdToAmount.put(longBigDecimalEntry.getKey(), userIdToAmount.get(longBigDecimalEntry.getKey()).add(longBigDecimalEntry.getValue()));
                } else {
                    userIdToAmount.put(longBigDecimalEntry.getKey(), longBigDecimalEntry.getValue());
                }
            }
            numberOfResults += 1;
            if (numberOfResults == numberOfMessages) {
                resultMapToFile(userIdToAmount);
                Duration duration = Duration.create(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
                listener.tell(new App.AmountAggregation(userIdToAmount, duration), getSelf());
                getContext().stop(getSelf());
            }
        } else {
            unhandled(message);
        }
    }

    private void resultMapToFile(Map<Long, BigDecimal> resultMap){
        try (FileWriter writer = new FileWriter(OUTPUT_FILE)){
            for (Map.Entry<Long, BigDecimal> longBigDecimalEntry : resultMap.entrySet()) {
                writer.write(String.format("\"%d;%s\";%n", longBigDecimalEntry.getKey(), longBigDecimalEntry.getValue().toString()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private List<String> getFile() {
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get(INPUT_FILE.getAbsolutePath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }
}
