# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

cmake_minimum_required(VERSION 3.4.1)

add_library( yuv-decoder SHARED src/main/cpp/yuv-decoder.cpp )

find_library( log-lib log )

target_link_libraries( yuv-decoder ${log-lib} )