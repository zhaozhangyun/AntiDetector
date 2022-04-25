#include "main.h"
#include <sys/system_properties.h>


static jstring n_getSystemProperties(JNIEnv *env, jclass klass, jstring key) {
    const char *key_str = env->GetStringUTFChars(key, 0);
    jstring ret = NULL;
    char value[93] = "";
    __system_property_get(key_str, value);
    ret = env->NewStringUTF(value);
    return ret;
}

static const char *native_class_name = "com/z/zz/zzz/antidetector/MainActivity";
static JNINativeMethod methods[] = {
        {"getSystemProperties", "(Ljava/lang/String;)Ljava/lang/String;", (void *) n_getSystemProperties}
};

static int registerNativeMethods(JNIEnv *env, const char *className, JNINativeMethod *gMethods,
                                 int numMethods) {
    jclass clazz = env->FindClass(className);
    if (clazz == NULL) {
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

static int registerNatives(JNIEnv *env) {
    if (!registerNativeMethods(env, native_class_name, methods,
                               sizeof(methods) / sizeof(methods[0]))) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

typedef union {
    JNIEnv *env;
    void *venv;
} UnionJNIEnvToVoid;

JavaVM *vm;

JNIEnv *getEnv() {
    JNIEnv *env;
    vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_4);
    return env;
}

JNIEnv *ensureEnvCreated() {
    JNIEnv *env = getEnv();
    if (env == NULL) {
        vm->AttachCurrentThread(&env, NULL);
    }
    return env;
}

jint JNI_OnLoad(JavaVM *_vm, void * /*reserved*/) {
    vm = _vm;
    UnionJNIEnvToVoid uenv;
    uenv.venv = NULL;
    jint result = -1;
    JNIEnv *env = NULL;

    if (_vm->GetEnv(&uenv.venv, JNI_VERSION_1_4) != JNI_OK) {
        goto bail;
    }
    env = uenv.env;
    if (registerNatives(env) != JNI_TRUE) {
        goto bail;
    }

    result = JNI_VERSION_1_4;

    bail:
    return result;
}
