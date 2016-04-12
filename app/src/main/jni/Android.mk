JNI_DIR := $(call my-dir)

include openssl/Android.mk

LOCAL_PATH := $(JNI_DIR)
ROOT_PATH := $(LOCAL_PATH)

########################################################
## libevent
########################################################

include $(CLEAR_VARS)

LIBEVENT_SOURCES := \
	buffer.c \
	bufferevent.c bufferevent_filter.c \
	bufferevent_openssl.c bufferevent_pair.c bufferevent_ratelim.c \
	bufferevent_sock.c epoll.c \
	epoll_sub.c evdns.c event.c \
    event_tagging.c evmap.c \
	evrpc.c evthread.c \
	evthread_pthread.c evutil.c \
	evutil_rand.c http.c \
	listener.c log.c poll.c \
	select.c signal.c strlcpy.c

LOCAL_MODULE := event
LOCAL_SRC_FILES := $(addprefix libevent/, $(LIBEVENT_SOURCES))
LOCAL_CFLAGS := -O2 -I$(LOCAL_PATH)/libevent \
	-I$(LOCAL_PATH)/libevent/include \
	-I$(LOCAL_PATH)/openssl/include

include $(BUILD_STATIC_LIBRARY)

########################################################
## minivtun
########################################################

include $(CLEAR_VARS)

LOCAL_MODULE := minivtun

LOCAL_C_INCLUDES := $(LOCAL_PATH)/minivtun/src $(LOCAL_PATH)/openssl/include

MINIVTUN_SOURCES := client.c  library.c  minivtun.c  #server.c
LOCAL_SRC_FILES := $(addprefix $(LOCAL_PATH)/minivtun/src/, $(MINIVTUN_SOURCES))

LOCAL_STATIC_LIBRARIES := cpufeatures crypto_static
LOCAL_CFLAGS := -O2 -DANDROID
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog

include $(BUILD_SHARED_LIBRARY)



# Import cpufeatures
$(call import-module,android/cpufeatures)
