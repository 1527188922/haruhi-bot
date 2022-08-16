package com.haruhi.bot.thread;

import com.haruhi.bot.utils.CommonUtil;
import com.haruhi.bot.utils.WordCloudUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 用于分词的线程
 * 生成词料（之后还要设置权重）
 */
@Slf4j
public class WordSlicesTask implements Callable<List<String>> {
    public static final int poolSize = 20;
    public static final ExecutorService pool = new ThreadPoolExecutor(poolSize, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),new CustomizableThreadFactory("pool-word-slices-"));
    private List<String> data;

    public WordSlicesTask(List<String> data){
        this.data = data;
    }

    @Override
    public List<String> call() throws Exception {
        List<String> res = new ArrayList<>();
        for (String item : data) {
            List<String> strings = WordCloudUtil.wordSlices(item);
            if(strings == null || strings.size() == 0){
                continue;
            }
            res.addAll(strings);
        }
        return res;
    }

    public static List<String> execute(List<String> corpus){
        List<String> strings = new ArrayList<>();
        // 根据线程池大小，计算每个线程需要跑几个词语 确保不会有线程空闲
        int limit = CommonUtil.averageAssignListSize(corpus.size(),poolSize);
        ArrayList<FutureTask<List<String>>> futureTasks = new ArrayList<>();
        List<List<String>> lists = CommonUtil.averageAssignList(corpus, limit);
        for (List<String> list : lists) {
            FutureTask<List<String>> listFutureTask = new FutureTask<>(new WordSlicesTask(list));
            futureTasks.add(listFutureTask);
            pool.submit(listFutureTask);
        }
        try {
            for (FutureTask<List<String>> futureTask : futureTasks) {
                strings.addAll(futureTask.get());
            }
            return strings;
        }catch (Exception e){
            log.error("分词异常",e);
            return null;
        }

    }
}
