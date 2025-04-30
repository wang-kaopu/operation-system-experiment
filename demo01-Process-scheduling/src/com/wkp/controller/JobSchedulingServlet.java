package com.wkp.controller;

import com.wkp.dao.impl.JcbDAOImpl;
import com.wkp.pojo.Jcb;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 作业调度算法
 */
@WebServlet("/JobScheduling")
public class JobSchedulingServlet extends HttpServlet {
    private JcbDAOImpl jcbDAO = new JcbDAOImpl();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String algorithm = req.getParameter("algorithm");
        Class<? extends JobSchedulingServlet> clazz = this.getClass();
        Method method;
        try {
            method = clazz.getDeclaredMethod(algorithm, HttpServletRequest.class, HttpServletResponse.class);
            method.setAccessible(true);
            method.invoke(this, req, resp);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 作业调度：短作业优先算法
     *
     * @param req
     * @param resp
     */
    private void SJF(HttpServletRequest req, HttpServletResponse resp) {
        // 1. 将所有进程转为队列，按时间排序，等待入堆
        List<Jcb> jcbList = jcbDAO.getAllJcb();
        jcbList.sort(new Comparator<Jcb>() {
            @Override
            public int compare(Jcb o1, Jcb o2) {
                return o1.getArriveTime().compareTo(o2.getArriveTime());
            }
        });
        HashMap<Integer, Integer> hashMap = new HashMap<>();
        for (int i = 0; i < jcbList.size(); ++i) {
            hashMap.put(jcbList.get(i).getJid(), i);
        }
        LinkedList<Jcb> jcbLinkedList = new LinkedList<>(jcbList);
        // 2. 创建小顶堆，按照需要运行时间（作业长度）排序
        PriorityQueue<Jcb> heap = new PriorityQueue<>(new Comparator<Jcb>() {
            @Override
            public int compare(Jcb o1, Jcb o2) {
                return Integer.compare(o1.getRunTime(), o2.getRunTime());
            }
        });
        // 3. 开始调度
        selectAndRun(resp, jcbLinkedList, heap, jcbList, hashMap);
    }

    /**
     * 向客户端输出作业运行情况
     *
     * @param jcb         作业控制块
     * @param respWriter  客户端输出流
     * @param currentTime 当前时间
     */
    private void JobRun(Jcb jcb, PrintWriter respWriter, LocalDateTime currentTime) {
        respWriter.println("开始作业->时间：" + currentTime +
                ", 作业" + jcb.getJid() + ":" + jcb.getJobName() + "开始<br>");
        respWriter.println("结束作业->时间：" + currentTime.plusMinutes(jcb.getRunTime()) +
                ", 作业" + jcb.getJid() + ":" + jcb.getJobName() + "结束<br>");
    }

    /**
     * 静态优先权算法，level越大级别越高
     *
     * @param req
     * @param resp
     */
    private void StaticLevelScheduling(HttpServletRequest req, HttpServletResponse resp) {
        // 1. 将所有进程转为队列，按时间排序，等待入堆
        List<Jcb> jcbList = jcbDAO.getAllJcb();
        jcbList.sort(new Comparator<Jcb>() {
            @Override
            public int compare(Jcb o1, Jcb o2) {
                return 0 - o1.getArriveTime().compareTo(o2.getArriveTime());
            }
        });
        HashMap<Integer, Integer> hashMap = new HashMap<>();
        for (int i = 0; i < jcbList.size(); ++i) {
            hashMap.put(jcbList.get(i).getJid(), i);
        }
        LinkedList<Jcb> jcbLinkedList = new LinkedList<>(jcbList);
        // 2. 创建小顶堆，按照需要运行时间（作业长度）排序
        PriorityQueue<Jcb> heap = new PriorityQueue<>(new Comparator<Jcb>() {
            @Override
            public int compare(Jcb o1, Jcb o2) {
                return Integer.compare(o1.getLevel(), o2.getLevel());
            }
        });
        // 3. 将初始时间设为第一个到达的作业的到达时间
        selectAndRun(resp, jcbLinkedList, heap, jcbList, hashMap);
    }

    /**
     * 从堆中选出目标进程并执行
     *
     * @param resp
     * @param jcbLinkedList 含有所有进程的队列
     * @param heap          用于选取目标进程的队列
     * @param jcbList
     * @param hashMap
     */
    private void selectAndRun(HttpServletResponse resp, LinkedList<Jcb> jcbLinkedList,
                              PriorityQueue<Jcb> heap, List<Jcb> jcbList, HashMap<Integer, Integer> hashMap) {
        // 3. 初始化时间为最早到达的任务的到达时间
        LocalDateTime currentTime = jcbLinkedList.getFirst().getArriveTime();
        resp.setContentType("text/html");
        try (PrintWriter respWriter = resp.getWriter()) {
            while (!jcbLinkedList.isEmpty() || !heap.isEmpty()) {
                // 4. 初始堆，推入所有最早到达的作业
                while (!jcbLinkedList.isEmpty()
                        && !jcbLinkedList.getFirst().getArriveTime().isAfter(currentTime)) {
                    heap.add(jcbLinkedList.removeFirst());
                }
                if (!heap.isEmpty()) {
                    // 5. 利用堆选取当前时间下，优先权最高的作业
                    Jcb top = heap.poll();
                    // 6. 运行这个作业
                    JobRun(top, respWriter, currentTime);
                    jcbList.get(hashMap.get(top.getJid())).setState(2);
                    plusWaitTimeOfOtherJob(jcbList, top);
                    // 7. 完成作业后，重新计算当前时间
                    currentTime = currentTime.plusMinutes(top.getRunTime());
                } else {
                    // 当前时间下还没有新作业到达，处理机空闲
                    // 跳过空闲时间，到达下一个最早到达的作业的到达时间
                    currentTime = jcbLinkedList.getFirst().getArriveTime();
                }
            }
            printJcbList(jcbList, respWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void printJcbList(List<Jcb> jcbList, PrintWriter respWriter) {
        double averageTime = 0.0, averageWeightedTime = 0.0;
        int n = jcbList.size();
        for (Jcb j : jcbList) {
            respWriter.println(j + "<br>");
            int waitTime = j.getWaitTime();
            int runTime = j.getRunTime();
            averageTime += waitTime + runTime;
            averageWeightedTime += (waitTime + runTime) / (double) runTime;
        }
        averageTime = averageTime / n;
        averageWeightedTime = averageWeightedTime / n;
        // 1. 计算平均周转时间
        respWriter.println("平均周转时间：" + averageTime);
        // 2. 计算带权平均周转时间
        respWriter.println("带权平均周转时间：" + averageWeightedTime);
        respWriter.println("<br>");
    }

    private void plusWaitTimeOfOtherJob(List<Jcb> jcbList, Jcb jcb) {
        for (Jcb j : jcbList) {
            if (j.getState() == 2 || j.getJid() == jcb.getJid()) {
                continue;
            }
            j.setWaitTime(j.getWaitTime() + jcb.getRunTime());
        }
    }
}