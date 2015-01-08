package cz.jakubmaly.pxslt;

import net.sf.saxon.s9api.XsltTransformer;

public interface TransformerInitializer {
    void initialize(XsltTransformer transformer);
}
