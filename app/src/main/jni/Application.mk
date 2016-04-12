APP_ABI      			:= armeabi-v7a x86
APP_PLATFORM 			:= android-16
APP_STL      			:= stlport_static
NDK_TOOLCHAIN_VERSION 	:= 4.8
# windows only
ifeq ($(OS),Windows_NT)
	APP_SHORT_COMMANDS		:= true
endif
