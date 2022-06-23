#include <jni.h>
#include <string>
#include <android/asset_manager_jni.h>
#include <android/bitmap.h>
#include <android/log.h>

#include <jni.h>
#include <cmath>

#include <vector>

// ncnn
#include "layer.h"
#include "net.h"
#include "benchmark.h"

static ncnn::UnlockedPoolAllocator g_blob_pool_allocator;
static ncnn::PoolAllocator g_workspace_pool_allocator;
static ncnn::Net SSD;

struct Object
{
    float x;
    float y;
    float w;
    float h;
    int lable;
    float prob;
};

static float soft_sum(std::vector<float>& v){
    float sum=0;
    float len=0;

    for(float f:v){
        sum+=f;
    }
    for (int i = 0; i < v.size(); i++)
    {
        float a=v[i]/sum*i;
//        v[i]=a;
        len+=a;

    }
    return len;


}

static inline float intersection_area(const Object& a, const Object& b)
{
    float zuo_x=std::max(a.x-0.5*a.w,b.x-0.5*b.w);
    float zuo_y=std::max(a.y-0.5*a.h,b.y-0.5*b.h);

    float you_x=std::min(a.x+0.5*a.w,b.x+0.5*b.w);
    float you_y=std::min(a.y+0.5*a.h,b.y+0.5*b.h);

    float inter_width=you_x-zuo_x;
    float inter_height=you_y-zuo_y;

    if(inter_height<=0 || inter_width<=0){
        return 0.f;
    }

    return inter_width * inter_height;
}

static void qsort_descent_inplace(std::vector<Object>& faceobjects, int left, int right)
{
    int i = left;
    int j = right;
    float p = faceobjects[(left + right) / 2].prob;

    while (i <= j)
    {
        while (faceobjects[i].prob > p)
            i++;

        while (faceobjects[j].prob < p)
            j--;

        if (i <= j)
        {
            // swap
            std::swap(faceobjects[i], faceobjects[j]);

            i++;
            j--;
        }
    }

#pragma omp parallel sections
    {
#pragma omp section
        {
            if (left < j) qsort_descent_inplace(faceobjects, left, j);
        }
#pragma omp section
        {
            if (i < right) qsort_descent_inplace(faceobjects, i, right);
        }
    }
}

static void nms_sorted_bboxes(std::vector<Object>& faceobjects,std::vector<Object>& picked,float nms_threshold)
{
    picked.clear();
    const int n=faceobjects.size();
    std::vector<float> areas(n);
//    for(int i=0;i<n;i++)
//    {
//        areas[i]=faceobjects[i].w*faceobjects[i].h;
//    }

    for(int i=0;i<n;i++)
    {
        const Object& a=faceobjects[i];
        int keep=1;

        for (int j = 0; j < (int)picked.size(); j++) {
            const Object& b=picked[j];
            float area_a=a.w*a.h;
            float area_b=b.w*b.h;
            float jiao=intersection_area(a,b);
            if (jiao/(area_a+area_b-jiao)>nms_threshold)
                keep=0;
        }


        if (keep)
            picked.push_back(a);
    }



}


extern "C" JNIEXPORT jstring JNICALL
Java_com_myapp_mycamera_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}



// FIXME DeleteGlobalRef is missing for objCls
static jclass objCls = NULL;
static jmethodID constructortorId;
static jfieldID xId;
static jfieldID yId;
static jfieldID wId;
static jfieldID hId;
static jfieldID labelId;
static jfieldID probId;

//初始化函数
extern "C" JNIEXPORT jboolean JNICALL

Java_com_myapp_mycamera_SSd_Init(JNIEnv *env, jobject thiz, jobject assetManager) {
    // TODO: implement Init()
    ncnn::Option opt;
    opt.lightmode = true;
    opt.num_threads = 4;
    opt.blob_allocator = &g_blob_pool_allocator;
    opt.workspace_allocator = &g_workspace_pool_allocator;
    opt.use_packing_layout = true;


    // use vulkan compute
    if (ncnn::get_gpu_count() != 0)
        opt.use_vulkan_compute = true;

    AAssetManager* mgr = AAssetManager_fromJava(env, assetManager);

    SSD.opt = opt;
    // init param
    {
        int ret = SSD.load_param(mgr, "model.param");
        if (ret != 0)
        {
            __android_log_print(ANDROID_LOG_DEBUG, "SSDNcnn", "load_param failed");
            return JNI_FALSE;
        }
    }

    // init bin
    {
        int ret = SSD.load_model(mgr, "model.bin");
        if (ret != 0)
        {
            __android_log_print(ANDROID_LOG_DEBUG, "SSDNcnn", "load_model failed");
            return JNI_FALSE;
        }
    }
    // init jni glue
    //获取java中的对应实例类
    jclass localObjCls = env->FindClass("com/myapp/mycamera/SSd$Obj");
    objCls = reinterpret_cast<jclass>(env->NewGlobalRef(localObjCls));


    constructortorId = env->GetMethodID(objCls, "<init>", "(Lcom/myapp/mycamera/SSd;)V");

    xId = env->GetFieldID(objCls, "x", "F");
    yId = env->GetFieldID(objCls, "y", "F");
    wId = env->GetFieldID(objCls, "w", "F");
    hId = env->GetFieldID(objCls, "h", "F");
    labelId = env->GetFieldID(objCls, "label", "Ljava/lang/String;");
    probId = env->GetFieldID(objCls, "prob", "F");



    return JNI_TRUE;

}

static int max(int a,int b){
    if (a>b){
        return a;
    } else{return b;}
}
static int min(int a,int b){
    if (a>b){
        return b;
    } else{return a;}
}

extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_myapp_mycamera_SSd_Detect(JNIEnv *env, jobject thiz, jobject bitmap, jboolean use_gpu) {

    // TODO: implement Detect()
    if (use_gpu == JNI_TRUE && ncnn::get_gpu_count() == 0)
    {
        return NULL;
        //return env->NewStringUTF("no vulkan capable gpu");
    }
    //计数当前时间
    double start_time = ncnn::get_current_time();

    AndroidBitmapInfo info;
    AndroidBitmap_getInfo(env, bitmap, &info);
    //原始图像的宽和高
    const int width = info.width;
    const int height = info.height;
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888)
        return NULL;
    // ncnn from bitmap
    const int target_size = 416;
    ncnn::Mat in = ncnn::Mat::from_android_bitmap_resize(env, bitmap, ncnn::Mat::PIXEL_RGB, target_size, target_size);

    const float norm_vals[3] = {1 / 255.f, 1 / 255.f, 1 / 255.f};
    in.substract_mean_normalize(0, norm_vals);

    ncnn::Extractor ex = SSD.create_extractor();

    ex.set_vulkan_compute(use_gpu);

    ex.input("data", in);


    ncnn::Mat out;
    ex.extract("output", out);

    const int num_boxs=out.h;
    const int num_w=out.w;
    const float thred=0.4;
    float stride;

    int x=0;
    int y=0;

    //符合要求的盒子都放入该容器中
    std::vector<Object> objects;

    for(int i=0;i<num_boxs;i++){
        if(i<2704){
            stride=8;
            int num_row=i/52;
            int cols=i%52;
            x=cols*(416/52);
            y=num_row*(416/52);

        } else if (i>=2704 && i<3380){
            stride=16;
            int num_row=(i-2704)/26;
            int cols=(i-2704)%26;
            x=cols*(416/26);
            y=num_row*(416/26);
        } else if (i>=3380 && i<3594){
            stride=32;
            int num_row=(i-3380)/13;
            int cols=(i-3380)%13;
            x=cols*(416/13);
            y=num_row*(416/13);
        } else{
            stride=64;
            int num_row=(i-3594)/7;
            int cols=(i-3594)%7;
            x=cols*(416/7);
            y=num_row*(416/7);
        }

        //取出每一行数据
        const float* featptr=out.row(i);


        if(featptr[0]<thred){
            continue;
        }
        float score=featptr[0];


//        int class_index=0;
//        float thred_score=0.4f;
//        int class_id=-1;
        Object obj;

        std::vector<float> lengs;
        lengs.clear();

        std::vector<float> v1;
        //遍历每一行,进行softmax.
        for (int k=1;k<num_w;k++){
            //创建一个数组用于存放后的数据


            if (k%8==0){
                float a=exp(featptr[k]);
                v1.push_back(a);
                float len=soft_sum(v1)*stride;
                lengs.push_back(len);

                v1.clear();
            } else{
                float a=exp(featptr[k]);
                v1.push_back(a);
            }

        }

        int x1=max(x-lengs[0],0);
        int y1=max(y-lengs[1],0);
        int x2=min(x+lengs[2],416);
        int y2=min(y+lengs[3],416);
        obj.x=float (x1+x2)/2;
        obj.y=float(y1+y2)/2;
        obj.w=x2-x1;
        obj.h=y2-y1;
        obj.lable=0;
        obj.prob=score;

        objects.push_back(obj);

    }

    //按照置信度从大到小排序
    if (objects.empty()){
        return NULL;
    }
    if (objects.size()>1){
        qsort_descent_inplace(objects,0,objects.size()-1);
    }
    //NMS非极大值抑制
    std::vector<Object> picked;
    nms_sorted_bboxes(objects, picked, 0.45f);



    static const char* class_name[]={"小鸟"};

    jobjectArray jObjArray = env->NewObjectArray(picked.size(), objCls, NULL);
    for(int i=0;i<picked.size();i++)
    {
        jobject jObj = env->NewObject(objCls, constructortorId, thiz);
        env->SetFloatField(jObj, xId, picked[i].x/416);
        env->SetFloatField(jObj, yId, picked[i].y/416);
        env->SetFloatField(jObj, wId, picked[i].w/416);
        env->SetFloatField(jObj, hId, picked[i].h/416);
        env->SetObjectField(jObj, labelId, env->NewStringUTF(class_name[picked[i].lable]));
        env->SetFloatField(jObj, probId, picked[i].prob);
        env->SetObjectArrayElement(jObjArray, i, jObj);
    }

    return jObjArray;


}