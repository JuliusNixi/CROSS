package CROSS.Types;


public class Price {
    Double bid, ask;
    public Price(Double bid, Double ask) {
        if (bid < 0) {
            throw new IllegalArgumentException("Bid cannot be negative.");
        }
        if (ask < 0) {
            throw new IllegalArgumentException("Ask cannot be negative.");
        }
        // If only one of them is present, the other one is 0.
        if (bid == 0 && ask == 0) {
            throw new IllegalArgumentException("At least one of the prices must be present.");
        }
        this.bid = bid;
        this.ask = ask;
    }
    public Double getBid() {
        return bid;
    }
    public Double getAsk() {
        return ask;
    }
    @Override
    public String toString() {
        return "Bid: " + bid + ", Ask: " + ask;
    }
}
