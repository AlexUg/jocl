/*
 * Copyright 2010 JogAmp Community. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of JogAmp Community.
 */

package com.jogamp.opencl;


import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.jogamp.opencl.test.util.MiscUtils;
import com.jogamp.opencl.test.util.UITestCase;

import static org.junit.Assert.*;
import static java.lang.System.*;
import static com.jogamp.common.nio.Buffers.*;
import static com.jogamp.opencl.CLImageFormat.ChannelOrder.*;
import static com.jogamp.opencl.CLImageFormat.ChannelType.*;

/**
 * Test testing CLImage API.
 * @author Michael Bien, et.al
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CLImageTest extends UITestCase {

    private static int[] pixels;

    @BeforeClass
    public static void init() throws IOException {
        final BufferedImage bi = ImageIO.read(CLImageTest.class.getResourceAsStream("jogamp.png"));
        pixels = new int[128*128*4];
        bi.getData().getPixels(0, 0, 128, 128, pixels);
    }

    public CLDevice getCompatibleDevice() {

        final CLPlatform[] platforms = CLPlatform.listCLPlatforms();
        for (final CLPlatform platform : platforms) {
            final CLDevice[] devices = platform.listCLDevices();

            for (final CLDevice device : devices) {
                if(device.isImageSupportAvailable()) {
                    return device;
                }
            }
        }

        return null;
    }


    @Test
    public void supportedImageFormatsTest() {
        final CLDevice device = getCompatibleDevice();
        if(device == null) {
            out.println("WARNING: can not test image api.");
            return;
        }
        final CLContext context = CLContext.create(device);

        try{
            final CLImageFormat[] formats = context.getSupportedImage2dFormats();
            assertTrue(formats.length > 0);
            out.println("sample image format: "+formats[0]);
//            for (CLImageFormat format : formats) {
//                out.println(format);
//            }
        }finally{
            context.release();
        }

    }

    @Test
    public void image2dCopyTest() throws IOException {
        final CLDevice device = getCompatibleDevice();
        if(device == null) {
            out.println("WARNING: can not test image api.");
            return;
        }
        final CLContext context = CLContext.create(device);

        final CLCommandQueue queue = device.createCommandQueue();

        try{

            final CLImageFormat format = new CLImageFormat(RGBA, UNSIGNED_INT32);

            final CLImage2d<IntBuffer> imageA = context.createImage2d(newDirectIntBuffer(pixels), 128, 128, format);
            final CLImage2d<IntBuffer> imageB = context.createImage2d(newDirectIntBuffer(pixels.length), 128, 128, format);

            queue.putWriteImage(imageA, false)
                 .putCopyImage(imageA, imageB)
                 .putReadImage(imageB, true);

            final IntBuffer bufferA = imageA.getBuffer();
            final IntBuffer bufferB = imageB.getBuffer();

            while(bufferA.hasRemaining()) {
                assertEquals(bufferA.get(), bufferB.get());
            }

        }finally{
            context.release();
        }

    }

    @Test
    public void image2dKernelCopyTest() throws IOException {
        final CLDevice device = getCompatibleDevice();
        if(device == null) {
            out.println("WARNING: can not test image api.");
            return;
        }
        final CLContext context = CLContext.create(device);

        final String src =
        "constant sampler_t imageSampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP | CLK_FILTER_NEAREST; \n" +
        "kernel void image2dCopy(read_only image2d_t input, write_only image2d_t output) { \n" +
        "    int2 coord = (int2)(get_global_id(0), get_global_id(1)); \n" +
        "    uint4 temp = read_imageui(input, imageSampler, coord); \n" +
        "    write_imageui(output, coord, temp); \n" +
        "} \n";

        final CLKernel kernel = context.createProgram(src).build().createCLKernel("image2dCopy");

        final CLCommandQueue queue = device.createCommandQueue();

        try{

            final CLImageFormat format = new CLImageFormat(RGBA, UNSIGNED_INT32);

            final CLImage2d<IntBuffer> imageA = context.createImage2d(newDirectIntBuffer(pixels), 128, 128, format);
            final CLImage2d<IntBuffer> imageB = context.createImage2d(newDirectIntBuffer(pixels.length), 128, 128, format);

            kernel.putArgs(imageA, imageB);
            queue.putWriteImage(imageA, false)
                 .put2DRangeKernel(kernel, 0, 0, 128, 128, 0, 0)
                 .putReadImage(imageB, true);

            final IntBuffer bufferA = imageA.getBuffer();
            final IntBuffer bufferB = imageB.getBuffer();

            while(bufferA.hasRemaining()) {
                assertEquals(bufferA.get(), bufferB.get());
            }

        }finally{
            context.release();
        }

    }
    public static void main(final String[] args) throws IOException {
        final String tstname = CLImageTest.class.getName();
        org.junit.runner.JUnitCore.main(tstname);
    }

}
