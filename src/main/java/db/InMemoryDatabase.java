import java.util.concurrent.ConcurrentHashMap;

public interface Database {
  byte[] get(String key);
  void set(String key, byte[] value, Long ttlMs);
  void start();
  void stop();
}

public final class InMemoryDatabase implements Database {
  private static final class Entry { final byte[] v; final long expireAt; Entry(byte[] v, long expireAt){ this.v=v; this.expireAt=expireAt; } }
  private final ConcurrentHashMap<String, Entry> map = new ConcurrentHashMap<>();
  private ScheduledExecutorService sweeper;

  public void start() {
    sweeper = Executors.newSingleThreadScheduledExecutor();
    sweeper.scheduleAtFixedRate(this::purgeExpired, 1, 1, TimeUnit.SECONDS);
  }
  public void stop() { if (sweeper != null) sweeper.shutdownNow(); }

  public byte[] get(String key) {
    Entry e = map.get(key);
    if (e == null) return null;
    if (e.expireAt > 0 && System.currentTimeMillis() >= e.expireAt) { map.remove(key, e); return null; }
    return e.v;
  }
  public void set(String key, byte[] value, Long ttlMs) {
    long exp = (ttlMs == null) ? -1 : System.currentTimeMillis() + ttlMs;
    map.put(key, new Entry(value, exp));
  }
  private void purgeExpired() {
    long now = System.currentTimeMillis();
    map.entrySet().removeIf(e -> e.getValue().expireAt > 0 && now >= e.getValue().expireAt);
  }
}
