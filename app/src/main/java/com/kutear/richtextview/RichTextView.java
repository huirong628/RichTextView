package com.kutear.richtextview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.Touch;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kutear.guo on 2015/10/26.
 * 富文本显示
 */
public class RichTextView extends TextView {
    private static final String TAG = RichTextView.class.getSimpleName();

    private Drawable mDefaultDrawable;
    private OnImageClickListener mOnImageClickListener;
    private OnUrlClickListener mOnUrlClickListener;
    private ArrayList<BitmapDrawable> mImagelist = new ArrayList<>();
    private float mDefaultDrawableWidth;
    private float mDefaultDrawableHeight;
    private Html.ImageGetter mImageGetter = new Html.ImageGetter() {
        @Override
        public Drawable getDrawable(String source) {
            URLDrawable mUrlDrawable;
            mUrlDrawable = new URLDrawable(mDefaultDrawable);
            doVolleyRequest(source, mUrlDrawable);
            mImagelist.add(mUrlDrawable);
            return mUrlDrawable;
        }

        private void doVolleyRequest(String url, final URLDrawable mUrlDrawable) {
            ImageRequest request = new ImageRequest(url,
                    new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap bitmap) {
                            BitmapDrawable drawable = new BitmapDrawable(getContext().getResources(), ImageCompressUtil.compressByQuality(bitmap, 100));
                            mUrlDrawable.setDrawable(drawable);
                        }
                    }, 480, 480, Bitmap.Config.RGB_565,
                    new Response.ErrorListener() {
                        public void onErrorResponse(VolleyError error) {
                            Drawable drawable = null;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                drawable = getContext().getDrawable(R.drawable.kutear_fialed);
                            } else {
                                drawable = getContext().getResources().getDrawable(R.drawable.kutear_fialed);
                            }
                            mUrlDrawable.setErrorDrawable(drawable);
                        }
                    });
            AppApplication.startRequest(request);
        }
    };

    public RichTextView(Context context) {
        super(context);
        init(context, null);
    }

    public RichTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RichTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RichTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    /**
     * 从xml读取属性
     */
    private void init(Context context, AttributeSet attrs) {
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.RichTextView);
        mDefaultDrawable = mTypedArray.getDrawable(R.styleable.RichTextView_default_drawable);
        if (mDefaultDrawable == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mDefaultDrawable = context.getDrawable(R.drawable.kutear_load);
            } else {
                mDefaultDrawable = context.getResources().getDrawable(R.drawable.kutear_load);
            }
        }
        mDefaultDrawableHeight = mTypedArray.getDimension(R.styleable.RichTextView_default_drawable_height, 0);
        mDefaultDrawableWidth = mTypedArray.getDimension(R.styleable.RichTextView_default_drawable_width, 0);
        mTypedArray.recycle();
    }

    /**
     * 图片点击响应
     *
     * @param onImageClickListener
     */
    public void setOnImageClickListener(OnImageClickListener onImageClickListener) {
        this.mOnImageClickListener = onImageClickListener;
    }

    /**
     * 链接点击响应
     *
     * @param onUrlClickListener
     */
    public void setOnUrlClickListener(OnUrlClickListener onUrlClickListener) {
        this.mOnUrlClickListener = onUrlClickListener;
    }

    public void setHtml(String html) {
        Spanned spanned = Html.fromHtml(html, mImageGetter, null);
        SpannableStringBuilder spannableStringBuilder;
        if (spanned instanceof SpannableStringBuilder) {
            spannableStringBuilder = (SpannableStringBuilder) spanned;
        } else {
            spannableStringBuilder = new SpannableStringBuilder(spanned);
        }

        ImageSpan[] imageSpans = spannableStringBuilder.getSpans(0, spannableStringBuilder.length(), ImageSpan.class);
        final List<String> imageUrls = new ArrayList<>();

        for (int i = 0, size = imageSpans.length; i < size; i++) {
            ImageSpan imageSpan = imageSpans[i];
            String imageUrl = imageSpan.getSource();
            int start = spannableStringBuilder.getSpanStart(imageSpan);
            int end = spannableStringBuilder.getSpanEnd(imageSpan);
            imageUrls.add(imageUrl);
            final int finalI = i;
            //使用自己的ClickableSpan去代替系统原本的的Clickable
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    if (mOnImageClickListener != null) {
                        mOnImageClickListener.imageClicked(imageUrls, finalI);
                    }
                }
            };
            //移除原本的
            ClickableSpan[] clickableSpans = spannableStringBuilder.getSpans(start, end, ClickableSpan.class);
            if (clickableSpans != null && clickableSpans.length != 0) {
                for (ClickableSpan cs : clickableSpans) {
                    spannableStringBuilder.removeSpan(cs);
                }
            }
            //添加自定义的
            spannableStringBuilder.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        super.setText(spanned);
        setMovementMethod(new CustomLinkMovementMethod());
    }


    public interface OnImageClickListener {
        void imageClicked(List<String> imageUrls, int position);
    }

    public interface OnUrlClickListener {
        void urlClicked(String url);
    }

    public void recycle() {
        for (BitmapDrawable items : mImagelist) {
            Bitmap bmp = items.getBitmap();
            if (bmp != null && !bmp.isRecycled()) {
                bmp.recycle();
                System.gc();
                items = null;
            }
        }
    }

    /**
     * 重置默认的点击链接打开默认浏览器
     */
    private final class CustomLinkMovementMethod extends LinkMovementMethod {
        @Override
        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP ||
                    action == MotionEvent.ACTION_DOWN) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();

                x += widget.getScrollX();
                y += widget.getScrollY();

                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);
                if (link.length != 0) {
                    if (action == MotionEvent.ACTION_UP) {
                        //强制转向自己的WebView,不响应link的onClick事件
                        if (link[0] instanceof URLSpan) {
                            String url = ((URLSpan) link[0]).getURL();
                            if (mOnUrlClickListener != null) {
                                mOnUrlClickListener.urlClicked(url);
                                return true;
                            }
                        } else {
                            //其他情况默认处理,比如图片点击响应
                            //实际图片对应的是函数setHtml()中的匿名ClickableSpan
                            //则会执行上面的OnClick
                            link[0].onClick(widget);
                        }
                    } else {
                        Selection.setSelection(buffer,
                                buffer.getSpanStart(link[0]),
                                buffer.getSpanEnd(link[0]));
                    }
                    return true;
                } else {
                    Selection.removeSelection(buffer);
                    Touch.onTouchEvent(widget, buffer, event);
                    return false;
                }
            }
            return Touch.onTouchEvent(widget, buffer, event);
        }
    }


    private final class URLDrawable extends BitmapDrawable {
        private Drawable drawable;

        public URLDrawable(Drawable defaultDrawable) {
            this.drawable = defaultDrawable;
            initDefaultBounds();
        }

        private void initDefaultBounds() {
            if (mDefaultDrawableWidth == 0) {
                mDefaultDrawableWidth = (float) (DeviceInfo.getScreenWidth(getContext()) * 0.8);
            }
            if (mDefaultDrawableHeight == 0) {
                mDefaultDrawableHeight = AndroidUtils.convertDpToPixel(100);
            }
            setBoundsByWH((int) mDefaultDrawableWidth, (int) mDefaultDrawableHeight);
        }

        public void setErrorDrawable(Drawable drawable) {
            this.drawable = drawable;
            initDefaultBounds();
        }

        @Override
        public void draw(Canvas canvas) {
            if (drawable != null)
                drawable.draw(canvas);
        }

        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
            float multiplier = (float) (DeviceInfo.getScreenWidth(getContext()) * 0.8) / (float) drawable.getIntrinsicWidth();
            int width = (int) (drawable.getIntrinsicWidth() * multiplier);
            int height = (int) (drawable.getIntrinsicHeight() * multiplier);

            setBoundsByWH(width, height);
        }

        private void invalidate() {
            RichTextView.this.invalidate();
            RichTextView.this.setText(RichTextView.this.getText());
        }

        /**
         * 根据设备信息设置宽高
         */
        private void setBoundsByWH(int w, int h) {
            //非常重要,否则图片只占位,不显示
            int left = (int) (DeviceInfo.getScreenWidth(getContext()) * 0.1);
            drawable.setBounds(left, (int) AndroidUtils.convertDpToPixel(10), w, h);
            setBounds(left, 0, w + left, h + (int) AndroidUtils.convertDpToPixel(10));
            invalidate();
        }
    }

}
