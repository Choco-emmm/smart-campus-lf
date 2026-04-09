package com.choco.smartlf.service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.choco.smartlf.entity.dto.*;
import com.choco.smartlf.entity.pojo.User;
import com.choco.smartlf.entity.vo.AdminUserInfoVO;
import com.choco.smartlf.entity.vo.UserInfoVO;
import com.choco.smartlf.entity.vo.UserLoginVO;
import com.choco.smartlf.entity.vo.UserProfileVO;
import com.choco.smartlf.enums.CheckType;
import org.springframework.web.multipart.MultipartFile;

/**
* @author renpe
* @description 针对表【user(用户核心表)】的数据库操作Service
* @createDate 2026-04-06 14:52:42
*/
public interface UserService extends IService<User> {

    void registerUser(UserRegisterDTO dto);

    UserLoginVO login(UserLoginDTO dto);

    boolean isExist(CheckType type, String value);

    UserInfoVO getUserInfo(Long userId);

    void updateUserInfo(Long userId, UserUpdateDTO dto);

    void updatePassword(Long userId, UpdatePasswordDTO dto);

    boolean isSame(Long userId, String value);

    String updateAvatar(Long userId, MultipartFile file);

    AdminUserInfoVO getUserDetailByAdmin(Long userId);

    IPage<User> pageQueryUserByAdmin(AdminUserPageDTO dto);

    void updateUserStatusByAdmin(Long userId, Integer status);

    UserProfileVO getUserProfile(Long userId);
}
