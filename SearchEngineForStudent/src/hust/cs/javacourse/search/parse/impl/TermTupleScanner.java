package hust.cs.javacourse.search.parse.impl;

import hust.cs.javacourse.search.index.AbstractTerm;
import hust.cs.javacourse.search.index.AbstractTermTuple;
import hust.cs.javacourse.search.index.impl.Term;
import hust.cs.javacourse.search.index.impl.TermTuple;
import hust.cs.javacourse.search.parse.AbstractTermTupleScanner;
import hust.cs.javacourse.search.util.Config;
import hust.cs.javacourse.search.util.StringSplitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TermTupleScanner extends AbstractTermTupleScanner {
    private int curPos;
    private List<String> terms;
    public TermTupleScanner(){
        super();
        curPos = 0;
        terms = new ArrayList<>();
    }
    public  TermTupleScanner(BufferedReader input){
        super(input);
        curPos = 0;
        terms = new ArrayList<>();
    }
    /**
     * 获得下一个三元组
     *
     * @return : 下一个三元组；如果到了流的末尾，返回null
     */

    @Override
    public AbstractTermTuple next() {
        try{
            if(terms.isEmpty()){//第一次读该流
                String line;
                StringBuilder text = new StringBuilder();
                while((line = super.input.readLine()) != null){
                    text.append(line);
                    text.append(" ");//在每行后添加空格，防止单词首尾相连
                }
                String textTmp = text.toString().trim().toLowerCase(Locale.ROOT);
                StringSplitter splitter = new StringSplitter();
                splitter.setSplitRegex(Config.STRING_SPLITTER_REGEX);//根据划分字符划分
                terms = splitter.splitByRegex(textTmp);
            }
            //if(terms.size() == 0) return null;
            else if(curPos == terms.size())//读到term的末尾
                return null;
            return new TermTuple(new Term(terms.get(curPos)), curPos++);//curPos后移一位
        }
        catch(IOException e){
            e.printStackTrace();
        }


        return null;
    }

}
