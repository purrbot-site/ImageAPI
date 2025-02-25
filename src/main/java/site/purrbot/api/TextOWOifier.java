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

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TextOWOifier{
    
    private final List<String> emotes = Arrays.asList(">w<", "^w^", "UwU", "owo");
    private final Random random = new Random();
    
    public void owoify(String text, Context ctx, long time){
        StringBuilder builder = new StringBuilder(text.length());
        for(char c : text.toCharArray()){
            switch(c){
                case 'l':
                case 'r':
                    builder.append('w');
                    break;
                case 'L':
                case 'R':
                    builder.append('L');
                    break;
                case '!':
                    synchronized(random){
                        builder.append(' ').append(emotes.get(random.nextInt(emotes.size()))).append(' ');
                    }
                    break;
                default:
                    builder.append(c);
            }
        }
        
        ctx.status(200);
        ctx.json(new OWOifiedTextResponse(builder.toString(), time));
    }
}
