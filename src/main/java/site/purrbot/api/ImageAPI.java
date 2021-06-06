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
import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import site.purrbot.api.endpoints.Quote;
import site.purrbot.api.endpoints.Status;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ImageAPI{
    
    private final Logger logger = (Logger)LoggerFactory.getLogger(ImageAPI.class);
    private final File base = new File("img/");
    
    private final String docs_url = "https://docs.purrbot.site/api";
    
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
        Gson gson = new Gson();
    
        // Setup Javalin and make it handle all Exceptions
        Javalin app = Javalin.create(config -> config.defaultContentType = "application/json").start(2000);
        app.exception(Exception.class, (ex, ctx) -> logger.error("Exception caught", ex));
        
        // Log any call to the /api endpoint
        app.before("/api/*", ctx -> logger.info("HTTP Request on " + ctx.path()));
        
        // Handle redirects
        app.get("/api/quote", ctx -> {
            logger.info("Performing redirect for /api/quote");
            ctx.redirect(docs_url + "#quote", 301);
        }).get("/api/status", ctx -> {
            logger.info("Performing redirect for /api/status");
            ctx.redirect(docs_url + "#status", 301);
        });
        
        // Handle all /api/img/* requests
        app.get("/api/img/*", ctx -> {
            logger.info("Handle GET request for " + ctx.path());
            long time = System.currentTimeMillis();
            
            String path = ctx.path().replaceFirst("/api/img", "").replace("../", "");
            util.generateResponse(path, ctx, time);
        });
        
        // Handle POST requests.
        app.post("/api/quote", ctx -> {
            logger.info("Handle POST request for /api/quote");
            
            Quote quote = gson.fromJson(ctx.body(), Quote.class);
            if(quote == null){
                sendErrorJSON(400, "Received invalid or empty JSON Body.", ctx);
                return;
            }
            
            try{
                HttpServletResponse raw = ctx.res;
                raw.setContentType("image/png");
                raw.getOutputStream().write(util.getQuote(quote));
                raw.getOutputStream().flush();
                raw.getOutputStream().close();
            }catch(IOException ex){
                sendErrorJSON(500, "Couldn't generate Image. Make sure the values are valid!", ctx);
            }
        }).post("/api/status", ctx -> {
            logger.info("Handle POST request for /api/status");
            
            Status status = gson.fromJson(ctx.body(), Status.class);
            if(status == null){
                sendErrorJSON(400, "Received invalid or empty JSON Body.", ctx);
                return;
            }
            
            try{
                HttpServletResponse raw = ctx.res;
                raw.setContentType("image/png");
                raw.getOutputStream().write(util.getStatus(status));
                raw.getOutputStream().flush();
                raw.getOutputStream().close();
            }catch(IOException ex){
                sendErrorJSON(500, "Couldn't generate Image. Make sure the values are valid!", ctx);
            }
        }).post("/api/img/*", ctx -> sendErrorJSON(403, "POST requests are not allowed for this path!", ctx));
    }
    
    void sendErrorJSON(int code, String msg, Context ctx){
        JSONObject details = new JSONObject(getDetailsMap(ctx));
        JSONObject json = new JSONObject()
                .put("error", true)
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
