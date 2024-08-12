package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    @Scheduled(cron = "0 * * * * ?")
    public void processTimeoutOrder(){
        log.info("定时处理超时订单:{}", LocalDateTime.now());
        LocalDateTime orderTime = LocalDateTime.now().minusMinutes(15);
        List<Orders>  list = orderMapper.selecByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, orderTime);
        for (Orders orders:list){
            orders.setStatus(Orders.CANCELLED);
            orders.setCancelReason("订单超时，自动取消");
            orders.setCancelTime(LocalDateTime.now());
            orderMapper.update(orders);
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void processOrders(){
        List<Orders>  list = orderMapper.selecByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().minusDays(1));
        for (Orders orders:list){
            orders.setStatus(Orders.COMPLETED);
            orderMapper.update(orders);
        }
    }
}
