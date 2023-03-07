package com.publicissapient.kpidashboard.jira.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {

    @Value("${rabbitmq.queue.name}")
    String queue;
    @Value("${rabbitmq.exchange.name}")
    String exchange;
    @Value("${rabbitmq.routing.key}")
    String routingKey;
    @Value("${rabbitmq.username}")
    private String username;
    @Value("${rabbitmq.password}")
    private String password;
    @Value("${rabbitmq.host}")
    private String host;
    @Value("${rabbitmq.virtualhost}")
    private String virtualHost;
    @Value("${rabbitmq.port}")
    private int port;

    private ObjectMapper mapper;

    @Bean
    public Queue queue() {
        return new Queue(queue);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

    @Bean
    public MessageConverter converter() {
        JodaModule module=new JodaModule();
//        module.addSerializer(Issue1.class,new EscapedJsonSerializer());
        ObjectMapper mapper = JsonMapper.builder().addModule(new JodaModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).build();
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setVirtualHost(virtualHost);
        connectionFactory.setHost(host);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setPort(port);
        return connectionFactory;
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public AmqpTemplate template() {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory());
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }

//    private ObjectMapper createObjectMapper() throws IOException {
//
//        String resultPath ="/issue.json";
//        if (mapper == null) {
//            mapper = new ObjectMapper();
//            mapper.registerModule(new JavaTimeModule());
//            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
//            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//            mapper.convertValue(issue, Map.class);
//        }
//
//		return mapper;
//    }

//    public class IssueFieldSerializer extends StdSerializer<IssueField> {
//
//        public IssueFieldSerializer() {
//            this(null);
//        }
//
//        public IssueFieldSerializer(Class<IssueField> t) {
//            super(t);
//        }
//
//        @Override
//        public void serialize(
//                IssueField issueField, JsonGenerator jgen, SerializerProvider provider)
//                throws IOException, JsonProcessingException {
//
//            jgen.writeStartObject();
//            jgen.writeStringField("id", issueField.getId());
//            jgen.writeStringField("name", issueField.getName());
//            jgen.writeStringField("type", issueField.getType());
//            jgen.writeObjectField("value",null);
//            jgen.writeEndObject();
//        }
//    }
//class EscapedJsonSerializer extends StdSerializer<Object> {
//    public EscapedJsonSerializer() {
//        super((Class<Object>) null);
//    }
//
//
//    @Override
//    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
//        StringWriter str = new StringWriter();
//        JsonGenerator tempGen = new JsonFactory().setCodec(gen.getCodec()).createGenerator(str);
//        if (value instanceof Collection || value.getClass().isArray()) {
//            tempGen.writeStartArray();
//            if (value instanceof Collection) {
//                for (Object it : (Collection) value) {
//                    writeTree(gen, it, tempGen);
//                }
//            } else if (value.getClass().isArray()) {
//                for (Object it : (Object[]) value) {
//                    writeTree(gen, it, tempGen);
//                }
//            }
//            tempGen.writeEndArray();
//        } else {
//            provider.defaultSerializeValue(value, tempGen);
//        }
//        tempGen.flush();
//        gen.writeString(str.toString());
//    }
//
//
//    @Override
//    public void serializeWithType(Object value, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
//        StringWriter str = new StringWriter();
//        JsonGenerator tempGen = new JsonFactory().setCodec(gen.getCodec()).createGenerator(str);
//        writeTree(gen, value, tempGen);
//        tempGen.flush();
//        gen.writeString(str.toString());
//    }
//
//    private void writeTree(JsonGenerator gen, Object it, JsonGenerator tempGen) throws IOException {
//        ObjectNode tree = ((ObjectMapper) gen.getCodec()).valueToTree(it);
//        tree.set("@class", new TextNode(it.getClass().getName()));
//        tempGen.writeTree(tree);
//    }
//}
}