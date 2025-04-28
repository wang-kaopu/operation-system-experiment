package com.wkp.test;

import com.wkp.dao.impl.PcbDAOImpl;
import com.wkp.pojo.Pcb;
import org.junit.Test;

import java.util.List;

public class getAllPcbTest {
    @Test
    public void test(){
        PcbDAOImpl pcbDAO = new PcbDAOImpl();
        List<Pcb> pcbList = pcbDAO.getAllPcb();
        for (Pcb pcb : pcbList) {
            System.out.println(pcb);
        }
    }
}
