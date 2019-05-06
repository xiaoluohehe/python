package com.zzwc.cms.admin.modules.system.service;

import com.baomidou.mybatisplus.service.IService;
import com.zzwc.cms.admin.modules.system.entity.SysRoleEntity;
import com.zzwc.cms.common.utils.PageUtils;

import java.util.List;
import java.util.Map;


/**
 * 角色
 *
 */
public interface SysRoleService extends IService<SysRoleEntity> {

	PageUtils queryPage(Map<String, Object> params);

	void save(SysRoleEntity role);

	void update(SysRoleEntity role);

	void deleteBatch(Long[] roleIds);

	
	/**
	 * 查询用户创建的角色ID列表
	 */
	List<Long> queryRoleIdList(Long createUserId);
}
