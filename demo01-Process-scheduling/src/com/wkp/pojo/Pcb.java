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
        String stateString = state == 0 ? "READY" : "FINISHED";
        return "Pcb{" +
                "pid=" + pid +
                ", processName='" + processName + '\'' +
                ", level=" + level +
                ", arriveTime=" + arriveTime +
                ", usedTime=" + usedTime +
                ", state=" + stateString +
                ", runTime=" + runTime +
                '}';
    }
}
