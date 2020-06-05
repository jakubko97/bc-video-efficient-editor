package sk.fei.videoeditor.animations;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.opengl.Visibility;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;

public class Animations {

    public static void showIn(final View v) {
        v.setVisibility(View.VISIBLE);
        v.setAlpha(0f);
        v.setTranslationY(v.getHeight());
        v.animate()
                .setDuration(400)
                .translationY(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                })
                .alpha(1f)
                .start();
    }
    public static void showOut(final View v) {
        v.setVisibility(View.VISIBLE);
        v.setAlpha(1f);
        v.setTranslationY(0);
        v.animate()
                .setDuration(400)
                .translationY(v.getHeight())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        v.setVisibility(View.GONE);
                        super.onAnimationEnd(animation);
                    }
                }).alpha(0f)
                .start();
    }

    public static void init(final View v) {
        v.setVisibility(View.GONE);
        v.setTranslationY(v.getHeight());
        v.setAlpha(0f);
    }

    public static boolean rotateFab(final View v, boolean rotate) {
        v.animate().setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                })
                .rotation(rotate ? 135f : 0f);
        return rotate;
    }

    public static boolean toggleArrow(View view, boolean isExpanded) {

        view.animate().setDuration(400);
        if (isExpanded) {
            //view.animate().setDuration(200).rotation(180);
            view.setVisibility(View.VISIBLE);
            return true;
        } else {
            //view.animate().setDuration(200).rotation(0);
            view.setVisibility(View.GONE);
            return false;
        }
    }


    public static void expand(final View v) {
        int prevHeight  = v.getHeight();

        v.setVisibility(View.VISIBLE);
        ValueAnimator valueAnimator = ValueAnimator.ofInt(prevHeight, 125);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                v.getLayoutParams().height = (int) animation.getAnimatedValue();
                v.requestLayout();
            }
        });
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(500);
        valueAnimator.start();
    }

    public static boolean fadeView(final View view, boolean isExpanded){


        if (isExpanded) {
            Animation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
            fadeIn.setDuration(1000);

            AnimationSet animation = new AnimationSet(false); //change to false
            animation.addAnimation(fadeIn);

            view.setAnimation(animation);
            view.setVisibility(View.VISIBLE);
            return true;
        } else {
//            Animation fadeOut = new AlphaAnimation(1, 0);
//            fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
//            //fadeOut.setStartOffset(1000);
//            fadeOut.setDuration(450);
//
//            AnimationSet animation = new AnimationSet(false); //change to false
//            animation.addAnimation(fadeOut);
//            view.setAnimation(animation);
            view.setVisibility(View.GONE);

            return false;
        }
    }

    public static void collapse(final View view) {

        final int actualHeight = view.getMeasuredHeight();

        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {

                if (interpolatedTime == 1) {
                    view.setVisibility(View.GONE);
                } else {
                    view.getLayoutParams().height = actualHeight - (int) (actualHeight * interpolatedTime);
                    view.requestLayout();

                }
            }
        };

        animation.setDuration(400);
        view.startAnimation(animation);
    }



}
