package com.wkp.pojo;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class Jcb {
    private String jobName;
    private int level;
    private LocalDateTime arriveTime;
    private int jid;
    private int usedTime;
    private int state;
    private int runTime;
    private int waitTime;

    @Override
    public String toString() {
        LocalDateTime startTime = arriveTime.plusMinutes(waitTime);
        LocalDateTime finishTime = startTime.plusMinutes(runTime);
        return "JCB{" +
                "JID=" + jid +
                ", 作业名='" + jobName + '\'' +
                ", 优先级=" + level +
                ", 到达时间=" + arriveTime +
                ", 开始时间=" + startTime +
                ", 完成时间=" + finishTime +
                ", 服务时间=" + runTime + "分钟" +
                ", 等待时间=" + waitTime + "分钟" +
                '}';
    }
}
