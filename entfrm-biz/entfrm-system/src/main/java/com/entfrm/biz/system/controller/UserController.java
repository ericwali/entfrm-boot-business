package com.entfrm.biz.system.controller;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.entfrm.base.api.R;
import com.entfrm.base.config.GlobalConfig;
import com.entfrm.base.constant.CommonConstants;
import com.entfrm.base.constant.SqlConstants;
import com.entfrm.base.util.ExcelUtil;
import com.entfrm.base.util.UploadUtil;
import com.entfrm.biz.system.entity.Role;
import com.entfrm.biz.system.entity.User;
import com.entfrm.biz.system.entity.UserRole;
import com.entfrm.biz.system.service.RoleService;
import com.entfrm.biz.system.service.UserRoleService;
import com.entfrm.biz.system.service.UserService;
import com.entfrm.biz.system.vo.ResultVo;
import com.entfrm.biz.system.vo.UserVo;
import com.entfrm.log.annotation.OperLog;
import com.entfrm.security.entity.EntfrmUser;
import com.entfrm.security.util.SecurityUtil;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户信息
 *
 * @author entfrm
 */
@RestController
@RequestMapping("/system/user")
@AllArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRoleService userRoleService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    private QueryWrapper<User> getQueryWrapper(User user) {
        return new QueryWrapper<User>().like(StrUtil.isNotBlank(user.getUserName()), "user_name", user.getUserName()).like(StrUtil.isNotBlank(user.getNickName()), "nick_name", user.getNickName()).eq(StrUtil.isNotBlank(user.getStatus()), "status", user.getStatus())
                .eq(ObjectUtil.isNotNull(user.getDeptId()), "dept_id", user.getDeptId())
                .between(StrUtil.isNotBlank(user.getBeginTime()) && StrUtil.isNotBlank(user.getEndTime()), "create_time", user.getBeginTime(), user.getEndTime()).apply(StrUtil.isNotBlank(user.getSqlFilter()), user.getSqlFilter());
    }

    @GetMapping("/list")
    public R list(Page page, User user)throws Exception{
        IPage<User> userIPage = userService.page(page, getQueryWrapper(user));
        return R.ok(userIPage.getRecords(), userIPage.getTotal());
    }

    @PreAuthorize("@ps.hasPerm('user_view')")
    @GetMapping("/userList")
    public R userList(User user) {
        List<UserVo> userList = userService.list(getQueryWrapper(user)).stream().map(userInfo -> {
            UserVo userVo = new UserVo();
            userVo.setId(userInfo.getId());
            userVo.setNickName(userInfo.getNickName());
            return userVo;
        }).collect(Collectors.toList());
        return R.ok(userList);
    }

    @GetMapping("/{id}")
    public R getById(@PathVariable("id") Integer id) {
        User user = userService.getById(id);
        List<Integer> roles = new ArrayList<>();
        List<Role> roleList = roleService.list();
        if (user != null) {
            roles = userRoleService.list(new QueryWrapper<UserRole>().eq("user_id", user.getId()))
                    .stream().map(userRole -> userRole.getRoleId()).collect(Collectors.toList());

            user.setRoles(ArrayUtil.toArray(roles, Integer.class));
        }
        return R.ok(ResultVo.builder().result(user).extend(roleList).build());
    }

    @GetMapping("getByIds/{ids}")
    public R getByIds(@PathVariable Integer[] ids) {
        List<User> users = userService.listByIds(Arrays.asList(ids));
        return R.ok(users);
    }

    /**
     * 获取当前用户全部信息
     *
     * @return 用户信息
     */
    @GetMapping("/info")
    public R info() {
        User user = userService.getOne(Wrappers.<User>query()
                .lambda().eq(User::getUserName, SecurityUtil.getUser().getUsername()));
        if (user == null) {
            return R.error("获取当前用户信息失败");
        }

        List<String> roles = SecurityUtil.getRoles()
                .stream().map(roleId -> CommonConstants.ROLE + roleId).collect(Collectors.toList());
        Set<String> permissions = new HashSet<>();
        SecurityUtil.getRoles().forEach(roleId -> {
            permissions.addAll(jdbcTemplate.query(SqlConstants.QUERY_PREMS, new Object[]{roleId.toString()}, new RowMapper<String>() {
                @Override
                public String mapRow(ResultSet rs, int i)
                        throws SQLException {
                    return rs.getString(1);
                }
            }));
        });
        user.setRoleList(ArrayUtil.toArray(roles, String.class));
        user.setPermissions(ArrayUtil.toArray(permissions, String.class));
        return R.ok(user);
    }

    @OperLog("用户新增")
    @PreAuthorize("@ps.hasPerm('user_add')")
    @PostMapping("/save")
    public R save(@RequestBody User user) {
        if (!StrUtil.isEmptyIfStr(user.getId()) && User.isAdmin(user.getId())) {
            return R.error("不允许修改超级管理员");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.saveUser(user);
        return R.ok();
    }

    @OperLog("用户修改")
    @PreAuthorize("@ps.hasPerm('user_edit')")
    @PutMapping("/update")
    public R update(@RequestBody User user) {
        userService.saveUser(user);
        return R.ok();
    }

    @OperLog("用户删除")
    @PreAuthorize("@ps.hasPerm('user_del')")
    @DeleteMapping("/remove/{id}")
    public R remove(@PathVariable Integer[] id) {
        if (ArrayUtil.contains(id, 1)) {
            return R.error("不允许删除超级管理员");
        }
        userService.removeByIds(Arrays.asList(id));
        return R.ok();
    }

    @GetMapping("/profile")
    public R profile() {
        EntfrmUser entfrmUser = SecurityUtil.getUser();
        if (entfrmUser != null) {
            User user = userService.getById(entfrmUser.getId() + "");
            if (user != null) {
                String roleNames = SecurityUtil.getRoles().stream().map(roleId -> roleService.getById(roleId + "").getName())
                        .collect(Collectors.joining(","));
                user.setRoleNames(roleNames);
                user.setPassword(null);
            }
            return R.ok(user);
        } else {
            return R.error("登录信息已过期，请重新登录");
        }
    }

    @OperLog("用户信息修改")
    @PreAuthorize("@ps.hasPerm('user_edit')")
    @PutMapping("/updateProfile")
    public R updateProfile(@RequestBody User user) {
        userService.update(new UpdateWrapper<User>().eq("id", user.getId()).set("nick_name", user.getNickName()).set(StrUtil.isNotBlank(user.getPhone()), "phone", user.getPhone()).set("email", user.getEmail()).set("sex", user.getSex()));
        return R.ok();
    }

    @OperLog("用户头像修改")
    @PreAuthorize("@ps.hasPerm('user_edit')")
    @PutMapping("/updateAvatar")
    public R updateAvatar(@RequestParam("avatarfile") MultipartFile file, HttpServletRequest request) {
        String avatar = "/profile/avatar/" + UploadUtil.fileUp(file, GlobalConfig.getAvatarPath(), "avatar" + new Date().getTime());
        userService.update(new UpdateWrapper<User>().eq("id", SecurityUtil.getUser().getId()).set("avatar", avatar));
        return R.ok(avatar);
    }

    @OperLog("用户密码修改")
    @PreAuthorize("@ps.hasPerm('user_edit')")
    @PutMapping("/updatePwd")
    public R updatePwd(User user) {
        User user1 = userService.getById(SecurityUtil.getUser().getId());
        if (user1 != null && passwordEncoder.matches(user.getPassword(), user1.getPassword())) {
            userService.update(new UpdateWrapper<User>().eq("id", user1.getId()).set("password", passwordEncoder.encode(user.getNewPassword())));
            return R.ok();
        } else {
            return R.error("原密码有误，请重试");
        }
    }

    @OperLog("用户密码重置")
    @PreAuthorize("@ps.hasPerm('user_reset')")
    @PutMapping("/resetPwd")
    public R resetPwd(@RequestBody User user) {
        userService.update(new UpdateWrapper<User>().eq("id", user.getId()).set("password", passwordEncoder.encode(user.getPassword())));
        return R.ok();
    }

    @OperLog("用户状态更改")
    @PreAuthorize("@ps.hasPerm('user_edit')")
    @PutMapping("/changeStatus")
    public R changeStatus(@RequestBody User user) {
        if (User.isAdmin(user.getId())) {
            return R.error("不允许修改超级管理员用户");
        }
        userService.update(new UpdateWrapper<User>().eq("id", user.getId()).set("status", user.getStatus()));
        return R.ok();
    }

    @SneakyThrows
    @OperLog("用户数据导出")
    @PreAuthorize("@ps.hasPerm('user_export')")
    @GetMapping("/exportUser")
    public R exportUser(User user) {
        List<User> list = userService.list(getQueryWrapper(user));
        ExcelUtil<User> util = new ExcelUtil<User>(User.class);
        return util.exportExcel(list, "用户数据");
    }

    @SneakyThrows
    @OperLog("用户数据导入")
    @PreAuthorize("@ps.hasPerm('user_import')")
    @PostMapping("/importUser")
    public R importUser(MultipartFile file, boolean updateSupport) {
        ExcelUtil<User> util = new ExcelUtil<User>(User.class);
        List<User> userList = util.importExcel(file.getInputStream());
        String message = userService.importUser(userList, updateSupport);
        return R.ok(message);
    }

    @GetMapping("/importTemplate")
    public R importTemplate() {
        ExcelUtil<User> util = new ExcelUtil<User>(User.class);
        return util.importTemplateExcel("用户数据");
    }
}
