package com.studioios.linhlee.icamera.filter;

import android.content.Context;

import com.studioios.linhlee.icamera.R;

/**
 * Created by ChuOnChuonOt on 10/24/2016.
 */

public class IFStarFilter extends IFImageFilter {
    public static final String STAR = "varying highp vec2 textureCoordinate;\n" +
            " varying highp vec2 textureCoordinate2;\n" +
            "\n" +
            " uniform sampler2D inputImageTexture;\n" +
            " uniform sampler2D inputImageTexture2;\n" +
            " \n" +
            " void main()\n" +
            " {\n" +
            "     mediump vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "     mediump vec4 textureColor2 = texture2D(inputImageTexture2, textureCoordinate2);\n" +
            "     gl_FragColor = vec4(abs(textureColor2.rgb - textureColor.rgb), textureColor.a);\n" +
            " }";

    public IFStarFilter(Context paramContext) {
        super(paramContext, STAR);
        setRes();
    }

    private void setRes() {
        addInputTexture(R.drawable.com_rcplatform_opengl_filter_shadow_18);
    }
}
