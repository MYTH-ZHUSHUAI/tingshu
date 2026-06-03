package com.atguigu.tingshu.user.api;

import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.user.service.UserInfoService;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户管理接口")
@RestController
@RequestMapping("api/user/userInfo")
public class UserInfoApiController {

	@Resource
	private UserInfoService userInfoService;

	@GetMapping("getUserInfoVo/{userId}")
	public Result<UserInfoVo> getUserInfoVo(@PathVariable Long userId) {
		UserInfoVo userInfoVo = userInfoService.getUserInfoVo(userId);
		return Result.ok(userInfoVo);
	}

}

