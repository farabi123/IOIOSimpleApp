package ioio.examples.simple;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class IOIOSimpleApp extends IOIOActivity {
	private TextView textView_, textView_2, textView_3 , textView_4;
	private SeekBar seekBar_;
	private ToggleButton toggleButton_;
	static double pos = 0;
	String s;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		textView_ = (TextView) findViewById(R.id.IR1reading);
		textView_2 = (TextView) findViewById(R.id.IR2reading);
		textView_3 = (TextView) findViewById(R.id.servoReading);
		textView_4 = (TextView) findViewById(R.id.status);

		enableUi(false);
	}

	class Looper extends BaseIOIOLooper {
		private AnalogInput input_IR1;
		private AnalogInput input_IR2;
		private PwmOutput pwmOutput_;
		private DigitalOutput led_;


		@Override
		public void setup() throws ConnectionLostException {
			led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
			input_IR1 = ioio_.openAnalogInput(40);
			input_IR2 = ioio_.openAnalogInput(42);
			pwmOutput_ = ioio_.openPwmOutput(12, 100);
			enableUi(true);
		}

		int i =2500;
		float dataIR1;
		float dataIR2;
		boolean complete1 = false, complete2 = false;
		@Override
		public void loop() throws ConnectionLostException, InterruptedException {
			boolean inorder= false;
			dataIR1 = input_IR1.read();
			setNumber(input_IR1.read(), 40);

			if (dataIR1 > 0.50 && complete1 == false && complete2 == false){
				//move the servo - i keeps dropping, starts at 180 and drops to 0
				s ="Intiating Stage 1";
				setStatus(s);
				System.out.println(i);
				pwmOutput_.setPulseWidth(i);
				pos = ((i - 500)*0.09);

				//System.out.println(i);
				if(pos >= 0 ) {
					setServoValue(pos); //<--------------------theres two of them
					i = i - 20;
				}
				else{
						complete1 = true;
						s = "Stage 1 Complete";
						setStatus(s);
					}

			//System.out.println(i);
			}


			dataIR2 = input_IR2.read();
			setNumber(input_IR2.read(), 42);

				if (dataIR2 > 0.60 && complete1 == true && complete2 == false){ // starts at 0 and goes up to 180
					s ="Intiating Stage 2";
					setStatus(s);
					pwmOutput_.setPulseWidth(i);
					pos = (i - 500)*0.09;

					if(pos <= 180){
						setServoValue(pos);
						i = i + 20;
					}
					else {
						complete2 = true;
						s = "Process Complete";
						setStatus(s);
					}
				}

			Thread.sleep(10);

		}



		@Override
		public void disconnected() {
			enableUi(false);
		}
	}

	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}

	private void enableUi(final boolean enable) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
//				seekBar_.setEnabled(enable);
				//toggleButton_.setEnabled(enable);
			}
		});
	}


	private void setServoValue(double f) {
		final String str = String.format("%.2f", f);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				textView_3.setText(str);
			}
		});
	}

	private void setStatus(final String str) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				textView_4.setText(str);
			}
		});
	}
	private void setNumber(float f, int pin) {
		final float position = (f*180);
		if(pin == 40){
			final String str = String.format("%.2f", f);
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					textView_.setText(str);

				}
			});
		}
		else{
			final String str = String.format("%.2f", f);
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					textView_2.setText(str);
				}
			});
		}
	}




}