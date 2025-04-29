package com.wkp.dao.impl;

import com.wkp.dao.BaseDAO;
import com.wkp.dao.JcbDAO;
import com.wkp.pojo.Jcb;

import java.util.List;

public class JcbDAOImpl extends BaseDAO implements JcbDAO {

    @Override
    public List<Jcb> getAllJcb() {
        String sql = "SELECT job_name jobName, level, arrive_time arriveTime," +
                "jid, used_time usedTime, state, run_time runTime, wait_time waitTime FROM jcb_table";
        List<Jcb> jcbList = null;
        try {
            jcbList = executeQuery(Jcb.class, sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jcbList;
    }
}
