package com.atguigu.tingshu.account.receiver;

import com.atguigu.tingshu.account.service.UserAccountService;
import com.atguigu.tingshu.common.rabbit.constant.MqConst;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/*
 *@auther:zhushuai
 *@verson 1.0
 *@2026/6/2 12:38
 */
@Component
@Slf4j
public class AccReceiver {

    @Resource
    private UserAccountService userAccountService;


    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(value = MqConst.EXCHANGE_USER, durable = "true"),
            value = @Queue(value = MqConst.QUEUE_USER_REGISTER, durable = "true"),
            key = {MqConst.ROUTING_USER_REGISTER}
    ))
    public void addUserAccount(Long userId, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            if (null == userId) {
                log.error("userId 为空，消息无法处理，丢弃");
                channel.basicReject(deliveryTag, false);
                return;
            }
            log.info("注册成功初始化用户账户信息：{}", userId);
            userAccountService.addUserAccount(userId);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("用户账户初始化失败，userId: {}，消息将重新入队", userId, e);
            channel.basicNack(deliveryTag, false, true);
        }
    }


    // todo 手动消息的问题

}
