package uart.terminal.androidstudio.com.myapplication1;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;

import uart.terminal.androidstudio.com.myapplication1.R;

import static java.lang.Integer.parseInt;
import static java.lang.Integer.valueOf;

//https://github.com/rcties/PrinterPlusCOMM
//https://github.com/mik3y/usb-serial-for-android
public class MainActivity extends AppCompatActivity  implements SerialInputOutputManager.Listener {
    private static final String ACTION_USB_PERMISSION = "com.android.recipes.USB_PERMISSION";

    TextView txtOut;
    UsbSerialPort port;
    EditText editText;
    Button sendBtn;
    TextView currentBaudrate, currentParityBit, currentStopBit, currentDataBit;
    Integer curBaud, curPar, curStop, curData;


    private RadioGroup radioBaudrateGroup, radioDatabitsGroup, radioStopbitsGroup, radioParitybitsGroup;
    private RadioButton radioBaudrateButton, radioDatabitsButton, radioStopbitsButton, radioParitybitsButton;
    private Button btnSubmit;

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addListenerOnButton();
        curBaud = 115200;
        curPar = 0;
        curStop = 1;
        curData = 8;

        txtOut = findViewById(R.id.txtOut);
        editText = findViewById(R.id.send_text);
        sendBtn = findViewById(R.id.send_btn);
        currentBaudrate = findViewById(R.id.currentBaudrate);
        currentParityBit = findViewById(R.id.currentParityBit);
        currentStopBit = findViewById(R.id.currentStopBit);
        currentDataBit = findViewById(R.id.currentDatabit);
        currentBaudrate.setText("Current baudrate: " + curBaud);
        currentParityBit.setText("Current parity bit: " + curPar);
        currentStopBit.setText("Current stop bit: " + curStop);
        currentDataBit.setText("Current data bit: " + curData);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    byte[] data = ((editText.getText().toString())+"\n").getBytes();

                    SpannableStringBuilder spn = new SpannableStringBuilder();

                    spn.append("Send " + data.length + " bytes\n");
                    spn.append(bytesToHex(data)+"\n");
                    spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorSendText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    port.write(data, 2000);
                    txtOut.append(spn);
                    txtOut.append("Sent Text: " + (editText.getText().toString()) + "\n");
                    editText.getText().clear();
                } catch (IOException e) {
                    e.printStackTrace();
                    txtOut.setText("Connect device");

                }
            }
        });

        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);

        if (availableDrivers.isEmpty()) {
            Log.d("UART", "UART is not available");
            txtOut.setText("UART is not available \n");
        }else {
            Log.d("UART", "UART is available");
            txtOut.setText("UART is available \n");

            UsbSerialDriver driver = availableDrivers.get(0);
            UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
            if (connection == null) {
                manager.requestPermission(driver.getDevice(), PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0));
            } else {
                // Most devices have just one port (port 0)
                port = driver.getPorts().get(0);
                try {
                    port.open(connection);
                    port.setParameters(115200  , 8, 1, 0);

                    SerialInputOutputManager usbIoManager = new SerialInputOutputManager(port, this);
                    Executors.newSingleThreadExecutor().submit(usbIoManager);
                } catch (Exception e) {
                    txtOut.setText("Sending a message is fail");
                }

            }
        }
    }

    public void addListenerOnButton() {

        radioBaudrateGroup = findViewById(R.id.radioBaudrate);
        radioDatabitsGroup = findViewById(R.id.radioDatabits);
        radioStopbitsGroup = findViewById(R.id.radioStopbits);
        radioParitybitsGroup = findViewById(R.id.radioParitybits);
        btnSubmit = findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get selected radio button from radioGroup
                int selectedId1 = radioBaudrateGroup.getCheckedRadioButtonId();
                int selectedId2 = radioDatabitsGroup.getCheckedRadioButtonId();
                int selectedId3 = radioStopbitsGroup.getCheckedRadioButtonId();
                int selectedId4 = radioParitybitsGroup.getCheckedRadioButtonId();

                // find the radiobutton by returned id
                radioBaudrateButton = findViewById(selectedId1);
                radioDatabitsButton = findViewById(selectedId2);
                radioStopbitsButton = findViewById(selectedId3);
                radioParitybitsButton = findViewById(selectedId4);
                txtOut.append("Set baudrate to " + radioBaudrateButton.getText().toString()
                        + ", Databits to " + radioDatabitsButton.getText().toString()
                        + ", Stopbits to " + radioStopbitsButton.getText().toString()
                        + " and Paritybits to " + radioParitybitsButton.getText().toString() + "\n");
                try {
                    port.setParameters(valueOf(radioBaudrateButton.getText().toString()),
                            valueOf(radioDatabitsButton.getText().toString()),
                            valueOf(radioStopbitsButton.getText().toString()),
                            valueOf(radioParitybitsButton.getText().toString()));

                } catch (IOException e) {
                    txtOut.append("Error!");
                }
                curBaud = Integer.parseInt(radioBaudrateButton.getText().toString());
                curData = Integer.parseInt(radioDatabitsButton.getText().toString());
                curStop = Integer.parseInt(radioStopbitsButton.getText().toString());
                curPar = Integer.parseInt(radioParitybitsButton.getText().toString());
                currentBaudrate.setText("Current baudrate: " + curBaud);
                currentParityBit.setText("Current parity bit: " + curPar);
                currentStopBit.setText("Current stop bit: " + curStop);
                currentDataBit.setText("Current data bit: " + curData);
            }
        });
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            port.close();
        }catch (Exception e) {}
    }

    @Override
    public void onNewData(final byte[] data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                receive(data);
            }
        });
    }

    @Override
    public void onRunError(Exception e) {
        txtOut.setText("Error");
        e.printStackTrace();

    }

    private void receive(byte[] data) {
        try {
            SpannableStringBuilder spn1 = new SpannableStringBuilder();
            spn1.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorReceiveText)), 0, spn1.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            spn1.append("Receive " + data.length + " bytes\n");
//        if(data.length > 0)
//            spn.append(bytesToHex(data) + "\n");
            txtOut.append(spn1);
            StringBuilder output = new StringBuilder();
            for (int i = 0; i < bytesToHex(data).length(); i += 2) {
                String str = bytesToHex(data).substring(i, i + 2);
                output.append((char) parseInt(str, 16));
            }
            txtOut.append("Received text:" + output.toString().trim() + "\n");
        }
        catch (Exception e) {
            txtOut.append("Can't receive text");
        }
    }
}
