package com.forester;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;
import org.bytedeco.javacpp.tools.*;

@Properties(
        value = @Platform(
                // point to the real location of the .h files
                includepath = {"src/main/cpp"},
                include     = {"AdsLib.h", "AdsDef.h", "standalone/AdsLib.h", "standalone/AdsDef.h"},

                // where CMake will later place the shared library
                linkpath    = {"src/main/cpp/build"},
                link        = {"ads"}
        ),
        target = "com.forester.AdsLib"
)

public class AdsLibConfig implements InfoMapper {
    public void map(InfoMap infoMap) {
        infoMap.put(new Info("PAdsNotificationFuncEx")
                .pointerTypes("PAdsNotificationFuncEx")
                .valueTypes  ("PAdsNotificationFuncEx"));
        // Define constants directly here with correct values
        final int ADSSYMBOLFLAG_PERSISTENT = (1);     // 1
        final int ADSSYMBOLFLAG_BITVALUE = (1 << 1);       // 2
        final int ADSSYMBOLFLAG_REFERENCETO = (1 << 2);    // 4
        final int ADSSYMBOLFLAG_TYPEGUID = (1 << 3);       // 8
        final int ADSSYMBOLFLAG_TCCOMIFACEPTR = (1 << 4);  // 16
        final int ADSSYMBOLFLAG_READONLY = (1 << 5);       // 32
        final int ADSSYMBOLFLAG_CONTEXTMASK = 0xF00;       // 3840

        infoMap.put(new Info("AdsAddRoute").skip());
        infoMap.put(new Info("AdsDelRoute").skip());
        infoMap.put(new Info("AdsSetLocalAddress").skip());

        // Tell JavaCPP to generate classes for these structs
        infoMap.put(new Info("AmsAddr").pointerTypes("AmsAddr"));
        infoMap.put(new Info("AmsNetId").pointerTypes("AmsNetId"));
        infoMap.put(new Info("AdsVersion").pointerTypes("AdsVersion"));
        infoMap.put(new Info("AdsNotificationAttrib").pointerTypes("AdsNotificationAttrib"));

        // If these structs are typedef'd, map the typedefs too
        infoMap.put(new Info("PAmsAddr").pointerTypes("AmsAddr"));
        infoMap.put(new Info("PAmsNetId").pointerTypes("AmsNetId"));
        infoMap.put(new Info("PAdsVersion").pointerTypes("AdsVersion"));

        // Create function aliases
        infoMap.put(new Info("bhf::ads::AddLocalRoute").cppNames("AdsAddRoute"));
        infoMap.put(new Info("bhf::ads::DelLocalRoute").cppNames("AdsDelRoute"));
        infoMap.put(new Info("bhf::ads::SetLocalAddress").cppNames("AdsSetLocalAddress"));
        infoMap.put(new Info("AdsAddRoute").skip());
        infoMap.put(new Info("AdsDelRoute").skip());
        infoMap.put(new Info("AdsSetLocalAddress").skip());

        // Map C++ types to Java types
        infoMap.put(new Info("uint32_t").cast().valueTypes("int").pointerTypes("IntPointer", "IntBuffer", "int[]"));
        infoMap.put(new Info("uint16_t").cast().valueTypes("short").pointerTypes("ShortPointer", "ShortBuffer", "short[]"));
        infoMap.put(new Info("uint8_t").cast().valueTypes("byte").pointerTypes("BytePointer", "ByteBuffer", "byte[]"));

        // Skip problematic symbols that don't translate well to Java
        infoMap.put(new Info("__stdcall").skip())
                .put(new Info("__int64").skip())
                .put(new Info("TCADSDLL_API").skip())
                .put(new Info("NULL").skip());


        // Skip duplicate definitions
        infoMap.put(new Info("nSystemServiceIndexGroups").skip());

        // Map specific constants to proper Java values
        infoMap.put(new Info("ADSSYMBOLFLAG_PERSISTENT").define().translate(false))
                .put(new Info("ADSSYMBOLFLAG_BITVALUE").define().translate(false))
                .put(new Info("ADSSYMBOLFLAG_REFERENCETO").define().translate(false))
                .put(new Info("ADSSYMBOLFLAG_TYPEGUID").define().translate(false))
                .put(new Info("ADSSYMBOLFLAG_TCCOMIFACEPTR").define().translate(false))
                .put(new Info("ADSSYMBOLFLAG_READONLY").define().translate(false))
                .put(new Info("ADSSYMBOLFLAG_CONTEXTMASK").define().translate(false));

        infoMap.put(new Info("TcAdsDef.h").skip());
        infoMap.put(new Info("USE_TWINCAT_ROUTER").skip());
        // Skip duplicate definitions
        infoMap.put(new Info("nSystemServiceIndexGroups").skip());
        //@Name("PAdsNotificationFuncEx")
        //class PAdsNotificationFuncEx extends FunctionPointer {
        //    static { Loader.load(); }
        //    protected PAdsNotificationFuncEx(Pointer p) { super(p); }
        //    /** no-arg constructor required by JavaCPP        */
        //    public    PAdsNotificationFuncEx()          { allocate(); }
        //    private native void allocate();
        //
        //    public native void call(@Const AdsLib.AmsAddr addr,
        //                            @Const AdsLib.AdsNotificationHeader header,
        //                            int   hUser);
        //}
        //infoMap.put(new Info("PAdsNotificationFuncEx")
        //        .pointerTypes("com.forester.AdsLibConfig.PAdsNotificationFuncEx")
        //        .valueTypes("com.forester.AdsLibConfig.PAdsNotificationFuncEx"));





    }


}
