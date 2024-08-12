package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单数据
     * @param order
     */
    void insert(Orders order);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from sky_take_out.orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 根据状态统计订单数量
     * @param status
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer countStatus(Integer status);

    @Update("update sky_take_out.orders set orders.status = #{orderstatus}, orders.pay_status = #{oderPaidStatus}, orders.checkout_time=#{checkoutTime} where orders.number = #{orderNumber}")
    void updateStatus(Integer orderstatus, Integer oderPaidStatus, LocalDateTime checkoutTime, String orderNumber);

    @Select("select * from sky_take_out.orders where orders.status = #{status} and orders.order_time < #{orderTime}")
    List<Orders> selecByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);


    Double countOneDayAmount(LocalDateTime beginTime, LocalDateTime endTime, Integer status);

    Integer countOneDayCount(LocalDateTime beginTime, LocalDateTime endTime, Integer status);

    List<GoodsSalesDTO> getSalesTop10(LocalDateTime beginTime, LocalDateTime endTime, Integer status);

    /**
     * 根据动态条件统计营业额数据
     * @param map
     * @return
     */
    Double sumByMap(Map map);

    /**
     * 根据动态条件统计订单数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
