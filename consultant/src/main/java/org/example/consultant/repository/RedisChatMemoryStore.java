package org.example.consultant.repository;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import java.time.Duration;
import java.util.List;
@Repository
/**
 * 相当于重写语言链4j的ChatMemoryStore接口,原先里面存储的会话消息是保存在内存中的，这里重写这个接口，将会话消息保存在redis中
 */
public class RedisChatMemoryStore implements ChatMemoryStore {
    @Autowired// 自动注入RedisTemplate
    private RedisTemplate redisTemplate;
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        //获取会话消息
        String json = (String) redisTemplate.opsForValue().get(memoryId);//根据memoryId获取redis中的数据
        //将json数据转成list  使用langchain4j的ChatMessageDeserializer反序列化器
        List<ChatMessage> list = ChatMessageDeserializer.messagesFromJson(json);
        return list;
    }
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        //更新会话消息
        //1.将list转成json数据  使用序列化
        String json = ChatMessageSerializer.messagesToJson(list);

        //2.把json数据保存到redis中  redis中数据的key为memoryId, value为json数据
        redisTemplate.opsForValue().set(memoryId,json, Duration.ofDays(2));//Duration.ofDays(2)保存2天
    }
    @Override
    public void deleteMessages(Object memoryId) {
        //删除会话消息
        redisTemplate.delete(memoryId.toString());
    }
}
