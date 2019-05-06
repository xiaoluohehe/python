package com.zzwc.cms.admin.modules.system.controller;


import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.zzwc.cms.admin.modules.system.entity.SysLogEntity;
import com.zzwc.cms.admin.modules.system.service.SysLogService;
import com.zzwc.cms.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * <p>
 * 系统日志 前端控制器
 * </p>
 *
 * @author weirdor
 */
@Controller
@RequestMapping("/sys/log")
public class SysLogController extends AbstractController {
    @Autowired
    private SysLogService sysLogService;

    /**
     * 列表
     */
    @ResponseBody
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        EntityWrapper<SysLogEntity> wrapper = new EntityWrapper<SysLogEntity>();
        if (params.containsKey("key")){
            wrapper.like("username",params.get("key").toString()).or().like("operation",params.get("key").toString());
        }
        Page<SysLogEntity> pageUtil=sysLogService.selectPage(new Page<SysLogEntity>(Convert.toInt(params.get("page")),Convert.toInt(params.get("rows"))),wrapper);
        return R.ok().put("page", pageUtil);
    }

}
