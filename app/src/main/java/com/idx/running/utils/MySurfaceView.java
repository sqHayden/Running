package com.idx.running.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MySurfaceView extends GLSurfaceView {
    private SceneRenderer mRenderer;
    public Context context;
    private int one = 0x10000;
    private Bitmap bitmap;
    private int[] textureids;
    private IntBuffer vertexBuffer;
    private IntBuffer texBuffer;
    // 旋转方向
    private float xrot, yrot, zrot;
    // 正方体顶点
    private int[] vertices = {
            one, one, -one,
            -one, one, -one,
            one, one, one,
            -one, one, one,

            one, -one, one,
            -one, -one, one,
            one, -one, -one,
            -one, -one, -one,

            one, one, one,
            -one, one, one,
            one, -one, one,
            -one, -one, one,

            one, -one, -one,
            -one, -one, -one,
            one, one, -one,
            -one, one, -one,

            -one, one, one,
            -one, one, -one,
            -one, -one, one,
            -one, -one, -one,

            one, one, -one,
            one, one, one,
            one, -one, -one,
            one, -one, one
    };
    //纹理点
    private int[] texCoords = {
            0, one,
            one, one,
            0, 0,
            one, 0
    };
    public MySurfaceView(Context context) {
        super(context);
        //传入定义的渲染器对象
        mRenderer = new SceneRenderer();
        setRenderer(mRenderer);
        //设置渲染模式为连续渲染
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        // 初始化
        textureids = new int[1];
        // 实例化bitmap
        bitmap = BitGL.bitmap;
        /*创建顶点坐标数据缓存，由于不同平台字节顺序不同，数据单元不是字节的（上面的事整型的缓存）
        一定要经过ByteBuffer转换，关键是通过ByteOrder设置nativeOrder()*/
        //---------------------------------------------------
        //缓存顶点数组
        //分配的内存块
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        //设置本地平台的字节顺序
        vbb.order(ByteOrder.nativeOrder());
        //转换为int型缓冲
        vertexBuffer = vbb.asIntBuffer();
        //向缓冲区中放入顶点坐标数据
        vertexBuffer.put(vertices);
        //设置缓冲区的起始位置
        vertexBuffer.position(0);
        //----------------------------------------------------
        //缓存纹理数组
        ByteBuffer tbb = ByteBuffer.allocateDirect(texCoords.length * 4 * 6);
        tbb.order(ByteOrder.nativeOrder());
        texBuffer = tbb.asIntBuffer();
        //为每一个面贴上纹理
        for (int i = 0; i < 6; i++) {
            texBuffer.put(texCoords);
        }
        texBuffer.position(0);
    }
    private class SceneRenderer implements Renderer {
        //小星星对象
        Celestial celestialSmall;
        //大星星对象
        Celestial celestialBig;
        //连续绘制函数
        public void onDrawFrame(GL10 gl) {
            //清除屏幕缓存和深度缓存
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            //启用顶点坐标数据
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            //启用纹理贴图坐标数据
            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
            //设置当前矩阵堆栈为模型堆栈
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            //关闭光照效果
            gl.glDisable(GL10.GL_LIGHTING);
            //重置当前的模型视图矩阵
            gl.glLoadIdentity();
            //光照属性(镜面光，前后受光3.5f)
            gl.glMaterialf(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, 3.5f);
            //设置顶点的位置数据
            gl.glVertexPointer(3, GL10.GL_FIXED, 0, vertexBuffer);
            //设置纹理点坐标数据
            gl.glTexCoordPointer(2, GL10.GL_FIXED, 0, texBuffer);
            //向z轴里移入6.0f
            gl.glTranslatef(0.0f, 0.0f, -6.0f);
            //入栈保存
            gl.glPushMatrix();
            // 设置3个方向的旋转
            gl.glRotatef(xrot, one, 0.0f, 0.0f);
            gl.glRotatef(yrot, 0.0f, one, 0.0f);
            gl.glRotatef(zrot, 0.0f, 0.0f, one);
            //缩放
            gl.glScalef(0.75f,0.75f, 0.75f);
            // 绘制正方体
            for (int i = 0; i < 6; i++) {
                gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, i * 4, 4);
            }
            //关闭顶点坐标功能
            gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
            //关闭纹理坐标功能
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            // 设置旋转角度
            xrot += 0.5f;
            yrot += 0.6f;
            zrot += 0.3f;
            //出栈恢复
            gl.glPopMatrix();
            //入栈保存
            gl.glPushMatrix();
            gl.glTranslatef(0, -24.0f, 0.0f);
            celestialSmall.drawSelf(gl);
            celestialBig.drawSelf(gl);
            //出栈恢复
            gl.glPopMatrix();
        }
        //视图改变函数
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            //设置3D视窗的大小及位置
            gl.glViewport(0, 0, width, height);
            //将当前矩阵模式设为投影矩阵
            gl.glMatrixMode(GL10.GL_PROJECTION);
            //初始化单位矩阵
            gl.glLoadIdentity();
            //计算透视视窗的宽度、高度比
            float ratio = (float) width / height;
            //调用此方法设置透视视窗的空间大小
            gl.glFrustumf(-ratio * 0.5f, ratio * 0.5f, -0.5f, 0.5f, 1, 100);
        }
        //视图创建函数
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            //关闭抗抖动
            gl.glDisable(GL10.GL_DITHER);
            //设置特定Hint项目的模式，这里设置为使用快速模式
            gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
            //设置屏幕背景色黑色rgb+a
            gl.glClearColor(0, 0, 0, 0);
            //设置着色模型为平滑着色
            gl.glShadeModel(GL10.GL_SMOOTH);
            //启用深度测试
            gl.glEnable(GL10.GL_DEPTH_TEST);
            //设置为打开背面剪裁
            gl.glEnable(GL10.GL_CULL_FACE);
            //创建星空
            celestialSmall = new Celestial(0, 0, 3, 0, 1250);
            celestialBig = new Celestial(0, 0, 5, 0,800);
            //启用纹理功能
            gl.glEnable(GL10.GL_TEXTURE_2D);
            // 创建纹理
            gl.glGenTextures(1, textureids, 0);
            // 绑定要使用的纹理
            gl.glBindTexture(GL10.GL_TEXTURE_2D, textureids[0]);
            // 生成纹理
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
            // 线性滤波
            gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
                    GL10.GL_LINEAR);
            gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
                    GL10.GL_LINEAR);
            //开启线程使星空背景移动
            new Thread() {
                public void run() {
                    while (true) {
                        celestialSmall.yAngle += 0.5;
                        if (celestialSmall.yAngle >= 360) {
                            celestialSmall.yAngle = 0;
                        }
                        celestialBig.yAngle += 0.5;
                        if (celestialBig.yAngle >= 360) {
                            celestialBig.yAngle = 0;
                        }
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        }
    }
}
