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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.jogamp.opencl.test.util.UITestCase;

import static org.junit.Assert.*;

/**
 * @author Michael Bien, et.al.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CLExceptionTest extends UITestCase {

    @Test
    public void testCLExceptions() throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        final Class<?>[] subTypes = CLException.class.getDeclaredClasses();

        for (final Class<?> type : subTypes) {

            if(type.getName().startsWith(CLException.class.getName()+"$CL")) {

                final CLException exception = (CLException) type.getConstructor(String.class).newInstance("foo");

                assertNotNull("can not resolve "+exception, CLException.resolveErrorCode(exception.errorcode));

                try{
                    CLException.checkForError(exception.errorcode, "foo");
                    fail("expected exception for: "+exception.getClass().getName()+" code: "+exception.errorcode);
                }catch(final CLException ex) {
                    assertTrue("wrong instance; expected "+exception.getClass()+" but got "+ex.getClass(),
                            exception.getClass().equals(ex.getClass()));
                }
            }
        }
    }

    public static void main(final String[] args) throws IOException {
        final String tstname = CLExceptionTest.class.getName();
        org.junit.runner.JUnitCore.main(tstname);
    }
}
