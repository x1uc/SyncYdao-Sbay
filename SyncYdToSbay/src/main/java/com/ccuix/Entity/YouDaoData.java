package com.ccuix.Entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author li
 * @date 2024/06/19
 * total is the sum of your YouDao collection
 * itemList is a list of words you recently collected. 
 * The specific number can be changed in the configuration file. It is prefix is youdao:collectionUrl
 *   
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class YouDaoData {
    private int total;
    private List<YouDaoWordItem> itemList;
}
