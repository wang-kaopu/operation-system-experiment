package com.wkp.controller;

import com.wkp.dao.impl.PcbDAOImpl;
import com.wkp.pojo.Pcb;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

@WebServlet("/Scheduling")
public class SchedulingServlet extends HttpServlet {
    private PcbDAOImpl pcbDAO = new PcbDAOImpl();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String algorithm = req.getParameter("algorithm");
        Class<? extends SchedulingServlet> clazz = this.getClass();
        Method method;
        try {
            method = clazz.getDeclaredMethod(algorithm, HttpServletRequest.class, HttpServletResponse.class);
            method.setAccessible(true);
            method.invoke(this, req, resp);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void RoundRobinScheduling(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=utf-8");
        // 1. 获取全部进程，按时间先后顺序排序
        List<Pcb> pcbList = pcbDAO.getAllPcb();
        pcbList.sort(new Comparator<Pcb>() {
            @Override
            public int compare(Pcb o1, Pcb o2) {
                return o1.getArriveTime().compareTo(o2.getArriveTime());
            }
        });
        // 2. 获取时间片长度（分钟）
        Integer timeQuantum = Integer.valueOf(req.getParameter("timeQuantum"));
        // 3. 将进程列表转为队列
        LinkedList<Pcb> pcbLinkedList = new LinkedList<>(pcbList);
        // 4. 开始轮转，一边进行一边输出
        PrintWriter respWriter = resp.getWriter();
        int count = 1;
        int run = 0;
        while (!pcbLinkedList.isEmpty()) {
            // 出队
            Pcb head = pcbLinkedList.removeFirst();
            // 获取两个时间：使用时间和需要运行时间
            int usedTime = head.getUsedTime();
            int runTime = head.getRunTime();
            if (usedTime >= runTime) {
                continue;
            }
            // 将状态设为1：busy运行
            head.setState(1);
            // 使用时间小于需要运行时间
            int sub = runTime - usedTime;
            String str;
            if (sub > timeQuantum) {
                // 一个时间片不够用
                head.setUsedTime(usedTime + timeQuantum);
                pcbLinkedList.addLast(head);
                run = timeQuantum;
                // 将状态设为0：prepared就绪
                head.setState(0);
                str = "PREPARED";
            } else {
                // 一个时间片够用
                head.setUsedTime(runTime);
                run = sub;
                // 将状态设为2：finished完成
                head.setState(2);
                str = "FINISHED";
            }
            respWriter.println("第" + count + "个时间片过去了,&emsp;" +
                    head.getPid() + ":" + head.getProcessName() + "运行了" + run + "分钟，现在其状态为：" +
                    str + "<br>");
            ++count;
        }
        respWriter.println("轮转完成");
    }

    private void FCFS(HttpServletRequest req, HttpServletResponse resp) {
        List<Pcb> pcbList = pcbDAO.getAllPcb();
        pcbList.sort(new Comparator<Pcb>() {
            @Override
            public int compare(Pcb o1, Pcb o2) {
                return o1.getArriveTime().compareTo(o2.getArriveTime());
            }
        });
        resp.setContentType("text/html");
        try (PrintWriter respWriter = resp.getWriter()) {
            while (!pcbList.isEmpty()) {
                Pcb head = pcbList.get(0);
                pcbList.remove(0);
                // 设状态为1：busy运行
                head.setState(1);
                int runTime = head.getRunTime();
                head.setUsedTime(runTime);
                // 设状态为2：finished完成
                head.setState(2);
                respWriter.println("pid：" + head.getPid() + ",process_name:" + head.getProcessName()
                        + ",arrive_time:" + head.getArriveTime() + "&emsp;运行完成,&emsp;" +
                        "运行了" + runTime + "分钟，现在其状态为：" +
                        "FINISHED<br>");
            }
            respWriter.println("全部进程运行完成");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
