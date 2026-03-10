package org.example.consultant.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.example.consultant.pojo.Reservation;
import org.example.consultant.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class ReservationTool {
    @Autowired
    private ReservationService reservationService;//对象进行注入,ReservationService里面有添加和查询的方法,都是对数据库进行操作的
     //添加预约信息
    @Tool("预约志愿填报服务")
    public void insert(
            @P("考生姓名") String name,// 考生姓名  @P("参数名") 告诉大模型参数名意思
            @P("考生性别") String gender,// 性别
            @P("考生手机号") String phone,// 手机号
            @P("预约沟通时间,格式为: yyyy-MM-dd'T'HH:mm") String communicationTime,// 沟通时间
            @P("考生所在省份") String province,// 省份
            @P("考生预估分数") Integer estimatedScore// 预计分数
    ){
        Reservation reservation = new Reservation(null,name,gender,phone, LocalDateTime.parse(communicationTime),province,estimatedScore);//构建预约信息对象
        reservationService.insert(reservation);
    }
    //2.工具方法: 查询预约信息
    @Tool("根据考生手机号查询预约单")
    public Reservation findByPhone(@P("考生手机号") String phone){
        return reservationService.findByPhone(phone);
    }
}
