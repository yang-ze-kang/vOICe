package com.example.lbstest.util;

import android.graphics.Bitmap;
import java.io.IOException;
import io.reactivex.annotations.Nullable;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
public class BitmapRequestBody extends RequestBody {
    private Bitmap bitmap;
    private Bitmap.CompressFormat format;
    private int quality;
    public BitmapRequestBody(Bitmap bitmap) {
        this.bitmap = bitmap;
        this.format = Bitmap.CompressFormat.JPEG;
        this.quality = 100;
    }
    public BitmapRequestBody(Bitmap bitmap, Bitmap.CompressFormat format) {
        this.bitmap = bitmap;
        this.format = format;
        this.quality = 100;
    }
    public BitmapRequestBody(Bitmap bitmap, int quality) {
        this.bitmap = bitmap;
        this.quality = quality;
        this.format = Bitmap.CompressFormat.JPEG;
    }
    public BitmapRequestBody(Bitmap bitmap, Bitmap.CompressFormat format, int quality) {
        this.bitmap = bitmap;
        this.format = format;
        this.quality = quality;
    }
    @Nullable
    @Override
    public MediaType contentType() {
        if (format == Bitmap.CompressFormat.WEBP) {
            return MediaType.parse("image/webp");
        } else if (format == Bitmap.CompressFormat.PNG) {
            return MediaType.parse("image/png");
        } else {
            return MediaType.parse("image/jpeg");
        }
    }
    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        bitmap.compress(format, quality, sink.outputStream());
    }
}