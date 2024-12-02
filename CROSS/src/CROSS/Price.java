package CROSS;
public class Price {
    Double bid, ask;
    public Price(Double bid, Double ask) {
        if (bid <= 0) {
            throw new IllegalArgumentException("Bid cannot be negative or 0.");
        }
        if (ask <= 0) {
            throw new IllegalArgumentException("Ask cannot be negative or 0.");
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
