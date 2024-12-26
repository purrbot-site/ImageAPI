/*
 * Copyright 2020 Andre601
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
import io.javalin.http.Context;
import org.slf4j.LoggerFactory;
import site.purrbot.api.objects.ImgLinkListResponse;
import site.purrbot.api.objects.ImgLinkResponse;

import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ImageUtil{
    
    private final ImageAPI api;
    
    private final Random random = new Random();
    private final Logger logger = (Logger)LoggerFactory.getLogger(ImageUtil.class);
    private final List<String> extensions = Arrays.asList(".png", ".jpg", ".jpeg", ".gif", ".svg");
    private final File base = new File("img/");
    private final FilenameFilter filter = (dir, name) -> {
        for(String ext : extensions){
            if(name.endsWith(ext))
                return true;
        }
        
        return false;
    };
    
    public ImageUtil(ImageAPI api){
        this.api = api;
    }
    
    void listContent(String path, Context ctx, long time){
        File[] files = getFiles(path, ctx, time);
        if(files.length == 0)
            return;
        
        List<String> links = Arrays.stream(files).map(this::getPath).collect(Collectors.toList());
        
        ctx.status(200);
        ctx.result(api.getGson().toJson(new ImgLinkListResponse(links, 200, time)));
    }
    
    void getFile(String path, Context ctx, long time){
        File[] files = getFiles(path, ctx, time);
        if(files.length == 0)
            return;
        
        File selected = files[random.nextInt(files.length)];
        
        ctx.status(200);
        ctx.result(api.getGson().toJson(new ImgLinkResponse(getPath(selected), time)));
    }
    
    private File[] getFiles(String path, Context ctx, long time){
        File folder = new File(base, path + "/");
        
        if(!folder.exists() || folder.isAbsolute()){
            logger.info("Received invalid path {} for Image GET request.", path);
            
            api.sendErrorJSON(403, "The provided path is not valid.", ctx, time);
            return new File[0];
        }else{
            File[] files = folder.listFiles(filter);
            if(files == null || files.length == 0){
                logger.info("Received path {} for Image GET request does not contain any images.", path);
        
                api.sendErrorJSON(403, "The provided path does not contain any images.", ctx, time);
                return new File[0];
            }
            
            return files;
        }
    }
    
    private String getPath(File file){
        return ("https://purrbot.site/" + file.getPath()).replace("\\", "/");
    }
}
