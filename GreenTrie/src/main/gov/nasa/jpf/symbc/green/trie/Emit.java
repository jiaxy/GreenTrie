package gov.nasa.jpf.symbc.green.trie;

import java.io.Serializable;


public class Emit implements Serializable{

	private static final long serialVersionUID = 1L;
    private final String keyword;

    public Emit(final int start, final int end, final String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return this.keyword;
    }

    @Override
    public String toString() {
        return super.toString() + "=" + this.keyword;
    }

}
