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
//import android.os.Message;


boolean isOpen;

private int retval;
private MyActivity myactivity;


void onCreate(Bundle savedInstanceState) {
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
void onActivityResult(int requestCode, int resultCode, Intent data) {
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
byte[] old_s={0};
void setup() {
  //System.out.print("fullscreen");
  fullScreen();
  sensor = new KetaiSensor(this);
  sensor.start();
  smooth();
  ellipseMode(RADIUS);
  textSize(50);
  strokeWeight(5);
  //bt.start();
  //klist = new KetaiList(this, bt.getPairedDeviceNames());
  //System.out.println("run here!1");
}

void draw() { 
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
  s1=("{\"accelermeter\":["+nfp(accelerometerX, 1, 3)+","+nfp(accelerometerY, 1, 3)+
  ","+nfp(accelerometerZ, 1, 3)+"],\"rotation\":["+nfp(rotationX, 1, 3)+
  ","+nfp(rotationY, 1, 3)+","+nfp(rotationZ, 1, 3)+"]}").getBytes();
  //text(read_recv+"!",320,1150);
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

void onAccelerometerEvent(float x, float y, float z)
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
 
 void onGyroscopeEvent(float x, float y, float z)
{
  rotationX = x;
  rotationY = y;
  rotationZ = z;
}
