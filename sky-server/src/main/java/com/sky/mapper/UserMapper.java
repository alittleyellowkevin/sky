package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.Map;

@Mapper
public interface UserMapper {
    /**
     * 根据openid查询用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    /**
     * 插入数据
     * @param user
     */
    void insert(User user);

    @Select("select * from user where user.id = #{userId}")
    User getById(Long userId);

    @Select("select COUNT(*) from user where user.create_time < #{endTime}")
    int selecCountOfUser(LocalDateTime endTime);

    /**
     * 根据动态条件统计用户数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
