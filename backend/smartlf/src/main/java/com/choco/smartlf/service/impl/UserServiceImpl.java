package com.choco.smartlf.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.choco.smartlf.entity.dto.*;
import com.choco.smartlf.entity.pojo.ItemInfo;
import com.choco.smartlf.entity.pojo.User;
import com.choco.smartlf.entity.vo.AdminUserInfoVO;
import com.choco.smartlf.entity.vo.UserInfoVO;
import com.choco.smartlf.entity.vo.UserLoginVO;
import com.choco.smartlf.enums.*;
import com.choco.smartlf.exception.BusinessException;
import com.choco.smartlf.mapper.ItemInfoMapper;
import com.choco.smartlf.mapper.UserMapper;
import com.choco.smartlf.service.UserService;
import com.choco.smartlf.utils.Constant;
import com.choco.smartlf.utils.ImageNameUtil;
import com.choco.smartlf.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Choco
 * @description 针对表【user(用户核心表)】的数据库操作Service实现
 * @createDate 2026-04-06 14:52:42
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    // 自动读取环境变量或 yaml 里的密钥
    @Value("${system.admin-secret-key:QG-Studio-Super-Key}")
    private String adminSecretKey;

    // 自动读取环境变量或 yaml 里的文件存储的前缀
    @Value("${system.file-prefix:D:/lfFile/}")
    private String filePrefix;

    private final StringRedisTemplate redisTemplate;
    // 注入 Mapper 而不是 Service，避开循环依赖死局
    private final ItemInfoMapper itemInfoMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerUser(UserRegisterDTO dto) {
        log.info("开始注册新用户，用户名: {}, 角色: {}", dto.getUsername(), dto.getRole());

        // 看身份，如果是管理员先验证密钥
        if (dto.getRole().equals(RoleEnum.ADMIN.getCode())) {
            if (!dto.getSecretKey().equals(adminSecretKey)) {
                log.warn("管理员注册失败：密钥错误。提交用户名: {}", dto.getUsername());
                throw new BusinessException(ResultCodeEnum.ADMIN_KEY_ERROR);
            }
        }

        // 验证用户名、邮箱、手机号是否重复
        LambdaQueryWrapper<User> queryWrapper = getUserLambdaQueryWrapper(dto.getUsername(), dto.getEmail(), dto.getPhone());
        if (this.exists(queryWrapper)) {
            log.warn("注册失败：用户名/邮箱/手机号已存在。提交数据: {}", dto.getUsername());
            throw new BusinessException(ResultCodeEnum.USER_ALREADY_EXISTS);
        }

        // 无重复，插入数据库
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole());
        // 密码加密（日志严禁记录明文密码）
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        // 自动分配昵称
        user.setNickname(createNickname());

        this.save(user);
        log.info("用户注册成功，生成用户ID: {}, 昵称: {}", user.getId(), user.getNickname());
    }

    @Override
    public UserLoginVO login(UserLoginDTO dto) {
        log.info("用户尝试登录，账号: {}", dto.getAccount());

        // 根据传来的账号查询用户
        User user = this.getOne(getUserLambdaQueryWrapper(dto.getAccount()));

        // 查不到用户或者密码对不上
        if (user == null || !BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            log.warn("登录失败：账号或密码错误。提交账号: {}", dto.getAccount());
            throw new BusinessException(ResultCodeEnum.USER_NOT_FOUND, "用户名或密码错误");
        }

        // 验证是否被封禁
        if (user.getStatus().equals(UserStatusEnum.BANNED.getCode())) {
            log.warn("登录失败：账号已被封禁。用户ID: {}", user.getId());
            throw new BusinessException(ResultCodeEnum.USER_BANNED);
        }

        // 生成token
        String token = JwtUtil.createJwtToken(user.getId(), user.getRole(), user.getUsername());

        // 将token存入redis并设置过期时间
        String key = Constant.TOKEN_PREFIX + user.getId();
        redisTemplate.opsForValue().set(key, token, Constant.TOKEN_EXPIRATION, TimeUnit.MINUTES);

        log.info("登录成功，用户ID: {}, 昵称: {}, Token已存入Redis", user.getId(), user.getNickname());

        // 返回登录信息
        return new UserLoginVO(token, user.getId(), user.getUsername(), user.getNickname(), user.getRole());
    }

    @Override
    public boolean isExist(CheckType type, String value) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
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
        }
        return exists(wrapper);
    }

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        User user = getById(userId);
        if (user == null) {
            log.warn("获取用户信息失败：ID {} 不存在", userId);
            throw new BusinessException(ResultCodeEnum.USER_NOT_FOUND);
        }
        UserInfoVO vo = new UserInfoVO();
        BeanUtil.copyProperties(user, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(Long userId, UserUpdateDTO dto) {
        log.info("修改个人信息，用户ID: {}, 提交数据: {}", userId, dto);

        // 查重逻辑：确保新手机号/邮箱没有被【其他用户】占用
        if (StrUtil.isNotBlank(dto.getEmail())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getEmail, dto.getEmail()).ne(User::getId, userId);
            if (this.exists(wrapper)) {
                log.warn("更新信息失败：邮箱 {} 已被占用。操作人ID: {}", dto.getEmail(), userId);
                throw new BusinessException(ResultCodeEnum.EMAIL_EXIST);
            }
        }
        if (StrUtil.isNotBlank(dto.getPhone())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getPhone, dto.getPhone()).ne(User::getId, userId);
            if (this.exists(wrapper)) {
                log.warn("更新信息失败：手机号 {} 已被占用。操作人ID: {}", dto.getPhone(), userId);
                throw new BusinessException(ResultCodeEnum.PHONE_EXIST);
            }
        }

        // 数据更新
        User user = new User();
        BeanUtil.copyProperties(dto, user);
        user.setId(userId);

        updateById(user);
        log.info("用户信息更新成功，用户ID: {}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, UpdatePasswordDTO dto) {
        log.info("修改密码操作，用户ID: {}", userId);

        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCodeEnum.USER_NOT_FOUND);
        }

        // 校验旧密码
        if (!BCrypt.checkpw(dto.getOldPassword(), user.getPassword())) {
            log.warn("修改密码失败：原密码输入错误。用户ID: {}", userId);
            throw new BusinessException(ResultCodeEnum.PASSWORD_ERROR);
        }

        // 校验新密码不能和旧密码一样
        if (BCrypt.checkpw(dto.getNewPassword(), user.getPassword())) {
            log.warn("修改密码失败：新密码与旧密码相同。用户ID: {}", userId);
            throw new BusinessException(ResultCodeEnum.PASSWORD_SAME_ERROR);
        }

        // 加密新密码并保存
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(BCrypt.hashpw(dto.getNewPassword()));

        this.updateById(updateUser);
        log.info("密码修改成功，用户ID: {}", userId);
    }

    @Override
    public boolean isSame(Long userId, String value) {
        User user = getById(userId);
        return BCrypt.checkpw(value, user.getPassword());
    }

    @Override
    public String updateAvatar(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCodeEnum.INVALID_FILE);
        }

        log.info("上传头像，用户ID: {}, 原始文件名: {}, 大小: {} bytes",
                userId, file.getOriginalFilename(), file.getSize());

        // 生成唯一文件名
        String imageName = ImageNameUtil.getImageName(file.getOriginalFilename());
        String fullSavePath = filePrefix + "avatars/" + imageName;

        java.io.File destFile = new java.io.File(fullSavePath);

        // 确保父目录存在
        if (!destFile.getParentFile().exists()) {
            boolean created = destFile.getParentFile().mkdirs();
            log.info("创建头像存储目录: {}, 结果: {}", destFile.getParentFile().getPath(), created);
        }

        // 存入本地
        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            log.error("物理文件保存失败！用户ID: {}, 目标路径: {}", userId, fullSavePath, e);
            throw new BusinessException(ResultCodeEnum.FILE_UPLOAD_ERROR);
        }

        // 拼接网络访问 URL 并存到数据库
        String accessUrl = "/images/avatars/" + imageName;
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setAvatarUrl(accessUrl);

        updateById(updateUser);
        log.info("头像更新成功，用户ID: {}, 访问路径: {}", userId, accessUrl);

        return accessUrl;
    }

    @Override
    public AdminUserInfoVO getUserDetailByAdmin(Long userId) {
        // 1. 查出用户基础信息
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("该用户不存在或已被注销");
        }

        if (user.getRole().equals(RoleEnum.ADMIN.getCode())) {
            throw new BusinessException(ResultCodeEnum.FORBIDDEN,"该用户同为管理员，无法查看敏感信息！");
        }

        // 2. 转换基础数据
        AdminUserInfoVO vo = new AdminUserInfoVO();
        BeanUtil.copyProperties(user, vo);

        // 3. 动态统计：违规发帖次数 (查询 item_info 表中，该用户 status = 3 的帖子数量)
        long violationCount = itemInfoMapper.selectCount(
                new LambdaQueryWrapper<ItemInfo>()
                        .eq(ItemInfo::getUserId, userId)
                        .eq(ItemInfo::getStatus, ItemStatusEnum.BANNED.getCode())
        );
        vo.setViolationCount((int) violationCount);

        log.info("管理员查询用户档案成功，用户ID: {}, 违规次数: {}", userId, violationCount);
        return vo;
    }

    @Override
    public IPage<User> pageQueryUser(AdminUserPageDTO dto) {
        //todo 虽然User会返回一个没必要的身份，懒得再弄一个vo了，之后有需要再说吧！

        // 1. 创建分页对象
        Page<User> page = new Page<>(dto.getPage(), dto.getPageSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        //管理员查不到管理员
        wrapper.ne(User::getRole, RoleEnum.ADMIN.getCode());

        // 动态条件 1：按状态精确筛选 (如果传了的话)
        wrapper.eq(dto.getStatus() != null, User::getStatus, dto.getStatus());

        // 动态条件 2：按关键字模糊搜索 (嵌套 OR)
        if (StrUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w.like(User::getUsername, dto.getKeyword())
                    .or()
                    .like(User::getNickname, dto.getKeyword())
                    .or()
                    .like(User::getPhone, dto.getKeyword()));
        }

        // 3. 排序：通常按注册时间倒序排列，新注册的用户在最前面
        wrapper.orderByDesc(User::getCreateTime);

        // 4. 执行查询并返回结果
        return this.page(page, wrapper);
    }

    // --- 私有辅助方法 ---

    private LambdaQueryWrapper<User> getUserLambdaQueryWrapper(String username, String email, String phone) {
        return new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .or().eq(User::getEmail, email)
                .or().eq(User::getPhone, phone);
    }

    private LambdaQueryWrapper<User> getUserLambdaQueryWrapper(String account) {
        return new LambdaQueryWrapper<User>()
                .eq(User::getUsername, account)
                .or().eq(User::getEmail, account)
                .or().eq(User::getPhone, account);
    }

    private String createNickname() {
        return Constant.NICKNAME_PREFIX + RandomUtil.randomNumbers(6);
    }
}