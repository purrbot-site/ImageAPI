/*
 * Copyright 2019 Andre601
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
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ImageAPI{
    
    private final Logger logger = (Logger)LoggerFactory.getLogger(ImageAPI.class);
    private final File base = new File("img/");
    
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
    
        // Setup Javalin and make it handle all Exceptions
        Javalin app = Javalin.create(config -> config.defaultContentType = "application/json").start(2000);
        app.exception(Exception.class, (ex, ctx) -> logger.error("Exception caught", ex));
        
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
        
        // Handle POST requests.
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
    
    JSONObject getBasicJson(boolean error, long time){
        return new JSONObject()
            .put("error", error)
            .put("time", System.currentTimeMillis() - time);
    }
    
    void sendErrorJSON(int code, String msg, Context ctx, long time){
        JSONObject details = new JSONObject(getDetailsMap(ctx));
        JSONObject json = getBasicJson(true, time)
                .put("message", msg)
                .put("details", details);
        
        ctx.status(code);
        ctx.result(json.toString(2));
    }
    
    private Map<String, String> getDetailsMap(Context ctx){
        Map<String, String> details = new HashMap<>();
        
        details.put("path", ctx.path());
        details.put("content-type", ctx.contentType() == null ? "NONE" : ctx.contentType());
        details.put("user-agent", ctx.userAgent() == null ? "NONE" : ctx.userAgent());
        
        return details;
    }
}
