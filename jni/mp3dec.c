#define MINIMP3_ONLY_MP3
#define MINIMP3_ONLY_SIMD

#define MINIMP3_IMPLEMENTATION

#include "snw_test_PureMP3Decoder.h"
#include "../lib/minimp3_ex.h"

void set_int_field(JNIEnv * env, jclass objectClass, jobject object, const char* fieldName, int value) {
    jfieldID fid = (*env)->GetFieldID(env, objectClass, fieldName, "I");
    (*env)->SetIntField(env, object, fid, value);
}

void set_long_field(JNIEnv * env, jclass objectClass, jobject object, const char* fieldName, long value) {
    jfieldID fid = (*env)->GetFieldID(env, objectClass, fieldName, "J");
    (*env)->SetLongField(env, object, fid, value);
}

JNIEXPORT jboolean JNICALL Java_snw_test_PureMP3Decoder_decodeMp3
  (JNIEnv * env, jclass thisClass, jstring jFileName, jobject jInfo) {
    mp3dec_t mp3d;
    mp3dec_file_info_t info;

    const char* fileName = (*env)->GetStringUTFChars(env, jFileName, NULL);
    if(!fileName) return JNI_FALSE;

    if(mp3dec_load(&mp3d, fileName, &info, NULL, NULL)) {
        return JNI_FALSE;
    }

    (*env)->ReleaseStringUTFChars(env, jFileName, fileName);

    jclass infoClass = (*env)->GetObjectClass(env, jInfo);

    jfieldID fidBytes = (*env)->GetFieldID(env, infoClass, "bytes", "[B");
    jbyteArray jBytes = (jbyteArray)(*env)->NewByteArray(env, info.samples * 2);
    jbyte* bytes = (*env)->GetByteArrayElements(env, jBytes, NULL);

    size_t i;
    for(i = 0; i < info.samples; i++) {
        bytes[2 * i] = info.buffer[i] & 0xFF;
        bytes[2 * i + 1] = (info.buffer[i] >> 8) & 0xFF;
    }

    (*env)->ReleaseByteArrayElements(env, jBytes, bytes, 0);
    (*env)->SetObjectField(env, jInfo, fidBytes, jBytes);

    free(info.buffer);

    set_long_field(env, infoClass, jInfo, "sampleNum", info.samples);
    set_int_field(env, infoClass, jInfo, "sampleRate", info.hz);
    set_int_field(env, infoClass, jInfo, "channelNum", info.channels);
    set_int_field(env, infoClass, jInfo, "layer", info.layer);
    set_int_field(env, infoClass, jInfo, "bitrate", info.avg_bitrate_kbps);

    return JNI_TRUE;
}
