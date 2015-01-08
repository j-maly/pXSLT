package cz.jakubmaly.pxslt;

public class SlicingHints {
    private int[] starts;
    private String[] prefixes;
    private String[] suffixes;

    public SlicingHints(int[] starts, String[] prefixes, String[] suffixes) {
        this.starts = starts;
        this.prefixes = prefixes;
        this.suffixes = suffixes;
    }

    public int[] getStarts() {
        return starts;
    }

    public String[] getPrefixes() {
        return prefixes;
    }

    public String[] getSuffixes() {
        return suffixes;
    }
}
