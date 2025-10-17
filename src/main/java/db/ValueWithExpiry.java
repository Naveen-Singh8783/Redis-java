public class ValueWithExpiry {
    private final String value;
    private final long expiryTime;

    public ValueWithExpiry(String value, long expiryTime){
        this.value = value;
        this.expiryTime = expiryTime;
    }

    public String getValue(){
        return this.value;
    }

    public boolean isExpired(){
        return expiryTime != -1 && System.currentTimeMillis() > this.expiryTime;
    }
    
}
