# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

PROJECT ( YUV C CXX )	# "C" is required even for C++ projects

SET ( ly_base_dir	${PROJECT_SOURCE_DIR} )
SET ( ly_src_dir	${ly_base_dir}/source )
SET ( ly_inc_dir	${ly_base_dir}/include )

INCLUDE_DIRECTORIES( BEFORE ${ly_inc_dir} )

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        libyuv-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
		source/compare.cc          
		source/compare_common.cc   
		source/compare_gcc.cc      
		source/compare_mmi.cc      
		source/compare_msa.cc      
		source/compare_neon64.cc   
		source/compare_neon.cc     
		source/compare_win.cc      
		source/convert_argb.cc     
		source/convert.cc          
		source/convert_from_argb.cc
		source/convert_from.cc     
		source/convert_to_argb.cc  
		source/convert_to_i420.cc  
		source/cpu_id.cc           
		source/planar_functions.cc 
		source/rotate_any.cc       
		source/rotate_argb.cc      
		source/rotate.cc           
		source/rotate_common.cc    
		source/rotate_gcc.cc       
		source/rotate_mmi.cc       
		source/rotate_msa.cc       
		source/rotate_neon64.cc    
		source/rotate_neon.cc      
		source/rotate_win.cc       
		source/row_any.cc          
		source/row_common.cc       
		source/row_gcc.cc          
		source/row_mmi.cc          
		source/row_msa.cc          
		source/row_neon64.cc       
		source/row_neon.cc         
		source/row_win.cc          
		source/scale_any.cc        
		source/scale_argb.cc       
		source/scale.cc            
		source/scale_common.cc     
		source/scale_gcc.cc        
		source/scale_mmi.cc        
		source/scale_msa.cc        
		source/scale_neon64.cc     
		source/scale_neon.cc       
		source/scale_win.cc        
		source/video_common.cc
        )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        libyuv-lib

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})