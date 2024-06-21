package com.ccuix;


import com.ccuix.Entity.YouDaoData;
import com.ccuix.ShanBay.ShanBayCollection;
import com.ccuix.YouDao.CollectionWordList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class Schedule {

    private final CollectionWordList collectionWordList;
    private final ShanBayCollection shanBayCollection;
    private static Integer count = 0;

    @Scheduled(fixedRate = 3600000)
    public void TransCollection() {
        log.warn("这是第{}次同步", count++);
        YouDaoData collectionWords = collectionWordList.getCollectionWordList();
        shanBayCollection.addWordToShanBay(collectionWords);
    }
}
