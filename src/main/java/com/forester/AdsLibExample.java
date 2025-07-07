package com.forester;
import org.bytedeco.javacpp.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static com.forester.AdsLib.*;

public class AdsLibExample {
    public static void main(String[] args) {
        try {
            String REMOTE_IP = "192.168.1.162";
            long port = AdsLib.AdsPortOpenEx();
            // Add Local Route
            AdsLib.AmsAddr  hostAddr= new AdsLib.AmsAddr();
            hostAddr.netId(new AdsLib.AmsNetId("192.168.1.233.1.1"));
            AdsLib.SetLocalAddress(hostAddr.netId());
            long err =AdsLib.AddLocalRoute(hostAddr.netId(),"192.168.1.233");
            System.out.println("Added local route: " + err);
            // Open Port for Reasons
            err = 0;
            System.out.println("Opened ADS port: " + port);

            AdsLib.AmsAddr  localAddr= new AdsLib.AmsAddr();
            err = AdsLib.AdsGetLocalAddressEx(port, localAddr);
            if(localAddr.netId().equals(hostAddr.netId())){
                System.out.println("Added local route: " + err);
                System.out.println("AMS Net-ID: " + formatAmsNetId(hostAddr.netId()));
            }
            //Set Remote Route
            AdsLib.AmsAddr RemoteAddr = new AdsLib.AmsAddr();
            AdsLib.AmsNetId amsNetId = new AdsLib.AmsNetId("1.1.1.1.1.1.1");
            long result = AdsLib.GetRemoteAddress(REMOTE_IP,amsNetId);
            System.out.println("GetRemoteAddress: " + formatAmsNetId(amsNetId));
            RemoteAddr.netId(amsNetId);
            RemoteAddr.port(AdsLib.AMSPORT_R0_PLC_RTS1);


            err =AdsLib.AddRemoteRoute(REMOTE_IP,hostAddr.netId(),"192.168.1.233","TestRoute","Administrator","2");
            System.out.println("Added remote route: " + err);



            // Example: Read device info
            BytePointer deviceName = new BytePointer(16);
            AdsLib.AdsVersion version = new AdsLib.AdsVersion();









            //AdsLib.AmsNetId netId = new AdsLib.AmsNetId("192.168.1.233.1.1");
            System.out.println("AMS Net-ID: " + formatAmsNetId(amsNetId));

            BytePointer devNamePtr = new BytePointer(16);
            System.out.println("Device name: " + formatDeviceName(devNamePtr));
            AdsLib.AdsVersion ver   = new AdsLib.AdsVersion();


            long rc = AdsLib.AdsSyncReadDeviceInfoReqEx(port, RemoteAddr, devNamePtr, ver);
            System.out.println("err" + rc);
                System.out.printf("Device: %s  Version: %d.%d.%d%n",
                        formatDeviceName(devNamePtr),
                        ver.version(), ver.revision(), ver.build());

            BytePointer  symName   = new BytePointer("Main.nbool"); // auto 0-terminated
            int          symLen    = (int) symName.limit();         // includes '\0'

            BytePointer  val       = new BytePointer(1);            // BOOL ⇒ 1 byte
            IntPointer   bytesRead = new IntPointer(1);             // receives #bytes

            rc = AdsLib.AdsSyncReadWriteReqEx2(
                    port, RemoteAddr,
                    0xF004,                 // ADSIGRP_SYM_VALBYNAME
                    0,                      // indexOffset
                    (int) val.capacity(), val,    // read: 1 byte -> val
                    symLen, symName,        // write: symbol name
                    bytesRead               // out: actual bytes read
            );

            if (rc != 0) {
                System.err.printf("AdsSyncReadWriteReqEx2 failed: 0x%08X%n", rc);
            } else {
                boolean nbool = val.get() != 0;
                System.out.println("Main.nbool = " + nbool);
            }


            AdsLib.AdsPortCloseEx(port);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Java
    /** Converts the 16-byte, 0-terminated device name returned by AdsSyncReadDeviceInfoReqEx */
    private static String formatDeviceName(BytePointer rawName) {
        // the buffer is exactly 16 bytes long
        byte[] tmp = new byte[16];
        rawName.get(tmp);

        // find zero terminator
        int len = 0;
        while (len < tmp.length && tmp[len] != 0) {
            len++;
        }
        return new String(tmp, 0, len, StandardCharsets.US_ASCII);
    }


    private static String formatAmsNetId(AdsLib.AmsNetId id) {
        ByteBuffer buf = id.asByteBuffer();          // six raw bytes
        return String.format(
                "%d.%d.%d.%d.%d.%d",
                buf.get(0) & 0xFF,
                buf.get(1) & 0xFF,
                buf.get(2) & 0xFF,
                buf.get(3) & 0xFF,
                buf.get(4) & 0xFF,
                buf.get(5) & 0xFF
        );
    }

}
