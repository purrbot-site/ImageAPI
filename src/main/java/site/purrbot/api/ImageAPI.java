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
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ImageAPI{
    
    private final Random random = new Random();
    private final Logger logger = (Logger)LoggerFactory.getLogger(ImageAPI.class);
    private final List<String> extensions = Arrays.asList(".png", ".jpg", ".jpeg", ".gif", ".svg");
    private final File base = new File("img/");
    private final FilenameFilter filter = (dir, name) -> {
        for(String ext : extensions)
            if(name.endsWith(ext))
                return true;
        
        return false;
    };
    
    public static void main(String[] args){
        new ImageAPI().boot();
    }
    
    private void boot(){
        logger.info("Starting ImageAPI...");
        if(!base.exists()){
            logger.info("Couldn't find base folder. Generating it...");
            //noinspection ResultOfMethodCallIgnored
            base.mkdir();
        }
    
        Spark.initExceptionHandler(e -> logger.error("Caught an exception", e));
        Spark.ipAddress("127.0.0.1");
        Spark.port(8001);
        
        Spark.get("/api/img/*", (request, response) -> {
            long time = System.currentTimeMillis();
            String path = request.pathInfo().replaceFirst("/api/img/", "").replace("../", "");
            
            File file = new File(base, path + "/");
            JSONObject json = new JSONObject();
            
            if(!file.exists() || file.isAbsolute()){
                json.put("code", 403)
                    .put("message", "Not supported API path.")
                    .put("time", System.currentTimeMillis() - time);
                
                response.status(403);
            }else{
                File[] files = file.listFiles(filter);
                if(files == null || files.length == 0){
                    json.put("code", 403).put("message", "The selected directory doesn't contain any images.");
                    
                    response.status(403);
                }else{
                    File selected = files[random.nextInt(files.length)];
                    json.put("code", 200)
                        .put("link", generateLink(selected))
                        .put("time", System.currentTimeMillis() - time);
                    
                    response.status(200);
                }
            }
            response.type("application/json");
            response.body(json.toString());
            return response.body();
        });
    }
    
    private String generateLink(File file){
        return ("https://purrbot.site/" + file.getPath()).replace("\\", "/");
    }
}
