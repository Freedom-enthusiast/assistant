package org.example.consultant.controller;

import dev.langchain4j.model.openai.OpenAiChatModel;
import org.example.consultant.aiservice.ConsultantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class ChatController {
//    @Autowired
//    private OpenAiChatModel model;
//    @RequestMapping("/chat")
//    public String chat(String message){//message为浏览器传递的用户信息
//        String result = model.chat(message);
//        return result;
//    }
    @Autowired//这里注入接口的代理对象而非接口本身,也不注入model对象，代理对象中封装了model对象
    private ConsultantService consultantService;
    @RequestMapping("/chat1")//阻塞式调用
    public String chat(String message){
        String result = consultantService.chat(message);
        return result;
    }

    @RequestMapping(value = "/chat2",produces = "text/html;charset=utf-8")//告诉浏览器返回的是html格式的数据，前端编码
    public Flux<String> streamChat(String message){//流式调用
        Flux<String> result = consultantService.streamChat(message);
        return result;
    }

    @RequestMapping(value = "/chat",produces = "text/html;charset=utf-8")//告诉浏览器返回的是html格式的数据，前端编码
    public Flux<String> streamChatIsolation(String memoryId,String message){//流式调用之会话隔离
        Flux<String> result = consultantService.streamChatIsolation(memoryId,message);
        return result;
    }
}
