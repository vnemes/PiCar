// Copyright © 2016-2017 Shawn Baker using the MIT License.
package vendetta.blecar.camera;

import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.util.Arrays;


import vendetta.blecar.ControllerActivity;
import vendetta.blecar.R;
import vendetta.blecar.camera.dependencies.Camera;
import vendetta.blecar.camera.dependencies.HttpReader;
import vendetta.blecar.camera.dependencies.MulticastReader;
import vendetta.blecar.camera.dependencies.RawH264Reader;
import vendetta.blecar.camera.dependencies.Source;
import vendetta.blecar.camera.dependencies.SpsParser;
import vendetta.blecar.camera.dependencies.TcpIpReader;

public class VideoFragment extends Fragment implements TextureView.SurfaceTextureListener
{

	// public constants
	public final static String CAMERA = "camera";

	// instance variables
	private Camera camera;
	private DecoderThread decoder;
	private Runnable finishRunner, startVideoRunner;
	private Handler finishHandler, startVideoHandler;

	//******************************************************************************
	// newInstance
	//******************************************************************************
	public static VideoFragment newInstance(Camera camera)
	{
		VideoFragment fragment = new VideoFragment();

		Bundle args = new Bundle();
		args.putParcelable(CAMERA, camera);
		fragment.setArguments(args);

		return fragment;
	}

	//******************************************************************************
	// onCreate
	//******************************************************************************
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// configure the activity
		super.onCreate(savedInstanceState);

		// get the parameters
		camera = getArguments().getParcelable(CAMERA);
		android.util.Log.d(getClass().getSimpleName(),"camera: " + camera.toString());


		// create the finish handler and runnable
		finishHandler = new Handler();
		finishRunner = () -> getActivity().finish();

		// create the start video handler and runnable
		startVideoHandler = new Handler();
		startVideoRunner = () -> {
        };
	}

	//******************************************************************************
	// onCreateView
	//******************************************************************************
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_video, container, false);

		// set the texture listener
        TextureView textureView = view.findViewById(R.id.video_surface);
		textureView.setSurfaceTextureListener(this);

		return view;
	}


	//******************************************************************************
	// onAttach
	//******************************************************************************
	@Override
	public void onAttach(Context context)
	{
		super.onAttach(context);
	}

	//******************************************************************************
	// onDestroy
	//******************************************************************************
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		finishHandler.removeCallbacks(finishRunner);
	}

	//******************************************************************************
	// onStart
	//******************************************************************************
	@Override
	public void onStart()
	{
		super.onStart();

		// create the decoder thread
		decoder = new DecoderThread();
		decoder.start();
	}

	//******************************************************************************
	// onStop
	//******************************************************************************
	@Override
	public void onStop()
	{
		super.onStop();

		if (decoder != null)
		{
			decoder.interrupt();
			decoder = null;
		}
	}

	//******************************************************************************
	// onPause
	//******************************************************************************
	@Override
	public void onPause()
	{
		super.onPause();
	}

	//******************************************************************************
	// onResume
	//******************************************************************************
	@Override
	public void onResume()
	{
		super.onResume();
	}

	//******************************************************************************
	// onSurfaceTextureAvailable
	//******************************************************************************
	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height)
	{
		if (decoder != null)
		{
			decoder.setSurface(new Surface(surfaceTexture), startVideoHandler, startVideoRunner);
		}
	}

	//******************************************************************************
	// onSurfaceTextureSizeChanged
	//******************************************************************************
	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height)
	{
	}

	//******************************************************************************
	// onSurfaceTextureDestroyed
	//******************************************************************************
	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture)
	{
		if (decoder != null)
		{
			decoder.setSurface(null, null, null);
		}
		return true;
	}

	//******************************************************************************
	// onSurfaceTextureUpdated
	//******************************************************************************
	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture)
	{
	}

	//******************************************************************************
	// stop
	//******************************************************************************
	public void stop()
	{
		if (decoder != null)
		{
			decoder.interrupt();
			try
			{
				decoder.join(TcpIpReader.IO_TIMEOUT * 2);
			}
			catch (Exception ex) {}
			decoder = null;
		}
	}

	////////////////////////////////////////////////////////////////////////////////
	// DecoderThread
	////////////////////////////////////////////////////////////////////////////////
	private class DecoderThread extends Thread
	{
		// local constants
		private final static int FINISH_TIMEOUT = 5000;
		private final static int MULTICAST_BUFFER_SIZE = 16384;
		private final static int TCPIP_BUFFER_SIZE = 16384;
		private final static int HTTP_BUFFER_SIZE = 4096;
		private final static int NAL_SIZE_INC = 4096;
		private final static int MAX_READ_ERRORS = 300;

		// instance variables
		private MediaCodec decoder = null;
		private MediaFormat format;
		private boolean decoding = false;
		private Surface surface;
		private Source source = null;
		private byte[] buffer = null;
		private ByteBuffer[] inputBuffers = null;
		private long presentationTime;
		private long presentationTimeInc = 66666;
		private RawH264Reader reader = null;
		private WifiManager.MulticastLock multicastLock = null;
		private Handler startVideoHandler;
		private Runnable startVideoRunner;

		//******************************************************************************
		// setSurface
		//******************************************************************************
		public void setSurface(Surface surface, Handler handler, Runnable runner)
		{
			this.surface = surface;
			this.startVideoHandler = handler;
			this.startVideoRunner = runner;
			if (decoder != null)
			{
				if (surface != null)
				{
					boolean newDecoding = decoding;
					if (decoding)
					{
						setDecodingState(false);
					}
					if (format != null)
					{
						try
						{
							decoder.configure(format, surface, null, 0);
						}
						catch (Exception ex) {}
						if (!newDecoding)
						{
							newDecoding = true;
						}
					}
					if (newDecoding)
					{
						setDecodingState(newDecoding);
					}
				}
				else if (decoding)
				{
					setDecodingState(false);
				}
			}
		}

		//******************************************************************************
		// getMediaFormat
		//******************************************************************************
		public MediaFormat getMediaFormat()
		{
			return format;
		}

		//******************************************************************************
		// setDecodingState
		//******************************************************************************
		private synchronized void setDecodingState(boolean newDecoding)
		{
			try
			{
				if (newDecoding != decoding && decoder != null)
				{
					if (newDecoding)
					{
						decoder.start();
					}
					else
					{
						decoder.stop();
					}
					decoding = newDecoding;
				}
			} catch (Exception ex) {}
		}

		//******************************************************************************
		// run
		//******************************************************************************
		@Override
		public void run()
		{
			byte[] nal = new byte[NAL_SIZE_INC];
			int nalLen = 0;
			int numZeroes = 0;
			int numReadErrors = 0;

			try
			{
				// get the multicast lock if necessary
				if (camera.source.connectionType == Source.ConnectionType.RawMulticast)
				{
					WifiManager wifi = (WifiManager)getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
					if (wifi != null)
					{
						multicastLock = wifi.createMulticastLock("rpicamlock");
						multicastLock.acquire();
					}
				}

				// create the decoder
				decoder = MediaCodec.createDecoderByType("video/avc");

				// create the reader
//				camera.source.address= ControllerActivity.getIP().substring(7);
				Log.d("DEBUG",camera.source.address);
				camera.source.port=1324;
				camera.source.width=1280;
				camera.source.height=720;
				camera.source.fps = 25;
				camera.source.bps=1000000;

				source = camera.source;
				if (source.connectionType == Source.ConnectionType.RawMulticast)
				{
					buffer = new byte[MULTICAST_BUFFER_SIZE];
					reader = new MulticastReader(source);
				}
				else if (source.connectionType == Source.ConnectionType.RawHttp)
				{
					buffer = new byte[HTTP_BUFFER_SIZE];
					reader = new HttpReader(source);
				}
				else
				{
					buffer = new byte[TCPIP_BUFFER_SIZE];
					reader = new TcpIpReader(source);
				}
				if (!reader.isConnected())
				{
					throw new Exception();
				}

				// read from the source
				while (!isInterrupted())
				{
					// read from the stream
					int len = reader.read(buffer);
					if (isInterrupted()) break;

					// process the input buffer
					if (len > 0)
					{
						numReadErrors = 0;
						for (int i = 0; i < len && !isInterrupted(); i++)
						{
							// add the byte to the NAL
							if (nalLen == nal.length)
							{
								nal = Arrays.copyOf(nal, nal.length + NAL_SIZE_INC);
							}
							nal[nalLen++] = buffer[i];

							// look for a header
							if (buffer[i] == 0)
							{
								numZeroes++;
							}
							else
							{
								if (buffer[i] == 1 && numZeroes == 3)
								{
									if (nalLen > 4)
									{
										int nalType = processNal(nal, nalLen - 4);
										if (isInterrupted()) break;
										if (nalType == -1)
										{
											nal[0] = nal[1] = nal[2] = 0;
											nal[3] = 1;
										}
									}
									nalLen = 4;
								}
								numZeroes = 0;
							}
						}
					}
					else
					{
						numReadErrors++;
						if (numReadErrors >= MAX_READ_ERRORS)
							break;

					}

					// send an output buffer to the surface
					if (format != null && decoding)
					{
						if (isInterrupted()) break;
						MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
						int index;
						do
						{
							index = decoder.dequeueOutputBuffer(info, 0);
							if (isInterrupted()) break;
							if (index >= 0)
							{
								decoder.releaseOutputBuffer(index, true);
							}
							//Log.info(String.format("dequeueOutputBuffer index = %d", index));
						} while (index >= 0);
					}
				}
			}
			catch (Exception ex)
			{
				Log.d(getClass().getSimpleName(),ex.toString());
				if (reader == null || !reader.isConnected())
					finishHandler.postDelayed(finishRunner, FINISH_TIMEOUT);
				ex.printStackTrace();
			}

			// close the reader
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (Exception ex) {}
				reader = null;
			}

			// stop the decoder
			if (decoder != null)
			{
				try
				{
					setDecodingState(false);
					decoder.release();
				}
				catch (Exception ex) {}
				decoder = null;
			}

			// release the multicast lock
			if (multicastLock != null)
			{
				try
				{
					if (multicastLock.isHeld())
					{
						multicastLock.release();
					}
				}
				catch (Exception ex) {}
				multicastLock = null;
			}
		}

		//******************************************************************************
		// processNal
		//******************************************************************************
		private int processNal(byte[] nal, int nalLen)
		{
			// get the NAL type
			int nalType = (nalLen > 4 && nal[0] == 0 && nal[1] == 0 && nal[2] == 0 && nal[3] == 1) ? (nal[4] & 0x1F) : -1;
			//Log.info(String.format("NAL: type = %d, len = %d", nalType, nalLen));

			// process the first SPS record we encounter
			if (nalType == 7 && !decoding)
			{
				SpsParser parser = new SpsParser(nal, nalLen);
				int width = (source.width != 0) ? source.width : parser.width;
				int height = (source.height != 0) ? source.height : parser.height;
				format = MediaFormat.createVideoFormat("video/avc", width, height);
				if (source.fps != 0)
				{
					format.setInteger(MediaFormat.KEY_FRAME_RATE, source.fps);
					presentationTimeInc = 1000000 / source.fps;
				}
				else
				{
					presentationTimeInc = 66666;
				}
				presentationTime = System.nanoTime() / 1000;
				if (source.bps != 0)
				{
					format.setInteger(MediaFormat.KEY_BIT_RATE, source.bps);
				}
				Log.d(getClass().getSimpleName(),String.format("SPS: %02X, %d x %d, %d, %d, %d", nal[4], width, height, source.fps, source.bps, presentationTimeInc));
				decoder.configure(format, surface, null, 0);
				setDecodingState(true);
				inputBuffers = decoder.getInputBuffers();
				startVideoHandler.post(startVideoRunner);
			}

			// queue the frame
			if (nalType > 0 && decoding)
			{
				int index = decoder.dequeueInputBuffer(0);
				if (index >= 0)
				{
					ByteBuffer inputBuffer = inputBuffers[index];
					//ByteBuffer inputBuffer = decoder.getInputBuffer(index);
					inputBuffer.put(nal, 0, nalLen);
					decoder.queueInputBuffer(index, 0, nalLen, presentationTime, 0);
					presentationTime += presentationTimeInc;
				}
				//Log.info(String.format("dequeueInputBuffer index = %d", index));
			}
			return nalType;
		}

	}
}
