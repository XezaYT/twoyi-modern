package com.hzy.libp7zip;

public class P7ZipApi {

    public static native String get7zVersionInfo();

    public static native int executeCommand(String command);

    static {
        System.loadLibrary("p7zip");
    }
}
