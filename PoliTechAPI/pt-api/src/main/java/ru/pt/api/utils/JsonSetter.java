package ru.pt.api.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micrometer.common.util.StringUtils;
import ru.pt.api.dto.product.LobVar;
import java.text.SimpleDateFormat;

public class JsonSetter {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
    private final ObjectNode root;
    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .setDateFormat(dateFormat);

    public JsonSetter(String json) {
        try {
            this.root = (ObjectNode) objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void set(LobVar value) {
        if (StringUtils.isEmpty(value.getVarValue())) {
            return;
        }

        root.set(value.getVarCode(), new TextNode(value.getVarValue()));
    }

    public void setRawValue(String key, String value) {
        root.set(key, new TextNode(value));
    }

    public String writeValue() {
        try {
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void setObjectValue(String propertyName, Object object) {
        objectMapper.valueToTree(object);
        root.set(propertyName, objectMapper.valueToTree(object));
    }
}
