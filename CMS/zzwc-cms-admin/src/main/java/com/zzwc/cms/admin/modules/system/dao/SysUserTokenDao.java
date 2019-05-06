package com.zzwc.cms.admin.modules.system.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.zzwc.cms.admin.modules.system.entity.SysUserTokenEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户Token
 *
 */
@Mapper
public interface SysUserTokenDao extends BaseMapper<SysUserTokenEntity> {

    SysUserTokenEntity queryByToken(String token);
	
}
