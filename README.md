# RichTextView
富文本显示的TextView


请求图片时使用Volley,也可以根据自己的情况改写ImageGetter

---
##基本用法

    public class MainActivity extends AppCompatActivity implements RichTextView.OnImageClickListener, RichTextView.OnUrlClickListener

    mRichTextView.setHtml(html);
    mRichTextView.setOnImageClickListener(this);
    mRichTextView.setOnUrlClickListener(this);
    
    @Override
        public void imageClicked(List<String> imageUrls, int position) {
            Toast.makeText(this,"图片地址:"+imageUrls.get(position),Toast.LENGTH_SHORT).show();
        }
    
        @Override
        public void urlClicked(String url) {
            Toast.makeText(this,"链接地址:"+url,Toast.LENGTH_SHORT).show();
        }

---

    public Drawable getDrawable(String source) {
            URLDrawable mUrlDrawable;
            mUrlDrawable = new URLDrawable(mDefaultDrawable);
            doVolleyRequest(source, mUrlDrawable);
            mImagelist.add(mUrlDrawable);
            return mUrlDrawable;
        }
        
    private void doVolleyRequest(String url, final URLDrawable mUrlDrawable)    {
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
      
比如我们可以使用原本的请求 类似[gist](https://gist.github.com/Kutear/00479c8f9d1c35093e27)

具体内容可看源码和参考[KuTear](http://www.kutear.com/index.php/archives/android_textview_show_html.html)
![](http://kutear.qiniudn.com/2015/10/534089544.png-logo)
