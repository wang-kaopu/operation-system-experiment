package com.wkp.dao;

import com.wkp.pojo.Jcb;

import java.util.List;

public interface JcbDAO {
    // 1. 查询全部的PCB
    List<Jcb> getAllJcb();
}
