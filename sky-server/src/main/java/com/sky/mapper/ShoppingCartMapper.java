package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    /**
     * 动态条件查询
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);


    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    @Insert("insert into shopping_cart (shopping_cart.name, shopping_cart.image, shopping_cart.user_id, shopping_cart.dish_id, shopping_cart.setmeal_id, shopping_cart.dish_flavor, shopping_cart.number, shopping_cart.amount, shopping_cart.create_time) " +
            "values (#{name}, #{image}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{createTime})")
    void insert(ShoppingCart shoppingCart);

    @Delete("delete from sky_take_out.shopping_cart where shopping_cart.user_id = #{currentId}")
    void deleteAll(Long currentId);

    void deleteOne(ShoppingCart shoppingCart);


    void insertBatch(List<ShoppingCart> shoppingCartList);
}
