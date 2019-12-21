package com.cbo.myattachment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class CaptureSignature extends AppCompatActivity {
    signature mSignature;
    Paint paint;
    LinearLayout mContent;
    Button clear, save;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.capture_signature);

        save = (Button) findViewById(R.id.save);
        save.setEnabled(false);
        clear = (Button) findViewById(R.id.clear);
        mContent = (LinearLayout) findViewById(R.id.signature_layout);


        intent = getIntent();

        mSignature = new signature(this, null);
        mContent.addView(mSignature);

        save.setOnClickListener(onButtonClick);
        clear.setOnClickListener(onButtonClick);
    }

    Button.OnClickListener onButtonClick = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if (v == clear) {
                mSignature.clear();
            } else if (v == save) {
                mSignature.save();
            }
        }
    };

    public class signature extends View {
        static final float STROKE_WIDTH = 10f;
        static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        Paint paint = new Paint();
        Path path = new Path();

        float lastTouchX;
        float lastTouchY;
        final RectF dirtyRect = new RectF();

        public signature(Context context, AttributeSet attrs) {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }

        public void clear() {
            path.reset();
            invalidate();
            save.setEnabled(false);
        }

        public void save() {


            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        Uri fileUri = (Uri) intent.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
                        //InputStream inputStream = getContentResolver().openInputStream(fileUri);
                        FileOutputStream fos = new FileOutputStream(FileUtil.getRealPathFromURI(CaptureSignature.this, fileUri));
                        Bitmap bm=null;

                        Bitmap returnedBitmap = Bitmap.createBitmap(mContent.getWidth(),
                                mContent.getHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(returnedBitmap);
                        Drawable bgDrawable = mContent.getBackground();
                        if (bgDrawable != null)
                            bgDrawable.draw(canvas);
                        else
                            canvas.drawColor(Color.WHITE);
                        mContent.draw(canvas);


                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        returnedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] byteArray = stream.toByteArray();
                        fos.write(byteArray);
                        fos.close();

                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                onSendResponse();

                            }
                        });

                    } catch (IOException e) {

                        e.printStackTrace();
                    }


                }
            }).start();

        }

        @Override
        protected void onDraw(Canvas canvas) {

            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();
            save.setEnabled(true);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:

                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void resetDirtyRect(float eventX, float eventY) {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }
    }



    public void onSendResponse() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }
}
