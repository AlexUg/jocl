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

package com.jogamp.opencl.test.util;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.jogamp.common.os.Platform;
import com.jogamp.opencl.llb.impl.CLAbstractImpl;

import static java.lang.System.*;
import static org.junit.Assert.*;

/**
 * @author Michael Bien, et.al
 */
public class MiscUtils {

    //decrease this value on systems with few memory.
    public final static int ONE_MB = 1048576;

    public final static int NUM_ELEMENTS = 10000000;

    public static final void fillBuffer(final ByteBuffer buffer, final int seed) {

        final Random rnd = new Random(seed);

        while(buffer.remaining() != 0)
            buffer.putInt(rnd.nextInt());

        buffer.rewind();
    }

    public static final int roundUp(final int groupSize, final int globalSize) {
        final int r = globalSize % groupSize;
        if (r == 0) {
            return globalSize;
        } else {
            return globalSize + groupSize - r;
        }
    }

    public static final void checkIfEqual(final ByteBuffer a, final ByteBuffer b, final int elements) {
        for(int i = 0; i < elements; i++) {
            final int aVal = a.getInt();
            final int bVal = b.getInt();
            if(aVal != bVal) {
                out.println("a: "+aVal);
                out.println("b: "+bVal);
                out.println("position: "+a.position());
                fail("a!=b");
            }
        }
        a.rewind();
        b.rewind();
    }

    private static final Set<Platform.OSType> knownOSWithoutCLImpl;
    static {
        knownOSWithoutCLImpl = new HashSet<Platform.OSType>();
        knownOSWithoutCLImpl.add(Platform.OSType.SUNOS);
    }

    /**
     * @return true if OpenCL is not available for this operating system, CPU architecture, et cetera.
     * This is meant to be a check that there can't possibly be a driver installed because
     * nobody makes one, not just a check that we didn't see one.
     */
    public static final boolean isOpenCLUnavailable() {
        return !CLAbstractImpl.isAvailable() && knownOSWithoutCLImpl.contains(Platform.getOSType());
    }
}
