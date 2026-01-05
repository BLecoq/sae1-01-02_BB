// Lecoq Brian Bittiger Bryan groupe de SAE n°4

public class MemoireProfiler {
    private long memAvant;
    private long memApres;

    public void debut() {
        Runtime r = Runtime.getRuntime();
        r.gc();
        memAvant = r.totalMemory() - r.freeMemory();
    }

    public void fin() {
        Runtime r = Runtime.getRuntime();
        memApres = r.totalMemory() - r.freeMemory();
    }

    public long getDeltaBytes() {
        return memApres - memAvant;
    }

    public double getDeltaKB() {
        return getDeltaBytes() / 1024.0;
    }

    public double getDeltaMB() {
        return getDeltaBytes() / (1024.0 * 1024.0);
    }
}
