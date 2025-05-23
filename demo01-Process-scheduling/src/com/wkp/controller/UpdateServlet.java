package com.wkp.controller;

import com.wkp.dao.JcbDAO;
import com.wkp.dao.impl.JcbDAOImpl;
import com.wkp.dao.impl.PcbDAOImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;

@WebServlet("/Update/*")
public class UpdateServlet extends HttpServlet {
    private PcbDAOImpl pcbDAO = new PcbDAOImpl();
    private JcbDAOImpl jcbDAO = new JcbDAOImpl();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        String[] split = req.getRequestURI().split("/");
        String methodName = split[split.length - 1];
        try {
            Method method = this.getClass().getDeclaredMethod(methodName, HttpServletRequest.class, HttpServletResponse.class);
            method.invoke(this, req, resp);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void addProcess(HttpServletRequest req, HttpServletResponse resp) {
        String processName = req.getParameter("processName");
        int level = Integer.valueOf(req.getParameter("level"));
        LocalDateTime arriveTime = LocalDateTime.parse(req.getParameter("arriveTime"));
        int runTime = Integer.valueOf(req.getParameter("runTime"));
        String sql = "INSERT INTO PCB_TABLE VALUES (?, ?, ?, DEFAULT, ?, ?, ?);";
        try {
            int rows = pcbDAO.executeUpdate(sql, processName, level, arriveTime, 0, 0, runTime);
            resp.setContentType("text/html");
            PrintWriter respWriter = resp.getWriter();
            if (rows > 0) {
                respWriter.println("成功更新" + rows + "行");
            } else {
                respWriter.println("更新失败");
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteProcess(HttpServletRequest req, HttpServletResponse resp) {
        int pid = Integer.parseInt(req.getParameter("pid"));
        String sql = "DELETE FROM PCB_TABLE WHERE PID = ?;";
        try {
            int rows = jcbDAO.executeUpdate(sql, pid);
            responseUpdateRows(resp, rows);
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addJob(HttpServletRequest req, HttpServletResponse resp) {
        String processName = req.getParameter("jobName");
        int level = Integer.parseInt(req.getParameter("level"));
        LocalDateTime arriveTime = LocalDateTime.parse(req.getParameter("arriveTime"));
        int runTime = Integer.parseInt(req.getParameter("runTime"));
        String sql = "INSERT INTO JCB_TABLE VALUES (?, ?, ?, DEFAULT, DEFAULT, DEFAULT, ?, DEFAULT);";
        try {
            int rows = jcbDAO.executeUpdate(sql, processName, level, arriveTime, runTime);
            responseUpdateRows(resp, rows);
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void responseUpdateRows(HttpServletResponse resp, int rows) throws IOException {
        resp.setContentType("text/html");
        PrintWriter respWriter = resp.getWriter();
        if (rows > 0) {
            respWriter.println("成功更新" + rows + "行");
        } else {
            respWriter.println("更新失败");
        }
    }

    private void deleteJob(HttpServletRequest req, HttpServletResponse resp) {
        int jid = Integer.parseInt(req.getParameter("jid"));
        String sql = "DELETE FROM JCB_TABLE WHERE JID = ?;";
        try {
            int rows = jcbDAO.executeUpdate(sql, jid);
            responseUpdateRows(resp, rows);
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
