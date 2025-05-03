package site.purrbot.api.objects;

import com.google.gson.annotations.SerializedName;

public class OWOifyRequest{
    private final String text;
    private final boolean stutter;
    private final boolean emoticons;
    @SerializedName("replace-words")
    private final boolean wordSubstitutions;
    
    public OWOifyRequest(String text, boolean stutter, boolean emoticons, boolean wordSubstitutions){
        this.text = text;
        this.stutter = stutter;
        this.emoticons = emoticons;
        this.wordSubstitutions = wordSubstitutions;
    }
    
    public String getText(){
        return text;
    }
    
    public boolean isStutter(){
        return stutter;
    }
    
    public boolean isEmoticons(){
        return emoticons;
    }
    
    public boolean isWordSubstitutions(){
        return wordSubstitutions;
    }
}
