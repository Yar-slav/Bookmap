package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class Main {

    private static final Map<Integer, Integer> asks = new TreeMap<>();
    private static final Map<Integer, Integer> bids = new TreeMap<>(Collections.reverseOrder());
    private static final Map<Integer, Integer> spreads = new HashMap<>();

    public static void main(String[] args) {
        String fileName = "input.txt";
        String row;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName));
             BufferedWriter write = new BufferedWriter(new FileWriter("output.txt"))) {
            while (((row = reader.readLine()) != null)) {
                String[] split = row.split(",");
                char charAt = split[0].charAt(0);
                if (charAt == 'u') {
                    updateOrder(split);
                }
                if (charAt == 'o') {
                    Integer size = Integer.parseInt(split[2]);
                    if (split[1].equals("sell")) {
                        makeOrderSell(size);
                    }
                    if (split[1].equals("buy")) {
                        makeOrderBuy(size);
                    }
                }
                if (charAt == 'q') {
                    try {
                        getQueries(split, write);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void updateOrder(String[] split) {
        Integer price = Integer.parseInt(split[1]);
        Integer size = Integer.parseInt(split[2]);
        String type = split[3];
        switch (type) {
            case "bid":
                if (size == 0) {
                    bids.remove(price);
                } else {
                    bids.put(price, size);
                }
                break;
            case "ask":
                if (size == 0) {
                    asks.remove(price);
                } else {
                    asks.put(price, size);
                }
                break;
            case "spread":
                spreads.put(price, size);
                break;
        }
    }

    private static void getQueries(String[] split, BufferedWriter write) throws IOException {
        switch (split[1]) {
            case "best_bid": {
                Integer bestBid = getBestBid();
                if (bestBid != null) {
                    write.write(bestBid + "," + bids.get(bestBid) + "\n");
                }

            }
            break;
            case "best_ask": {
                Integer bestAsk = getBestAsk();
                if (bestAsk != null) {
                    write.write(bestAsk + "," + asks.get(bestAsk) + "\n");
                }
            }
            break;
            case "size": {
                Integer price = Integer.parseInt(split[2]);
                Integer size = bids.get(price);
                if (size == null) {
                    size = asks.get(price);
                }
                if (size == null) {
                    size = spreads.get(price);
                }
                write.write(Objects.requireNonNullElse(size, 0) + "\n");
            }
            break;
        }
    }

    private static void makeOrderSell(Integer size) {
        Integer bestBid = getBestBid();
        if (bestBid != null) {
            int remain = bids.get(bestBid) - size;
            if (remain <= 0) {
                bids.remove(bestBid);
                while (remain <= 0) {
                    bestBid = getBestBid();
                    if (bestBid == null) break;
                    remain += bids.get(bestBid);
                    removeOrPutSize(bestBid, remain, bids);
                }
            } else {
                bids.put(bestBid, remain);
            }
        }
    }

    private static void makeOrderBuy(Integer size) {
        Integer bestAsk = getBestAsk();
        if (bestAsk != null) {
            int remain = asks.get(bestAsk) - size;
            if (remain <= 0) {
                asks.remove(bestAsk);
                while (remain < 0) {
                    bestAsk = getBestAsk();
                    if (bestAsk == null) break;
                    remain += asks.get(bestAsk);
                    removeOrPutSize(bestAsk, remain, asks);
                }
            } else {
                asks.put(bestAsk, remain);
            }
        }
    }

    private static void removeOrPutSize(Integer bestAsk, int remain, Map<Integer, Integer> map) {
        if (remain <= 0) {
            map.remove(bestAsk);
        } else {
            map.put(bestAsk, remain);
        }
    }

    private static Integer getBestAsk() {
        if (!asks.isEmpty()) {
            return asks.keySet().iterator().next();
        }
        return null;
    }

    private static Integer getBestBid() {
        if (!bids.isEmpty()) {
            return bids.keySet().iterator().next();
        }
        return null;
    }
}
