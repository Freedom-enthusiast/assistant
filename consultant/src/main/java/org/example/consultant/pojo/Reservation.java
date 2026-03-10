package org.example.consultant.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data//set和get方法自动添加好
@NoArgsConstructor//无参构造方法自动添加
@AllArgsConstructor //有参/全参构造方法自动添加
public class Reservation {
    private Long id;
    private String name;
    private String gender;
    private String phone;
    private LocalDateTime communicationTime;
    private String province;
    private Integer estimatedScore;
}
