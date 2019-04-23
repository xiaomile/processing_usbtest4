import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.app.PendingIntent;
import android.app.Activity;
import cn.wch.ch34xuartdriver.CH34xUARTDriver;
import android.os.Bundle;
import processing.core.PApplet;


public class MyActivity {
  private static final String ACTION_USB_PERMISSION = "cn.wch.wchusbdriver.USB_PERMISSION";
  PApplet parent;
  Context context;
  String read_recv = "";
  public CH34xUARTDriver usbdriver;
  
  public MyActivity(PApplet parent){
    this.parent = parent;
    this.context = parent.getActivity();
    usbdriver = new CH34xUARTDriver((UsbManager)context.getSystemService(Context.USB_SERVICE),context,ACTION_USB_PERMISSION);
  }
  
  /**
     *  usb 
     */
    private void openUsbDevice(){
        //before open usb device
        //should try to get usb permission
        tryGetUsbPermission();
    }
    UsbManager mUsbManager;
    

    private void tryGetUsbPermission(){
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        context.registerReceiver(mUsbPermissionActionReceiver, filter);

        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this.context, 0, new Intent(ACTION_USB_PERMISSION), 0);

        //here do emulation to ask all connected usb device for permission
        for (final UsbDevice usbDevice : mUsbManager.getDeviceList().values()) {
            //add some conditional check if necessary
            //if(isWeCaredUsbDevice(usbDevice)){
            if(mUsbManager.hasPermission(usbDevice)){
                //if has already got permission, just goto connect it
                //that means: user has choose yes for your previously popup window asking for grant perssion for this usb device
                //and also choose option: not ask again
                afterGetUsbPermission(usbDevice);
            }else{
                //this line will let android popup window, ask user whether to allow this app to have permission to operate this usb device
                mUsbManager.requestPermission(usbDevice, mPermissionIntent);
            }
            //}
        }
    }


    private void afterGetUsbPermission(UsbDevice usbDevice){
        //call method to set up device communication
        //Toast.makeText(this, String.valueOf("Got permission for usb device: " + usbDevice), Toast.LENGTH_LONG).show();
        //Toast.makeText(this, String.valueOf("Found USB device: VID=" + usbDevice.getVendorId() + " PID=" + usbDevice.getProductId()), Toast.LENGTH_LONG).show();

        doYourOpenUsbDevice(usbDevice);
    }

    private void doYourOpenUsbDevice(UsbDevice usbDevice){
        //now follow line will NOT show: User has not given permission to device UsbDevice
        UsbDeviceConnection connection = mUsbManager.openDevice(usbDevice);
        //System.out.println("doYourOpenUsbDevice");
        Toast.makeText(context, String.valueOf("device opened" + usbDevice), Toast.LENGTH_LONG).show();
        //add your operation code here
    }

    private final BroadcastReceiver mUsbPermissionActionReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice usbDevice = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        //user choose YES for your previously popup window asking for grant perssion for this usb device
                        if(null != usbDevice){
                            afterGetUsbPermission(usbDevice);
                        }
                    }
                    else {
                        //user choose NO for your previously popup window asking for grant perssion for this usb device
                        Toast.makeText(context, String.valueOf("Permission denied for device" + usbDevice), Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    };

}
