package com.sky.controller.admin;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 菜品 前端控制器
 * </p>
 *
 * @author author
 * @since 2024-08-09
 */
@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 新增菜品
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result addDish(@RequestBody DishDTO dishDTO){
        log.info("新增菜品", dishDTO);
        dishService.addDish(dishDTO);

        String key = "dish_" +  dishDTO.getCategoryId();
        redisTemplate.delete(key);

        return Result.success();
    }

    /**
     * 菜品的分页查询
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页")
    public Result<PageResult> pageDish(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页");
        PageResult pageResult = dishService.pageDish(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId){
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }

    /**
     * 菜品批量删除
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("菜品批量删除")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("菜品批量删除：{}", ids);
        dishService.deleteBatch(ids);//后绪步骤实现
        //将所有菜品缓存数据清理
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        return Result.success();
    }
    /**
     * 根据id查询菜品
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据id查询菜品：{}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);//后绪步骤实现
        return Result.success(dishVO);
    }
    /**
     * 修改菜品
     *
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品：{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);
        //将所有菜品缓存数据清理
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        return Result.success();
    }

    /**
     * 修改菜品状态
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用或禁用菜品")
    public Result<String> updateStatus(@PathVariable("status") Integer status, Long id) {
        dishService.startOrStop(status,id);
        //将所有菜品缓存数据清理
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        return Result.success();
    }


}
