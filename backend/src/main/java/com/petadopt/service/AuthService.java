package com.petadopt.service;

import com.petadopt.common.BusinessException;
import com.petadopt.dto.LoginRequest;
import com.petadopt.dto.PasswordRequest;
import com.petadopt.dto.RegisterRequest;
import com.petadopt.entity.User;
import com.petadopt.mapper.UserMapper;
import com.petadopt.security.AesUtil;
import com.petadopt.security.JwtUtil;
import com.petadopt.security.TokenBlacklistService;
import com.petadopt.security.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.petadopt.util.DateUtil;
import com.petadopt.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务（注册 / 登录 / 个人信息 / 改密 / 登出）
 * <p>
 * 这是整个系统的安全入口，负责用户的注册与登录、JWT 令牌签发、
 * 个人资料维护以及密码修改。涉及多个安全组件：
 * - PasswordEncoder：密码不可逆加密（BCrypt），数据库只存密文；
 * - AesUtil：对手机号等敏感信息做可逆加解密；
 * - JwtUtil：登录成功后签发 JWT 令牌；
 * - TokenBlacklistService：登出/改密时把旧令牌拉黑，使其立即失效。
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AesUtil aesUtil;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    /**
     * 用户注册
     * <p>
     * 流程：
     * 1. 校验用户名唯一，重复则抛异常；
     * 2. 密码用 BCrypt 加密后入库（明文不落库）；
     * 3. 昵称未填时默认用用户名；
     * 4. 手机号用 AES 加密入库，角色固定为 USER、状态为正常；
     * 5. 注册成功直接返回登录凭证（实现「注册即登录」）。
     *
     * @param request 注册请求参数
     * @return 登录凭证（含 token 与用户信息）
     */
    public LoginVO register(RegisterRequest request) {
        // 用户名唯一性校验
        if (userMapper.selectByUsername(request.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // 密码 BCrypt 加密
        // 昵称为空时回退为用户名
        user.setNickname(request.getNickname() != null && !request.getNickname().isEmpty()
                ? request.getNickname() : request.getUsername());
        user.setPhone(aesUtil.encrypt(request.getPhone())); // 手机号 AES 加密
        user.setEmail(request.getEmail());
        user.setRole("USER"); // 新用户默认普通用户
        user.setStatus(0);    // 状态 0 表示正常
        user.setCreateTime(DateUtil.now());
        userMapper.insert(user);
        return buildLoginVO(user);
    }

    /**
     * 用户登录
     * <p>
     * 流程：
     * 1. 按用户名查用户，查不到统一提示「用户名或密码错误」（避免暴露用户是否存在）；
     * 2. 检查账号是否被禁用；
     * 3. 用 BCrypt 比对密码；
     * 4. 全部通过后签发令牌返回。
     *
     * @param request 登录请求参数
     * @return 登录凭证
     */
    public LoginVO login(LoginRequest request) {
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        // status == 1 表示账号被禁用
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new BusinessException("账号已被禁用");
        }
        // 密码比对：matches 内部用 BCrypt 校验，而不是明文相等
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        return buildLoginVO(user);
    }

    /**
     * 查询当前登录用户信息
     * <p>
     * 从登录上下文取当前用户 id，查库后清空密码、解密手机号再返回。
     *
     * @return 用户实体
     */
    public User info() {
        Long userId = UserContext.userId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null); // 密码永不出后端
        user.setPhone(aesUtil.decrypt(user.getPhone())); // 解密手机号
        return user;
    }

    /**
     * 修改密码
     * <p>
     * 流程：
     * 1. 校验原密码是否正确；
     * 2. 新密码加密后更新；
     * 3. 吊销当前令牌，强制用户重新登录（安全策略）。
     *
     * @param request 改密请求参数
     */
    public void changePassword(PasswordRequest request) {
        Long userId = UserContext.userId();
        User user = userMapper.selectById(userId);
        // 原密码校验不通过则拒绝修改
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.update(user);
        // 修改密码后吊销当前令牌，强制重新登录
        revokeCurrentToken();
    }

    /**
     * 退出登录：把当前令牌加入黑名单，使其立即失效
     */
    public void logout() {
        revokeCurrentToken();
    }

    /**
     * 把当前令牌的 jti 写入黑名单，TTL 对齐令牌剩余有效期
     * <p>
     * JWT 是无状态的，服务端默认无法主动让某令牌失效，
     * 所以采用「黑名单」方案：记录令牌唯一标识 jti，并在其剩余有效期内拒绝它。
     */
    private void revokeCurrentToken() {
        // 计算令牌剩余有效时间（毫秒）
        long remain = UserContext.expiresAt() - System.currentTimeMillis();
        try {
            tokenBlacklistService.blacklist(UserContext.jti(), remain);
        } catch (Exception e) {
            // 黑名单写入失败只记日志，不影响主流程
            log.warn("令牌黑名单写入失败: {}", e.getMessage());
        }
    }

    /**
     * 更新个人资料
     * <p>
     * 只更新允许修改的字段，手机号需要重新 AES 加密后入库。
     *
     * @param request 用户资料（前端回传，只取非敏感可改字段）
     */
    public void updateProfile(User request) {
        Long userId = UserContext.userId();
        User user = new User();
        user.setId(userId);
        user.setNickname(request.getNickname());
        user.setRealName(request.getRealName());
        user.setPhone(aesUtil.encrypt(request.getPhone())); // 手机号加密
        user.setEmail(request.getEmail());
        user.setGender(request.getGender());
        user.setAge(request.getAge());
        user.setAddress(request.getAddress());
        user.setDescription(request.getDescription());
        user.setAvatar(request.getAvatar());
        userMapper.update(user);
    }

    /**
     * 组装登录返回对象
     * <p>
     * 生成 JWT 令牌，并清理用户敏感字段（密码置空、手机号解密）后返回。
     *
     * @param user 用户实体
     * @return 登录凭证
     */
    private LoginVO buildLoginVO(User user) {
        String token = generateToken(user);
        user.setPassword(null); // 清除密码
        user.setPhone(aesUtil.decrypt(user.getPhone())); // 解密手机号，方便前端直接展示
        return new LoginVO(token, user);
    }

    /**
     * 生成 JWT 令牌
     * <p>
     * 把用户 id、用户名、角色放入 claims（令牌载荷），由 JwtUtil 签名生成 token。
     *
     * @param user 用户实体
     * @return JWT 令牌字符串
     */
    private String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", String.valueOf(user.getId()));
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());
        return jwtUtil.generateToken(claims);
    }
}
