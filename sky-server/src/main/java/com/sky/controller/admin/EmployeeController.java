package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "员工相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);
        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("用户登出")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * 新增员工
     *
     * @return
     */
    @PostMapping("")
    @ApiOperation("增加用户")
    public Result<String> addEmployee(@RequestBody EmployeeDTO employeeDTO) {
        employeeService.addEmployee(employeeDTO);
        return Result.success();
    }

    /**
     * 分页查询
     *
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(EmployeePageQueryDTO employeeDTO) {
        PageResult pageResult = employeeService.PageQuery(employeeDTO);
        return Result.success(pageResult);
    }

    /**
     * 启用，禁用分类
     *
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用，禁用员工账号")
    public Result updateStatus(@PathVariable("status") Integer status, long id) {
        employeeService.updateStatus(status, id);
        return Result.success("更新成功 ");
    }
    /**
     * 更新员工
     *
     * @return
     */
    @PutMapping("")
    @ApiOperation("更新员工")
    public Result updateEmployee(@RequestBody EmployeeDTO employeeDTO) {
        employeeService.updateEmployee(employeeDTO);
        return Result.success("更新成功 ");
    }

    /**
     * 根据id查找员工
     *
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查找员工")
    public Result<Employee> selectEmployeeById(@PathVariable("id") long id) {
        Employee employee = employeeService.selectEmployeeById(id);
        return Result.success(employee);
    }

    /**
     * 修改员工密码
     *
     * @return
     */
    @PutMapping("/editPassword")
    @ApiOperation("修改员工密码")
    public Result<String> updatePassword(@RequestBody PasswordEditDTO passwordEditDTO) {
        return employeeService.updatePassword(passwordEditDTO);
    }
}
