# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html
cmake_minimum_required(VERSION 3.4.1)

# 设置 native_lib 目录
set(distribution_DIR ${CMAKE_SOURCE_DIR}/../distribution)
set(dlib_path ${CMAKE_SOURCE_DIR}/src/main/dlib)

# 设置dlib
include(${dlib_path}/cmake)

# 添加自定义库
add_library(
        face_lib SHARED
        src/main/dlib/all/source.cpp
        src/main/cpp/jstring_complier.cpp
        src/main/cpp/face_detector.cpp
)

# 关联 log 库
find_library(log-lib log)

# 设置输出文件可见地址
set_target_properties(
        face_lib PROPERTIES
        LIBRARY_OUTPUT_DIRECTORY ${distribution_DIR}/libs/${ANDROID_ABI}
)

set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI})

target_link_libraries(
        face_lib
        dlib
        ${log-lib}
)

set(CMAKE_BUILE_TYPE "RELEASE")
set(CMAKE_C_FLAGS_RELEASE "$ENV{CFLAGS} -s -O3 -Wall")
set(CMAKE_CXX_FLAGS_RELEASE "$ENV{CXXFLAGS} -s -O3 -Wall")

#[[
set(CMAKE_BUILE_TYPE "DEBUG")
set(CMAKE_C_FLAGS_DEBUG "-g -Wall")
set(CMAKE_CXX_FLAGS_DEBUG "-g -Wall")
]]


