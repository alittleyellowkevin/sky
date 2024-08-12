package com.sky.service.impl;

import com.sky.dto.DataOverViewQueryDTO;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import io.swagger.models.auth.In;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkspaceService workspaceService;
    /**
     * 根据时间段统计营业数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO turnover(LocalDate begin, LocalDate end) {
        StringJoiner dateList = new StringJoiner(",", "", "");
        StringJoiner AmountList =  new StringJoiner(",", "", "");
        while (begin.isBefore(end.plusDays(1))){
            dateList.add(begin.toString());
            LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(begin, LocalTime.MAX);
            Double amountInOneDay = orderMapper.countOneDayAmount(beginTime, endTime, Orders.COMPLETED);
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

    public void exportBusinessData(HttpServletResponse response) {
        //1. 查询数据库，获取营业数据---查询最近30天的运营数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        //查询概览数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));

        //2. 通过POI将数据写入到Excel文件中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            //基于模板文件创建一个新的Excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);

            //获取表格文件的Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            //填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);

            //获得第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //获得第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                //获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            //3. 通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            //关闭资源
            out.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
