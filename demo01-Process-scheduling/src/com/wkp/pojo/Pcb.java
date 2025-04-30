package com.wkp.pojo;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
//@ToString
@Getter
@Setter
public class Pcb {
    private String processName;
    private int level;
    private LocalDateTime arriveTime;
    private int pid;
    private int usedTime;
    private int state;
    private int runTime;

    @Override
    public String toString() {
        String stateString = state == 0 ? "就绪" : "完成";
        return "PCB{" +
                "PID=" + pid +
                ", 进程名='" + processName + '\'' +
                ", 优先级=" + level +
                ", 到达时间=" + arriveTime +
                ", 已运行时长=" + usedTime + "分钟" +
                ", 状态=" + stateString +
                ", 服务时长=" + runTime + "分钟" +
                '}';
    }
}
