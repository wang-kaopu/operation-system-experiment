package com.wkp.dao.impl;

import com.wkp.dao.BaseDAO;
import com.wkp.dao.PcbDAO;
import com.wkp.pojo.Pcb;

import java.util.List;

public class PcbDAOImpl extends BaseDAO implements PcbDAO {
    @Override
    public List<Pcb> getAllPcb() {
        String sql = "SELECT process_name processName, level, arrive_time arriveTime," +
                "pid, used_time usedTime, state, run_time runTime FROM pcb_table";
        List<Pcb> pcbList = null;
        try {
            pcbList = executeQuery(Pcb.class, sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pcbList;
    }
}
