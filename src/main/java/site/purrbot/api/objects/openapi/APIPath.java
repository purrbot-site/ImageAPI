package site.purrbot.api.objects.openapi;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class APIPath{
    
    private final String path;
    private final String method;
    private final String description;
    @SerializedName("request-bodies")
    private final Map<String, String> requestBodies;
    private final Boolean deprecated;
    private final List<APIParameter> parameters;
    
    public APIPath(String path, String method, String description, Map<String, String> requestBodies, Boolean deprecated, List<APIParameter> parameters){
        this.path = path;
        this.method = method;
        this.description = description;
        this.requestBodies = requestBodies;
        this.deprecated = deprecated;
        this.parameters = parameters;
    }
}
