package hust.cs.javacourse.search.index.impl;

import hust.cs.javacourse.search.index.*;

import java.io.*;
import java.util.*;

/**
 * AbstractIndex的具体实现类
 */
public class Index extends AbstractIndex {
    /**
     * 返回索引的字符串表示
     *
     * @return 索引的字符串表示
     */
    @Override
    public String toString() {
        return "docIdToDocPathMapping = " + docIdToDocPathMapping.toString() + "termToPostingListMapping = " + termToPostingListMapping.toString();
    }

    /**
     * 添加文档到索引，更新索引内部的HashMap
     *
     * @param document ：文档的AbstractDocument子类型表示
     */
    @Override
    public void addDocument(AbstractDocument document) {
        if(document != null && !docIdToDocPathMapping.containsKey(document.getDocId())){
            docIdToDocPathMapping.put(document.getDocId(), document.getDocPath());
            List<AbstractTermTuple> tuples = document.getTuples();
            Map<AbstractTerm, List<Integer>> tmpMap = new TreeMap<>();//List中保存的是每个term对应出现的位置的列表，即positions
            for(AbstractTermTuple tuple: tuples){
                if(!tmpMap.containsKey(tuple.term))
                    tmpMap.put(tuple.term, new ArrayList<>());
                tmpMap.get(tuple.term).add(tuple.curPos);
            }//先把此文档中所有的出现位置加入表中
            for(Map.Entry<AbstractTerm, List<Integer>> entry : tmpMap.entrySet()){
                Posting posting = new Posting(document.getDocId(), entry.getValue().size(), entry.getValue());
                if(termToPostingListMapping.containsKey(entry.getKey())) {//term 已经存在于映射当中，此时只需要在对应的键值对中做修改即可，即修改PostingList
                    termToPostingListMapping.get(entry.getKey()).add(posting);
                }
                else{
                    AbstractPostingList npl = new PostingList();
                    npl.add(posting);
                    termToPostingListMapping.put(entry.getKey(), npl);
                }
            }

        }

    }

    /**
     * <pre>
     * 从索引文件里加载已经构建好的索引.内部调用FileSerializable接口方法readObject即可
     * @param file ：索引文件
     * </pre>
     */
    @Override
    public void load(File file) {
        try{
            ObjectInputStream input = new ObjectInputStream(new FileInputStream(file));
            readObject(input);
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * <pre>
     * 将在内存里构建好的索引写入到文件. 内部调用FileSerializable接口方法writeObject即可
     * @param file ：写入的目标索引文件
     * </pre>
     */
    @Override
    public void save(File file) {
        try{
            ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file));
            writeObject(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回指定单词的PostingList
     *
     * @param term : 指定的单词
     * @return ：指定单词的PostingList;如果索引字典没有该单词，则返回null
     */
    @Override
    public AbstractPostingList search(AbstractTerm term) {
        return this.termToPostingListMapping.getOrDefault(term, null);
    }

    /**
     * 返回索引的字典.字典为索引里所有单词的并集
     *
     * @return ：索引中Term列表
     */
    @Override
    public Set<AbstractTerm> getDictionary() {
        return this.termToPostingListMapping.keySet();
    }

    /**
     * <pre>
     * 对索引进行优化，包括：
     *      对索引里每个单词的PostingList按docId从小到大排序
     *      同时对每个Posting里的positions从小到大排序
     * 在内存中把索引构建完后执行该方法
     * </pre>
     */
    @Override
    public void optimize() {
        for(Map.Entry<AbstractTerm, AbstractPostingList> e : termToPostingListMapping.entrySet()){
            e.getValue().sort();//调用PostingList中的sort()方法
            for(int i=0; i<e.getValue().size(); i++)
                Collections.sort(e.getValue().get(i).getPositions());
        }

    }

    /**
     * 根据docId获得对应文档的完全路径名
     *
     * @param docId ：文档id
     * @return : 对应文档的完全路径名
     */
    @Override
    public String getDocName(int docId) {
        return docIdToDocPathMapping.get(docId);
    }

    /**
     * 写到二进制文件
     *
     * @param out :输出流对象
     */
    @Override
    public void writeObject(ObjectOutputStream out) {
        try {
            out.writeObject(docIdToDocPathMapping);
            out.writeObject(termToPostingListMapping);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从二进制文件读
     *
     * @param in ：输入流对象
     */
    @Override
    @SuppressWarnings("unchecked")
    public void readObject(ObjectInputStream in) {
        try{
            docIdToDocPathMapping = (Map<Integer, String>) in.readObject();
            termToPostingListMapping = (Map<AbstractTerm, AbstractPostingList>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
