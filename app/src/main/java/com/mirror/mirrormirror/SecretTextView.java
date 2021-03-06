package com.mirror.mirrormirror;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by matt on 5/27/14.
 * https://github.com/matthewrkula/SecretTextView
 */
public class SecretTextView extends TextView {
    private String mTextString;
    private SpannableString mSpannableString;

    private double[] mAlphas;
    private MutableForegroundColorSpan[] mSpans;
    private boolean mIsVisible;
    private boolean mIsTextResetting = false;
    private int mDuration = 2000;

    private boolean shouldHide = false;

    ValueAnimator animator;
    ValueAnimator.AnimatorUpdateListener listener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            Float percent = (Float) valueAnimator.getAnimatedValue();
            resetSpannableString(mIsVisible ? percent : 2.0f - percent);
        }
    };

    public SecretTextView(Context context) {
        super(context);
        init();
    }

    public SecretTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.mIsVisible = false;
        animator = ValueAnimator.ofFloat(0.0f, 2.0f);
        animator.addUpdateListener(listener);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (!mIsVisible) {
                    animate()
                            .alpha(0.0f)
                            .setDuration(250);
                }
                if (shouldHide) {
                    shouldHide = false;
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hide();
                        }
                    }, 5000);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        animator.setDuration(mDuration);
    }

    public void toggle() {
        if (mIsVisible) {
            hide();
        } else {
            show();
        }
    }

    public void showAndHide() {
        shouldHide = true;
        mIsVisible = true;
        animate()
                .alpha(1.0f)
                .setDuration(250)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        animator.start();
                    }
                });
    }

    public void show() {
        mIsVisible = true;
        animate()
                .alpha(1.0f)
                .setDuration(250)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        animator.start();
                    }
                });
    }

    public void hide() {
        mIsVisible = false;
        animator.start();
    }

    public void setIsVisible(boolean isVisible) {
        mIsVisible = isVisible;
        resetSpannableString(isVisible ? 2.0f : 0.0f);
    }

    public boolean getIsVisible() {
        return mIsVisible;
    }

    private void resetSpannableString(double percent) {
        mIsTextResetting = true;

        int color = getCurrentTextColor();
        for (int i = 0; i < this.mTextString.length(); i++) {
            MutableForegroundColorSpan span = mSpans[i];
            span.setColor(Color.argb(clamp(mAlphas[i] + percent), Color.red(color), Color.green(color), Color.blue(color)));
        }

        setText(mSpannableString);

        mIsTextResetting = false;
    }

    private void resetAlphas(int length) {
        mAlphas = new double[length];
        for (int i = 0; i < mAlphas.length; i++) {
            mAlphas[i] = Math.random() - 1;
        }
    }

    private void resetIfNeeded() {
        if (!mIsTextResetting) {
            mTextString = getText().toString();
            mSpannableString = new SpannableString(this.mTextString);
            mSpans = new MutableForegroundColorSpan[this.mTextString.length()];
            for (int i = 0; i < this.mTextString.length(); i++) {
                MutableForegroundColorSpan span = new MutableForegroundColorSpan();
                mSpannableString.setSpan(span, i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                mSpans[i] = span;
            }
            resetAlphas(mTextString.length());
            resetSpannableString(mIsVisible ? 2.0f : 0);
        }
    }

    public void setText(String text) {
        super.setText(text);
        resetIfNeeded();
    }

    @Override
    public void setText(CharSequence text, TextView.BufferType type) {
        super.setText(text, type);
        resetIfNeeded();
    }

    private int clamp(double f) {
        return (int) (255 * Math.min(Math.max(f, 0), 1));
    }

    public void setDuration(int duration) {
        this.mDuration = duration;
        animator.setDuration(duration);
    }
}
