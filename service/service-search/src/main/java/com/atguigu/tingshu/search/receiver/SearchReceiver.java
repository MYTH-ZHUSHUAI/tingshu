package com.atguigu.tingshu.search.receiver;

import com.atguigu.tingshu.common.rabbit.constant.MqConst;
import com.atguigu.tingshu.search.service.SearchService;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SearchReceiver {

    @Resource
    private SearchService searchService;

    // todo 异常处理 - 退避、死信


    /**
     * 专辑上架
     */
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(value = MqConst.EXCHANGE_ALBUM, durable = "true"),
            value = @Queue(value = MqConst.QUEUE_ALBUM_UPPER, durable = "true"),
            key = {MqConst.ROUTING_ALBUM_UPPER}
    ),concurrency = "8")
    public void upperGoods(Long albumId, Message message, Channel channel) throws Exception {

        try {
            if (null != albumId) {
                searchService.upperAlbum(albumId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }

    }

    /**
     * 专辑下架
     */
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(value = MqConst.EXCHANGE_ALBUM, durable = "true"),
            value = @Queue(value = MqConst.QUEUE_ALBUM_LOWER, durable = "true"),
            key = {MqConst.ROUTING_ALBUM_LOWER}
    ))
    public void lowerGoods(Long albumId, Message message, Channel channel) throws Exception {
        try {
            if (null != albumId) {
                searchService.lowerAlbum(albumId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }
}
