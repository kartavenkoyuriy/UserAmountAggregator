package com.akka.userAmountAggregator;

import akka.actor.UntypedActor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Worker extends UntypedActor {

    private final String ID_AMOUNT_PATTERN = "[.]*([0-9]*);(-?[0-9.]*)[.]*";

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof App.Work) {
            App.Work work = (App.Work) message;
            Map<Long, BigDecimal> result = aggregateAmountFor(work.getPartition());
            getSender().tell(new App.Result(result), getSelf());
        } else {
            unhandled(message);
        }
    }

    private Map<Long, BigDecimal> aggregateAmountFor(List<String> partition) {
        Map<Long, BigDecimal> resultMap = new HashMap<Long, BigDecimal>();
        for (String s : partition) {
            Map<Long, BigDecimal> tempMap = (parseStringToData(s));
            for (Map.Entry<Long, BigDecimal> longBigDecimalEntry : tempMap.entrySet()) {
                if (resultMap.containsKey(longBigDecimalEntry.getKey())) {
                    resultMap.put(longBigDecimalEntry.getKey(), resultMap.get(longBigDecimalEntry.getKey()).add(longBigDecimalEntry.getValue()));
                } else {
                    resultMap.putAll(tempMap);
                }
            }
        }
        return resultMap;
    }

    private Map<Long, BigDecimal> parseStringToData(String idAmountString) {
        Pattern p = Pattern.compile(ID_AMOUNT_PATTERN);
        Matcher m = p.matcher(idAmountString);
        Map<Long, BigDecimal> resultMap = new HashMap<Long, BigDecimal>();
        if (m.find()) {
            resultMap.put(new Long(m.group(1)), new BigDecimal(m.group(2)));
        }
        return resultMap;
    }
}
