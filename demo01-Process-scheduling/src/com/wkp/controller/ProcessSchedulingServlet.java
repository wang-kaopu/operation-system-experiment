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
import java.time.LocalDateTime;
import java.util.*;

@WebServlet("/ProcessScheduling")
public class ProcessSchedulingServlet extends HttpServlet {
    private PcbDAOImpl pcbDAO = new PcbDAOImpl();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
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

    /**
     * 进程调度：时间片轮转法
     *
     * @param req
     * @param resp
     * @throws IOException
     */
    private void RoundRobinScheduling(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html;charset=utf-8");
        PrintWriter respWriter = resp.getWriter();
        // 1. 获取全部进程，按时间先后顺序排序
        // pcbList是完整的PCB表
        List<Pcb> pcbList = pcbDAO.getAllPcb();
        pcbList.sort(new Comparator<Pcb>() {
            @Override
            public int compare(Pcb o1, Pcb o2) {
                return o1.getArriveTime().compareTo(o2.getArriveTime());
            }
        });
        // hashMap用于映射pid->对应进程在pcbList的索引
        HashMap<Integer, Integer> hashMap = new HashMap<>();
        for (int i = 0; i < pcbList.size(); ++i) {
            hashMap.put(pcbList.get(i).getPid(), i);
        }
        // pcbLinkedList是队列
        LinkedList<Pcb> pcbLinkedList = new LinkedList<>(pcbList);
        // 2. 获取时间片长度（分钟）
        int timeQuantum = Integer.parseInt(req.getParameter("timeQuantum"));
        // 3. 开始轮转，一边进行一边输出
        LocalDateTime currentTime = pcbLinkedList.getFirst().getArriveTime();
        int run = 0;
        while (!pcbLinkedList.isEmpty()) {
            Pcb head;
            if (!pcbLinkedList.getFirst().getArriveTime().isAfter(currentTime)) {
                head = pcbLinkedList.removeFirst();
                // 获取两个时间：使用时间和需要运行时间
                int pid = head.getPid(), usedTime = head.getUsedTime(), runTime = head.getRunTime();
                if (usedTime >= runTime) {
                    continue;
                }
                // 使用时间小于需要运行时间
                int sub = runTime - usedTime;
                if (sub > timeQuantum) {
                    // 一个时间片不够用
                    run = timeQuantum;

                    pcbList.get(hashMap.get(pid)).setUsedTime(usedTime + timeQuantum);
                    pcbList.get(hashMap.get(pid)).setState(0);

                    head.setUsedTime(usedTime + timeQuantum);
                    processRun(head, respWriter, currentTime, run, "READY");
                    printQueue(respWriter, pcbLinkedList);
                    printPcbList(respWriter, pcbList);

                    pcbLinkedList.addLast(head);
                    currentTime = currentTime.plusMinutes(timeQuantum);
                } else {
                    // 一个时间片够用
                    run = sub;

                    pcbList.get(hashMap.get(pid)).setUsedTime(runTime);
                    pcbList.get(hashMap.get(pid)).setState(2);

                    processRun(head, respWriter, currentTime, run, "FINISHED");
                    printQueue(respWriter, pcbLinkedList);
                    printPcbList(respWriter, pcbList);

                    head.setUsedTime(runTime);
                    currentTime = currentTime.plusMinutes(sub);
                }
            }
        }
        respWriter.println("轮转完成");
    }

    private void printPcbList(PrintWriter respWriter, List<Pcb> pcbList) {
        for (Pcb pcb : pcbList) {
            respWriter.println(pcb + "<br>");
        }
        respWriter.println("<br>");
    }

    private void printQueue(PrintWriter respWriter, LinkedList<Pcb> pcbLinkedList) {
        int length = pcbLinkedList.size();
        respWriter.write("就绪队列为：");
        if (0 == length) {
            respWriter.println("（空）<br>");
            return;
        }
        respWriter.write(pcbLinkedList.getFirst().getPid() + ":" + pcbLinkedList.getFirst().getProcessName());
        for (int i = 1; i < length; i++) {
            respWriter.write("->" + pcbLinkedList.get(i).getPid() + ":" + pcbLinkedList.get(i).getProcessName());
        }
        respWriter.println("<br>");
    }

    /**
     * 向客户端输出进程运行情况
     *
     * @param pcb         进程控制块
     * @param respWriter  客户端输出流
     * @param currentTime 当前时间
     */
    private void processRun(Pcb pcb, PrintWriter respWriter, LocalDateTime currentTime, int run, String state) {
        respWriter.println("开始调用进程->时间：" + currentTime +
                ", 进程" + pcb.getPid() + ":" + pcb.getProcessName() + "开始，状态为：" + "RUNNING" + "<br>");
        respWriter.println("结束调用进程->时间：" + currentTime.plusMinutes(run) +
                ", 进程" + pcb.getPid() + ":" + pcb.getProcessName() + "结束，状态为：" + state + "<br>");
    }

    /**
     * 进程调度：先来先服务算法
     *
     * @param req
     * @param resp
     */
    private void FCFS(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("text/html");
        // 1. 获取全部进程，按时间先后顺序排序
        List<Pcb> pcbList = pcbDAO.getAllPcb();
        pcbList.sort(new Comparator<>() {
            @Override
            public int compare(Pcb o1, Pcb o2) {
                return o1.getArriveTime().compareTo(o2.getArriveTime());
            }
        });
        HashMap<Integer, Integer> hashMap = new HashMap<>();
        for (int i = 0; i < pcbList.size(); ++i) {
            hashMap.put(pcbList.get(i).getPid(), i);
        }
        LinkedList<Pcb> pcbLinkedList = new LinkedList<>(pcbList);
        // 2. 设置初始时间为最早到达的进程的到达时间
        LocalDateTime currentTime = pcbLinkedList.getFirst().getArriveTime();
        try (PrintWriter respWriter = resp.getWriter()) {
            // 3. 开始调度，每次执行一个完整的进程
            while (!pcbLinkedList.isEmpty()) {
                Pcb head = pcbLinkedList.removeFirst();
                // 4. 调整当前时间
                // 如果当前时间晚于队头进程的到达时间，则将其调整为队头进程的到达时间
                if (!pcbLinkedList.isEmpty() && currentTime.isBefore(pcbLinkedList.getFirst().getArriveTime())) {
                    currentTime = pcbLinkedList.getFirst().getArriveTime();
                }
                // 5. 向客户端输出运行信息，并修改PCB表
                processRun(head, respWriter, currentTime, head.getRunTime(), "FINISHED");
                pcbList.get(hashMap.get(head.getPid())).setState(2);
                // 6. 打印就绪队列和PCB表
                printQueue(respWriter, pcbLinkedList);
                printPcbList(respWriter, pcbList);
                // 7. 调整当前时间到进程完成的时刻
                currentTime = currentTime.plusMinutes(head.getRunTime());
            }
            respWriter.println("调度完成");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 进程调度：多级反馈队列调度算法（3级）
     *
     * @param req
     * @param resp
     */
    private void MultilevelFeedbackQueue(HttpServletRequest req, HttpServletResponse resp) {
        resp.setCharacterEncoding("UTF-8");
        // 1. 创建3个队列，作为反馈队列
        // 其中，queue1初始化为含有所有进程的队列，并按到达时间排序
        List<Pcb> pcbList = pcbDAO.getAllPcb();
        pcbList.sort(new Comparator<>() {
            @Override
            public int compare(Pcb o1, Pcb o2) {
                return o1.getArriveTime().compareTo(o2.getArriveTime());
            }
        });
        HashMap<Integer, Integer> hashMap = new HashMap<>();
        for (int i = 0; i < pcbList.size(); ++i) {
            hashMap.put(pcbList.get(i).getPid(), i);
        }
        LinkedList<Pcb> queue1 = new LinkedList<>(pcbList),
                queue2 = new LinkedList<>(), queue3 = new LinkedList<>();
        // 2. 获取参数给定的3个时间片长度
        int timeQuantum1 = Integer.parseInt(req.getParameter("timeQuantum1")),
                timeQuantum2 = Integer.parseInt(req.getParameter("timeQuantum2")),
                timeQuantum3 = Integer.parseInt(req.getParameter("timeQuantum3"));
        // 3. 将当前时间初始化为最早到达的进程的到达时间
        LocalDateTime currentTime = queue1.getFirst().getArriveTime();
        List<LinkedList> queueList = List.of(new LinkedList[]{queue1, queue2, queue3});
        try (PrintWriter writer = resp.getWriter()) {
            resp.setContentType("text/html");
            // 4. 优先处理队列1
            detect(queueList, 0, 1, timeQuantum1, writer, currentTime, pcbList, hashMap);
            // 5. 队列1为空时，处理队列2
            detect(queueList, 1, 2, timeQuantum2, writer, currentTime, pcbList, hashMap);
            // 6. 队列2为空时，处理队列3
            detect(queueList, 2, 2, timeQuantum3, writer, currentTime, pcbList, hashMap);
            writer.println("所有进程调度完成");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * 检测一个进程队列，如果非空则分配处理直到为空
     *
     * @param timeQuantum 对应于queue的时间片长度
     * @param writer      客户端输出流
     * @param currentTime 当前时间
     * @param queueList   含有所有反馈队列的列表
     * @param i           当前需要处理的队列在列表queueList的索引
     * @param j           处理后仍然未完成的进程，需要存放到列表queueList中下标为j的队列中去
     * @param pcbList     PCB表
     * @param hashMap     用于映射pid->PCB在PCB表中的索引
     */
    private void detect(List<LinkedList> queueList,
                        int i, int j, int timeQuantum, PrintWriter writer, LocalDateTime currentTime,
                        List<Pcb> pcbList, HashMap<Integer, Integer> hashMap) {
        LinkedList<Pcb> queue = queueList.get(i), queue1 = queueList.get(j);
        while (!queue.isEmpty()) {
            // 1. 出队
            Pcb head = queue.removeFirst();
            int usedTime = head.getUsedTime(), runTime = head.getRunTime();
            // 2. 确认需要处理
            if (usedTime >= runTime) {
                continue;
            }
            // 3. 计算服务时间
            int sub = runTime - usedTime;
            if (timeQuantum >= sub) {
                // 一个时间片可以解决
                processRun(head, writer, currentTime, sub, "FINISHED");
                pcbList.get(hashMap.get(head.getPid())).setState(2);
                pcbList.get(hashMap.get(head.getPid())).setUsedTime(head.getRunTime());
                head.setUsedTime(head.getRunTime());
            } else {
                // 一个时间片还不能解决
                processRun(head, writer, currentTime, timeQuantum, "READY");
                pcbList.get(hashMap.get(head.getPid())).setState(0);
                pcbList.get(hashMap.get(head.getPid())).setUsedTime(usedTime + timeQuantum);
                head.setUsedTime(usedTime + timeQuantum);
                queue1.addLast(head);
            }
            // 4. 输出调度队列、PCB表
            for (int k = 0; k < queueList.size(); ++k) {
                writer.write("第" + (k + 1) + "个");
                printQueue(writer, queueList.get(k));
            }
            printPcbList(writer, pcbList);
        }
    }
}