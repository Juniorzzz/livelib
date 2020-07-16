#include <jni.h>
#include <assert.h>
#include "libyuv/libyuv.h"

#define YUV_UTILS_JAVA "com/example/livelib/yuv/YuvHelper"

#ifdef __cplusplus
extern "C" {
#endif

static int (*rgbaToI420Func[])(const uint8_t *,int,uint8_t *,int,uint8_t *,int ,uint8_t *,int,int,int)={
    libyuv::ABGRToI420,libyuv::RGBAToI420,libyuv::ARGBToI420,libyuv::BGRAToI420,
    libyuv::RGB24ToI420,libyuv::RGB565ToI420
};

static int (*i420ToRgbaFunc[])(const uint8_t *,int, const uint8_t *,int,const uint8_t *,int,uint8_t *,
            int ,int ,int )={
    libyuv::I420ToABGR,libyuv::I420ToRGBA,libyuv::I420ToARGB,libyuv::I420ToBGRA,
    libyuv::I420ToRGB24,libyuv::I420ToRGB565
};

static void (*rotatePlaneFunc[])(const uint8_t* src,int src_stride,uint8_t* dst,int dst_stride,
                                   int width,int height)={
        libyuv::RotatePlane90,libyuv::RotatePlane180,libyuv::RotatePlane270,
};

static void (*rotateUVFunc[])(const uint8_t* src, int src_stride, uint8_t* dst_a, int dst_stride_a,
                              uint8_t* dst_b, int dst_stride_b, int width, int height)={
        libyuv::RotateUV90,libyuv::RotateUV180,libyuv::RotateUV270,
};

int rgbaToI420(JNIEnv * env,jclass clazz,jbyteArray rgba,jint rgba_stride,
                jbyteArray yuv,jint y_stride,jint u_stride,jint v_stride,
                jint width,jint height,
                int (*func)(const uint8_t *,int,uint8_t *,int,uint8_t *,int ,uint8_t *,int,int,int)){
    size_t ySize=(size_t) (y_stride * height);
    size_t uSize=(size_t) (u_stride * height >> 1);
    jbyte * rgbaData= env->GetByteArrayElements(rgba,JNI_FALSE);
    jbyte * yuvData=env->GetByteArrayElements(yuv,JNI_FALSE);
    int ret=func((const uint8_t *) rgbaData, rgba_stride, (uint8_t *) yuvData, y_stride,
                 (uint8_t *) (yuvData) + ySize, u_stride, (uint8_t *) (yuvData )+ ySize + uSize,
                 v_stride, width, height);
    env->ReleaseByteArrayElements(rgba,rgbaData,JNI_OK);
    env->ReleaseByteArrayElements(yuv,yuvData,JNI_OK);
    return ret;
}

int Jni_RgbaToI420(JNIEnv * env,jclass clazz,jint type,jbyteArray rgba,jbyteArray yuv,jint width,jint height){
    uint8_t cType=(uint8_t) (type & 0x0F);
    int rgba_stride= ((type & 0xF0) >> 4)*width;
    int y_stride=width;
    int u_stride=width>>1;
    int v_stride=u_stride;
    return rgbaToI420(env,clazz,rgba,rgba_stride,yuv,y_stride,u_stride,v_stride,width,height,rgbaToI420Func[cType]);
}

//libyuv中，rgba表示abgrabgrabgr这样的顺序写入文件，java使用的时候习惯rgba表示rgbargbargba写入文件
static JNINativeMethod g_methods[]={
        {"RgbaToI420","(I[B[BII)I",   (void *)Jni_RgbaToI420},
};

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
    JNIEnv* env = nullptr;

    if (vm->GetEnv((void**)&env, JNI_VERSION_1_4) != JNI_OK) {
        return JNI_ERR;
    }
    assert(env != nullptr);
    jclass clazz=env->FindClass(YUV_UTILS_JAVA);
    env->RegisterNatives(clazz, g_methods, (int) (sizeof(g_methods) / sizeof((g_methods)[0])));

    return JNI_VERSION_1_4;
}

JNIEXPORT void JNI_OnUnload(JavaVM *jvm, void *reserved){
    JNIEnv* env = nullptr;

    if (jvm->GetEnv((void**)&env, JNI_VERSION_1_4) != JNI_OK) {
        return;
    }
    jclass clazz=env->FindClass(YUV_UTILS_JAVA);
    env->UnregisterNatives(clazz);
}


#ifdef __cplusplus
}
#endif





