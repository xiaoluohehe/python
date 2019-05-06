package com.zzwc.cms.admin.modules.system.service;

import com.baomidou.mybatisplus.service.IService;
import com.zzwc.cms.admin.modules.system.entity.SysUserTokenEntity;
import com.zzwc.cms.common.utils.R;

/**
 * 用户Token
 *
 */
public interface SysUserTokenService extends IService<SysUserTokenEntity> {

	/**
	 * 生成token
	 * @param userId  用户ID
	 */
	R createToken(long userId);

	/**
	 * 退出，修改token值
	 * @param userId  用户ID
	 */
	void logout(long userId);

}
