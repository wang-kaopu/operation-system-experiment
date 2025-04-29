package com.wkp.test;

import com.wkp.dao.impl.JcbDAOImpl;
import com.wkp.pojo.Jcb;
import org.junit.Test;

import java.util.List;

public class getAllJcbTest {
    private JcbDAOImpl jcbDAO = new JcbDAOImpl();

    @Test
    public void test(){
        List<Jcb> jcbList = jcbDAO.getAllJcb();
        for (Jcb jcb : jcbList) {
            System.out.println(jcb);
        }
    }
}
