package processing.test.processing_usbtest4;

import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import android.content.Intent; 
import android.os.Bundle; 
import cn.wch.ch34xuartdriver.CH34xUARTDriver; 
import android.hardware.usb.UsbManager; 
import android.content.Context; 
import android.content.DialogInterface; 
import android.app.Dialog; 
import android.app.AlertDialog; 
import android.widget.Toast; 
import ketai.sensors.*; 
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

import cn.wch.ch34xuartdriver.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class processing_usbtest4 extends PApplet {











//import android.os.Message;


boolean isOpen;

private int retval;
private MyActivity myactivity;


public void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);

try{
myactivity = new MyActivity(this);
myactivity.openUsbDevice();
  if(!myactivity.usbdriver.UsbFeatureSupported()){
    new AlertDialog.Builder(this.getActivity())
    .setTitle("notice")
    .setMessage("Your cell phone does not support USB HOST,Please change and try again!")
    .setPositiveButton("exit",new DialogInterface.OnClickListener(){
      @Override
      public void onClick(DialogInterface arg0,int arg1){System.exit(0);}
    }).create();
  }
  //System.out.print("isopen");
  if(!isOpen){
    retval = myactivity.usbdriver.ResumeUsbList();
    switch(retval){
    case -1:
      Toast.makeText(this.getActivity(),"open devices failed",Toast.LENGTH_SHORT).show();
      myactivity.usbdriver.CloseDevice();
      break;
    case 0:
      if(!myactivity.usbdriver.UartInit()){
        Toast.makeText(this.getActivity(),"device init failed",Toast.LENGTH_SHORT).show();
        return;
      }
      Toast.makeText(this.getActivity(),"open devices successed",Toast.LENGTH_SHORT).show();
      isOpen = true;
      if (myactivity.usbdriver.SetConfig(9600, (byte)8, (byte)1, (byte)0,(byte)0)) {
          Toast.makeText(this.getActivity(), "Serial config successed!",
              Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(this.getActivity(), "Serial config failed!",
              Toast.LENGTH_SHORT).show();
        }
      new readThread().start();
      break;
    default:
      AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
      builder.setTitle("no permission"+" "+str(retval)+"!");
      builder.setMessage("Confirm to exit?");
      builder.setPositiveButton("confirm",new DialogInterface.OnClickListener(){
      @Override
      public void onClick(DialogInterface arg0,int arg1){System.exit(0);}
      });
      builder.setNegativeButton("cancel",new DialogInterface.OnClickListener(){
      @Override
      public void onClick(DialogInterface arg0,int arg1){}
      });
      builder.show();
    }
  }
  else{
    myactivity.usbdriver.CloseDevice();
    isOpen = false;
  }
}
catch(Exception e){
e.printStackTrace();
}/*

  
  */
}
public void onActivityResult(int requestCode, int resultCode, Intent data) {
//bt.onActivityResult(requestCode, resultCode, data);
}
byte[] val_read  =new byte[1]; 
byte[] val_write =new byte[1];  
String read_recv = "";
KetaiSensor sensor;
float accelerometerX, accelerometerY, accelerometerZ;
float rotationX, rotationY, rotationZ;
int y=640;
byte[] s1;
int new_time;
int old_time=0;
byte[] old_s= new byte[1];
public void setup() {
  //System.out.print("fullscreen");
  
  sensor = new KetaiSensor(this);
  sensor.start();
  
  ellipseMode(RADIUS);
  textSize(50);
  strokeWeight(5);
  //bt.start();
  //klist = new KetaiList(this, bt.getPairedDeviceNames());
  //System.out.println("run here!1");
}

public void draw() { 
  background(78, 93, 75);
  text("Accelerometer: \n" +
    "x: " + nfp(accelerometerX, 1, 3) + "\n" +
    "y: " + nfp(accelerometerY, 1, 3) + "\n" +
    "z: " + nfp(accelerometerZ, 1, 3) + "\n" +
    "Gyroscope: \n" +
    "rx: " + nfp(rotationX, 1, 3) + "\n" +
    "ry: " + nfp(rotationY, 1, 3) + "\n" +
    "rz: " + nfp(rotationZ, 1, 3), 0, 0, width, height);
  //s1=(byte)((int)(accelerometerX*10));
  s1=("!{\"accelermeter\":["+nfp(accelerometerX, 1, 3)+","+nfp(accelerometerY, 1, 3)+
  ","+nfp(accelerometerZ, 1, 3)+"],\"rotation\":["+nfp(rotationX, 1, 3)+
  ","+nfp(rotationY, 1, 3)+","+nfp(rotationZ, 1, 3)+"]}!").getBytes();
  s1[0] = (byte)0xFE; //the range of char in java is 0~127 not 0~255
  s1[s1.length-1] = (byte)0xFE; //so correct the first byte and the last byte
  text(toHexString(s1,s1.length)+"!",0,640);
  text("read String:",300,1150);
  String[] t2 = read_recv.split(" ");
  String t1 = "";
  for(int i = 0;i<t2.length;i++){
    if(t2[i].length()>=2)
  t1+=(char)(Integer.parseInt(t2[i],16));}
  text(t1+"?",320,1210);
  new_time= millis();
  if((new_time-old_time)>100){
  if(s1!=old_s){
    val_write = s1;
    //bt.broadcast(val_write);
    myactivity.usbdriver.WriteData(val_write,s1.length);
    old_s = s1;}
    old_time=new_time;
}
}

  private class readThread extends Thread{
  public void run(){
    byte[] buffer = new byte[4096];
    while(true){
      //Message msg = Message.obtain();
      if(!isOpen){
        break;
      }
      int length = myactivity.usbdriver.ReadData(buffer,4096);
      if(length>0){
        String recv = toHexString(buffer,length);
        //String recv = new String(buffer,0,length);
        //msg.obj = recv;
        //handler.sendMessage(msg);
        read_recv = recv;
      }
    }
  }
}

public void onAccelerometerEvent(float x, float y, float z)
{
  accelerometerX = x;
  accelerometerY = y;
  accelerometerZ = z;
}

private String toHexString(byte[] arg, int length) {
    String result = new String();
    if (arg != null) {
      for (int i = 0; i < length; i++) {
        result = result
            + (Integer.toHexString(
                arg[i] < 0 ? arg[i] + 256 : arg[i]).length() == 1 ? "0"
                + Integer.toHexString(arg[i] < 0 ? arg[i] + 256
                    : arg[i])
                : Integer.toHexString(arg[i] < 0 ? arg[i] + 256
                    : arg[i])) + " ";
      }
      return result;
    }
    return "";
  }
 
 public void onGyroscopeEvent(float x, float y, float z)
{
  rotationX = x;
  rotationY = y;
  rotationZ = z;
}












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
  public void settings() {  fullScreen();  smooth(); }
}
