package site.purrbot.api.objects.openapi;

import site.purrbot.api.objects.RequestResponse;

import java.util.ArrayList;
import java.util.List;

public class APIPathsResponse extends RequestResponse{
    
    private final List<APIPath> paths;
    
    public APIPathsResponse(long time, List<APIPath> paths){
        super(false, 200, time);
        this.paths = paths;
    }
}
