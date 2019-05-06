package com.zzwc.cms.admin.modules.system.controller;

import com.zzwc.cms.admin.modules.system.entity.SysUserEntity;
import com.zzwc.cms.admin.modules.system.form.PasswordForm;
import com.zzwc.cms.admin.modules.system.service.SysUserRoleService;
import com.zzwc.cms.admin.modules.system.service.SysUserService;
import com.zzwc.cms.common.annotation.SysLog;
import com.zzwc.cms.common.utils.Constant;
import com.zzwc.cms.common.utils.PageUtils;
import com.zzwc.cms.common.utils.R;
import com.zzwc.cms.common.bean.validator.Assert;
import com.zzwc.cms.common.bean.validator.ValidatorUtils;
import com.zzwc.cms.common.bean.validator.group.AddGroup;
import com.zzwc.cms.common.bean.validator.group.UpdateGroup;
import org.apache.commons.lang.ArrayUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统用户
 *
 */
@RestController
@RequestMapping("/sys/user")
public class SysUserController extends AbstractController {
	@Autowired
	private SysUserService sysUserService;
	@Autowired
	private SysUserRoleService sysUserRoleService;


	/**
	 * 所有用户列表
	 */
	@GetMapping("/list")
	public R list(@RequestParam Map<String, Object> params){
		//只有超级管理员，才能查看所有管理员列表
		if(getUserId() != Constant.SUPER_ADMIN){
			params.put("createUserId", getUserId());
		}
		PageUtils pageUtils = sysUserService.queryPage(params);

		return R.ok().put("page", pageUtils);
	}
	
	/**
	 * 获取登录的用户信息
	 */
	@GetMapping("/info")
	public R info(){
		return R.ok().put("user", getUser());
	}
	
	/**
	 * 修改登录用户密码
	 */
	@SysLog("修改密码")
	@PostMapping("/password")
	public R password(@RequestBody PasswordForm form){
		Assert.isBlank(form.getNewPassword(), "新密码不为能空");
		
		//sha256加密
		String password = new Sha256Hash(form.getPassword(), getUser().getSalt()).toHex();
		//sha256加密
		String newPassword = new Sha256Hash(form.getNewPassword(), getUser().getSalt()).toHex();
				
		//更新密码
		boolean flag = sysUserService.updatePassword(getUserId(), password, newPassword);
		if(!flag){
			return R.error("原密码不正确");
		}
		
		return R.ok();
	}
	
	/**
	 * 用户信息
	 */
	@GetMapping("/info/{userId}")
	public R info(@PathVariable("userId") Long userId){
		SysUserEntity user = sysUserService.selectById(userId);
		//获取用户所属的角色列表

		List<Long> roleIdList = sysUserRoleService.queryRoleIdList(userId);

		user.setRoleIdList(roleIdList);
		
		return R.ok().put("user", user);
	}
	
	/**
	 * 保存用户
	 */
	@PostMapping("/save")
	public R save(@RequestBody SysUserEntity user){

        System.out.println(user);

		ValidatorUtils.validateEntity(user, AddGroup.class);

		//获取用户id
		user.setCreateUserId(getUserId());

		sysUserService.save(user);
		
		return R.ok();
	}
	
	/**
	 * 修改用户
	 */
	@SysLog("修改用户")
	@RequestMapping("/update")
	@RequiresPermissions("sys:user:update")
	public R update(@RequestBody SysUserEntity user){
		ValidatorUtils.validateEntity(user, UpdateGroup.class);
		user.setCreateUserId(getUserId());
		sysUserService.update(user);
		
		return R.ok();
	}
	
	/**
	 * 删除用户
	 */
	@SysLog("删除用户")
	@RequestMapping("/delete")
	@RequiresPermissions("sys:user:delete")
	public R delete(@RequestBody Long[] userIds){
		if(ArrayUtils.contains(userIds, 1L)){
			return R.error("系统管理员不能删除");
		}
		
		if(ArrayUtils.contains(userIds, getUserId())){
			return R.error("当前用户不能删除");
		}
		
		sysUserService.deleteBatch(userIds);
		
		return R.ok();
	}
}
