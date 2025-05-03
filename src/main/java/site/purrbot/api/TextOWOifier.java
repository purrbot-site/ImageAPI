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

import io.javalin.http.Context;
import site.purrbot.api.objects.OWOifiedTextResponse;
import site.purrbot.api.objects.OWOifyRequest;

import java.util.*;

public class TextOWOifier{
    
    private final List<String> emotes = Arrays.asList("(・`ω´・)", ";;w;;", ">w<", "^w^", "UwU", "owo", "^^", "x3");
    private final Map<String, String> wordMap = new HashMap<String, String>(){{
        put("hello", "hewwo");
        put("hi", "hai");
        put("hey", "haiii");
        put("love", "wuv");
        put("friend", "fwend");
        put("stop", "stahp");
        put("no", "nu");
        put("you're", "ur");
        put("you", "uu");
        put("has", "haz");
    }};
    
    private final Random random = new Random();
    
    public void owoify(OWOifyRequest request, Context ctx, long time, boolean deprecated){
        String[] words = request.getText().split("\\s+");
        StringBuilder builder = new StringBuilder();
        
        for(String word : words){
            String key = word.toLowerCase(Locale.ROOT);
            
            if(request.isWordSubstitutions() && wordMap.containsKey(key)){
                word = matchCase(word, wordMap.get(key));
            }
            
            word = word.replaceAll("[rl]", "w").replaceAll("[RL]", "W");
            
            double d;
            synchronized(random){
                d = random.nextDouble();
            }
            if(request.isStutter() && word.length() > 2 && Character.isLetter(word.charAt(0)) && d < 0.2){
                word = word.charAt(0) + "-" + word;
            }
            
            builder.append(word).append(' ');
        }
        
        String owoified = builder.toString().trim();
        
        if(request.isEmoticons()){
            owoified = owoified.replaceAll("[.!?]", " " + getRandomEmoticon());
        }
        
        ctx.status(200);
        if(deprecated){
            ctx.json(new OWOifiedTextResponse(
                owoified,
                time,
                "This endpoint was deprecated and will be removed in the future. " +
                "Please forward any future requests towards https://api.purrbot.site/v2/owoify"
            ));
        }else{
            ctx.json(new OWOifiedTextResponse(owoified, time));
        }
    }
    
    private String matchCase(String original, String replacement){
        if(original.equals(original.toLowerCase(Locale.ROOT)))
            return replacement.toLowerCase(Locale.ROOT);
        
        if(original.equals(original.toUpperCase(Locale.ROOT)))
            return replacement.toUpperCase(Locale.ROOT);
        
        if(Character.isUpperCase(original.charAt(0)))
            return Character.toUpperCase(replacement.charAt(0)) + replacement.substring(1);
        
        return replacement;
    }
    
    private String getRandomEmoticon(){
        synchronized(random){
            return emotes.get(random.nextInt(emotes.size()));
        }
    }
}
