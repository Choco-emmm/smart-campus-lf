package service.impl;

import entity.po.UserActiveLog;
import mapper.UserActiveLogMapper;
import service.IUserActiveLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户活跃流水表 服务实现类
 * </p>
 *
 * @author 
 * @since 2026-04-06
 */
@Service
public class UserActiveLogServiceImpl extends ServiceImpl<UserActiveLogMapper, UserActiveLog> implements IUserActiveLogService {

}
