package com.zzwc.cms.admin.modules.system.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.zzwc.cms.admin.modules.system.dao.SysLogDao;
import com.zzwc.cms.admin.modules.system.entity.SysLogEntity;
import com.zzwc.cms.admin.modules.system.service.SysLogService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 系统日志 服务实现类
 * </p>
 *
 * @author weirdor
 */
@Service("sysLogService")
public class SysLogServiceImpl extends ServiceImpl<SysLogDao, SysLogEntity> implements SysLogService {
	
}
