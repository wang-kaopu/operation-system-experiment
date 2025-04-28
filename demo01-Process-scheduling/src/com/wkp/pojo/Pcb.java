package com.wkp.pojo;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
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
}
