package com.zzwc.cms.admin.modules.system.service.impl;

import com.zzwc.cms.admin.modules.system.dao.SysMenuDao;
import com.zzwc.cms.admin.modules.system.dao.SysUserDao;
import com.zzwc.cms.admin.modules.system.dao.SysUserTokenDao;
import com.zzwc.cms.admin.modules.system.entity.SysMenuEntity;
import com.zzwc.cms.admin.modules.system.entity.SysUserEntity;
import com.zzwc.cms.admin.modules.system.entity.SysUserTokenEntity;
import com.zzwc.cms.admin.modules.system.service.ShiroService;
import com.zzwc.cms.common.utils.Constant;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ShiroServiceImpl implements ShiroService {
    @Autowired
    private SysMenuDao sysMenuDao;
    @Autowired
    private SysUserDao sysUserDao;
    @Autowired
    private SysUserTokenDao sysUserTokenDao;

    @Override
    public Set<String> getUserPermissions(long userId) {
        List<String> permsList;

        //系统管理员，拥有最高权限
        if(userId == Constant.SUPER_ADMIN){
            List<SysMenuEntity> menuList = sysMenuDao.selectList(null);
            permsList = new ArrayList<>(menuList.size());
            for(SysMenuEntity menu : menuList){
                permsList.add(menu.getPerms());
            }
        }else{
            permsList = sysUserDao.queryAllPerms(userId);
        }
        //用户权限列表
        Set<String> permsSet = new HashSet<>();
        for(String perms : permsList){
            if(StringUtils.isBlank(perms)){
                continue;
            }
            permsSet.addAll(Arrays.asList(perms.trim().split(",")));
        }
        return permsSet;
    }

    @Override
    public SysUserTokenEntity queryByToken(String token) {
        return sysUserTokenDao.queryByToken(token);
    }

    @Override
    public SysUserEntity queryUser(Long userId) {
        return sysUserDao.selectById(userId);
    }
}
