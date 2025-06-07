/*
 * Copyright 2025 Andre601
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package site.purrbot.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import site.purrbot.api.mapper.GsonMapper;
import site.purrbot.api.objects.ErrorResponse;
import site.purrbot.api.objects.OWOifyRequest;
import site.purrbot.api.objects.RequestDetails;
import site.purrbot.api.objects.openapi.APIPath;
import site.purrbot.api.objects.openapi.APIPathsResponse;
import site.purrbot.api.objects.openapi.APIParameter;

import java.io.*;
import java.util.*;

public class ImageAPI{
    
    private final Logger logger = LoggerFactory.getLogger(ImageAPI.class);
    private final File base = new File("img/");
    private final Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .create();
    
    private JsonObject infoJson = null;
    private OpenAPI openAPI = null;
    
    private TextOWOifier owoifier;
    
    public static void main(String[] args){
        new ImageAPI().start();
    }
    
    private void start(){
        logger.info("Starting ImageAPI...");
        if(!base.exists()){
            logger.info("Couldn't find base folder. Generating it...");
            if(base.mkdirs()){
                logger.info("Successfully created base folder!");
            }else{
                logger.warn("Couldn't create base folder!");
            }
        }
        
        ImageUtil util = new ImageUtil(this);
        this.owoifier = new TextOWOifier();
        
        // Setup Javalin and make it handle all Exceptions
        Javalin app = Javalin.create(config -> {
            config.jsonMapper(new GsonMapper(gson));
            config.requestLogger.http((ctx, ms) -> logger.info("Processed request {} in {}ms", ctx.path(), ms));
        }).start(2000);
        app.exception(Exception.class, (ex, ctx) -> {
            logger.error("Exception caught", ex);
            
            sendErrorJSON(500, "Encountered an Exception while handling request. Exception: " + ex.getMessage(), ctx, System.currentTimeMillis());
        });
        
        app.get("/v2/", ctx -> {
            fetchOpenAPIJson(ctx, System.currentTimeMillis());
        });
        
        // New v2 API endpoints.
        app.get("/v2/list/<path>", ctx -> {
            long time = System.currentTimeMillis();
            
            String path = ctx.pathParam("path");
            util.listContent(path, ctx, time, false);
        });
        app.get("/v2/img/<path>", ctx -> {
            long time = System.currentTimeMillis();
            
            String path = ctx.pathParam("path");
            util.getFile(path, ctx, time, false);
        });
        
        app.post("/v2/owoify", ctx -> processOWOifyJSON(ctx, false))
            .get("/v2/owoify", ctx -> {
                long time = System.currentTimeMillis();
                
                String text = ctx.queryParam("text");
                boolean stutter = Boolean.parseBoolean(ctx.queryParam("stutter"));
                boolean emoticons = Boolean.parseBoolean(ctx.queryParam("emoticons"));
                boolean wordSubstitution = Boolean.parseBoolean(ctx.queryParam("replace-words"));
                
                if(text == null || text.isEmpty()){
                    sendErrorJSON(400, "Received request does not contain a 'text' query parameter, or it was empty.", ctx, time);
                    return;
                }
                
                owoifier.owoify(new OWOifyRequest(text, stutter, emoticons, wordSubstitution), ctx, time, false);
            });
        
        app.get("/", ctx -> {
            long time = System.currentTimeMillis();
            fetchInfoJson(ctx, time);
        });
        
        // Old /api/list/img/* Endpoints
        app.get("/api/list/<path>", ctx -> {
            logger.info("Handle GET request for {}", ctx.path());
            long time = System.currentTimeMillis();
            
            String path = ctx.pathParam("path");
            util.listContent(path, ctx, time, true);
        });
        
        // Old /api/img/* Endpoints
        app.get("/api/img/<path>", ctx -> {
            logger.info("Handle GET request for {}", ctx.path());
            long time = System.currentTimeMillis();
            
            String path = ctx.pathParam("path");
            util.getFile(path, ctx, time, true);
        });
        
        // Old /api/owoify Endpoints
        app.post("/api/owoify", ctx -> processOWOifyJSON(ctx, true)).get("/api/owoify", ctx -> {
            logger.info("Handle GET request for {}", ctx.path());
            long time = System.currentTimeMillis();
            
            String text = ctx.queryParam("text");
            boolean stutter = Boolean.getBoolean(ctx.queryParam("stutter"));
            boolean emoticons = Boolean.getBoolean(ctx.queryParam("emoticons"));
            boolean wordSubstitution = Boolean.getBoolean(ctx.queryParam("replace-words"));
            
            if(text == null || text.isEmpty()){
                sendErrorJSON(400, "Received request does not contain a 'text' query parameter, or it was empty.", ctx, time);
                return;
            }
            
            owoifier.owoify(new OWOifyRequest(text, stutter, emoticons, wordSubstitution), ctx, time, true);
        });
        
        // Old /api/info Endpoint
        app.get("/api/info", ctx -> {
            logger.info("Handle GET request for {}", ctx.path());
            long time = System.currentTimeMillis();
            
            fetchInfoJson(ctx, time);
        });
        
        // Handle unsupported requests.
        app.post("/api/quote", ctx -> {
            logger.info("Unsupported POST request on /api/quote");
            sendErrorJSON(410, "/api/quote has been removed from the API.", ctx, System.currentTimeMillis());
        }).post("/api/status", ctx -> {
            logger.info("Unsupported POST request on /api/status");
            sendErrorJSON(410, "/api/status has been removed from the API.", ctx, System.currentTimeMillis());
        }).post("/api/img/*", ctx -> {
            logger.info("Not allowed POST request towards {}", ctx.path());
            ctx.header("Allow", "GET");
            sendErrorJSON(405, "POST requests towards " + ctx.path() + " are not allowed.", ctx, System.currentTimeMillis());
        }).post("/api/list/*", ctx -> {
            logger.info("Not allowed POST request towards {}", ctx.path());
            ctx.header("Allow", "GET");
            sendErrorJSON(405, "POST requests towards " + ctx.path() + " are not allowed.", ctx, System.currentTimeMillis());
        });
    }
    
    void sendErrorJSON(int code, String msg, Context ctx, long time){
        ErrorResponse response = new ErrorResponse(getDetails(ctx), msg, code, time);
        
        ctx.json(response);
        ctx.status(code);
    }
    
    private RequestDetails getDetails(Context ctx){
        return new RequestDetails(
            ctx.path(),
            ctx.contentType() == null ? "NONE" : ctx.contentType(),
            ctx.userAgent() == null ? "NONE" : ctx.userAgent()
        );
    }
    
    private void fetchInfoJson(Context ctx, long time){
        if(infoJson != null){
            ctx.json(infoJson);
            ctx.status(200);
            return;
        }
        
        try(InputStream stream = getClass().getResourceAsStream("/info.json")){
            if(stream == null){
                sendErrorJSON(500, "Cannot retrieve API information. Reason: Received input stream was null.", ctx, time);
                return;
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            
            infoJson = gson.fromJson(reader, JsonObject.class);
            if(infoJson == null){
                sendErrorJSON(500, "Cannot retrieve API information. Reason: Retrieved JSON was null.", ctx, time);
                reader.close();
                return;
            }
            
            ctx.json(infoJson);
            ctx.status(200);
            
            reader.close();
        }catch(IOException ex){
            sendErrorJSON(500, "Encountered an IOException: " + ex.getMessage(), ctx, time);
        }
    }
    
    private void fetchOpenAPIJson(Context ctx, long time){
        if(openAPI != null){
            displayAPIPaths(ctx, time);
            return;
        }
        
        SwaggerParseResult result = new OpenAPIV3Parser().readLocation("https://raw.githubusercontent.com/purrbot-site/Docs/master/docs/assets/imageapi.json", null, null);
        
        if(result.getMessages() != null && !result.getMessages().isEmpty()){
            sendErrorJSON(
                    500,
                    String.format(
                            "Encountered an error while parsing OpenAPI JSON: %s",
                            String.join(";", result.getMessages())
                    ),
                    ctx,
                    time
            );
            return;
        }
        
        OpenAPI openAPI = result.getOpenAPI();
        if(openAPI == null){
            sendErrorJSON(
                    500,
                    "Received OpenAPI instance was null.",
                    ctx,
                    time
            );
            return;
        }
        
        this.openAPI = openAPI;
        
        displayAPIPaths(ctx, time);
    }
    
    private void processOWOifyJSON(Context ctx, boolean deprecated){
        long time = System.currentTimeMillis();
        
        try{
            OWOifyRequest request = gson.fromJson(ctx.body(), OWOifyRequest.class);
            if(request == null){
                sendErrorJSON(400, "The received JSON was invalid or didn't exist.", ctx, time);
                return;
            }
            
            if(request.getText() == null || request.getText().isEmpty()){
                sendErrorJSON(400, "The received JSON does not contain a 'text' field or it was empty.", ctx, time);
                return;
            }
            
            if(owoifier == null){
                sendErrorJSON(500, "The TextOWOIfier is not available. If this issue persists, report it to the developer!", ctx, time);
                return;
            }
            
            owoifier.owoify(request, ctx, time, deprecated);
        }catch(JsonSyntaxException ex){
            sendErrorJSON(400, "Received invalid JSON Body: " + ex.getMessage(), ctx, time);
        }
    }
    
    private void displayAPIPaths(Context ctx, long time){
        List<APIPath> paths = new ArrayList<>();
        for(Map.Entry<String, PathItem> path : openAPI.getPaths().entrySet()){
            if(path.getValue().getGet() != null){
                paths.add(processAPIPath(path.getKey(), "GET", path.getValue().getGet()));
            }
            if(path.getValue().getPost() != null){
                paths.add(processAPIPath(path.getKey(), "POST", path.getValue().getPost()));
            }
        }
        
        APIPathsResponse response = new APIPathsResponse(time, paths);
        
        ctx.status(200);
        ctx.json(response);
    }
    
    private APIPath processAPIPath(String name, String method, Operation operation){
        String pathName = "https://api.purrbot.site/v2" + name;
        String description = operation.getDescription();
        Map<String, String> requestBodies = null;
        
        
        if(operation.getRequestBody() != null && operation.getRequestBody().getContent() != null){
            Content content = operation.getRequestBody().getContent();
            requestBodies = new HashMap<>();
            for(Map.Entry<String, MediaType> schema : content.entrySet()){
                if(schema.getValue().getSchema() == null || schema.getValue().getSchema().get$ref() == null)
                    continue;
                
                String ref = schema.getValue().getSchema().get$ref().toLowerCase(Locale.ROOT);
                if(ref.startsWith("#/components/schemas/")){
                    requestBodies.put(
                            schema.getKey(),
                            "https://docs.purrbot.site/api/#" + ref.substring("#/components/schemas/".length()));
                }else{
                    requestBodies.put(schema.getKey(), ref);
                }
            }
        }
        
        Boolean deprecated = operation.getDeprecated();
        
        List<APIParameter> parameters = null;
        if(operation.getParameters() != null){
            parameters = new ArrayList<>();
            for(Parameter parameter : operation.getParameters())
                parameters.add(new APIParameter(parameter.getName(), parameter.getIn(), parameter.getDescription(), parameter.getRequired()));
        }
        
        return new APIPath(pathName, method, description, requestBodies, deprecated, parameters);
    }
}
