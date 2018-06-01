package info.nightscout.androidaps.plugins.PumpDanaR.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.nightscout.androidaps.Constants;
import info.nightscout.androidaps.MainApp;
import info.nightscout.androidaps.R;
import info.nightscout.androidaps.interfaces.PluginType;
import info.nightscout.androidaps.plugins.PumpDanaR.DanaRPump;
import info.nightscout.androidaps.plugins.PumpDanaRKorean.DanaRKoreanPlugin;
import info.nightscout.androidaps.plugins.PumpDanaRS.DanaRSPlugin;
import info.nightscout.androidaps.plugins.PumpDanaRS.services.DanaRSService;
import info.nightscout.utils.SP;

/**
 * Created by Rumen Georgiev on 5/31/2018.
 */

public class DanaRUserOptionsActivity extends Activity {
    private static Logger log = LoggerFactory.getLogger(DanaRUserOptionsActivity.class);

    Switch timeFormat;
    Switch buttonScroll;
    Switch beep;
    RadioGroup pumpAlarm;
    RadioButton pumpAlarmSound;
    RadioButton pumpAlarmVibrate;
    RadioButton pumpAlarmBoth;
    Switch pumpUnits;
    EditText screenTimeout;
    EditText backlightTimeout;
    EditText shutdown;
    EditText lowReservoir;
    Button saveToPumpButton;

    @Override
    protected void onResume() {
        super.onResume();
        MainApp.bus().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MainApp.bus().unregister(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.danar_user_options);

        timeFormat = (Switch) findViewById(R.id.danar_timeformat);
        buttonScroll = (Switch) findViewById(R.id.danar_buttonscroll);
        beep = (Switch) findViewById(R.id.danar_beep);
        pumpAlarm = (RadioGroup) findViewById(R.id.danar_pumpalarm);
        pumpAlarmSound = (RadioButton) findViewById(R.id.danar_pumpalarm_sound);
        pumpAlarmVibrate = (RadioButton) findViewById(R.id.danar_pumpalarm_vibrate);
        pumpAlarmBoth = (RadioButton) findViewById(R.id.danar_pumpalarm_both);
        screenTimeout = (EditText) findViewById(R.id.danar_screentimeout);
        backlightTimeout = (EditText) findViewById(R.id.danar_backlight);
        pumpUnits = (Switch) findViewById(R.id.danar_units);
        shutdown = (EditText) findViewById(R.id.danar_shutdown);
        lowReservoir = (EditText) findViewById(R.id.danar_lowreservoir);
        saveToPumpButton = (Button) findViewById(R.id.save_user_options);

        saveToPumpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveClick();
            }
        });

        boolean isKorean = MainApp.getSpecificPlugin(DanaRKoreanPlugin.class) != null && MainApp.getSpecificPlugin(DanaRKoreanPlugin.class).isEnabled(PluginType.PUMP);
        boolean isRS = MainApp.getSpecificPlugin(DanaRSPlugin.class) != null && MainApp.getSpecificPlugin(DanaRSPlugin.class).isEnabled(PluginType.PUMP);


        Activity activity = this;
        if (activity != null)
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DanaRPump pump = DanaRPump.getInstance();
                    //used for debugging
                    log.debug("UserOptionsLoadedd:"+(System.currentTimeMillis() - pump.lastConnection)/1000+" s ago"
                            +"\ntimeDisplayType:"+pump.timeDisplayType
                            +"\nbuttonScroll:"+pump.buttonScrollOnOff
                            +"\ntimeDisplayType:"+pump.timeDisplayType
                            +"\nlcdOnTimeSec:"+pump.lcdOnTimeSec
                            +"\nbacklight:"+pump.backlightOnTimeSec
                            +"\npumpUnits:"+pump.units
                            +"\nlowReservoir:"+pump.lowReservoirRate);


                    if (pump.timeDisplayType != 0) {
                        timeFormat.setChecked(false);
                    }

                    if(pump.buttonScrollOnOff != 0) {
                        buttonScroll.setChecked(true);
                    }
                    if (pump.beepAndAlarm != 0) {
                        beep.setChecked(true);
                    }

                    screenTimeout.setText(String.valueOf(pump.lcdOnTimeSec));
                    backlightTimeout.setText(String.valueOf(pump.backlightOnTimeSec));
                    if(pump.lastSettingsRead == 0)
                        backlightTimeout.setText(String.valueOf(666));
                    if (pump.getUnits() != null) {
                        if(pump.getUnits().equals(Constants.MMOL)) {
                            pumpUnits.setChecked(true);
                        }
                    }
                    shutdown.setText(String.valueOf(pump.shutdownHour));
                    lowReservoir.setText(String.valueOf(pump.lowReservoirRate));
                }
            });
    }

    public void onSaveClick(){
        boolean isRS = MainApp.getSpecificPlugin(DanaRSPlugin.class) != null && MainApp.getSpecificPlugin(DanaRSPlugin.class).isEnabled(PluginType.PUMP);
        if(!isRS){
            //exit if pump is not DanaRS
            return;
        }
        DanaRPump pump = DanaRPump.getInstance();

        if(timeFormat.isChecked())
            pump.timeDisplayType = 1;
        else
            pump.timeDisplayType = 0;
        if(buttonScroll.isChecked())
            pump.buttonScrollOnOff = 1;
        else
            pump.buttonScrollOnOff = 0;
        // step is 5 seconds
        int screenTimeoutValue = (Integer.parseInt(screenTimeout.getText().toString()) / 5) * 5;
        if(screenTimeoutValue > 4 && screenTimeoutValue < 241){
            pump.lcdOnTimeSec = screenTimeoutValue;
        } else {
            pump.lcdOnTimeSec = 5;
        }
        int backlightTimeoutValue = Integer.parseInt(backlightTimeout.getText().toString());
        if(backlightTimeoutValue > 0 && backlightTimeoutValue < 61){
            pump.backlightOnTimeSec = backlightTimeoutValue;
        }
        if(pumpUnits.isChecked()){
            pump.units = 1;
        } else {
            pump.units = 0;
        }
        int shutDownValue = Integer.parseInt(shutdown.getText().toString());
        if(shutDownValue > -1 && shutDownValue < 25 ){
            pump.shutdownHour = shutDownValue;
        } else {
            pump.shutdownHour = 0;
        }
        int lowReservoirValue = ( Integer.parseInt(lowReservoir.getText().toString()) *10 )/10;
        if(lowReservoirValue > 9 && lowReservoirValue <51){
            pump.lowReservoirRate = lowReservoirValue;
        } else
            pump.lowReservoirRate = 10;

        // push new settings to pump
        DanaRSPlugin pumpPlugin = MainApp.getSpecificPlugin(DanaRSPlugin.class);
        if(!pumpPlugin.isConnected())
            pumpPlugin.connect("UpdateUserOptions");
        pumpPlugin.updateUserOptions();
        pumpPlugin.disconnect("UpdateUserOprions");
    }

}