package cz.jakubmaly.pxslt;

import java.io.File;
import java.io.IOException;

public abstract class ResultMerger {
    protected void init() {}
    protected abstract void append(File file) throws IOException;
    protected void complete() {}
}


