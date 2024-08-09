package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Employee::getUsername, username);
        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.selectOne(lambdaQueryWrapper);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    @Override
    public void addEmployee(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();

        //对象属性拷贝
        BeanUtils.copyProperties(employeeDTO, employee);
        //设置账号状态
        employee.setStatus(StatusConstant.ENABLE);
        //设置初始密码123456
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        //设置当前时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //设置创始人
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(employee);

    }

    @Override
    public PageResult PageQuery(EmployeePageQueryDTO employeeDTO) {
        //构建page对象
        Page<Employee> page = Page.of(employeeDTO.getPage(), employeeDTO.getPageSize());
        if(employeeDTO.getName() != null) {
            LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Employee::getName, employeeDTO.getName());
            page = employeeMapper.selectPage(page, queryWrapper);
        }else {
            page = employeeMapper.selectPage(page, null);
        }

        return new PageResult(page.getTotal(), page.getRecords());
    }

    @Override
    public void updateStatus(Integer status, long id) {
        Employee employee = employeeMapper.selectById(id);
        employee.setStatus(status);
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.updateById(employee);
    }

    @Override
    public void updateEmployee(EmployeeDTO employeeDTO) {
        Employee employee = employeeMapper.selectById(employeeDTO.getId());

        //设置IDnumber
        employee.setIdNumber(employeeDTO.getIdNumber());
        //设置名字
        employee.setName(employeeDTO.getName());

        //设置手机
        employee.setPhone(employeeDTO.getPhone());
        //设置性别
        employee.setSex(employeeDTO.getSex());
        //设置名字
        employee.setUsername(employeeDTO.getUsername());
        //设置当前时间
        employee.setUpdateTime(LocalDateTime.now());

        //设置创始人
        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.updateById(employee);
    }

    @Override
    public Employee selectEmployeeById(Long id) {
        return employeeMapper.selectById(id);
    }

    @Override
    public Result<String> updatePassword(PasswordEditDTO passwordEditDTO) {
        Employee employee = employeeMapper.selectById(passwordEditDTO.getEmpId());
        if(employee.getPassword().equals(passwordEditDTO.getOldPassword())){
            employee.setPassword(passwordEditDTO.getNewPassword());
            employeeMapper.updateById(employee);
            return Result.success("密码更新成功");
        }else {
            return Result.error("密码错误");
        }
    }

}
