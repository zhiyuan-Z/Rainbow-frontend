package com.rainbow;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import static org.opencv.imgcodecs.Imgcodecs.IMREAD_COLOR;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

// 建立Class一定要让它属于CameraActivity父类，即使用不到camera功能。不然没法用BaseLoaderCallback初始化OpenCV库
public class ColorPixelDetectionActivity extends CameraActivity implements View.OnTouchListener {
    private static final String  TAG              = "PixelDetect::Activity";

    private Mat                  sample_img;
    private ImageView            sample_img_view;
    private android.widget.TextView RTextView, GTextView, BTextView, ColorNameTextView;

    /** Initialize OpenCV */
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @SuppressLint({"ClickableViewAccessibility"})
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
//                    sample_img = Imgcodecs.imread(Environment.getExternalStorageDirectory()
//                            .getAbsolutePath()+"/DCIM/paint.png", IMREAD_COLOR);
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.paint);
//                    Bitmap bitmap = BitmapFactory.decodeFile
//                            (Environment.getExternalStorageDirectory()
//                                   .getAbsolutePath()+"/DCIM/paint.png");
                    sample_img = new Mat();
                    Utils.bitmapToMat(bitmap, sample_img);
                } break;
                default: {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public ColorPixelDetectionActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.color_pixel_detection_surface_view);

        RTextView = findViewById(R.id.pixel_r_value);
        GTextView = findViewById(R.id.pixel_g_value);
        BTextView = findViewById(R.id.pixel_b_value);
        ColorNameTextView = findViewById(R.id.pixel_color_name);

        sample_img_view = findViewById(R.id.sample_img_view);
        sample_img_view.setOnTouchListener(ColorPixelDetectionActivity.this);

        Log.i("Success", "onCreate launched");
    }

    /** Check whether OpenCV is successfully loaded. */
    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, getApplicationContext(), mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch (View v, MotionEvent event) {
        // event.getX / getY 获得的是基于ImageView大小的点击坐标
        // 所以要换算成基于图片本身的坐标 才能获得正确的像素点
        int cols = sample_img.cols(); // width
        int rows = sample_img.rows(); // height
        int view_cols = sample_img_view.getMeasuredWidth();
        int view_rows = sample_img_view.getMeasuredHeight();
        int rawX = (int)event.getX();
        int rawY = (int)event.getY();
        int x = 0; int y = 0;
        // get touch coordinates
        if ((double)cols / rows > (double)view_cols / view_rows) {
            // image wider than ImageView
            x = rawX * cols / view_cols;
            int yOffset = (view_rows - rows * view_cols / cols) / 2;
            y =  (rawY - yOffset) * rows / (view_rows - 2 * yOffset);
        } else {
            // image higher than ImageView / same proportion as ImageView
            y = rawY * rows / view_rows;
            int xOffset = (view_cols - cols * view_rows / rows) / 2;
            x = (rawX - xOffset) * cols / (view_cols - 2 * xOffset);
        }
        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");
        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;
        // get pixel point BGR (byte)
        byte[] TouchedPixel = new byte[sample_img.channels()];
        // notice: get(rows, cols, xxxx)
        // so it should be (y,x)!!! not (x,y)
        sample_img.get(y, x, TouchedPixel);
        // transform BGR(byte) to BGR(int)
        int PixelR = TouchedPixel[0]&0xff;
        int PixelG = TouchedPixel[1]&0xff;
        int PixelB = TouchedPixel[2]&0xff;
        // show pixel RGB on the screen after clicking
        RTextView.setText("R = " + String.valueOf(PixelR));
        GTextView.setText( "G = " + String.valueOf(PixelG));
        BTextView.setText( "B = " + String.valueOf(PixelB));
        ColorNameTextView.setText("This color is: " + getColorFromRGB(PixelR, PixelG, PixelB));
        return false; // don't need subsequent touch events
        // comment: When reading the image with imread(), the Mat value point is organized in the
        //          sequence of "B-G-R"; but when using BitmapFactory.decodeResource
        //          + Utils.bitmapToMat, the sequence is "R-G-B".

        //FileRead: imread() is more concise to code, but I met with a bug: only .png can be read.
        //          The function can't receive other format and will return a 0x0 Mat.
        //          Thus I choose to use bitmapToMat() here.
    }

    /** Initialize color list */
    /** data from: https://xkcd.com/color/rgb.txt */
    public static HashMap<String, String> ColorName = new HashMap<String, String>();
    static {
        ColorName.put("F0F8FF", "AliceBlue");
        ColorName.put("FAEBD7", "AntiqueWhite");
//        ColorName.put("00FFFF", "Aqua");
        ColorName.put("7FFFD4", "Aquamarine");
        ColorName.put("F0FFFF", "Azure");
        ColorName.put("F5F5DC", "Beige");
        ColorName.put("FFE4C4", "Bisque");
        ColorName.put("000000", "Black");
        ColorName.put("FFEBCD", "BlanchedAlmond");
        ColorName.put("0000FF", "Blue");
        ColorName.put("8A2BE2", "BlueViolet");
        ColorName.put("A52A2A", "Brown");
        ColorName.put("DEB887", "BurlyWood");
        ColorName.put("5F9EA0", "CadetBlue");
        ColorName.put("7FFF00", "Chartreuse");
        ColorName.put("D2691E", "Chocolate");
        ColorName.put("FF7F50", "Coral");
        ColorName.put("6495ED", "CornflowerBlue");
        ColorName.put("FFF8DC", "Cornsilk");
        ColorName.put("DC143C", "Crimson");
        ColorName.put("00FFFF", "Cyan");
        ColorName.put("00008B", "DarkBlue");
        ColorName.put("008B8B", "DarkCyan");
        ColorName.put("B8860B", "DarkGoldenRod");
        ColorName.put("A9A9A9", "DarkGray");
        ColorName.put("006400", "DarkGreen");
        ColorName.put("BDB76B", "DarkKhaki");
        ColorName.put("8B008B", "DarkMagenta");
        ColorName.put("556B2F", "DarkOliveGreen");
        ColorName.put("FF8C00", "DarkOrange");
        ColorName.put("9932CC", "DarkOrchid");
        ColorName.put("8B0000", "DarkRed");
        ColorName.put("E9967A", "DarkSalmon");
        ColorName.put("8FBC8F", "DarkSeaGreen");
        ColorName.put("483D8B", "DarkSlateBlue");
        ColorName.put("2F4F4F", "DarkSlateGray");
        ColorName.put("00CED1", "DarkTurquoise");
        ColorName.put("9400D3", "DarkViolet");
        ColorName.put("FF1493", "DeepPink");
        ColorName.put("00BFFF", "DeepSkyBlue");
        ColorName.put("696969", "DimGray");
        ColorName.put("1E90FF", "DodgerBlue");
        ColorName.put("B22222", "FireBrick");
        ColorName.put("FFFAF0", "FloralWhite");
        ColorName.put("228B22", "ForestGreen");
//        ColorName.put("FF00FF", "Fuchsia");
        ColorName.put("DCDCDC", "Gainsboro");
        ColorName.put("F8F8FF", "GhostWhite");
        ColorName.put("FFD700", "Gold");
        ColorName.put("DAA520", "GoldenRod");
        ColorName.put("808080", "Gray");
        ColorName.put("008000", "Green");
        ColorName.put("ADFF2F", "GreenYellow");
        ColorName.put("F0FFF0", "HoneyDew");
        ColorName.put("FF69B4", "HotPink");
        ColorName.put("CD5C5C", "IndianRed");
        ColorName.put("4B0082", "Indigo");
        ColorName.put("FFFFF0", "Ivory");
        ColorName.put("F0E68C", "Khaki");
        ColorName.put("E6E6FA", "Lavender");
        ColorName.put("FFF0F5", "LavenderBlush");
        ColorName.put("7CFC00", "LawnGreen");
        ColorName.put("FFFACD", "LemonChiffon");
        ColorName.put("ADD8E6", "LightBlue");
        ColorName.put("F08080", "LightCoral");
        ColorName.put("E0FFFF", "LightCyan");
        ColorName.put("FAFAD2", "LightGoldenRodYellow");
        ColorName.put("D3D3D3", "LightGray");
        ColorName.put("90EE90", "LightGreen");
        ColorName.put("FFB6C1", "LightPink");
        ColorName.put("FFA07A", "LightSalmon");
        ColorName.put("20B2AA", "LightSeaGreen");
        ColorName.put("87CEFA", "LightSkyBlue");
        ColorName.put("778899", "LightSlateGray");
        ColorName.put("B0C4DE", "LightSteelBlue");
        ColorName.put("FFFFE0", "LightYellow");
        ColorName.put("00FF00", "Lime");
        ColorName.put("32CD32", "LimeGreen");
        ColorName.put("FAF0E6", "Linen");
        ColorName.put("FF00FF", "Magenta");
        ColorName.put("800000", "Maroon");
        ColorName.put("66CDAA", "MediumAquaMarine");
        ColorName.put("0000CD", "MediumBlue");
        ColorName.put("BA55D3", "MediumOrchid");
        ColorName.put("9370DB", "MediumPurple");
        ColorName.put("3CB371", "MediumSeaGreen");
        ColorName.put("7B68EE", "MediumSlateBlue");
        ColorName.put("00FA9A", "MediumSpringGreen");
        ColorName.put("48D1CC", "MediumTurquoise");
        ColorName.put("C71585", "MediumVioletRed");
        ColorName.put("191970", "MidnightBlue");
        ColorName.put("F5FFFA", "MintCream");
        ColorName.put("FFE4E1", "MistyRose");
        ColorName.put("FFE4B5", "Moccasin");
        ColorName.put("FFDEAD", "NavajoWhite");
        ColorName.put("000080", "Navy");
        ColorName.put("FDF5E6", "OldLace");
        ColorName.put("808000", "Olive");
        ColorName.put("6B8E23", "OliveDrab");
        ColorName.put("FFA500", "Orange");
        ColorName.put("FF4500", "OrangeRed");
        ColorName.put("DA70D6", "Orchid");
        ColorName.put("EEE8AA", "PaleGoldenRod");
        ColorName.put("98FB98", "PaleGreen");
        ColorName.put("AFEEEE", "PaleTurquoise");
        ColorName.put("DB7093", "PaleVioletRed");
        ColorName.put("FFEFD5", "PapayaWhip");
        ColorName.put("FFDAB9", "PeachPuff");
        ColorName.put("CD853F", "Peru");
        ColorName.put("FFC0CB", "Pink");
        ColorName.put("DDA0DD", "Plum");
        ColorName.put("B0E0E6", "PowderBlue");
        ColorName.put("800080", "Purple");
        ColorName.put("FF0000", "Red");
        ColorName.put("BC8F8F", "RosyBrown");
        ColorName.put("4169E1", "RoyalBlue");
        ColorName.put("8B4513", "SaddleBrown");
        ColorName.put("FA8072", "Salmon");
        ColorName.put("F4A460", "SandyBrown");
        ColorName.put("2E8B57", "SeaGreen");
        ColorName.put("FFF5EE", "SeaShell");
        ColorName.put("A0522D", "Sienna");
        ColorName.put("C0C0C0", "Silver");
        ColorName.put("87CEEB", "SkyBlue");
        ColorName.put("6A5ACD", "SlateBlue");
        ColorName.put("708090", "SlateGray");
        ColorName.put("FFFAFA", "Snow");
        ColorName.put("00FF7F", "SpringGreen");
        ColorName.put("4682B4", "SteelBlue");
        ColorName.put("D2B48C", "Tan");
        ColorName.put("008080", "Teal");
        ColorName.put("D8BFD8", "Thistle");
        ColorName.put("FF6347", "Tomato");
        ColorName.put("40E0D0", "Turquoise");
        ColorName.put("EE82EE", "Violet");
        ColorName.put("F5DEB3", "Wheat");
        ColorName.put("FFFFFF", "White");
        ColorName.put("F5F5F5", "WhiteSmoke");
        ColorName.put("FFFF00", "Yellow");
        ColorName.put("9ACD32", "YellowGreen");
    }

    public String getColorFromRGB (int r, int g, int b) {
        String RGBHex = Integer.toHexString(r) + Integer.toHexString(g) + Integer.toHexString(b);
        RGBHex = RGBHex.toUpperCase(Locale.ROOT);
        if (ColorName.containsKey(RGBHex)) {
            return ColorName.get(RGBHex);
        } else {
            return getNearestColor(r, g, b);
        }
    }

    public String getNearestColor (int r, int g, int b) {
        String chosenKey = null;
        double minDistance = 0.0;
        boolean flag = false;
        for (String getKey: ColorName.keySet()) {
            int key_r = convertUpperHextoInt(getKey.substring(0,2));
            int key_g = convertUpperHextoInt(getKey.substring(2,4));
            int key_b = convertUpperHextoInt(getKey.substring(4,6));
            double distance =
                    Math.pow(r - key_r, 2) + Math.pow(g - key_g, 2) + Math.pow(b - key_b, 2);
            if (!flag) { // the first distance calculated
                minDistance = distance;
                chosenKey = getKey;
                flag = true;
            } else {
                if (distance < minDistance) {
                    minDistance = distance;
                    chosenKey = getKey;
                }
            }
        }
        return ColorName.get(chosenKey);
    }

    /** In the hashmap database, the color is provided in form like "FFFFFF".
     * Thus, we need to divide the String into 3 parts and convert them like: FF -> ff -> 255*/
    private int convertUpperHextoInt (String x) {
        x = x.toLowerCase(Locale.ROOT);
        return Integer.parseInt(x,16);
    }

}
