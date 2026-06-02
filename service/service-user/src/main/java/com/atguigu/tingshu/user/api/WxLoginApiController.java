package com.atguigu.tingshu.user.api;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.common.rabbit.constant.MqConst;
import com.atguigu.tingshu.common.rabbit.service.RabbitService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.user.service.UserInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Tag(name = "微信授权登录接口")
@RestController
@RequestMapping("/api/user/wxLogin")
@Slf4j
public class WxLoginApiController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RabbitService rabbitService;

    @Resource
    private WxMaService wxMaService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    @GetMapping("/wxLogin/{code}")
    public Result<Map> wxLogin(@PathVariable String code) throws WxErrorException {

        // 获取openid
        WxMaJscode2SessionResult sessionInfo = wxMaService.getUserService().getSessionInfo(code);
        String openId = sessionInfo.getOpenid();


        // 判断用户是否已经存在
        UserInfo userInfo = userInfoService.getOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getWxOpenId, openId));

        // 如果数据库中没有这个对象，则在数据库中新建对象
        if (null == userInfo) {
            //  创建对象
            userInfo = new UserInfo();
            //  赋值用户昵称
            userInfo.setNickname("听友" + System.currentTimeMillis());
            //  赋值用户头像图片
            userInfo.setAvatarUrl("https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
            //  赋值wxOpenId
            userInfo.setWxOpenId(openId);
            //  保存用户信息
            userInfoService.save(userInfo);
            //  初始化账户信息
            rabbitService.sendMessage(
                    MqConst.EXCHANGE_USER,
                    MqConst.ROUTING_USER_REGISTER,
                    userInfo.getId());
        }


        // 删除该用户旧token，防止重复登录导致Redis膨胀
        String tokenMapKey = RedisConstant.USER_LOGIN_TOKEN_KEY_PREFIX + userInfo.getId();
        String oldToken = (String) redisTemplate.opsForValue().get(tokenMapKey);
        if (oldToken != null) {
            redisTemplate.delete(RedisConstant.USER_LOGIN_KEY_PREFIX + oldToken);
        }

        String token = UUID.randomUUID().toString().replaceAll("-", "");
        // 存储 token → UserInfo
        redisTemplate.opsForValue().set(
                RedisConstant.USER_LOGIN_KEY_PREFIX + token,
                userInfo,
                RedisConstant.USER_LOGIN_KEY_TIMEOUT,
                TimeUnit.SECONDS);
        // 存储 userId → token 反向映射，用于下次登录时清理旧token
        redisTemplate.opsForValue().set(
                tokenMapKey,
                token,
                RedisConstant.USER_LOGIN_KEY_TIMEOUT,
                TimeUnit.SECONDS);

        HashMap<String, Object> map = new HashMap<>();
        map.put("token", token);

        return Result.ok(map);
    }
}
