package com.sky.service.impl;

import com.sky.dto.DataOverViewQueryDTO;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public TurnoverReportVO turnover(LocalDate begin, LocalDate end) {
        StringJoiner dateList = new StringJoiner(",", "", "");
        StringJoiner AmountList =  new StringJoiner(",", "", "");
        while (begin.isBefore(end.plusDays(1))){
            dateList.add(begin.toString());
            LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(begin, LocalTime.MAX);
            Integer amountInOneDay = orderMapper.countOneDayAmount(beginTime, endTime, Orders.COMPLETED);
            if(amountInOneDay != null){
                AmountList.add(amountInOneDay.toString());
            } else {
                AmountList.add("0");
            }
            begin = begin.plusDays(1);
        }
        return TurnoverReportVO
                .builder()
                .dateList(dateList.toString())
                .turnoverList(AmountList.toString())
                .build();
    }

    @Override
    public UserReportVO userReport(LocalDate begin, LocalDate end) {
        StringJoiner dateList = new StringJoiner(",", "", "");
        StringJoiner newUserList =  new StringJoiner(",", "", "");
        StringJoiner totalUserList =  new StringJoiner(",", "", "");
        while (begin.isBefore(end.plusDays(1))){
            dateList.add(begin.toString());
            LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(begin, LocalTime.MAX);

            //首先查该天的总用户数,并加入到当前
            Integer today = userMapper.selecCountOfUser(endTime);
            totalUserList.add(today.toString());
            //再查前一天的总用户数
            Integer before = userMapper.selecCountOfUser(endTime.minusDays(1));
            //两者相减得出今天的新增用户数
            Integer newAdd = today - before;
            newUserList.add(newAdd.toString());
            begin = begin.plusDays(1);
        }
        return UserReportVO
                .builder()
                .dateList(dateList.toString())
                .newUserList(newUserList.toString())
                .totalUserList(totalUserList.toString())
                .build();
    }

    @Override
    public OrderReportVO orderReport(LocalDate begin, LocalDate end) {
        StringJoiner dateList = new StringJoiner(",", "", "");
        StringJoiner orderCountList =  new StringJoiner(",", "", "");
        StringJoiner validOrderCountList =  new StringJoiner(",", "", "");
        Double orderCompletionRate = 0.0;
        Integer totalOrderCount = 0;
        Integer validOrderCount = 0;
        while (begin.isBefore(end.plusDays(1))){
            dateList.add(begin.toString());
            LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(begin, LocalTime.MAX);

            //查询当天所有订单，将总订单量，加入list，
            Integer countToday = orderMapper.countOneDayCount(beginTime, endTime, null);
            if(countToday != null){
                orderCountList.add(countToday.toString());
                totalOrderCount += countToday;
            }else {
                orderCountList.add("0");
            }
            //查询有效订单
            Integer countValidToday = orderMapper.countOneDayCount(beginTime, endTime, Orders.COMPLETED);
            if(countValidToday!=null) {
                validOrderCountList.add(countValidToday.toString());
                validOrderCount += countValidToday;
            }else {
                validOrderCountList.add("0");
            }
            begin = begin.plusDays(1);
        }
        orderCompletionRate = (!totalOrderCount.equals(0)) ? validOrderCount.doubleValue()/totalOrderCount.doubleValue():0.0;
        return OrderReportVO
                .builder()
                .dateList(dateList.toString())
                .orderCountList(orderCountList.toString())
                .validOrderCountList(validOrderCountList.toString())
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        StringJoiner nameList = new StringJoiner(",", "", "");
        StringJoiner numberList =  new StringJoiner(",", "", "");
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> goodsSalesDTOS = orderMapper.getSalesTop10(beginTime, endTime, Orders.COMPLETED);

        for(GoodsSalesDTO goodsSalesDTO:goodsSalesDTOS){
            nameList.add(goodsSalesDTO.getName());
            numberList.add(goodsSalesDTO.getNumber().toString());
        }

        return SalesTop10ReportVO
                .builder()
                .numberList(numberList.toString())
                .nameList(nameList.toString())
                .build();
    }
}
