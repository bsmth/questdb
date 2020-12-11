/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_questdb_network_Net */

#ifndef _Included_com_questdb_network_Net
#define _Included_com_questdb_network_Net
#ifdef __cplusplus
extern "C" {
#endif
#undef com_questdb_network_Net_ERETRY
#define com_questdb_network_Net_ERETRY 0L
#undef com_questdb_network_Net_EPEERDISCONNECT
#define com_questdb_network_Net_EPEERDISCONNECT -1L
#undef com_questdb_network_Net_EOTHERDISCONNECT
#define com_questdb_network_Net_EOTHERDISCONNECT -2L
/*
 * Class:     com_questdb_network_Net
 * Method:    abortAccept
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_io_questdb_network_Net_abortAccept
        (JNIEnv *, jclass, jlong);

/*
 * Class:     com_questdb_network_Net
 * Method:    accept
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_io_questdb_network_Net_accept0
        (JNIEnv *, jclass, jlong);

/*
 * Class:     com_questdb_network_Net
 * Method:    bindTcp
 * Signature: (JII)Z
 */
JNIEXPORT jboolean JNICALL Java_io_questdb_network_Net_bindTcp
        (JNIEnv *, jclass, jlong, jint, jint);

/*
 * Class:     com_questdb_network_Net
 * Method:    bindUdp
 * Signature: (JII)Z
 */
JNIEXPORT jboolean JNICALL Java_io_questdb_network_Net_bindUdp
        (JNIEnv *, jclass, jlong, jint, jint);

/*
 * Class:     com_questdb_network_Net
 * Method:    configureNoLinger
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_io_questdb_network_Net_configureNoLinger
        (JNIEnv *, jclass, jlong);

/*
 * Class:     com_questdb_network_Net
 * Method:    configureNonBlocking
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_io_questdb_network_Net_configureNonBlocking
        (JNIEnv *, jclass, jlong);

/*
 * Class:     com_questdb_network_Net
 * Method:    connect
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL Java_io_questdb_network_Net_connect
        (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     com_questdb_network_Net
 * Method:    freeMsgHeaders
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_io_questdb_network_Net_freeMsgHeaders
        (JNIEnv *, jclass, jlong);

/*
 * Class:     com_questdb_network_Net
 * Method:    freeSockAddr
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_io_questdb_network_Net_freeSockAddr
        (JNIEnv *, jclass, jlong);

/*
 * Class:     com_questdb_network_Net
 * Method:    getPeerIP
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_io_questdb_network_Net_getPeerIP
        (JNIEnv *, jclass, jlong);

/*
 * Class:     com_questdb_network_Net
 * Method:    getPeerPort
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_io_questdb_network_Net_getPeerPort
        (JNIEnv *, jclass, jlong);

/*
 * Class:     com_questdb_network_Net
 * Method:    isDead
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_io_questdb_network_Net_isDead
        (JNIEnv *, jclass, jlong);

/*
 * Class:     com_questdb_network_Net
 * Method:    join
 * Signature: (JII)Z
 */
JNIEXPORT jboolean JNICALL Java_io_questdb_network_Net_join
        (JNIEnv *, jclass, jlong, jint, jint);

/*
 * Class:     com_questdb_network_Net
 * Method:    listen
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_io_questdb_network_Net_listen
        (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     com_questdb_network_Net
 * Method:    msgHeaders
 * Signature: (II)J
 */
JNIEXPORT jlong JNICALL Java_io_questdb_network_Net_msgHeaders
        (JNIEnv *, jclass, jint, jint);

/*
 * Class:     com_questdb_network_Net
 * Method:    recv
 * Signature: (JJI)I
 */
JNIEXPORT jint JNICALL Java_io_questdb_network_Net_recv
        (JNIEnv *, jclass, jlong, jlong, jint);

/*
 * Class:     com_questdb_network_Net
 * Method:    recvmmsg
 * Signature: (JJI)I
 */
JNIEXPORT jint JNICALL Java_io_questdb_network_Net_recvmmsg
        (JNIEnv *, jclass, jlong, jlong, jint);

/*
 * Class:     com_questdb_network_Net
 * Method:    send
 * Signature: (JJI)I
 */
JNIEXPORT jint JNICALL Java_io_questdb_network_Net_send
        (JNIEnv *, jclass, jlong, jlong, jint);

/*
 * Class:     com_questdb_network_Net
 * Method:    sendTo
 * Signature: (JJIJ)I
 */
JNIEXPORT jint JNICALL Java_io_questdb_network_Net_sendTo
        (JNIEnv *, jclass, jlong, jlong, jint, jlong);

/*
 * Class:     com_questdb_network_Net
 * Method:    getRcvBuf
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_io_questdb_network_Net_getRcvBuf
        (JNIEnv *, jclass, jlong);

/*
 * Class:     com_questdb_network_Net
 * Method:    setRcvBuf
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_io_questdb_network_Net_setRcvBuf
        (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     com_questdb_network_Net
 * Method:    getSndBuf
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_io_questdb_network_Net_getSndBuf
        (JNIEnv *, jclass, jlong);

/*
 * Class:     com_questdb_network_Net
 * Method:    getTcpNoDelay
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_io_questdb_network_Net_getTcpNoDelay
        (JNIEnv *, jclass, jlong);

/*
 * Class:     com_questdb_network_Net
 * Method:    setSndBuf
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_io_questdb_network_Net_setSndBuf
        (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     com_questdb_network_Net
 * Method:    setTcpNoDelay
 * Signature: (JZ)I
 */
JNIEXPORT jint JNICALL Java_io_questdb_network_Net_setTcpNoDelay
        (JNIEnv *, jclass, jlong, jboolean);

/*
 * Class:     com_questdb_network_Net
 * Method:    setMulticastInterface
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_io_questdb_network_Net_setMulticastInterface
        (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     com_questdb_network_Net
 * Method:    setMulticastLoop
 * Signature: (JZ)I
 */
JNIEXPORT jint JNICALL Java_io_questdb_network_Net_setMulticastLoop
        (JNIEnv *, jclass, jlong, jboolean);

/*
 * Class:     com_questdb_network_Net
 * Method:    setReuseAddress
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_io_questdb_network_Net_setReuseAddress
        (JNIEnv *, jclass, jlong);

/*
 * Class:     com_questdb_network_Net
 * Method:    setReusePort
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_io_questdb_network_Net_setReusePort
        (JNIEnv *, jclass, jlong);

/*
 * Class:     com_questdb_network_Net
 * Method:    sockaddr
 * Signature: (II)J
 */
JNIEXPORT jlong JNICALL Java_io_questdb_network_Net_sockaddr
        (JNIEnv *, jclass, jint, jint);

/*
 * Class:     com_questdb_network_Net
 * Method:    socketTcp
 * Signature: (Z)J
 */
JNIEXPORT jlong JNICALL Java_io_questdb_network_Net_socketTcp0
        (JNIEnv *, jclass, jboolean);

/*
 * Class:     com_questdb_network_Net
 * Method:    socketUdp0
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_io_questdb_network_Net_socketUdp0
        (JNIEnv *, jclass);

/*
 * Class:     com_questdb_network_Net
 * Method:    getMsgHeaderSize
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_io_questdb_network_Net_getMsgHeaderSize
        (JNIEnv *, jclass);

/*
 * Class:     com_questdb_network_Net
 * Method:    getMsgHeaderBufferAddressOffset
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_io_questdb_network_Net_getMsgHeaderBufferAddressOffset
        (JNIEnv *, jclass);

/*
 * Class:     com_questdb_network_Net
 * Method:    getMsgHeaderBufferLengthOffset
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_io_questdb_network_Net_getMsgHeaderBufferLengthOffset
        (JNIEnv *, jclass);

/*
 * Class:     com_questdb_network_Net
 * Method:    getEwouldblock
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_io_questdb_network_Net_getEwouldblock
        (JNIEnv *, jclass);

JNIEXPORT jint JNICALL Java_io_questdb_network_Net_getEinprogress
        (JNIEnv *, jclass);

JNIEXPORT jint JNICALL Java_io_questdb_network_Net_getEalready
        (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif
