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

import ch.qos.logback.classic.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.slf4j.LoggerFactory;
import site.purrbot.api.mapper.GsonMapper;
import site.purrbot.api.objects.ErrorResponse;
import site.purrbot.api.objects.RequestDetails;

import java.io.*;

public class ImageAPI{
    
    private final Logger logger = (Logger)LoggerFactory.getLogger(ImageAPI.class);
    private final File base = new File("img/");
    private final Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .create();
    
    private JsonObject infoJson = null;
    
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
        TextOWOifier owoifier = new TextOWOifier();
    
        // Setup Javalin and make it handle all Exceptions
        Javalin app = Javalin.create(config -> config.jsonMapper(new GsonMapper())).start(2000);
        app.exception(Exception.class, (ex, ctx) -> {
            logger.error("Exception caught", ex);
            
            sendErrorJSON(500, "Encountered an Exception while handling request. Exception: " + ex.getMessage(), ctx, System.currentTimeMillis());
        });
        
        // Log any call to the /api endpoint
        app.before("/api/*", ctx -> logger.info("HTTP Request on {}", ctx.path()));
        
        // Handle all /api/list/img/* requests
        app.get("/api/list/*", ctx -> {
            logger.info("Handle GET request for {}", ctx.path());
            long time = System.currentTimeMillis();
            
            String path = ctx.path().replaceFirst("/api/list", "").replace("../", "");
            util.listContent(path, ctx, time);
        });
        
        // Handle all /api/img/* requests
        app.get("/api/img/*", ctx -> {
            logger.info("Handle GET request for {}", ctx.path());
            long time = System.currentTimeMillis();
            
            String path = ctx.path().replaceFirst("/api/img", "").replace("../", "");
            util.getFile(path, ctx, time);
        });
        
        // Handle POST/GET towards /api/owoify.
        app.post("/api/owoify", ctx -> {
            logger.info("Handle POST request for {}", ctx.path());
            long time = System.currentTimeMillis();
            
            try{
                
                JsonObject json = gson.fromJson(ctx.body(), JsonObject.class);
                if(!json.has("text")){
                    sendErrorJSON(400, "Received JSON Body does not contain a 'text' value.", ctx, time);
                    return;
                }
                
                owoifier.owoify(json.getAsJsonPrimitive("text").getAsString(), ctx, time);
            }catch(JsonSyntaxException ex){
                sendErrorJSON(400, "Received malformed JSON Body in request. Reason: " + ex.getMessage(), ctx, time);
            }
        }).get("/api/owoify", ctx -> {
            logger.info("Handle GET request for {}", ctx.path());
            long time = System.currentTimeMillis();
            
            String query = ctx.queryParam("text");
            if(query == null || query.isEmpty()){
                sendErrorJSON(400, "Received request does not contain a 'text' query parameter, or it was empty.", ctx, time);
                return;
            }
            
            owoifier.owoify(query, ctx, time);
        });
        
        // Handle /api/info requests
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
                sendErrorJSON(500, "Cannot retrieve API information. Reason Retrieved JSON was null.", ctx, time);
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
}
