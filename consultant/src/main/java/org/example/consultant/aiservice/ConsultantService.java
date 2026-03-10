package org.example.consultant.aiservice;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

@AiService(//该注解可以为下面的接口自动创建代理对象，不用手动创建代理对象，创建代理对象时，会自动将model对象注入到代理对象中，使用时会创建代理对象，并且注入到容器中
        wiringMode = AiServiceWiringMode.EXPLICIT,//代表将来AiService创建代理对象时手动装配还是自动装配，EXPLICIT：手动装配，AUTOMATTC：自动装配
        chatModel = "openAiChatModel",//langchain4j往IOC容器中注入的模型对象名字，首字母小写
        streamingChatModel = "openAiStreamingChatModel",//流式调用模型对象名字
//        chatMemory = "chatMemory"//配置会话记忆对象，相当于共有的会话记忆对象，那么会话记忆也共享
        chatMemoryProvider = "chatMemoryProvider",//配置会话记忆提供者对象
        contentRetriever = "contentRetriever",//配置向量数据库检索对象
        tools = "reservationTool"//配置工具对象,类名小写
)
//@AiService  默认参数
public interface ConsultantService {
    //用于聊天的方法
//    @SystemMessage("你是学习小助手,你叫小明")
//    @UserMessage("你是小月月，人美心善{{it}}")
//    @UserMessage("你是小月月，人美心善!{{it}}")
    @UserMessage("你是小月月，{{msg}}")
    public String chat(@V("msg") String message);

    //流式调用
    @SystemMessage(fromResource = "system.txt")//基于工程的类路径找文件
    public Flux<String> streamChat(String message);

    //流式调用之会话隔离
    @SystemMessage(fromResource = "system.txt")//基于工程的类路径找文件
    public Flux<String> streamChatIsolation(@MemoryId String memoryId, @UserMessage String message);//@MemoryId告诉langchain4j，这个参数是会话记忆的id

}
