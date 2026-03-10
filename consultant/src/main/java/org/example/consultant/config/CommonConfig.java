package org.example.consultant.config;

import dev.langchain4j.community.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.example.consultant.aiservice.ConsultantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration//配置类
public class CommonConfig {
    @Autowired
    private OpenAiChatModel model;//将模型注入进来
    @Autowired
    private ChatMemoryStore RedisChatMemoryStore;//使用重写后的ChatMemoryStore对象

    @Autowired
    private EmbeddingModel myembeddingModel;//向量模型的作用是把分割后的文本片段向量化或者把用户消息向量化

    @Autowired
    private RedisEmbeddingStore redisembeddingStore;//创建一个向量数据库对象,使用redisearch创建向量数据库

//    @Bean
//    public ConsultantService consultantService(){
//        //使用AiServices为ConsultantService接口创建动态代理对象
//        ConsultantService consultantService = AiServices.builder(ConsultantService.class)//创建哪个接口的代理对象就传入哪个接口的类型
//                .chatModel(model)//指定聊天使用的model对象
//                .build();
//        return consultantService;
//    }

    //构建会话记忆对象
    @Bean
    public ChatMemory chatMemory(){
        MessageWindowChatMemory message = MessageWindowChatMemory.builder()
                .maxMessages(20)//最多保存20条消息
                .build();
        return message;
    }

    /**构建chatMemoryProvider对象
     * 如果langchain4j如果从容器中没找到指定id的对象，则从ChatMemoryProvider对象的get方法中获取新的ChatMemory对象
     * @return
     */
    @Bean
    public ChatMemoryProvider chatMemoryProvider(){
        ChatMemoryProvider provider = new ChatMemoryProvider() {//采用匿名类的方式为接口创建一个实现对象
            @Override
            public ChatMemory get(Object memoryId) {//根据参数memoryId获取新的ChatMemory对象
                return MessageWindowChatMemory.builder()
                        .id(memoryId)//将memoryId设置给会话记忆对象
                        .maxMessages(20)//最大会话记录存储数
                        .chatMemoryStore(RedisChatMemoryStore)//使用我们自己设置好的会话记忆对象
                        .build();
            }
        };
        return provider;
    }
    //构建向量数据库操作对象
//    @Bean  //第一次启动时会执行该方法,创建一个向量数据库对象,向量数据库对象会保存在redis中,但是后续启动时可以不用执行该方法,因为向量数据库对象已经存在数据了,可以将bean注释掉
    public EmbeddingStore embeddingStore(){
        //1.加载文档进内存
//        List<Document> document = ClassPathDocumentLoader.loadDocuments("content");//类加载器加载一些文档
        List<Document> document = ClassPathDocumentLoader.loadDocuments("content",new ApachePdfBoxDocumentParser());//类加载器加载一些文档,使用阿帕奇pdf解析器
//        List<Document> document = FileSystemDocumentLoader.loadDocuments("D:\\Desktop\\javaWeb\\consultant\\src\\main\\resources\\content");//根据本地磁盘绝对路径加载一些文档
        //2.创建向量数据库操作对象  操作的是内存版本的向量数据库
        InMemoryEmbeddingStore embeddingStore = new InMemoryEmbeddingStore();//操作内存版本的向量数据库,目前还没有数据

        //构建文本分割器对象
        DocumentSplitter documentSplitter = DocumentSplitters.recursive(500, 100);//创建文本切割器对象,500表示每个文本块的长度,100表示文本块的重叠

        //3.将加载到内存里面的文档中的内容,进行文本切割,向量化后存储在向量数据库中  
        //构建一个EmbeddingStoreIngestor对象
        EmbeddingStoreIngestor ingestor =  EmbeddingStoreIngestor.builder()
//                .embeddingStore(embeddingStore)//向量化的数据存储在embeddingStore中
                .embeddingStore(redisembeddingStore)//向量化的数据存储在redis向量数据库中
                .documentSplitter(documentSplitter)//文档分割器
                .embeddingModel(myembeddingModel)//向量模型对象
                .build();
        ingestor.ingest(document);//对象构建完毕后,将文档进行向量化存储
//        return embeddingStore;//返回原本的默认的向量数据库对象
        return redisembeddingStore;//返回我们自己设置的redis向量数据库对象
    }

    //构建向量数据库检索对象
    @Bean
    public ContentRetriever contentRetriever(EmbeddingStore embeddingStore){
        //构建ContentRetriever对象
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
//                .embeddingStore(embeddingStore)//指定向量数据库对象
                .embeddingStore(redisembeddingStore)//指定redis向量数据库对象
                .minScore(0.7)//指定向量数据库中匹配的分数阈值
                .maxResults(3)//指定向量数据库中匹配的记录数
                .embeddingModel(myembeddingModel)//向量模型对象
                .build();
        return contentRetriever;
    }

}
