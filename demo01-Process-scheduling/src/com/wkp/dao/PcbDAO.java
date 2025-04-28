package com.wkp.dao;

import com.wkp.pojo.Pcb;

import java.util.List;

public interface PcbDAO{
    // 1. 查询全部的PCB
    public List<Pcb> getAllPcb();
}
