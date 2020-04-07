package io.github.ichisadashioko.android.enableadbtcpip;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
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
    static final String INDENT = "  ";

    EditText ipAddressET;
    TextView logTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ipAddressET = (EditText) findViewById(R.id.ip_address);
        showIpAddress(null);
        logTV = (TextView) findViewById(R.id.log);
        Logger.LOG_TEXT_VIEW = logTV;
    }

    public void showIpAddress(View view) {
        if (ipAddressET != null) {
            ipAddressET.setText(getIpAddress() + ":" + ADB_PORT);
        }
    }

    public void printStackTrace(Exception ex) {
        ex.printStackTrace();
        Logger.error(ex.toString());
        StackTraceElement[] stackTraceElements = ex.getStackTrace();
        for (StackTraceElement e : stackTraceElements) {
            Logger.error(INDENT + "at " + e.toString());
        }
    }

    public void tryThrowException(View view) {
        try {
            Integer.parseInt("asdf");
        } catch (Exception ex) {
            printStackTrace(ex);
        }
    }

    public void startAdbTcpIp(View view) {
        Logger.info("Setting ADB PORT.");
        setProp(ADB_PORT_PROP_NAME, ADB_PORT);
        try {
            boolean isADBRunning = isProcessRunning(ADB_PROCESS_NAME);
            if (isADBRunning) {
                Logger.info("Stopping ADB.");
                runRootCommand("stop " + ADB_PROCESS_NAME);
            } else {
                Logger.debug("ADB is not running.");
            }
        } catch (Exception ex) {
            printStackTrace(ex);
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
        Logger.debug("Trying to run command: " + cmd);
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
            printStackTrace(ex);
            return false;
        } finally {
            try {
                if (dos != null) {
                    dos.close();
                }
                process.destroy();
            } catch (Exception ex) {
                printStackTrace(ex);
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
