package com.petadopt.config;

import com.petadopt.entity.User;
import com.petadopt.mapper.UserMapper;
import com.petadopt.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 首次启动初始化管理员账号（admin / admin123）。
 *
 * <p>系统部署后如果没有管理员，就无法登录后台做任何管理操作。
 * 所以利用 {@link CommandLineRunner}（应用启动后自动执行一次）在启动时
 * 检查管理员是否存在，不存在就自动创建一个默认账号，保证系统"开箱即用"。</p>
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 应用启动后自动执行：确保存在默认管理员账号。
     */
    @Override
    public void run(String... args) {
        // 只有管理员不存在时才创建，避免每次重启重复插入
        if (userMapper.selectByUsername("admin") == null) {
            User admin = new User();
            admin.setUsername("admin");
            // 密码必须用 BCrypt 加密后存储，绝不能存明文
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNickname("管理员");
            admin.setRole("ADMIN");
            // status=0 表示正常启用状态（具体含义视项目约定）
            admin.setStatus(0);
            admin.setCreateTime(DateUtil.now());
            userMapper.insert(admin);
        }
    }
}
