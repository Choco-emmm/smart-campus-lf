package service.impl;

import entity.po.User;
import mapper.UserMapper;
import service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户核心表 服务实现类
 * </p>
 *
 * @author 
 * @since 2026-04-06
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
