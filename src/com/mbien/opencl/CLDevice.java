package com.mbien.opencl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import static com.mbien.opencl.CLException.*;

/**
 * 
 * @author Michael Bien
 */
public final class CLDevice {

    private final CL cl;
    private CLContext context;

    /**
     * OpenCL device id for this device.
     */
    public final long ID;

    CLDevice(CL cl, long id) {
        this.cl = cl;
        this.ID = id;
    }

    CLDevice(CLContext context, long id) {
        this.context = context;
        this.cl = context.cl;
        this.ID = id;
    }

    public CLCommandQueue createCommandQueue() {
        return createCommandQueue(0);
    }

    public CLCommandQueue createCommandQueue(CLCommandQueue.Mode property) {
        return createCommandQueue(property.CL_QUEUE_MODE);
    }

    public CLCommandQueue createCommandQueue(CLCommandQueue.Mode... properties) {
        int flags = 0;
        if(properties != null) {
            for (int i = 0; i < properties.length; i++) {
                flags |= properties[i].CL_QUEUE_MODE;
            }
        }
        return createCommandQueue(flags);
    }
    
    public CLCommandQueue createCommandQueue(long properties) {
        if(context == null)
            throw new IllegalStateException("this device is not associated with a context");
        return context.createCommandQueue(this, properties);
    }

    /*keep this package private for now, may be null*/
    CLContext getContext() {
        return context;
    }

    /**
     * Returns the name of this device.
     */
    public String getName() {
        return getInfoString(CL.CL_DEVICE_NAME);
    }

    /**
     * Returns the OpenCL profile of this device.
     */
    public String getProfile() {
        return getInfoString(CL.CL_DEVICE_PROFILE);
    }

    /**
     * Returns the vendor of this device.
     */
    public String getVendor() {
        return getInfoString(CL.CL_DEVICE_VENDOR);
    }

    /**
     * Returns the type of this device.
     */
    public Type getType() {
        return Type.valueOf((int)getInfoLong(CL.CL_DEVICE_TYPE));
    }

    /**
     * Returns the number of parallel compute cores on the OpenCL device.
     * The minimum value is 1.
     */
    public int getMaxComputeUnits() {
        return (int) getInfoLong(CL.CL_DEVICE_MAX_COMPUTE_UNITS);
    }

    /**
     * Returns the maximum number of work-items in a work-group executing
     * a kernel using the data parallel execution model.
     * The minimum value is 1.
     */
    public int getMaxWorkGroupSize() {
        return (int) getInfoLong(CL.CL_DEVICE_MAX_WORK_GROUP_SIZE);
    }

    /**
     * Returns the maximum configured clock frequency of the device in MHz.
     */
    public int getMaxClockFrequency() {
        return (int) (getInfoLong(CL.CL_DEVICE_MAX_CLOCK_FREQUENCY));
    }

    /**
     * Returns the maximum dimensions that specify the global and local work-item
     * IDs used by the data parallel execution model.
     * The minimum value is 3.
     */
    public int getMaxWorkItemDimensions() {
        return (int) getInfoLong(CL.CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS);
    }

    /**
     * Returns the global memory size in bytes.
     */
    public long getGlobalMemSize() {
        return getInfoLong(CL.CL_DEVICE_GLOBAL_MEM_SIZE);
    }

    /**
     * Returns the local memory size in bytes.
     */
    public long getLocalMemSize() {
        return getInfoLong(CL.CL_DEVICE_LOCAL_MEM_SIZE);
    }

    /**
     * Returns the max size in bytes of a constant buffer allocation.
     * The minimum value is 64 KB.
     */
    public long getMaxConstantBufferSize() {
        return getInfoLong(CL.CL_DEVICE_MAX_CONSTANT_BUFFER_SIZE);
    }

    /**
     * Returns true if this device is available.
     */
    public boolean isAvailable() {
        return getInfoLong(CL.CL_DEVICE_AVAILABLE) == CL.CL_TRUE;
    }

    /**
     * Returns false if the implementation does not have a compiler available to
     * compile the program source. Is true if the compiler is available.
     * This can be false for the OpenCL ES profile only.
     */
    public boolean isCompilerAvailable() {
        return getInfoLong(CL.CL_DEVICE_COMPILER_AVAILABLE) == CL.CL_TRUE;
    }

    /**
     * Returns all device extension names as unmodifiable Set.
     */
    public Set<String> getExtensions() {

        String ext = getInfoString(CL.CL_DEVICE_EXTENSIONS);

        Scanner scanner = new Scanner(ext);
        Set<String> extSet = new HashSet<String>();

        while(scanner.hasNext())
            extSet.add(scanner.next());

        return Collections.unmodifiableSet(extSet);
    }

    //TODO CL_DEVICE_IMAGE_SUPPORT
    //TODO CL_DEVICE_MAX_WORK_ITEM_SIZES


    private final long getInfoLong(int key) {

        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.order(ByteOrder.nativeOrder());

        int ret = cl.clGetDeviceInfo(ID, key, bb.capacity(), bb, null, 0);

        checkForError(ret, "can not receive device info");

        return bb.getLong();
    }

    public final String getInfoString(int key) {

        long[] longBuffer = new long[1];
        ByteBuffer bb = ByteBuffer.allocate(512);

        int ret = cl.clGetDeviceInfo(ID, key, bb.capacity(), bb, longBuffer, 0);
        
        checkForError(ret, "can not receive device info string");

        return new String(bb.array(), 0, (int)longBuffer[0]);
        
    }


    @Override
    public String toString() {
        return "CLDevice [id: " + ID
                      + " name: " + getName()
                      + " type: " + getType()
                      + " profile: " + getProfile()+"]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CLDevice other = (CLDevice) obj;
        if (this.ID != other.ID) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + (int) (this.ID ^ (this.ID >>> 32));
        return hash;
    }

    /**
     * Enumeration for the type of a device.
     */
    public enum Type {
        /**
         * CL_DEVICE_TYPE_CPU
         */
        CPU(CL.CL_DEVICE_TYPE_CPU),
        /**
         * CL_DEVICE_TYPE_GPU
         */
        GPU(CL.CL_DEVICE_TYPE_GPU),
        /**
         * CL_DEVICE_TYPE_ACCELERATOR
         */
        ACCELERATOR(CL.CL_DEVICE_TYPE_ACCELERATOR),
        /**
         * CL_DEVICE_TYPE_DEFAULT. This type can be used for creating a context on
         * the default device, a single device can never have this type.
         */
        DEFAULT(CL.CL_DEVICE_TYPE_DEFAULT),
        /**
         * CL_DEVICE_TYPE_ALL. This type can be used for creating a context on
         * all devices, a single device can never have this type.
         */
        ALL(CL.CL_DEVICE_TYPE_ALL);

        /**
         * Value of wrapped OpenCL device type.
         */
        public final long CL_TYPE;

        private Type(long CL_TYPE) {
            this.CL_TYPE = CL_TYPE;
        }

        public static Type valueOf(long clDeviceType) {

            if(clDeviceType == CL.CL_DEVICE_TYPE_ALL)
                return ALL;

            switch((int)clDeviceType) {
                case(CL.CL_DEVICE_TYPE_DEFAULT):
                    return DEFAULT;
                case(CL.CL_DEVICE_TYPE_CPU):
                    return CPU;
                case(CL.CL_DEVICE_TYPE_GPU):
                    return GPU;
                case(CL.CL_DEVICE_TYPE_ACCELERATOR):
                    return ACCELERATOR;
            }
            return null;
        }
    }

}
