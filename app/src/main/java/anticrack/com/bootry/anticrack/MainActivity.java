package anticrack.com.bootry.anticrack;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        System.out.println("AntiCrack=====" + getSignStrByJava());
        System.out.println("AntiCrack=====" + getSignStrByJni());
        System.out.println("AntiCrack=====" + getPostData("getPostData"));
        System.out.println("AntiCrack=====" + getResponseData("getResponseData"));
    }

    public native String getPostData(String postData); // 加密上传给服务器的数据
    public native String getResponseData(String responseData); // 解密服务器返回的数据
    public native String getSignStrByJni(); // 通过反射下面Java方法

    public String getSignStrByJava() {
        String sign = "";
        try {
            PackageManager packageManager = this.getPackageManager();
            PackageInfo info = packageManager.getPackageInfo(getPackageName(), 64);
            Signature signature = info.signatures[0];
            sign = signature.toCharsString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sign;
    }

}
