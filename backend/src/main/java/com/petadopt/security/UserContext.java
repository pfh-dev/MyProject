package com.petadopt.security;

/**
 * 线程隔离上下文，基于 ThreadLocal 保存当前登录用户。
 *
 * <p>ThreadLocal 是什么：它为每个线程提供一个独立的"小储物柜"，
 * 同一个线程里存进去的值只有这个线程自己能读到，别的线程读不到。
 * 因为每个 HTTP 请求都由一个独立线程处理，所以把用户信息放进 ThreadLocal，
 * 就能实现"本次请求内全局可拿、请求之间互不串扰"的效果。</p>
 *
 * <p>用法：登录拦截器先 {@link #set(LoginUser)} 写入，之后任何 Service、Controller
 * 都能通过 {@link #userId()}、{@link #role()} 等直接拿到当前用户，无需层层传参。</p>
 */
public class UserContext {

    // 存放当前线程用户信息的 ThreadLocal。注意它是 static 的，
    // 但每个线程存取的都是各自的副本，所以天然线程安全。
    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    /**
     * 把当前登录用户绑定到当前线程。
     */
    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    /**
     * 获取当前线程的登录用户对象（可能为 null，表示未登录）。
     */
    public static LoginUser get() {
        return HOLDER.get();
    }

    /**
     * 获取当前登录用户 id，未登录返回 null。
     */
    public static Long userId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getId();
    }

    /**
     * 获取当前登录用户名，未登录返回 null。
     */
    public static String username() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getUsername();
    }

    /**
     * 获取当前登录用户角色，未登录返回 null。
     */
    public static String role() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getRole();
    }

    /**
     * 获取当前令牌的唯一 id（jti），未登录返回 null。
     */
    public static String jti() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getJti();
    }

    /**
     * 获取当前令牌过期时间（毫秒时间戳），未登录返回 0。
     */
    public static long expiresAt() {
        LoginUser user = HOLDER.get();
        return user == null ? 0L : user.getExpiresAt();
    }

    /**
     * 判断当前用户是否拥有指定角色。
     *
     * @param role 目标角色，如 "ADMIN"
     * @return true 表示角色匹配
     */
    public static boolean hasRole(String role) {
        LoginUser user = HOLDER.get();
        return user != null && role.equals(user.getRole());
    }

    /**
     * 清理当前线程的数据。
     *
     * <p>必须在请求结束时调用（拦截器 afterCompletion 里），
     * 否则线程池复用线程会导致下一个请求读到上一个用户的残留信息，造成严重的数据串号问题。</p>
     */
    public static void remove() {
        HOLDER.remove();
    }
}
