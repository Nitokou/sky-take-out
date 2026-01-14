package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkSpaceService;
import com.sky.vo.*;
import io.swagger.models.auth.In;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private WorkSpaceService workSpaceService;

//    营业额统计
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {

//        计算字符串 当前集合用于存放从begin 到 end 范围内每天的日期
        List<LocalDate> dateList = new ArrayList<>();
        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();

//       将每一天加入
        dateList.add(begin);
        while(!begin.isAfter(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

//        设置日期
        String join = StringUtils.join(dateList, ",");
        turnoverReportVO.setDateList(join);

        ArrayList<Double> turnoverList = new ArrayList<>();

//        查询营业额
        for(LocalDate date: dateList){
//            查询date日期且对应的营业额数据
//            select sum(amount) from orders where order_time > ? and order_time < ? and status = 5
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);

            Double turnover = orderMapper.sumByMap(map);
            turnover = (turnover == null ? 0 : turnover);
            turnoverList.add(turnover);
        }

        String turnoverJoin = StringUtils.join(turnoverList, ",");
        turnoverReportVO.setTurnoverList(turnoverJoin);

        return turnoverReportVO;
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        ArrayList<LocalDate> dateList = new ArrayList<>();

        UserReportVO userReportVO = new UserReportVO();

        dateList.add(begin);
        while(!begin.isAfter(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

//        设置时间列表
        String date = StringUtils.join(dateList, ",");
        userReportVO.setDateList(date);

//        获取新用户列表
        ArrayList<Long> newUserList = new ArrayList<>();
        ArrayList<Long> userList = new ArrayList<>();
        for (LocalDate localDate: dateList){
//            select sum(id) from user where create_time < localDate.Max and create_time > localDate.Min
//            select sum(id) from user where create_time < localDate.Max

            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

            Map map = new HashMap();
//          统计截至时间多少用户
            map.put("end", endTime);
            Long user = orderMapper.sumUserByMap(map);
            user = (user == null ? 0: user);
            userList.add(user);

            map.put("begin", beginTime);
            Long newUser = orderMapper.sumUserByMap(map);
            newUser = (newUser == null ? 0: newUser);
            newUserList.add(newUser);

        }
        String newUserString = StringUtils.join(newUserList, ",");
        String userString = StringUtils.join(userList, ",");

        userReportVO.setNewUserList(newUserString);
        userReportVO.setTotalUserList(userString);

        return userReportVO;
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        ArrayList<LocalDate> dateList = new ArrayList<>();

        OrderReportVO orderReportVO = new OrderReportVO();
//    日期，以逗号分隔，例如：2022-10-01,2022-10-02,2022-10-03
//    private String dateList;
//
//    //每日订单数，以逗号分隔，例如：260,210,215
//    private String orderCountList;
//
//    //每日有效订单数，以逗号分隔，例如：20,21,10
//    private String validOrderCountList;
//
//    //订单总数
//    private Integer totalOrderCount;
//
//    //有效订单数
//    private Integer validOrderCount;
//
//    //订单完成率
//    private Double orderCompletionRate;

        dateList.add(begin);
        while(!begin.isAfter(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

//        设置时间列表
        String date = StringUtils.join(dateList, ",");
        orderReportVO.setDateList(date);

//        获取新用户列表
        ArrayList<Integer> totalOrderList = new ArrayList<>();
        ArrayList<Integer> validOrderList = new ArrayList<>();
        Integer totalOrderCount = 0;
        Integer validOrderCount = 0;

        for (LocalDate localDate: dateList){
//            select count(id) from order where status == Complement
//            select count(id) from order where status != Complement

            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

            Map map = new HashMap();
//          统计截至时间多少用户
            map.put("begin", beginTime);
            map.put("end", endTime);

//            统计所有订单
            Integer totalOrder = orderMapper.sumOrderByMap(map);
            totalOrder = (totalOrder == null ? 0: totalOrder);
            totalOrderList.add(totalOrder);
            totalOrderCount += totalOrder;

            //            统计有效订单

            map.put("status", Orders.COMPLETED);
            Integer validOrder = orderMapper.sumOrderByMap(map);
            validOrder = (validOrder == null ? 0: validOrder);
            validOrderList.add(validOrder);
            validOrderCount += validOrder;

        }
        String totalOrderString = StringUtils.join(totalOrderList, ",");
        String validOrderListString = StringUtils.join(validOrderList, ",");

        orderReportVO.setValidOrderCountList(validOrderListString);
        orderReportVO.setOrderCountList(totalOrderString);
        orderReportVO.setTotalOrderCount(totalOrderCount);
        orderReportVO.setValidOrderCount(validOrderCount);


        orderReportVO.setOrderCompletionRate((double)validOrderCount / totalOrderCount);

        return orderReportVO;
    }

    @Override
    public SalesTop10ReportVO getSaleTop10Statistics(LocalDate begin, LocalDate end) {
//        ArrayList<LocalDate> dateList = new ArrayList<>();

        SalesTop10ReportVO salesTop10ReportVO = new SalesTop10ReportVO();

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        Map map = new HashMap();
//      统计截至时间多少用户
        map.put("begin", beginTime);
        map.put("end", endTime);
        List<GoodsSalesDTO> SaleTop10 = orderMapper.getSaleTop10(map);


        List<String> nameList = SaleTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numberList = SaleTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        String name = StringUtils.join(nameList, ",");
        String number = StringUtils.join(numberList, ",");
        salesTop10ReportVO.setNameList(name);
        salesTop10ReportVO.setNumberList(number);

        return salesTop10ReportVO;
    }

    @Override
    public void exportBusinessData(HttpServletResponse response) {
//        1 查询数据库 获取营业数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        LocalDateTime begin = LocalDateTime.of(dateBegin, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(dateEnd, LocalTime.MAX);


        BusinessDataVO businessData = workSpaceService.getBusinessData(begin, end);

//        2 通过POI将数据写入到Excel中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            XSSFWorkbook excels = new XSSFWorkbook(in);
            XSSFSheet sheet1 = excels.getSheet("Sheet1");
//            获得第二行
            sheet1.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);

            XSSFRow row3 = sheet1.getRow(3);
            row3.getCell(2).setCellValue(businessData.getTurnover());
            row3.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row3.getCell(6).setCellValue(businessData.getNewUsers());
            XSSFRow row4 = sheet1.getRow(4);
            row4.getCell(2).setCellValue(businessData.getValidOrderCount());
            row4.getCell(4).setCellValue(businessData.getUnitPrice());

            for (int i=0; i < 30; i++){
                LocalDate date = dateBegin.plusDays(i);
                BusinessDataVO dayData = workSpaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                XSSFRow row = sheet1.getRow(i + 7);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(dayData.getTurnover());
                row.getCell(3).setCellValue(dayData.getValidOrderCount());
                row.getCell(4).setCellValue(dayData.getOrderCompletionRate());
                row.getCell(5).setCellValue(dayData.getUnitPrice());
                row.getCell(6).setCellValue(dayData.getNewUsers());
            }




            //        3 通过输出流将Excel文件下载到客户端浏览器中
            ServletOutputStream out = response.getOutputStream();
            excels.write(out);
            out.close();
            excels.close();


        } catch (IOException e) {
            throw new RuntimeException(e);
        }




    }
}
