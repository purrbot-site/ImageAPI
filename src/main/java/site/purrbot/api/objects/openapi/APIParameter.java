package site.purrbot.api.objects.openapi;

public class APIParameter{
    private final String name;
    private final String location;
    private final String description;
    private final Boolean required;
    
    public APIParameter(String name, String location, String description, Boolean required){
        this.name = name;
        this.location = location;
        this.description = description;
        this.required = required;
    }
}
