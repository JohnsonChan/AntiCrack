
# 一套Android防篡改的方案
## 代码混淆

1.指定混淆字典为Java代码的关键词如do, `if, for, int, new try, super`等
（混淆反编译源码阅读）
2.指定混淆字典为Window系统和Linux系统两个系统命名系统严令禁止使用的文件名，如`CON, PRN, AUX, NUL COM1, COM2, COM3, COM4, COM5, COM6, COM7, COM8, COM9 LPT1, LPT2, LPT3, LPT4, LPT5, LPT6, LPT7, LPT8, LPT9`等。
（利用apktool等反编译的工具漏洞， 最新的apktool已经修复此漏洞）

## 防篡改so加载初始化中校验apk签名值
```c++
jint JNI_OnLoad(JavaVM* vm, void* reserved) {
     JNIEnv* env = NULL;
     jint result = -1;
     if (vm->GetEnv((void**)&env, JNI_VERSION_1_4) != JNI_OK) {
         LOGE("ERROR: GetEnv failed\n");
     }
     if (NULL == env) {
         LOGE("ERROR: GetEnv failed\n");
     }
 
     // 获得当前签名值
     jclass localClass = env->FindClass("android/app/ActivityThread");
     jobject context;
     if (localClass!= NULL) {
         jmethodID getapplication = env->GetStaticMethodID(localClass, "currentApplication", "()Landroid/app/Application;");
         if (getapplication!=NULL) {
             context = env->CallStaticObjectMethod(localClass, getapplication);
         }
     }
     jstring appSign = getSignStr(env, context);
 
     // 比对签名值
     jclass clsstring = env->FindClass("java/lang/String");
     std::string hardcodeSign = "3082036b30820253a003020102020441ae5e97300d06092a864886f70d01010b05003066310f300d06035504061306626f6f747279310f300d06035504081306626f6f747279310f300d06035504071306626f6f747279310f300d060355040a1306626f6f747279310f300d060355040b1306626f6f747279310f300d06035504031306626f6f747279301e170d3138303332333131303931305a170d3433303331373131303931305a3066310f300d06035504061306626f6f747279310f300d06035504081306626f6f747279310f300d06035504071306626f6f747279310f300d060355040a1306626f6f747279310f300d060355040b1306626f6f747279310f300d06035504031306626f6f74727930820122300d06092a864886f70d01010105000382010f003082010a0282010100a1765d9bee8f272cc3f1d17afa3644ba0f7338faf5ae0b74af3102b30e380b7b5ff941f1a33e5ec8c2e028f09f897f135f613bd7e11f245548e2880b7a88dbe7f3e11f7d0a1f59accd0f3a49726da61ab35a57d689f86ca6193f8d7d9a29f49144061bbe7a1197fcee9324258c4f50dcc3e2860bcb77363e37de371d1bda56b78b1eb440c223ff2da9da16f9c9a901d8ee3182f7af6b29b3d55b35936121562ecf9458b86872f199edbd2917be8cc72f231bd56e170521b49c685c07f2f2848217dd209f308c8988221c18177c27d60929abd10fe94e206e8d40f3b63e8462e50c9d38a8f5ce010a060fb71cac53e6c9c19c8d664824255330050b53769210bf0203010001a321301f301d0603551d0e041604145834c99a9fef78f11cb52a343794d45e401451d0300d06092a864886f70d01010b0500038201010092054a626758b614d0d0fd05213ce69629cf3154c46f81c43b52fe5a57aab7d8f3e08b6a2563a5b20436f0407b491f3236aa0d34383f026bcb04f210f6731b87b1ebec4193f5022b6335e02c16e1f2a1dbbeaf30a9af035583b788b259ca907ea669044dd7dbd48474ab80e8d819b826ae73a24cf54251e4c9fb89d4345de1d6aedb5efe0523f760f695c1ac4a8a99f6cd0413edffd32d02cf107c1f05b4a2e2bd4df728aac0dc2f03900ef9fe00a01cb08f09f629792904b8d14b18578316a522004b4bd327872a83b921a499a0d06306d6e5242f5c9c6efe286744ee515c69afa486965f8785da13c0e9c5ecc8183114688d35f69a3aa20e37e64e8944305d";
     jstring correctSign = env->NewStringUTF(hardcodeSign.c_str());
     jmethodID equalsIgnoreCase = env->GetMethodID(clsstring, "equalsIgnoreCase", "(Ljava/lang/String;)Z");
     jboolean compareResult = (jboolean) env->CallBooleanMethod(correctSign, equalsIgnoreCase, appSign);
     if (compareResult) {
         LOGI("签名正确\n");
     } else {
         LOGI("签名不正确\n");
         // 可以在这里制造崩溃，用于标志被篡改的监控，添加数据采集上报逻辑
         exit(-1);
     }
 
     return JNI_VERSION_1_4;
 }
 ```

## 将签名值加密作为密钥，用于加密解密重要业务接口数据，让业务强依赖防篡改so

根据业务需求选择加密算法
