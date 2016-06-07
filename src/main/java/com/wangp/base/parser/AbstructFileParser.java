package com.wangp.base.parser;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by wangpeng627 on 16-3-10.
 */
public abstract class AbstructFileParser<T> {

    abstract List<T> parse(String fileName,Class<?> T,LinkedHashMap<String,Class> mapRelation)  throws Exception ;
}
