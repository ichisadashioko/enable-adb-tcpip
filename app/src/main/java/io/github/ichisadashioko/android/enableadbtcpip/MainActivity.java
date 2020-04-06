package io.github.ichisadashioko.android.enableadbtcpip;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.text.format.Formatter;

public class MainActivity extends Activity {

    static final String ADB_PORT = "5555";
    static final String ADB_PROCESS_NAME = "adbd";
    static final String ADB_PORT_PROP_NAME = "service.adb.tcp.port";

    EditText ipAddressET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void setIpAddressETAndDisplayIp(View view) {
        if (this.ipAddressET == null) {
            this.ipAddressET = (EditText) view;
            this.ipAddressET.setText(getIpAddress() + ":" + ADB_PORT);
        }
    }

    public void startAdbTcpIp(View view) {
        setProp(ADB_PORT_PROP_NAME, ADB_PORT);
        try {
            if (isProcessRunning(ADB_PROCESS_NAME)) {
                runRootCommand("stop " + ADB_PROCESS_NAME);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        runRootCommand("start " + ADB_PROCESS_NAME);
        Toast.makeText(this, "Starting " + ADB_PROCESS_NAME + ".", Toast.LENGTH_SHORT);
    }

    String getIpAddress() {
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }

    boolean runRootCommand(String cmd) {
        Process process = null;
        DataOutputStream dos = null;
        try {
            process = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(process.getOutputStream());
            dos.writeBytes(cmd + "\n");
            dos.writeBytes("exit\n");
            dos.flush();
            process.waitFor();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } finally {
            try {
                if (dos != null) {
                    dos.close();
                }
                process.destroy();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return true;
    }

    boolean setProp(String property, String value) {
        return runRootCommand("setprop " + property + " " + value);
    }

    boolean isProcessRunning(String name) throws IOException, InterruptedException {
        boolean running = false;
        Process process = Runtime.getRuntime().exec("ps");
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            if (line.contains(name)) {
                running = true;
                break;
            }
        }
        in.close();
        process.waitFor();
        return running;
    }
}
