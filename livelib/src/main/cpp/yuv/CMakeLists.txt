# CMakeLists for libyuv
# Originally created for "roxlu build system" to compile libyuv on windows
# Run with -DTEST=ON to build unit tests

PROJECT ( YUV C CXX )	# "C" is required even for C++ projects
cmake_minimum_required(VERSION 3.4.1)

# this creates the shared library (.so)
add_library( # Sets the name of the library.
        libyuv-lib
        # Sets the library as a shared library.
        SHARED
        # Provides a relative path to your libyuv file(s).
		JNI.cpp
		libyuv/compare.cc          
		libyuv/compare_common.cc   
		libyuv/compare_gcc.cc      
		libyuv/compare_mmi.cc      
		libyuv/compare_msa.cc      
		libyuv/compare_neon64.cc   
		libyuv/compare_neon.cc     
		libyuv/compare_win.cc      
		libyuv/convert_argb.cc     
		libyuv/convert.cc          
		libyuv/convert_from_argb.cc
		libyuv/convert_from.cc     
		libyuv/convert_to_argb.cc  
		libyuv/convert_to_i420.cc  
		libyuv/cpu_id.cc           
		libyuv/planar_functions.cc 
		libyuv/rotate_any.cc       
		libyuv/rotate_argb.cc      
		libyuv/rotate.cc           
		libyuv/rotate_common.cc    
		libyuv/rotate_gcc.cc       
		libyuv/rotate_mmi.cc       
		libyuv/rotate_msa.cc       
		libyuv/rotate_neon64.cc    
		libyuv/rotate_neon.cc      
		libyuv/rotate_win.cc       
		libyuv/row_any.cc          
		libyuv/row_common.cc       
		libyuv/row_gcc.cc          
		libyuv/row_mmi.cc          
		libyuv/row_msa.cc          
		libyuv/row_neon64.cc       
		libyuv/row_neon.cc         
		libyuv/row_win.cc          
		libyuv/scale_any.cc        
		libyuv/scale_argb.cc       
		libyuv/scale.cc            
		libyuv/scale_common.cc     
		libyuv/scale_gcc.cc        
		libyuv/scale_mmi.cc        
		libyuv/scale_msa.cc        
		libyuv/scale_neon64.cc     
		libyuv/scale_neon.cc       
		libyuv/scale_win.cc        
		libyuv/video_common.cc
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
		
target_link_libraries( # Specifies the target library.
		libyuv-lib

		# Links the target library to the log library
		# included in the NDK.
		${log-lib})


