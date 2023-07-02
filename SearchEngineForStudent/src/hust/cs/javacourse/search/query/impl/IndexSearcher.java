package hust.cs.javacourse.search.query.impl;

import hust.cs.javacourse.search.index.AbstractPosting;
import hust.cs.javacourse.search.index.AbstractPostingList;
import hust.cs.javacourse.search.index.AbstractTerm;
import hust.cs.javacourse.search.index.impl.Posting;
import hust.cs.javacourse.search.query.AbstractHit;
import hust.cs.javacourse.search.query.AbstractIndexSearcher;
import hust.cs.javacourse.search.query.Sort;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class IndexSearcher extends AbstractIndexSearcher {
    /**
     * 从指定索引文件打开索引，加载到index对象里. 一定要先打开索引，才能执行search方法
     *
     * @param indexFile ：指定索引文件
     */
    @Override
    public void open(String indexFile) {
        index.load(new File(indexFile));
        index.optimize();
    }

    /**
     * 根据单个检索词进行搜索
     *
     * @param queryTerm ：检索词
     * @param sorter    ：排序器
     * @return ：命中结果数组
     */
    @Override
    public AbstractHit[] search(AbstractTerm queryTerm, Sort sorter) {
        List<AbstractHit> abstractHits = new ArrayList<>();
        AbstractPostingList postingList = index.search(queryTerm);
        for(int i=0; i<postingList.size(); i++){

            AbstractPosting posting = postingList.get(i);
            Map<AbstractTerm, AbstractPosting> tmpMap = new TreeMap<>();
            tmpMap.put(queryTerm, postingList.get(i));
            AbstractHit hit = new Hit(posting.getDocId(), index.getDocName(posting.getDocId()), tmpMap);
            hit.setScore(sorter.score(hit));
            abstractHits.add(hit);
        }
        sorter.sort(abstractHits);
        return abstractHits.toArray(new AbstractHit[0]);
    }

    /**
     * 根据二个检索词进行搜索
     *
     * @param queryTerm1 ：第1个检索词
     * @param queryTerm2 ：第2个检索词
     * @param sorter     ：    排序器
     * @param combine    ：   多个检索词的逻辑组合方式
     * @return ：命中结果数组
     */
    @Override
    public AbstractHit[] search(AbstractTerm queryTerm1, AbstractTerm queryTerm2, Sort sorter, LogicalCombination combine) {
        List<AbstractHit> abstractHits = new ArrayList<>();
        AbstractPostingList postingList1 = index.search(queryTerm1);//保存对于queryTerm1的搜索结果
        AbstractPostingList postingList2 = index.search(queryTerm2);//保存对于queryTerm2的搜索结果
        Map<Integer, Integer> scoreMap = new TreeMap<>();//key为docId，value为该文档中出现了queryTerm1、queryTerm2中的几个
        Map<Integer, AbstractPosting> postingMap1 = new TreeMap<>();//用于存放docId与queryTerm1对应的posting的键值对
        Map<Integer, AbstractPosting> postingMap2 = new TreeMap<>();//用于存放docId与queryTerm2对应的posting的键值对
        for (int i=0; i<postingList1.size(); i++){
            AbstractPosting posting1 = postingList1.get(i);
            scoreMap.put(posting1.getDocId(), 1);//该docId记为“出现了一个”
            postingMap1.put(posting1.getDocId(), posting1);
        }

        for (int i=0; i<postingList2.size(); i++){
            AbstractPosting posting2 = postingList2.get(i);
            postingMap2.put(posting2.getDocId(), posting2);
            if (scoreMap.containsKey(posting2.getDocId()))
                scoreMap.put(posting2.getDocId(), 2);//已经存在docId，说明在遍历postingList1时就已经出现过，此处再次出现，说明该文档两个单词都包含
            else
                scoreMap.put(posting2.getDocId(), 1);
        }
        if(combine.equals(LogicalCombination.AND)){
            for(Map.Entry<Integer, Integer> entry : scoreMap.entrySet()){//遍历scoreMap中的每一个键值对，判断其value是否为2
                if(entry.getValue() == 2){
                    Map<AbstractTerm, AbstractPosting> tmpMap = new TreeMap<>();
                    tmpMap.put(queryTerm1, postingMap1.get(entry.getKey()));
                    tmpMap.put(queryTerm2, postingMap2.get(entry.getKey()));
                    AbstractHit hit = new Hit(entry.getKey(), index.getDocName(entry.getKey()), tmpMap);
                    hit.setScore(sorter.score(hit));
                    abstractHits.add(hit);
                }
            }

        }
        if(combine.equals(LogicalCombination.OR)){
            for(Map.Entry<Integer, Integer> entry : scoreMap.entrySet()){//遍历scoreMap中的每一个键值对
                if(entry.getValue() == 1){//只有一个单词出现，则判断出现的是哪个单词
                    Map<AbstractTerm, AbstractPosting> tmpMap = new TreeMap<>();
                    if(postingMap1.containsKey(entry.getKey())){
                        tmpMap.put(queryTerm1, postingMap1.get(entry.getKey()));
                        AbstractHit hit = new Hit(entry.getKey(), index.getDocName(entry.getKey()), tmpMap);
                        hit.setScore(sorter.score(hit));
                        abstractHits.add(hit);
                    }

                    else if(postingMap2.containsKey(entry.getKey())){
                        tmpMap.put(queryTerm2, postingMap2.get(entry.getKey()));
                        AbstractHit hit = new Hit(entry.getKey(), index.getDocName(entry.getKey()), tmpMap);
                        hit.setScore(sorter.score(hit));
                        abstractHits.add(hit);
                    }
                }
                else if(entry.getValue() == 2){//两个单词都出现了，则与AND相同
                    Map<AbstractTerm, AbstractPosting> tmpMap = new TreeMap<>();
                    tmpMap.put(queryTerm1, postingMap1.get(entry.getKey()));
                    tmpMap.put(queryTerm2, postingMap2.get(entry.getKey()));
                    AbstractHit hit = new Hit(entry.getKey(), index.getDocName(entry.getKey()), tmpMap);
                    hit.setScore(sorter.score(hit));
                    abstractHits.add(hit);
                }
            }
        }
        sorter.sort(abstractHits);//将Hit按照得分排序
        return abstractHits.toArray(new AbstractHit[0]);
    }
}
