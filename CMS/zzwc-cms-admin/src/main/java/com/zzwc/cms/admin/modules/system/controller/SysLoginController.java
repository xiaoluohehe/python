package com.zzwc.cms.admin.modules.system.controller;

import com.zzwc.cms.admin.modules.system.entity.SysUserEntity;
import com.zzwc.cms.admin.modules.system.service.SysUserService;
import com.zzwc.cms.admin.modules.system.service.SysUserTokenService;
import com.zzwc.cms.admin.modules.system.utils.ShiroUtils;
import com.zzwc.cms.common.utils.CaptchaUtil;
import com.zzwc.cms.common.utils.R;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 登录相关
 *
 */
@RestController
public class SysLoginController extends  AbstractController{
	@Autowired
	private SysUserService sysUserService;
	@Autowired
	private SysUserTokenService sysUserTokenService;
	/**
	 * 登录
	 */
	@RequestMapping(value = "/sys/login", method = RequestMethod.POST)
	public Map<String, Object> login( @RequestParam(value = "username")  String username,
									 @RequestParam(value = "password") String password) {
        //用户信息
		SysUserEntity user = sysUserService.queryByUserName(username);

		//账号不存在、密码错误
		if(user == null || !user.getPassword().equals(new Sha256Hash(password, user.getSalt()).toHex())) {
			return R.error("账号或密码不正确");
		}

		//账号锁定
		if(user.getStatus() == 0){
			return R.error("账号已被锁定,请联系管理员");
		}

		//生成token，并保存到数据库
		R r = sysUserTokenService.createToken(user.getUserId());
		return r;
	}

	/**
	 * 登录页面验证码
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/captcha", method = RequestMethod.GET)
	public void verify( HttpServletResponse response ,String t) {
		try {
			String verifyCode = CaptchaUtil.outputImage(response.getOutputStream());
			ShiroUtils.setSessionAttribute("verify", verifyCode);
			System.out.println(ShiroUtils.getKaptcha("verify"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * 退出
	 */
	@PostMapping("/sys/logout")
	public R logout() {
		sysUserTokenService.logout(getUserId());
		return R.ok();
	}
	
}
