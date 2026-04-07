package com.choco.smartlf.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.choco.smartlf.entity.dto.UserLoginDTO;
import com.choco.smartlf.entity.dto.UserRegisterDTO;
import com.choco.smartlf.entity.pojo.User;
import com.choco.smartlf.entity.vo.UserInfoVO;
import com.choco.smartlf.entity.vo.UserLoginVO;
import com.choco.smartlf.enums.CheckType;
import com.choco.smartlf.exception.BusinessException;
import com.choco.smartlf.mapper.UserMapper;
import com.choco.smartlf.service.UserService;
import com.choco.smartlf.utils.Constant;
import com.choco.smartlf.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

/**
 * @author Choco
 * @description 针对表【user(用户核心表)】的数据库操作Service实现
 * @createDate 2026-04-06 14:52:42
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {
    // 自动读取环境变量或 yaml 里的密钥
    @Value("${system.admin-secret-key:QG-Studio-Super-Key}")
    private String adminSecretKey;

    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void registerUser(UserRegisterDTO dto) {
        //看身份，如果是管理员先验证密钥
        if (dto.getRole().equals(Constant.ADMIN_ROLE)) {
            if (!dto.getSecretKey().equals(adminSecretKey)) {
                throw new BusinessException(SC_UNAUTHORIZED, "管理员密钥错误！");
            }
        }
        //验证用户名、邮箱、手机号是否重复
        LambdaQueryWrapper<User> queryWrapper = getUserLambdaQueryWrapper(dto.getUsername(), dto.getEmail(), dto.getPhone());
        List<User> users = userMapper.selectList(queryWrapper);
        if (!users.isEmpty()) {
            throw new BusinessException(SC_UNAUTHORIZED, "用户名、邮箱或手机号已存在！");
        }
        //无重复，插入数据库
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        //密码加密
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setRole(dto.getRole());
        //校验有无昵称
        if (StrUtil.isBlank(dto.getNickname())) {
            //自动分配昵称，格式为用户+随机六位数数字
            user.setNickname(Constant.NICKNAME_PREFIX + RandomUtil.randomNumbers(6));
        } else {
            user.setNickname(dto.getNickname());
        }
        userMapper.insert(user);
    }

    @Override
    public UserLoginVO login(UserLoginDTO dto) {
        //根据传来的账号查询用户
        String account = dto.getAccount();
        LambdaQueryWrapper<User> queryWrapper = getUserLambdaQueryWrapper(account);
        User user = userMapper.selectOne(queryWrapper);
        //查不到用户或者密码对不上
        if (user == null || !BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(SC_UNAUTHORIZED, "用户不存在或密码错误");
        }
        //验证是否被封禁
        if (user.getStatus().equals(Constant.STATUS_BANNED)) {
            throw new BusinessException(SC_FORBIDDEN, "用户已封禁！请联系管理员");
        }
        //将用户ID，用户角色，用户名封装，生成token
        String token = JwtUtil.createJwtToken(user.getId(), user.getRole(), user.getUsername());
        //将token存入redis并设置过期时间
        String key = Constant.TOKEN_PREFIX + user.getId();
        redisTemplate.opsForValue().set(key, token);
        //设置过期时间
        redisTemplate.expire(key, Constant.TOKEN_EXPIRATION, TimeUnit.MINUTES);

        //返回登录信息
        return new UserLoginVO(token, user.getId(), user.getUsername(), user.getNickname(), user.getRole());

    }

    @Override
    public boolean checkUnique(CheckType type, String value) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        //根据枚举类型判断
        switch (type) {
            case USERNAME:
                wrapper.eq(User::getUsername, value);
                break;
            case PHONE:
                wrapper.eq(User::getPhone, value);
                break;
            case EMAIL:
                wrapper.eq(User::getEmail, value);
                break;
            // 因为 type 是枚举，所以能传过来一定是对的type，不用再判断剩下的了
        }
        return exists(wrapper);

    }

    private LambdaQueryWrapper<User> getUserLambdaQueryWrapper(String username, String email, String phone) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username)
                .or()
                .eq(User::getEmail, email)
                .or()
                .eq(User::getPhone, phone);
        return queryWrapper;
    }

    private LambdaQueryWrapper<User> getUserLambdaQueryWrapper(String account) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, account)
                .or()
                .eq(User::getEmail, account)
                .or()
                .eq(User::getPhone, account);
        return queryWrapper;
    }

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        //根据用户id去查询用户信息
        User user = getById(userId);
        if(user==null){
            throw new BusinessException("用户不存在");
        }
        UserInfoVO vo = new UserInfoVO();
        BeanUtil.copyProperties(user, vo);
        return vo;
    }
}