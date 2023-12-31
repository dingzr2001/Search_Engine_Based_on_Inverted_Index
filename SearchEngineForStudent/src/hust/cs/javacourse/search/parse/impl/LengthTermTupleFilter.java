package hust.cs.javacourse.search.parse.impl;

import hust.cs.javacourse.search.index.AbstractTermTuple;
import hust.cs.javacourse.search.index.impl.TermTuple;
import hust.cs.javacourse.search.parse.AbstractTermTupleFilter;
import hust.cs.javacourse.search.parse.AbstractTermTupleStream;
import hust.cs.javacourse.search.util.Config;

import java.util.regex.Pattern;

public class LengthTermTupleFilter extends AbstractTermTupleFilter {
    public LengthTermTupleFilter(AbstractTermTupleStream input) {
        super(input);
    }

    /**
     * 获得下一个三元组
     *
     * @return: 下一个三元组；如果到了流的末尾，返回null
     */
    @Override
    public AbstractTermTuple next() {
        TermTuple t = (TermTuple) this.input.next();
        if(t == null) return null;
        while(t.term.getContent().length() < Config.TERM_FILTER_MINLENGTH || t.term.getContent().length() > Config.TERM_FILTER_MAXLENGTH){
            t = (TermTuple) this.input.next();
            if(t == null) return null;
        }
        return t;
    }
}
