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
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import site.purrbot.api.endpoints.Quote;
import site.purrbot.api.endpoints.Status;
import spark.Redirect;
import spark.Response;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

import static spark.Spark.*;

public class ImageAPI{
    
    private final Logger logger = (Logger)LoggerFactory.getLogger(ImageAPI.class);
    private final File base = new File("img/");
    
    private final String docs_url = "https://docs.purrbot.site/api";
    
    public static void main(String[] args){
        new ImageAPI().boot();
    }
    
    private void boot(){
        logger.info("Starting ImageAPI...");
        if(!base.exists()){
            logger.info("Couldn't find base folder. Generating it...");
            if(base.mkdir())
                logger.info("Successfully created folder!");
        }
        
        ImageUtil imageUtil = new ImageUtil();
    
        initExceptionHandler(e -> logger.error("Caught an exception", e));
        port(2000);
        
        
        path("/api", () -> {
            redirect.get("/quote", docs_url + "#quote", Redirect.Status.MOVED_PERMANENTLY);
            redirect.get("/status", docs_url + "#status", Redirect.Status.MOVED_PERMANENTLY);
            
            get("/img/*", (request, response) -> {
                logger.info("GET: " + request.pathInfo().replace("../", ""));
                long time = System.currentTimeMillis();
                
                String path = request.pathInfo().replaceFirst("/api/img/", "").replace("../", "");
                
                return imageUtil.getResponse(path, response, time);
            });
    
            Gson gson = new Gson();
    
            post("/quote", (request, response) -> {
                logger.info("POST: " + request.pathInfo().replace("../", ""));
        
                Quote quote = gson.fromJson(request.body(), Quote.class);
        
                if(quote == null)
                    return getErrorJSON(response, 403, "Invalid or empty JSON body received.");
        
                try{
                    HttpServletResponse raw = response.raw();
                    raw.setContentType("image/png");
                    raw.getOutputStream().write(imageUtil.getQuote(quote));
                    raw.getOutputStream().flush();
                    raw.getOutputStream().close();
            
                    return raw;
                }catch(IOException ex){
                    logger.error("Couldn't perform POST request for /quote!", ex);
                    return getErrorJSON(response, 500, "Couldn't generate Image. Make sure the values are valid!");
                }
            });
    
            post("/status", (request, response) -> {
                logger.info("POST: " + request.pathInfo().replace("../", ""));
        
                Status status = gson.fromJson(request.body(), Status.class);
        
                if(status == null)
                    return getErrorJSON(response, 403, "Invalid or empty JSON body received.");
        
                try{
                    HttpServletResponse raw = response.raw();
                    raw.setContentType("image/png");
                    raw.getOutputStream().write(imageUtil.getStatus(status));
                    raw.getOutputStream().flush();
                    raw.getOutputStream().close();
    
                    return raw;
                }catch(IOException ex){
                    logger.error("Couldn't perform POST request for /status!", ex);
                    return getErrorJSON(response, 500, "Couldn't generate Image. Make sure the values are valid!");
                }
            });
        });
        
    }
    
    private String getErrorJSON(Response response, int code, String message){
        JSONObject json = new JSONObject()
                .put("error", true)
                .put("message", message);
        
        response.status(code);
        response.type("application/json");
        response.body(json.toString());
        
        return response.body();
    }
}
