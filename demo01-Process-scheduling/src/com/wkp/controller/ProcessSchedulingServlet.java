package com.wkp.controller;

import com.wkp.dao.impl.PcbDAOImpl;
import com.wkp.pojo.Pcb;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

@WebServlet("/ProcessScheduling")
public class ProcessSchedulingServlet extends HttpServlet {
    private PcbDAOImpl pcbDAO = new PcbDAOImpl();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp){
        String algorithm = req.getParameter("algorithm");
        Class<? extends ProcessSchedulingServlet> clazz = this.getClass();
        Method method;
        try {
            method = clazz.getDeclaredMethod(algorithm, HttpServletRequest.class, HttpServletResponse.class);
            method.setAccessible(true);
            method.invoke(this, req, resp);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void RoundRobinScheduling(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
        int timeQuantum = Integer.parseInt(req.getParameter("timeQuantum"));
        // 3. 将进程列表转为队列
        LinkedList<Pcb> pcbLinkedList = new LinkedList<>(pcbList);
        // 4. 开始轮转，一边进行一边输出
        PrintWriter respWriter = resp.getWriter();
        int count = 1;
        int run;
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
        pcbList.sort(new Comparator<>() {
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

    private void MultilevelFeedbackQueue(HttpServletRequest req, HttpServletResponse resp) {
        resp.setCharacterEncoding("UTF-8");
        List<Pcb> pcbList = pcbDAO.getAllPcb();
        LinkedList<Pcb> queue1 = new LinkedList<>(pcbList), queue2 = new LinkedList<>(), queue3 = new LinkedList<>();
        pcbList.sort(new Comparator<>() {
            @Override
            public int compare(Pcb o1, Pcb o2) {
                return o1.getArriveTime().compareTo(o2.getArriveTime());
            }
        });
        int timeQuantum1 = Integer.parseInt(req.getParameter("timeQuantum1")),
                timeQuantum2 = Integer.parseInt(req.getParameter("timeQuantum2")),
                timeQuantum3 = Integer.parseInt(req.getParameter("timeQuantum3"));

        try (PrintWriter writer = resp.getWriter()){
            resp.setContentType("text/html");
            detect(queue1, queue2, timeQuantum1, writer);
            detect(queue2, queue3, timeQuantum2, writer);
            detect(queue3, queue3, timeQuantum3, writer);
            writer.println("所有进程调度完成");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void detect(LinkedList<Pcb> queue2, LinkedList<Pcb> queue3, int timeQuantum1, PrintWriter writer) {
        String str;
        while (!queue2.isEmpty()) {
            Pcb head = queue2.removeFirst();
            head.setState(1);
            int usedTime = head.getUsedTime(), runTime = head.getRunTime();
            if (usedTime >= runTime) {
                head.setState(2);
                continue;
            }
            int sub = runTime - usedTime;
            if (timeQuantum1 > sub) {
                // 一个时间片可以解决
                head.setState(2);
                head.setUsedTime(runTime);
                str = "FINISHED, 运行了" + sub + "分钟";
            } else {
                // 一个时间片还不能解决
                head.setUsedTime(usedTime + timeQuantum1);
                head.setState(0);
                queue3.addLast(head);
                str = "READY, 运行了" + timeQuantum1 + "分钟";
            }

            writer.println("pid:" + head.getPid() + "," + "process_name:" + head.getProcessName()
                    + ":" + str + "<br>");
        }
    }
}
