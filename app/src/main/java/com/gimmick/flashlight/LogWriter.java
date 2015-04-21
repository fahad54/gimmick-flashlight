package com.gimmick.flashlight;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogWriter {

	String TAG = "TEX";

	public void appendLog(String text) {
		try {
			File logFile = new File("/sdcard/DeviceSettings.file");
			if (!logFile.exists()) {

				logFile.createNewFile();

			}

			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
					true));
			buf.append(text);
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
