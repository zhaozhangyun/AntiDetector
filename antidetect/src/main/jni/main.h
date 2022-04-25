#ifndef MAIN_H_
#define MAIN_H_

#include <jni.h>
#include <stdlib.h>

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved);

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved);

extern JavaVM *vm;

JNIEnv *getEnv();

JNIEnv *ensureEnvCreated();

#endif // MAIN_H_