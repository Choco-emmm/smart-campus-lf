package com.choco.smartlf.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.choco.smartlf.entity.dto.UserLoginDTO;
import com.choco.smartlf.entity.dto.UserRegisterDTO;
import com.choco.smartlf.entity.pojo.User;
import com.choco.smartlf.entity.vo.UserLoginVO;
import com.choco.smartlf.enums.CheckType;

/**
* @author renpe
* @description 针对表【user(用户核心表)】的数据库操作Service
* @createDate 2026-04-06 14:52:42
*/
public interface UserService extends IService<User> {

    void registerUser(UserRegisterDTO dto);

    UserLoginVO login(UserLoginDTO dto);

    boolean checkUnique(CheckType type, String value);
}
