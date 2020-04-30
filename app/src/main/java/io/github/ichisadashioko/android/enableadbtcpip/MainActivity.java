package io.github.ichisadashioko.android.enableadbtcpip;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

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

    public static void getIpAddresses() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            Logger.info("> " + networkInterface.getDisplayName());

            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                Logger.info(">> " + inetAddress.getHostName());
                byte[] ipAddress = inetAddress.getAddress();

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < ipAddress.length; i++) {
                    sb.append(ipAddress[i]);
                }

                Logger.info(">>> " + sb.toString());
            }
        }
    }

    public void showIpAddress(View view) {
        if (ipAddressET != null) {
            ipAddressET.setText(getIpAddress());
        }

        try {
            getIpAddresses();
        } catch (Exception ex) {
            printStackTrace(ex);
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

    String runRootCommand(String cmd) {
        Logger.debug("> " + cmd);
        Process su = null;
        DataOutputStream dos = null;
        DataInputStream dis = null;
        StringBuilder sb = new StringBuilder();
        try {
            su = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(su.getOutputStream());
            dis = new DataInputStream(su.getInputStream());
            dos.writeBytes(cmd + "\n");
            dos.writeBytes("exit\n");
            dos.flush();
            su.waitFor();

            String line;
            while ((line = dis.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception ex) {
            printStackTrace(ex);
        } finally {
            try {
                if (dos != null) {
                    dos.close();
                }
                if (dis != null) {
                    dis.close();
                }
                su.destroy();
            } catch (Exception ex) {
                printStackTrace(ex);
            }
        }

        return sb.toString();
    }

    String setProp(String property, String value) {
        String cmd = "setprop " + property + " " + value;
        return runRootCommand(cmd);
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
