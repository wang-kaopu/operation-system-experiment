package com.wkp.pojo;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
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
}
