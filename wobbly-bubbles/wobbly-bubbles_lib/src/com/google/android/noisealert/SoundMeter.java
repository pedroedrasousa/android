/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.noisealert;

import java.io.IOException;
import android.media.MediaRecorder;

public class SoundMeter {
	
	private static final double EMA_FILTER = 0.6;

	private static MediaRecorder mRecorder = null;
	private static double mEMA = 0.0;
	
	public static boolean start() {
		
		if (mRecorder == null) {
			mRecorder = new MediaRecorder();
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");
            try {
				mRecorder.prepare();
			} catch (IllegalStateException e) {
				return false;
			} catch (IOException e) {
				return false;
			}
            
            try {
            	mRecorder.start();
            } catch(Exception e) {
            	return false;
            }

		    mEMA = 0.0;
		}
		
		return true;
	}
       
    public static boolean stop() {

        if (mRecorder != null) {
        	try {
	            mRecorder.stop();
	            mRecorder.reset();
	            mRecorder.release();
            } catch(Exception e) {
            	return false;
            }
            
	    	mRecorder = null;	            
        }
        
        return true;
    }
       
    public static double getAmplitude() {
	    if (mRecorder != null) {
	    	try {
	    		return  (mRecorder.getMaxAmplitude() / 2700.0);
            } catch(Exception e) {
            	return 0;
            }
	    }
	    else {
	    	return 0;
	    }
    }

	public static double getAmplitudeEMA() {
	    double amp = getAmplitude();
	    mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
	    return mEMA;
	}
}
