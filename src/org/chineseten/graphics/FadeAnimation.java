package org.chineseten.graphics;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.media.client.Audio;
import com.google.gwt.user.client.ui.Image;


/*
 * This class is used to form a fading animation,
 * The code is from internet. The source is as follows:
 * http://map-notes.blogspot.com/2012/11/fade-animation.html
 */
public class FadeAnimation extends Animation {

    private Image image;
    private double opacityIncrement;
    private double targetOpacity;
    private double baseOpacity;
    private Audio soundEfx;
    
    public FadeAnimation(Image image, Audio soundEfx) {
        this.image = image;
        this.soundEfx = soundEfx;
    }
    
    @Override
    protected void onUpdate(double progress) {
        image.getElement().getStyle().setOpacity(baseOpacity + progress * opacityIncrement);
    }
    
    @Override
    protected void onComplete() {
        super.onComplete();
        if (soundEfx != null) {
            soundEfx.play();
        }
        image.getElement().getStyle().setOpacity(targetOpacity);
        
    }
    
    public void fade(int duration, double targetOpacity) {
        if (targetOpacity > 1.0) {
            targetOpacity = 1.0;
        }
        if (targetOpacity < 0.0) {
            targetOpacity = 0.0;
        }
        this.targetOpacity = targetOpacity;
        try {
            baseOpacity = 0.0;
            opacityIncrement = targetOpacity - baseOpacity;
            run(duration);
        } catch (NumberFormatException e) {
            onComplete();
        }
    }
  
}